package com.numan.wcmp.core.permissions

/**
 * State representation for system security permissions
 */
enum class WcmpPermissionStatus {
    GRANTED,
    DENIED,
    PERMANENTLY_DENIED,
    NOT_REQUESTED
}

/**
 * Clean permission contract for hardware and system capabilities
 */
interface PermissionContract {
    fun hasCameraPermission(): Boolean
    fun hasUsageStatsPermission(): Boolean
    fun hasNotificationPermission(): Boolean
    fun hasOverlayPermission(): Boolean
}
