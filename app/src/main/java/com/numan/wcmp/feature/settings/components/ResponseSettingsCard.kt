package com.numan.wcmp.feature.settings.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ResponseSettingsCard(modifier: Modifier = Modifier) {
    SettingsSection(
        title = "RESPONSE SETTINGS",
        modifier = modifier
    ) {
        CapabilityStatusRow(label = "Silent Mode", statusText = "CONFIGURATION ONLY")
        CapabilityStatusRow(label = "Siren Alarm", statusText = "NOT CONNECTED")
        CapabilityStatusRow(label = "Fake Crash", statusText = "NOT CONNECTED")
        CapabilityStatusRow(label = "Notification Alert", statusText = "CONFIGURATION ONLY")
    }
}
