package com.numan.wcmp.domain.protection

/**
 * Operating protection modes for WCMP.
 */
enum class ProtectionMode {
    STEALTH,    // Silent capture on unauthorized access
    ALERT,      // Visible alert and deterrent screen
    TRAP,       // Decoy interface
    PASSIVE     // Audit logging only
}

/**
 * Domain model describing active protection configuration.
 */
data class ProtectionConfiguration(
    val mode: ProtectionMode = ProtectionMode.STEALTH,
    val captureSelfieOnFail: Boolean = true,
    val maxUnlockAttemptsAllowed: Int = 2,
    val monitorPowerMenu: Boolean = false,
    val lockProtectedApps: Boolean = false
)
