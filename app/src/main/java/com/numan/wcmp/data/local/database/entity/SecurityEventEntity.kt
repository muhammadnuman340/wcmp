package com.numan.wcmp.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "security_events")
data class SecurityEventEntity(
    @PrimaryKey
    val id: String,
    val sessionId: String? = null,
    val type: String,
    val severity: String,
    val title: String,
    val description: String,
    val trigger: String,
    val packageName: String? = null,
    val applicationLabel: String? = null,
    val timestamp: Long,
    val evidenceCount: Int,
    val duration: Long,
    val status: String? = null,
    val metadata: String? = null
)
