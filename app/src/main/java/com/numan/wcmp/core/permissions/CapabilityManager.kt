package com.numan.wcmp.core.permissions

class CapabilityManager(
    private val runtimePermissionManager: RuntimePermissionManager,
    private val specialAccessManager: SpecialAccessManager
) {

    fun getCapabilityState(capability: AndroidCapability): CapabilityState {
        if (!capability.isImplemented) {
            return CapabilityState(
                capability = capability,
                status = CapabilityStatus.NOT_IMPLEMENTED,
                canRequest = false,
                canOpenSettings = false
            )
        }

        val status = when (capability.category) {
            CapabilityCategory.RUNTIME_PERMISSION -> runtimePermissionManager.checkPermissionStatus(capability)
            CapabilityCategory.SPECIAL_ACCESS -> specialAccessManager.checkSpecialAccessStatus(capability)
            CapabilityCategory.MANIFEST_BEHAVIOR -> CapabilityStatus.GRANTED // Assumed granted if in manifest and no runtime check needed
        }

        val canRequest = when (capability.category) {
            CapabilityCategory.RUNTIME_PERMISSION -> runtimePermissionManager.canRequest(capability)
            else -> false
        }

        val canOpenSettings = when (capability.category) {
            CapabilityCategory.SPECIAL_ACCESS -> specialAccessManager.canOpenSettings(capability)
            CapabilityCategory.RUNTIME_PERMISSION -> status == CapabilityStatus.DENIED // Can always direct to app settings for runtime perms
            else -> false
        }

        return CapabilityState(
            capability = capability,
            status = status,
            canRequest = canRequest,
            canOpenSettings = canOpenSettings
        )
    }

    fun getAllCapabilityStates(): List<CapabilityState> {
        return AndroidCapability.entries.map { getCapabilityState(it) }
    }
}
