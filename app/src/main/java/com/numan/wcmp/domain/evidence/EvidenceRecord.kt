package com.numan.wcmp.domain.evidence

/**
 * Types of evidence captured by authorized security triggers.
 */
enum class EvidenceType {
    FRONT_CAMERA_PHOTO,
    AUDIO_CLIP,
    LOCATION_STAMP,
    AUDIT_LOG_SNAPSHOT
}

/**
 * Domain record describing captured evidence metadata.
 */
data class EvidenceRecord(
    val id: String,
    val type: EvidenceType,
    val localUri: String,
    val capturedAtTimestamp: Long,
    val isEncrypted: Boolean = true,
    val fileSizeInBytes: Long = 0L,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracyMeters: Float? = null,
    val altitude: Double? = null,
    val encryptionVersion: Int? = null
)
