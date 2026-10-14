package com.numan.wcmp.feature.vault.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numan.wcmp.data.repository.SecurityRepository
import com.numan.wcmp.domain.evidence.health.ControlledDecryptionService
import com.numan.wcmp.domain.evidence.health.EvidenceHealth
import com.numan.wcmp.domain.evidence.health.EvidenceHealthEvaluator
import com.numan.wcmp.feature.vault.model.VaultEvidence
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class VaultEvidenceUiModel(
    val evidence: VaultEvidence,
    val health: EvidenceHealth
)

class VaultViewModel(
    private val securityRepository: SecurityRepository,
    private val integrityService: com.numan.wcmp.domain.evidence.health.EvidenceIntegrityService,
    private val decryptionService: ControlledDecryptionService,
    private val directoryProvider: com.numan.wcmp.domain.evidence.health.EvidenceDirectoryProvider
) : ViewModel() {

    private val _evidenceList = MutableStateFlow<List<VaultEvidenceUiModel>>(emptyList())
    val evidenceList: StateFlow<List<VaultEvidenceUiModel>> = _evidenceList.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            securityRepository.observeVaultEvidence().collect { list ->
                val uiModels = list.map { evidence ->
                    val health = integrityService.inspectFast(evidence)
                    VaultEvidenceUiModel(evidence, health)
                }
                _evidenceList.value = uiModels
            }
        }
    }

    suspend fun deleteEvidence(evidence: VaultEvidence): com.numan.wcmp.feature.vault.model.EvidenceDeletionResult {
        return withContext(Dispatchers.IO) {
            try {
                val encryptedFileName = "evidence_${evidence.id}.enc"
                val encryptedFile = File(directoryProvider.getEvidenceDirectory(), encryptedFileName)
                if (encryptedFile.exists()) {
                    val deleted = encryptedFile.delete()
                    if (!deleted || encryptedFile.exists()) {
                        return@withContext com.numan.wcmp.feature.vault.model.EvidenceDeletionResult.Failed("Physical deletion failed")
                    }
                } else {
                    val health = integrityService.inspectFast(evidence)
                    if (health != EvidenceHealth.MISSING_FILE && health != EvidenceHealth.LEGACY) {
                        return@withContext com.numan.wcmp.feature.vault.model.EvidenceDeletionResult.FileMissing
                    }
                }
                try {
                    securityRepository.deleteVaultEvidence(evidence.id)
                    com.numan.wcmp.feature.vault.model.EvidenceDeletionResult.Success
                } catch (e: Exception) {
                    com.numan.wcmp.feature.vault.model.EvidenceDeletionResult.Failed("Database deletion failed: ${e.message}")
                }
            } catch (e: Exception) {
                com.numan.wcmp.feature.vault.model.EvidenceDeletionResult.Failed("Deletion failed due to exception")
            }
        }
    }

    /**
     * Decrypts the file to cache and returns the path.
     * Returns null if decryption failed (e.g., INTEGRITY_ERROR).
     */
    suspend fun requestDecryptionForViewing(evidence: VaultEvidence): File? {
        return withContext(Dispatchers.IO) {
            try {
                decryptionService.decryptForViewing(evidence)
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Cleanup all temporary decrypted files when the Vault is closed
        decryptionService.cleanupCache()
    }
}


