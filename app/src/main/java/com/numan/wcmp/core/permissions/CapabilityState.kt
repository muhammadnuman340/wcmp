package com.numan.wcmp.core.permissions

data class CapabilityState(
    val capability: AndroidCapability,
    val status: CapabilityStatus,
    val canRequest: Boolean,
    val canOpenSettings: Boolean
)
