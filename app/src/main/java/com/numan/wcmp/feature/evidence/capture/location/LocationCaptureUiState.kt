package com.numan.wcmp.feature.evidence.capture.location

sealed interface LocationCaptureUiState {
    data object Idle : LocationCaptureUiState
    data object PermissionRequired : LocationCaptureUiState
    data object Acquiring : LocationCaptureUiState
    data class Success(
        val recordId: String,
        val latitude: Double,
        val longitude: Double,
        val accuracyMeters: Float
    ) : LocationCaptureUiState
    data class Failed(val message: String) : LocationCaptureUiState
    data object Cancelled : LocationCaptureUiState
}
