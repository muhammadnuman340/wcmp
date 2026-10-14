package com.numan.wcmp.feature.settings.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.numan.wcmp.core.permissions.AndroidCapability
import com.numan.wcmp.core.permissions.CapabilityState

@Composable
fun DiagnosticsCard(
    capabilities: List<CapabilityState>,
    modifier: Modifier = Modifier
) {
    SettingsSection(
        title = "ANDROID CAPABILITY LAYER",
        modifier = modifier
    ) {
        if (capabilities.isEmpty()) {
            CapabilityStatusRow(label = "Android Capability Layer", statusText = "NOT CONNECTED")
            return@SettingsSection
        }

        // Specifically demonstrate the requested capabilities for this step
        val displayedCapabilities = listOf(
            AndroidCapability.CAMERA,
            AndroidCapability.NOTIFICATIONS,
            AndroidCapability.USAGE_ACCESS,
            AndroidCapability.ACCESSIBILITY_SERVICE
        )

        capabilities
            .filter { it.capability in displayedCapabilities }
            .sortedBy { displayedCapabilities.indexOf(it.capability) }
            .forEach { state ->
                CapabilityStatusRow(
                    label = state.capability.displayName,
                    statusText = state.status.name
                )
            }
            
        CapabilityStatusRow(label = "Background Maintenance", statusText = "READY (SCHEDULED)")
        CapabilityStatusRow(label = "App Lock Controller", statusText = "READY")
        
        val context = androidx.compose.ui.platform.LocalContext.current
        val biometricManager = androidx.biometric.BiometricManager.from(context)
        val authenticators = androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
        val biometricStatus = when (biometricManager.canAuthenticate(authenticators)) {
            androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS -> "READY"
            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "NO HARDWARE"
            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "HW UNAVAILABLE"
            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "NONE ENROLLED"
            else -> "UNAVAILABLE"
        }
        CapabilityStatusRow(label = "Biometric Authentication", statusText = biometricStatus)
        
        CapabilityStatusRow(label = "Security Engine", statusText = "NOT CONNECTED")
        CapabilityStatusRow(label = "Vault Engine", statusText = "NOT CONNECTED")
    }
}
