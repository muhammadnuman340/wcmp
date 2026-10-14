package com.numan.wcmp.feature.evidence.capture

sealed interface CameraCaptureUiState {
    data object Idle : CameraCaptureUiState
    data object Initializing : CameraCaptureUiState
    data object Ready : CameraCaptureUiState
    data object Capturing : CameraCaptureUiState
    data class Success(val recordId: String) : CameraCaptureUiState
    data class PermissionRequired(val message: String) : CameraCaptureUiState
    data class Unavailable(val message: String) : CameraCaptureUiState
    data class Failed(val message: String) : CameraCaptureUiState
    data object Cancelled : CameraCaptureUiState
}
