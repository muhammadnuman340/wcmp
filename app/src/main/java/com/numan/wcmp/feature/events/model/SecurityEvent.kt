package com.numan.wcmp.feature.events.model

import androidx.compose.ui.graphics.vector.ImageVector

enum class EventSeverity {
    INFO, LOW, MEDIUM, HIGH, CRITICAL
}

enum class EventFilter(val label: String) {
    ALL("ALL"),
    INTRUDER("INTRUDER"),
    APP_ACCESS("APP ACCESS"),
    DEVICE("DEVICE"),
    WCMP("WCMP")
}

data class SecurityEvent(
    val id: String,
    val sessionId: String?,
    val title: String,
    val description: String,
    val triggerSource: String,
    val packageName: String?,
    val applicationLabel: String?,
    val evidenceCount: Int,
    val duration: String,
    val timestamp: String,
    val severity: EventSeverity,
    val type: String,
    val icon: ImageVector,
    val status: String?,
    val metadata: String?
)
