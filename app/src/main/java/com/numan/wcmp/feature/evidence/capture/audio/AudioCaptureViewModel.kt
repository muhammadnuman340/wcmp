package com.numan.wcmp.feature.evidence.capture.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numan.wcmp.core.evidence.AudioCaptureSession
import com.numan.wcmp.domain.evidence.EvidenceRecord
import com.numan.wcmp.domain.evidence.EvidenceType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class AudioCaptureViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<AudioCaptureUiState>(AudioCaptureUiState.Idle)
    val uiState: StateFlow<AudioCaptureUiState> = _uiState.asStateFlow()

    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var recordId: String? = null
    private var startTimestamp: Long = 0L

    private var timerJob: Job? = null
    
    companion object {
        private const val MAX_RECORDING_SECONDS = 60L
    }

    fun onPermissionGranted() {
        if (_uiState.value is AudioCaptureUiState.Idle || _uiState.value is AudioCaptureUiState.PermissionRequired) {
            _uiState.value = AudioCaptureUiState.Ready
        }
    }

    fun onPermissionDenied() {
        _uiState.value = AudioCaptureUiState.PermissionRequired
    }

    fun startRecording(context: Context) {
        if (_uiState.value !is AudioCaptureUiState.Ready) return
        _uiState.value = AudioCaptureUiState.Initializing

        try {
            val id = UUID.randomUUID().toString()
            val timestamp = System.currentTimeMillis()
            val file = File(context.cacheDir, "temp_audio_${timestamp}_$id.m4a")

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            recorder.setOutputFile(file.absolutePath)
            
            recorder.prepare()
            recorder.start()

            mediaRecorder = recorder
            outputFile = file
            recordId = id
            startTimestamp = timestamp

            _uiState.value = AudioCaptureUiState.Recording(0L)
            startTimer()
            
        } catch (e: Exception) {
            releaseRecorder()
            _uiState.value = AudioCaptureUiState.Failed(e.message ?: "Failed to start recording")
            AudioCaptureSession.complete(null)
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var elapsed = 0L
            while (elapsed < MAX_RECORDING_SECONDS) {
                delay(1000)
                elapsed++
                if (_uiState.value is AudioCaptureUiState.Recording) {
                    _uiState.value = AudioCaptureUiState.Recording(elapsed)
                }
            }
            // Auto stop when max duration reached
            stopAndSave()
        }
    }

    fun stopAndSave() {
        if (_uiState.value !is AudioCaptureUiState.Recording) return
        timerJob?.cancel()
        _uiState.value = AudioCaptureUiState.Finalizing

        try {
            mediaRecorder?.stop()
            releaseRecorder()
            
            val file = outputFile
            val id = recordId
            
            if (file != null && file.exists() && file.length() > 0 && id != null) {
                val record = EvidenceRecord(
                    id = id,
                    type = EvidenceType.AUDIO_CLIP,
                    localUri = "file://${file.absolutePath}",
                    capturedAtTimestamp = startTimestamp,
                    isEncrypted = false,
                    fileSizeInBytes = file.length()
                )
                
                _uiState.value = AudioCaptureUiState.Success(id)
                AudioCaptureSession.complete(record)
            } else {
                outputFile?.delete()
                _uiState.value = AudioCaptureUiState.Failed("Recording file is empty or invalid")
                AudioCaptureSession.complete(null)
            }
        } catch (e: Exception) {
            outputFile?.delete()
            releaseRecorder()
            _uiState.value = AudioCaptureUiState.Failed(e.message ?: "Failed to save recording")
            AudioCaptureSession.complete(null)
        }
    }

    fun cancel() {
        timerJob?.cancel()
        try {
            if (_uiState.value is AudioCaptureUiState.Recording) {
                mediaRecorder?.stop()
            }
        } catch (e: Exception) {
            // Ignore stop errors on cancel
        } finally {
            releaseRecorder()
            // Delete temporary file if exists
            outputFile?.let {
                if (it.exists()) it.delete()
            }
            outputFile = null
            recordId = null
            
            _uiState.value = AudioCaptureUiState.Cancelled
            AudioCaptureSession.complete(null)
        }
    }

    private fun releaseRecorder() {
        try {
            mediaRecorder?.release()
        } catch (e: Exception) {
            // Ignore release errors
        }
        mediaRecorder = null
    }

    override fun onCleared() {
        super.onCleared()
        releaseRecorder()
    }
}
