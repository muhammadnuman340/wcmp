package com.numan.wcmp.domain.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class SecuritySessionManager {
    private val _currentSession = MutableStateFlow<SecuritySession?>(null)
    val currentSession: StateFlow<SecuritySession?> = _currentSession.asStateFlow()

    private val _lastCompletedSession = MutableStateFlow<SecuritySession?>(null)
    val lastCompletedSession: StateFlow<SecuritySession?> = _lastCompletedSession.asStateFlow()

    fun startSession(trigger: String, protectedPackage: String? = null): String {
        val sessionId = UUID.randomUUID().toString()
        val session = SecuritySession(
            sessionId = sessionId,
            startedAt = System.currentTimeMillis(),
            trigger = trigger,
            protectedPackage = protectedPackage,
            status = SecuritySessionStatus.ACTIVE,
            eventCount = 0,
            evidenceCount = 0
        )
        _currentSession.value = session
        return sessionId
    }

    fun recordEvent() {
        _currentSession.update { current ->
            current?.copy(eventCount = current.eventCount + 1)
        }
    }

    fun recordEvidence() {
        _currentSession.update { current ->
            current?.copy(evidenceCount = current.evidenceCount + 1)
        }
    }

    fun endSession(status: SecuritySessionStatus = SecuritySessionStatus.COMPLETED) {
        _currentSession.update { current ->
            if (current != null) {
                _lastCompletedSession.value = current.copy(
                    endedAt = System.currentTimeMillis(),
                    status = status
                )
            }
            null
        }
    }
}
