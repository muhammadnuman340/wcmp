package com.numan.wcmp.feature.settings.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun NotificationSettingsCard(modifier: Modifier = Modifier) {
    SettingsSection(
        title = "NOTIFICATION SETTINGS",
        modifier = modifier
    ) {
        CapabilityStatusRow(label = "Security Alerts", statusText = "ON")
        CapabilityStatusRow(label = "Event Notifications", statusText = "ON")
        CapabilityStatusRow(label = "Protection Status", statusText = "ON")
        CapabilityStatusRow(label = "Evidence Notifications", statusText = "ON")
        CapabilityStatusRow(label = "Permission State", statusText = "NOT CONNECTED")
    }
}
