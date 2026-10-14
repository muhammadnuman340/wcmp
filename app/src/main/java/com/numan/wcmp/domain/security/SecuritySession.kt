package com.numan.wcmp.domain.security

data class SecuritySession(
    val sessionId: String,
    val startedAt: Long,
    val endedAt: Long? = null,
    val trigger: String, // e.g., "Protected App Access", "Motion Detection"
    val protectedPackage: String? = null,
    val status: SecuritySessionStatus,
    val eventCount: Int = 0,
    val evidenceCount: Int = 0
)

enum class SecuritySessionStatus {
    ACTIVE,
    COMPLETED,
    CANCELLED,
    ERROR
}
