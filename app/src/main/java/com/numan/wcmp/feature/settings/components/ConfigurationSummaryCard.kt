package com.numan.wcmp.feature.settings.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ConfigurationSummaryCard(modifier: Modifier = Modifier) {
    SettingsSection(
        title = "WCMP CONFIGURATION",
        modifier = modifier
    ) {
        CapabilityStatusRow(label = "Protection", statusText = "READY")
        CapabilityStatusRow(label = "Protected Apps", statusText = "8 CONFIGURED")
        CapabilityStatusRow(label = "Vault", statusText = "NOT CONNECTED")
        CapabilityStatusRow(label = "Security Engine", statusText = "NOT CONNECTED")
    }
}
