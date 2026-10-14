package com.numan.wcmp.feature.settings.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.numan.wcmp.feature.settings.model.SettingsUiState

@Composable
fun ProtectionSettingsCard(
    state: SettingsUiState,
    modifier: Modifier = Modifier
) {
    SettingsSection(
        title = "PROTECTION SETTINGS",
        modifier = modifier
    ) {
        CapabilityStatusRow(label = "Protection Mode", statusText = state.protectionMode)
        CapabilityStatusRow(
            label = "Auto Protection",
            statusText = if (state.autoProtectionEnabled) "ON" else "OFF"
        )
        CapabilityStatusRow(label = "Protection on App Access", statusText = "CONFIGURED")
        CapabilityStatusRow(label = "Protection Duration", statusText = "DEFAULT")
        CapabilityStatusRow(label = "Engine Status", statusText = "NOT CONNECTED")
    }
}
