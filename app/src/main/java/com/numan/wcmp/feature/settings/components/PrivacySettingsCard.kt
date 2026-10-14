package com.numan.wcmp.feature.settings.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PrivacySettingsCard(modifier: Modifier = Modifier) {
    SettingsSection(
        title = "PRIVACY SETTINGS",
        modifier = modifier
    ) {
        CapabilityStatusRow(label = "Local Data", statusText = "DEVICE ONLY")
        CapabilityStatusRow(label = "Cloud Sync", statusText = "NOT CONNECTED")
        CapabilityStatusRow(label = "Analytics", statusText = "OFF")
        CapabilityStatusRow(label = "Data Export", statusText = "NOT IMPLEMENTED")
    }
}
