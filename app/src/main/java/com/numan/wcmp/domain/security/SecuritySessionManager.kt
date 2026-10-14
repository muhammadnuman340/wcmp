package com.numan.wcmp.domain.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class SecuritySessionManager {
    
    private val _currentSession = MutableStateFlow<SecuritySession?>(null)
    val currentSession: StateFlow<SecuritySession?> = _currentSession.asStateFlow()
    
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
        _currentSession.value?.let { current ->
            _currentSession.value = current.copy(eventCount = current.eventCount + 1)
        }
    }
    
    fun recordEvidence() {
        _currentSession.value?.let { current ->
            _currentSession.value = current.copy(evidenceCount = current.evidenceCount + 1)
        }
    }
    
    fun endSession(status: SecuritySessionStatus = SecuritySessionStatus.COMPLETED) {
        _currentSession.value?.let { current ->
            _currentSession.value = current.copy(
                endedAt = System.currentTimeMillis(),
                status = status
            )
        }
        // In a real app, we would persist the completed session to the database here.
        // For this step, we just clear it or let it remain as the last completed session.
        _currentSession.value = null
    }
}
