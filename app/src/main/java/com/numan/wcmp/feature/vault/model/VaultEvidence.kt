package com.numan.wcmp.feature.vault.model

enum class VaultEvidenceType {
    PHOTO,
    VIDEO,
    AUDIO,
    LOCATION,
    EVENT,
    DOCUMENT,
    OTHER
}

enum class VaultCategory {
    ALL,
    PHOTOS,
    VIDEOS,
    AUDIO,
    EVENTS,
    DOCUMENTS,
    OTHER
}

data class VaultEvidence(
    val id: String,
    val title: String,
    val type: VaultEvidenceType,
    val relatedEvent: String,
    val timestamp: String,
    val size: String,
    val category: VaultCategory,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracyMeters: Float? = null,
    val altitude: Double? = null,
    val isEncrypted: Boolean = false,
    val encryptionVersion: Int? = null,
    val sessionId: String? = null
)
