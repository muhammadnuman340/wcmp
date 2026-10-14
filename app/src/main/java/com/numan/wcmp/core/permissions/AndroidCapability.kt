package com.numan.wcmp.core.permissions

enum class AndroidCapability(
    val id: String,
    val displayName: String,
    val description: String,
    val category: CapabilityCategory,
    val isImplemented: Boolean = true
) {
    CAMERA(
        id = "android.permission.CAMERA",
        displayName = "Camera",
        description = "Allows WCMP to capture photos of potential intruders.",
        category = CapabilityCategory.RUNTIME_PERMISSION
    ),
    MICROPHONE(
        id = "android.permission.RECORD_AUDIO",
        displayName = "Microphone",
        description = "Allows WCMP to record audio evidence.",
        category = CapabilityCategory.RUNTIME_PERMISSION
    ),
    LOCATION(
        id = "android.permission.ACCESS_FINE_LOCATION",
        displayName = "Location",
        description = "Allows WCMP to track where the device was accessed.",
        category = CapabilityCategory.RUNTIME_PERMISSION,
        isImplemented = false
    ),
    NOTIFICATIONS(
        id = "android.permission.POST_NOTIFICATIONS",
        displayName = "Notifications",
        description = "Allows WCMP to send alert notifications.",
        category = CapabilityCategory.RUNTIME_PERMISSION
    ),
    USAGE_ACCESS(
        id = "android.settings.USAGE_ACCESS_SETTINGS",
        displayName = "Usage Access",
        description = "Allows WCMP to detect which apps are being used.",
        category = CapabilityCategory.SPECIAL_ACCESS
    ),
    ACCESSIBILITY_SERVICE(
        id = "android.settings.ACCESSIBILITY_SETTINGS",
        displayName = "Foreground Monitoring",
        description = "Allows WCMP to securely detect foreground application transitions for App Lock.",
        category = CapabilityCategory.SPECIAL_ACCESS,
        isImplemented = true
    ),
    PACKAGE_VISIBILITY(
        id = "android.permission.QUERY_ALL_PACKAGES",
        displayName = "Package Visibility",
        description = "Allows WCMP to discover installed apps for protection.",
        category = CapabilityCategory.MANIFEST_BEHAVIOR
    ),
    FOREGROUND_SERVICE(
        id = "android.permission.FOREGROUND_SERVICE",
        displayName = "Background Surveillance",
        description = "Allows WCMP to monitor the device in the background.",
        category = CapabilityCategory.MANIFEST_BEHAVIOR,
        isImplemented = false
    )
}

enum class CapabilityCategory {
    RUNTIME_PERMISSION,
    SPECIAL_ACCESS,
    MANIFEST_BEHAVIOR
}
