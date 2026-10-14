package com.numan.wcmp.data.model

/**
 * Data entity representing an incident or access check record.
 */
data class SecurityEventEntity(
    val id: String,
    val timestamp: Long,
    val eventType: String,
    val description: String,
    val evidenceFilePath: String? = null,
    val isBreachConfirmed: Boolean = false
)
