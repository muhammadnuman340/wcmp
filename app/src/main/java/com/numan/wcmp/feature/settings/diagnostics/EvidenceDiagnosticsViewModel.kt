package com.numan.wcmp.feature.settings.diagnostics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.numan.wcmp.data.repository.SecurityRepository
import com.numan.wcmp.domain.evidence.EvidenceCaptureCoordinator
import com.numan.wcmp.domain.evidence.EvidenceCaptureRequest
import com.numan.wcmp.domain.evidence.EvidenceCaptureResult
import com.numan.wcmp.domain.evidence.EvidenceTrigger
import com.numan.wcmp.domain.evidence.EvidenceType
import com.numan.wcmp.domain.evidence.health.EvidenceHealth
import com.numan.wcmp.domain.evidence.health.EvidenceIntegrityService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class EvidenceTestState {
    object Idle : EvidenceTestState()
    object Running : EvidenceTestState()
    data class Success(val evidenceId: String, val details: String) : EvidenceTestState()
    data class Blocked(val reason: String) : EvidenceTestState()
    data class Failed(val reason: String) : EvidenceTestState()
}

data class EvidenceHealthMetrics(
    val healthy: Int = 0,
    val missing: Int = 0,
    val integrityErrors: Int = 0,
    val keyUnavailable: Int = 0,
    val legacy: Int = 0,
    val orphans: Int = 0,
    val hasRunCheck: Boolean = false
)

class EvidenceDiagnosticsViewModel(
    private val coordinator: EvidenceCaptureCoordinator,
    private val repository: SecurityRepository
) : ViewModel() {

    private val _testState = MutableStateFlow<EvidenceTestState>(EvidenceTestState.Idle)
    val testState: StateFlow<EvidenceTestState> = _testState.asStateFlow()

    private val _healthMetrics = MutableStateFlow(EvidenceHealthMetrics())
    val healthMetrics: StateFlow<EvidenceHealthMetrics> = _healthMetrics.asStateFlow()

    private val _isCheckingIntegrity = MutableStateFlow(false)
    val isCheckingIntegrity: StateFlow<Boolean> = _isCheckingIntegrity.asStateFlow()

    fun runIntegrityCheck(integrityService: EvidenceIntegrityService) {
        if (_isCheckingIntegrity.value) return
        _isCheckingIntegrity.value = true

        viewModelScope.launch {
            try {
                val existing = repository.observeVaultEvidence().first()
                var healthy = 0
                var missing = 0
                var integrity = 0
                var keyUnavail = 0
                var legacy = 0
                for (evidence in existing) {
                    when (integrityService.inspect(evidence)) {
                        EvidenceHealth.HEALTHY -> healthy++
                        EvidenceHealth.MISSING_FILE -> missing++
                        EvidenceHealth.INTEGRITY_ERROR -> integrity++
                        EvidenceHealth.KEY_UNAVAILABLE -> keyUnavail++
                        EvidenceHealth.LEGACY -> legacy++
                        else -> {}
                    }
                }
                
                val orphans = integrityService.scanForOrphans(existing)
                
                _healthMetrics.value = EvidenceHealthMetrics(
                    healthy = healthy,
                    missing = missing,
                    integrityErrors = integrity,
                    keyUnavailable = keyUnavail,
                    legacy = legacy,
                    orphans = orphans.size,
                    hasRunCheck = true
                )
            } finally {
                _isCheckingIntegrity.value = false
            }
        }
    }

    fun clearTestEvidence() {
        viewModelScope.launch {
            repository.deleteTestEvidence()
            _testState.value = EvidenceTestState.Idle
        }
    }

    fun runPipelineTest(type: EvidenceType) {
        if (_testState.value is EvidenceTestState.Running) return

        _testState.value = EvidenceTestState.Running

        viewModelScope.launch {
            val request = EvidenceCaptureRequest(
                trigger = EvidenceTrigger.MANUAL_TEST,
                preferredType = type
            )
            
            val result = coordinator.requestCapture(request)
            
            _testState.value = when (result) {
                is EvidenceCaptureResult.Success -> {
                    EvidenceTestState.Success(
                        evidenceId = result.evidenceRecord.id,
                        details = "Policy: Allowed\nStorage: Success\nStrategy: Simulated/Test"
                    )
                }
                is EvidenceCaptureResult.Failure -> EvidenceTestState.Failed(result.reason)
                                EvidenceCaptureResult.RejectedByPolicy -> EvidenceTestState.Blocked("Policy Rejected")
                EvidenceCaptureResult.CapabilityUnavailable -> EvidenceTestState.Failed("Capability Unavailable")
            }
        }
    }

    class Factory(
        private val coordinator: EvidenceCaptureCoordinator,
        private val repository: SecurityRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EvidenceDiagnosticsViewModel(coordinator, repository) as T
        }
    }
}

