package com.numan.wcmp.domain.events

import com.numan.wcmp.domain.evidence.EvidenceRecord

/**
 * Domain model representing a verified security incident.
 */
data class SecurityEvent(
    val id: String,
    val timestamp: Long,
    val triggerType: EventTriggerType,
    val summary: String,
    val evidence: EvidenceRecord? = null,
    val isConfirmedIntruder: Boolean = false
)

enum class EventTriggerType {
    WRONG_PIN_ENTERED,
    WRONG_PATTERN_ENTERED,
    FAILED_BIOMETRIC,
    APP_UNAUTHORIZED_LAUNCH,
    POWER_MENU_TRIGGER,
    DEVICE_UNLOCKED
}
