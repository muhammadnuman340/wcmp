package com.numan.wcmp.core.permissions

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

interface RuntimePermissionManager {
    fun checkPermissionStatus(capability: AndroidCapability): CapabilityStatus
    fun canRequest(capability: AndroidCapability): Boolean
}

class DefaultRuntimePermissionManager(private val context: Context) : RuntimePermissionManager {

    override fun checkPermissionStatus(capability: AndroidCapability): CapabilityStatus {
        if (!capability.isImplemented) {
            return CapabilityStatus.NOT_IMPLEMENTED
        }

        if (capability.category != CapabilityCategory.RUNTIME_PERMISSION) {
            return CapabilityStatus.NOT_APPLICABLE
        }

        // Handle version-aware permission differences
        if (capability == AndroidCapability.NOTIFICATIONS && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return CapabilityStatus.GRANTED // Pre-Android 13 doesn't require runtime permission
        }

        val permission = capability.id
        val isGranted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        
        // At this level, we can only report GRANTED or DENIED (which includes NOT_REQUESTED)
        // Differentiating DENIED vs NOT_REQUESTED requires Activity-level ActivityResultLauncher APIs
        // (shouldShowRequestPermissionRationale). We will default to DENIED here if not granted,
        // and the UI layer can handle rationale if needed.
        return if (isGranted) CapabilityStatus.GRANTED else CapabilityStatus.DENIED
    }

    override fun canRequest(capability: AndroidCapability): Boolean {
        if (!capability.isImplemented) return false
        if (capability.category != CapabilityCategory.RUNTIME_PERMISSION) return false
        if (capability == AndroidCapability.NOTIFICATIONS && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
        
        return checkPermissionStatus(capability) != CapabilityStatus.GRANTED
    }
}
