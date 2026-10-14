package com.numan.wcmp.core.permissions

import android.app.AppOpsManager
import android.content.Context
import android.os.Build
import android.os.Process

interface SpecialAccessManager {
    fun checkSpecialAccessStatus(capability: AndroidCapability): CapabilityStatus
    fun canOpenSettings(capability: AndroidCapability): Boolean
}

class DefaultSpecialAccessManager(private val context: Context) : SpecialAccessManager {

    override fun checkSpecialAccessStatus(capability: AndroidCapability): CapabilityStatus {
        if (!capability.isImplemented) {
            return CapabilityStatus.NOT_IMPLEMENTED
        }

        if (capability.category != CapabilityCategory.SPECIAL_ACCESS) {
            return CapabilityStatus.NOT_APPLICABLE
        }

        return when (capability) {
            AndroidCapability.USAGE_ACCESS -> {
                if (isUsageAccessGranted()) CapabilityStatus.GRANTED else CapabilityStatus.REQUIRES_SPECIAL_ACCESS
            }
            AndroidCapability.ACCESSIBILITY_SERVICE -> {
                if (isAccessibilityServiceEnabled()) CapabilityStatus.GRANTED else CapabilityStatus.REQUIRES_SPECIAL_ACCESS
            }
            else -> CapabilityStatus.UNKNOWN
        }
    }

    private fun isUsageAccessGranted(): Boolean {
        val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOpsManager.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expectedComponentName = android.content.ComponentName(
            context,
            "com.numan.wcmp.core.monitoring.WcmpAccessibilityService"
        ).flattenToString()
        
        val enabledServices = android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        return enabledServices.split(":").contains(expectedComponentName)
    }

    override fun canOpenSettings(capability: AndroidCapability): Boolean {
        if (!capability.isImplemented) return false
        return capability.category == CapabilityCategory.SPECIAL_ACCESS
    }
}
