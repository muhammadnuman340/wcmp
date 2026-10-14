package com.numan.wcmp.feature.settings.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun DetectionSettingsCard(modifier: Modifier = Modifier) {
    SettingsSection(
        title = "DETECTION SETTINGS",
        modifier = modifier
    ) {
        CapabilityStatusRow(label = "Motion Detection", statusText = "CONFIGURATION ONLY")
        CapabilityStatusRow(label = "Wrong PIN / Pattern", statusText = "CONFIGURATION ONLY")
        CapabilityStatusRow(label = "Protected App Access", statusText = "CONFIGURATION ONLY")
        CapabilityStatusRow(label = "Device Activity", statusText = "CONFIGURATION ONLY")
    }
}
