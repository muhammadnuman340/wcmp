package com.numan.wcmp.feature.rules.model

data class ProtectionRuleItem(
    val id: String,
    val name: String,
    val description: String,
    val status: ProtectionRuleStatus,
    val trigger: String,
    val target: String,
    val action: String,
    val stealthMode: Boolean = true,
    val retryLimit: Int = 2
)
