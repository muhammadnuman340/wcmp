package com.numan.wcmp.domain.rules

import com.numan.wcmp.domain.events.EventTriggerType

/**
 * Domain model representing a user-configured detection rule.
 */
data class SecurityRule(
    val id: String,
    val name: String,
    val description: String,
    val isEnabled: Boolean,
    val triggerType: EventTriggerType,
    val thresholdAttempts: Int = 1,
    val capturePhoto: Boolean = true,
    val playAlarmSound: Boolean = false
)
