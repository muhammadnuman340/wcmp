package com.numan.wcmp.domain.activity

/**
 * Domain model representing system activity signals (screen on, lock, unlock attempts).
 */
data class DeviceActivity(
    val id: String,
    val timestamp: Long,
    val type: ActivityType,
    val sourcePackage: String? = null,
    val details: String = ""
)

enum class ActivityType {
    SCREEN_ON,
    SCREEN_OFF,
    UNLOCK_SUCCESS,
    UNLOCK_FAILURE,
    APP_OPENED,
    SYSTEM_BOOT
}
