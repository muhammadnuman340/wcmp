package com.numan.wcmp.data.model

/**
 * Data entity representing a security rule configuration.
 */
data class ProtectionRuleEntity(
    val id: String,
    val name: String,
    val isEnabled: Boolean,
    val triggerType: String,
    val thresholdCount: Int = 1
)
