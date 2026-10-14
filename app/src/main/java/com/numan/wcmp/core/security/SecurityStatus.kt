package com.numan.wcmp.core.security

/**
 * High-level security and monitoring status of the device.
 */
enum class SecurityStatus {
    ARMED,
    STANDBY,
    TRIGGERED,
    RESTRICTED,
    UNAVAILABLE
}

/**
 * System Protection State details
 */
data class ProtectionState(
    val status: SecurityStatus = SecurityStatus.STANDBY,
    val isArmed: Boolean = false,
    val activeRulesCount: Int = 0,
    val lastCheckTimestamp: Long = 0L,
    val totalBreachCount: Int = 0
)
