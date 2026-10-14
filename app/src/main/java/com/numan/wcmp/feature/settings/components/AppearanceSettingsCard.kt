package com.numan.wcmp.feature.settings.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AppearanceSettingsCard(
    animationsEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    SettingsSection(
        title = "APPEARANCE SETTINGS",
        modifier = modifier
    ) {
        CapabilityStatusRow(label = "Theme", statusText = "DARK")
        CapabilityStatusRow(label = "Accent Style", statusText = "WCMP BLUE")
        CapabilityStatusRow(
            label = "Animations",
            statusText = if (animationsEnabled) "ON" else "OFF"
        )
    }
}
