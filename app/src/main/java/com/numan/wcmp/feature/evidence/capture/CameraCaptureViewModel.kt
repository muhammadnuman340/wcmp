package com.numan.wcmp.feature.evidence.capture

import android.content.Context
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numan.wcmp.core.evidence.CameraCaptureSession
import com.numan.wcmp.domain.evidence.EvidenceRecord
import com.numan.wcmp.domain.evidence.EvidenceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import java.util.concurrent.Executor

class CameraCaptureViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<CameraCaptureUiState>(CameraCaptureUiState.Idle)
    val uiState: StateFlow<CameraCaptureUiState> = _uiState.asStateFlow()

    fun onPermissionGranted() {
        _uiState.value = CameraCaptureUiState.Ready
    }

    fun onPermissionDenied() {
        _uiState.value = CameraCaptureUiState.PermissionRequired("Camera permission is required to capture evidence.")
    }

    fun capturePhoto(
        context: Context,
        cameraController: LifecycleCameraController,
        executor: Executor
    ) {
        if (_uiState.value !is CameraCaptureUiState.Ready) return
        _uiState.value = CameraCaptureUiState.Capturing

        val evidenceDir = File(context.filesDir, "evidence")
        if (!evidenceDir.exists()) {
            evidenceDir.mkdirs()
        }

        val recordId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        val photoFile = File(evidenceDir, "camera_${timestamp}_$recordId.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        cameraController.takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val record = EvidenceRecord(
                        id = recordId,
                        type = EvidenceType.FRONT_CAMERA_PHOTO,
                        localUri = "file://${photoFile.absolutePath}",
                        capturedAtTimestamp = timestamp,
                        isEncrypted = false, // Documenting that standard file storage is used without app-level encryption yet
                        fileSizeInBytes = photoFile.length()
                    )
                    
                    viewModelScope.launch {
                        _uiState.value = CameraCaptureUiState.Success(recordId)
                        CameraCaptureSession.complete(record)
                    }
                }

                override fun onError(exc: ImageCaptureException) {
                    viewModelScope.launch {
                        _uiState.value = CameraCaptureUiState.Failed(exc.message ?: "Unknown error")
                        CameraCaptureSession.complete(null)
                    }
                }
            }
        )
    }

    fun cancel() {
        _uiState.value = CameraCaptureUiState.Cancelled
        CameraCaptureSession.complete(null)
    }
}
