package com.numan.wcmp.domain.security

import com.numan.wcmp.data.local.database.entity.SecurityEventEntity
import com.numan.wcmp.data.repository.SecurityRepository
import java.util.UUID

open class SecurityEventProcessor(
    private val securityRepository: SecurityRepository?,
    private val sessionManager: SecuritySessionManager? = null
) {
    open suspend fun processProtectedAppAccess(app: ForegroundApp) {
        val sessionId = sessionManager?.currentSession?.value?.sessionId
        
        val event = SecurityEventEntity(
            id = UUID.randomUUID().toString(),
            sessionId = sessionId,
            type = "PROTECTED_APP_ACCESS",
            severity = "HIGH", // Deterministic rule: Protected app access = HIGH
            title = "Protected App Accessed",
            description = "Access detected for ${app.applicationName ?: app.packageName}",
            trigger = app.packageName,
            packageName = app.packageName,
            applicationLabel = app.applicationName,
            timestamp = app.observedAt,
            evidenceCount = 0,
            duration = 0L,
            status = "RECORDED",
            metadata = "{\"source\":\"ForegroundMonitor\"}"
        )
        securityRepository?.insertSecurityEvent(event)
        sessionManager?.recordEvent()
    }
}
