package com.numan.wcmp.feature.settings.model

data class SettingsUiState(
    val protectionMode: String = "Quick Protect",
    val autoProtectionEnabled: Boolean = false,
    val motionDetectionEnabled: Boolean = false,
    val notificationAlertsEnabled: Boolean = true,
    val animationsEnabled: Boolean = true
)
