package com.numan.wcmp.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vault_evidence")
data class VaultEvidenceEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val type: String,
    val relatedEventId: String,
    val timestamp: Long,
    val size: String,
    val category: String,
    val storageStatus: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracyMeters: Float? = null,
    val altitude: Double? = null,
    val isEncrypted: Boolean = false,
    val encryptionVersion: Int?,
    val sessionId: String? = null
)
