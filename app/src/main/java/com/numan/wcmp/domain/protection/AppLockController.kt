package com.numan.wcmp.domain.protection

import com.numan.wcmp.data.local.database.entity.SecurityEventEntity
import com.numan.wcmp.data.repository.SecurityRepository
import com.numan.wcmp.domain.evidence.EvidenceCaptureCoordinator
import com.numan.wcmp.domain.evidence.EvidenceCaptureRequest
import com.numan.wcmp.domain.evidence.EvidenceCaptureResult
import com.numan.wcmp.domain.evidence.EvidenceTrigger
import com.numan.wcmp.domain.evidence.EvidenceType
import com.numan.wcmp.domain.security.ForegroundApp
import com.numan.wcmp.domain.security.SecuritySessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

class AppLockController(
    private val policy: AppLockPolicy,
    private val securityRepository: SecurityRepository,
    private val presenter: AppLockPresenter,
    private val evidenceCoordinator: EvidenceCaptureCoordinator,
    private val coroutineScope: CoroutineScope,
    private val sessionManager: SecuritySessionManager? = null
) {
    private val _state = MutableStateFlow(AppLockState.WAITING_FOR_PROTECTED_APP)
    val state: StateFlow<AppLockState> = _state.asStateFlow()

    private val sessionMutex = Mutex()

    @Volatile
    private var currentSession: AppLockSession? = null

    fun onProtectedAppAccessed(app: ForegroundApp) {
        coroutineScope.launch {
            sessionMutex.withLock {
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
    }

    fun onProtectedAppLeft(packageName: String) {
        coroutineScope.launch {
            sessionMutex.withLock {
                if (currentSession?.packageName == packageName) {
                    currentSession = null
                    _state.value = AppLockState.WAITING_FOR_PROTECTED_APP
                }
            }
        }
    }

    fun onAuthenticationCompleted(packageName: String, result: AuthenticationResult) {
        coroutineScope.launch {
            val sessionId = sessionManager?.currentSession?.value?.sessionId
            when (result) {
                AuthenticationResult.SUCCESS -> {
                    sessionMutex.withLock {
                        currentSession = AppLockSession(packageName = packageName)
                        _state.value = AppLockState.UNLOCKED
                    }
                    val event = SecurityEventEntity(
                        id = UUID.randomUUID().toString(),
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
                    val eventId = UUID.randomUUID().toString()
                    var capturedCount = 0
                    val captureTypes = listOf(
                        EvidenceType.FRONT_CAMERA_PHOTO,
                        EvidenceType.LOCATION_STAMP
                    )
                    for (type in captureTypes) {
                        val captureResult = evidenceCoordinator.requestCapture(
                            EvidenceCaptureRequest(
                                trigger = EvidenceTrigger.AUTHENTICATION_FAILED,
                                preferredType = type,
                                relatedEventId = eventId,
                                sessionId = sessionId
                            )
                        )
                        if (captureResult is EvidenceCaptureResult.Success) {
                            capturedCount++
                            sessionManager?.recordEvidence()
                        }
                    }
                    val event = SecurityEventEntity(
                        id = eventId,
                        sessionId = sessionId,
                        type = "AUTHENTICATION_FAILED",
                        severity = "HIGH",
                        title = "Authentication Failed",
                        description = "Failed to unlock $packageName",
                        trigger = "Biometric / Device Credential",
                        packageName = packageName,
                        timestamp = System.currentTimeMillis(),
                        evidenceCount = capturedCount,
                        duration = 0L,
                        status = "FAILED"
                    )
                    securityRepository.insertSecurityEvent(event)
                    sessionManager?.recordEvent()
                }
                AuthenticationResult.CANCELLED -> {
                    _state.value = AppLockState.DENIED
                    val eventId = UUID.randomUUID().toString()
                    val captureResult = evidenceCoordinator.requestCapture(
                        EvidenceCaptureRequest(
                            trigger = EvidenceTrigger.AUTHENTICATION_CANCELLED,
                            preferredType = EvidenceType.FRONT_CAMERA_PHOTO,
                            relatedEventId = eventId,
                            sessionId = sessionId
                        )
                    )
                    val capturedCount = if (captureResult is EvidenceCaptureResult.Success) {
                        sessionManager?.recordEvidence()
                        1
                    } else {
                        0
                    }
                    val event = SecurityEventEntity(
                        id = eventId,
                        sessionId = sessionId,
                        type = "AUTHENTICATION_CANCELLED",
                        severity = "MEDIUM",
                        title = "Authentication Cancelled",
                        description = "User cancelled unlock for $packageName",
                        trigger = "Biometric / Device Credential",
                        packageName = packageName,
                        timestamp = System.currentTimeMillis(),
                        evidenceCount = capturedCount,
                        duration = 0L,
                        status = "CANCELLED"
                    )
                    securityRepository.insertSecurityEvent(event)
                    sessionManager?.recordEvent()
                }
                AuthenticationResult.UNAVAILABLE,
                AuthenticationResult.ERROR -> {
                    _state.value = AppLockState.ERROR
                    val event = SecurityEventEntity(
                        id = UUID.randomUUID().toString(),
                        sessionId = sessionId,
                        type = "AUTHENTICATION_ERROR",
                        severity = "MEDIUM",
                        title = "Authentication Error",
                        description = "Authentication error ($result) for $packageName",
                        trigger = "Biometric / Device Credential",
                        packageName = packageName,
                        timestamp = System.currentTimeMillis(),
                        evidenceCount = 0,
                        duration = 0L,
                        status = result.name
                    )
                    securityRepository.insertSecurityEvent(event)
                    sessionManager?.recordEvent()
                }
            }
        }
    }
}
