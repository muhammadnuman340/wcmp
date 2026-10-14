package com.numan.wcmp.feature.evidence.capture.audio

sealed interface AudioCaptureUiState {
    data object Idle : AudioCaptureUiState
    data object PermissionRequired : AudioCaptureUiState
    data object Initializing : AudioCaptureUiState
    data object Ready : AudioCaptureUiState
    data class Recording(val elapsedSeconds: Long) : AudioCaptureUiState
    data object Finalizing : AudioCaptureUiState
    data class Success(val recordId: String) : AudioCaptureUiState
    data class Failed(val message: String) : AudioCaptureUiState
    data object Cancelled : AudioCaptureUiState
}
