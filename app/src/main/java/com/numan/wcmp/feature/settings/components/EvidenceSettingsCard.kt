package com.numan.wcmp.feature.settings.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import com.numan.wcmp.data.WcmpRepositoryProvider
import androidx.compose.runtime.getValue

@Composable
fun EvidenceSettingsCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val repository = WcmpRepositoryProvider.getSecurityRepository(context)
    val cameraEvidenceEnabled by repository.cameraEvidenceEnabled.collectAsState(initial = false)
    val audioEvidenceEnabled by repository.audioEvidenceEnabled.collectAsState(initial = false)

    SettingsSection(
        title = "EVIDENCE SETTINGS",
        modifier = modifier
    ) {
        val cameraStatus = if (cameraEvidenceEnabled) "ENABLED" else "DISABLED"
        val audioStatus = if (audioEvidenceEnabled) "ENABLED" else "DISABLED"
        
        CapabilityStatusRow(label = "Camera Capture", statusText = cameraStatus)
        CapabilityStatusRow(label = "Audio Capture", statusText = audioStatus)
        CapabilityStatusRow(label = "Video Recording", statusText = "NOT IMPLEMENTED")
        CapabilityStatusRow(label = "Location Tracking", statusText = "NOT CONNECTED")
        CapabilityStatusRow(label = "Evidence Retention", statusText = "DEFAULT")
        CapabilityStatusRow(label = "Maximum Evidence Items", statusText = "25")
    }
}
