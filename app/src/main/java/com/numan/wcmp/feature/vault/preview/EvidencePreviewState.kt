package com.numan.wcmp.feature.vault.preview

import android.net.Uri

sealed interface EvidencePreviewState {
    data object Idle : EvidencePreviewState
    data object Preparing : EvidencePreviewState
    data object Decrypting : EvidencePreviewState

    data class ImageReady(
        val image: PreviewImageData
    ) : EvidencePreviewState

    data class AudioReady(
        val audioUri: Uri,
        val durationMs: Long?
    ) : EvidencePreviewState

    data class LocationReady(
        val latitude: Double,
        val longitude: Double,
        val accuracy: Float?
    ) : EvidencePreviewState

    data object KeyUnavailable : EvidencePreviewState
    data object IntegrityError : EvidencePreviewState
    data object MissingFile : EvidencePreviewState
    data object LegacyEvidence : EvidencePreviewState
    data object MigrationRequired : EvidencePreviewState
    data class Failed(val message: String) : EvidencePreviewState
}


