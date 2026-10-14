package com.numan.wcmp.domain.protection

import com.numan.wcmp.data.repository.SecurityRepository
import com.numan.wcmp.domain.security.ForegroundApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

import com.numan.wcmp.domain.evidence.EvidenceCaptureCoordinator
import com.numan.wcmp.domain.evidence.EvidenceCaptureRequest
import com.numan.wcmp.domain.evidence.EvidenceTrigger
import com.numan.wcmp.domain.evidence.EvidenceType

class AppLockController(
    private val policy: AppLockPolicy,
    private val securityRepository: SecurityRepository,
    private val presenter: AppLockPresenter,
    private val evidenceCoordinator: EvidenceCaptureCoordinator,
    private val coroutineScope: CoroutineScope,
    private val sessionManager: com.numan.wcmp.domain.security.SecuritySessionManager? = null
) {
    private val _state = MutableStateFlow(AppLockState.WAITING_FOR_PROTECTED_APP)
    val state: StateFlow<AppLockState> = _state.asStateFlow()

    private var currentSession: AppLockSession? = null

    // Called by SecurityEngine when a protected app is accessed
    fun onProtectedAppAccessed(app: ForegroundApp) {
        coroutineScope.launch {
            val isAppLockEnabled = securityRepository.autoProtectionEnabled.first()
            
            val decision = policy.requiresAuthentication(
                packageName = app.packageName,
                isProtected = true,
                isAppLockEnabled = isAppLockEnabled,
                currentSession = currentSession
            )

            when (decision) {
                AppLockDecision.Allow -> {
                    _state.value = AppLockState.UNLOCKED
                    // We don't create a new session if it was allowed by policy without auth,
                    // unless we want to track it.
                }
                AppLockDecision.RequireAuthentication -> {
                    _state.value = AppLockState.LOCK_REQUIRED
                    presenter.presentLockScreen(app.packageName, app.applicationName)
                }
                AppLockDecision.Ignore -> {
                    // Do nothing
                }
            }
        }
    }

    // Called by ForegroundMonitor / SecurityEngine when the app is left
    fun onProtectedAppLeft(packageName: String) {
        if (currentSession?.packageName == packageName) {
            currentSession = null
            _state.value = AppLockState.WAITING_FOR_PROTECTED_APP
        }
    }

    // Called by the Lock UI when authentication concludes
    fun onAuthenticationCompleted(packageName: String, result: AuthenticationResult) {
        coroutineScope.launch {
            val sessionId = sessionManager?.currentSession?.value?.sessionId
            
            when (result) {
                AuthenticationResult.SUCCESS -> {
                    currentSession = AppLockSession(packageName = packageName)
                    _state.value = AppLockState.UNLOCKED
                    
                    val event = com.numan.wcmp.data.local.database.entity.SecurityEventEntity(
                        id = java.util.UUID.randomUUID().toString(),
                        sessionId = sessionId,
                        type = "AUTHENTICATION_SUCCESS",
                        severity = "INFO",
                        title = "App Unlocked",
                        description = "Authentication successful for $packageName",
                        trigger = "Biometric / Device Credential",
                        packageName = packageName,
                        timestamp = System.currentTimeMillis(),
                        evidenceCount = 0,
                        duration = 0L,
                        status = "SUCCESS"
                    )
                    securityRepository.insertSecurityEvent(event)
                    sessionManager?.recordEvent()
                }
                AuthenticationResult.FAILED -> {
                    _state.value = AppLockState.DENIED
                    
                    val eventId = java.util.UUID.randomUUID().toString()
                    val event = com.numan.wcmp.data.local.database.entity.SecurityEventEntity(
                        id = eventId,
                        sessionId = sessionId,
                        type = "AUTHENTICATION_FAILED",
                        severity = "HIGH", // Explicit deterministic rule
                        title = "Authentication Failed",
                        description = "Failed to unlock $packageName",
                        trigger = "Biometric / Device Credential",
                        packageName = packageName,
                        timestamp = System.currentTimeMillis(),
                        evidenceCount = 1, // We expect to capture evidence
                        duration = 0L,
                        status = "FAILED"
                    )
                    securityRepository.insertSecurityEvent(event)
                    sessionManager?.recordEvent()
                    
                    val request = EvidenceCaptureRequest(
                        trigger = EvidenceTrigger.AUTHENTICATION_FAILED,
                        preferredType = EvidenceType.FRONT_CAMERA_PHOTO,
                        relatedEventId = eventId,
                        sessionId = sessionId
                    )
                    evidenceCoordinator.requestCapture(request)
                }
                AuthenticationResult.CANCELLED -> {
                    _state.value = AppLockState.DENIED
                    
                    val eventId = java.util.UUID.randomUUID().toString()
                    val event = com.numan.wcmp.data.local.database.entity.SecurityEventEntity(
                        id = eventId,
                        sessionId = sessionId,
                        type = "AUTHENTICATION_CANCELLED",
                        severity = "MEDIUM",
                        title = "Authentication Cancelled",
                        description = "User cancelled unlock for $packageName",
                        trigger = "Biometric / Device Credential",
                        packageName = packageName,
                        timestamp = System.currentTimeMillis(),
                        evidenceCount = 1,
                        duration = 0L,
                        status = "CANCELLED"
                    )
                    securityRepository.insertSecurityEvent(event)
                    sessionManager?.recordEvent()

                    val request = EvidenceCaptureRequest(
                        trigger = EvidenceTrigger.AUTHENTICATION_CANCELLED,
                        preferredType = EvidenceType.FRONT_CAMERA_PHOTO,
                        relatedEventId = eventId,
                        sessionId = sessionId
                    )
                    evidenceCoordinator.requestCapture(request)
                }
                else -> {
                    _state.value = AppLockState.ERROR
                }
            }
        }
    }
}
