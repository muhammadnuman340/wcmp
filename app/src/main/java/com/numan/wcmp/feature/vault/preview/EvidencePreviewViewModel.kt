package com.numan.wcmp.feature.vault.preview

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.numan.wcmp.core.security.evidence.DefaultEvidenceKeyManager
import com.numan.wcmp.domain.evidence.health.ControlledDecryptionService
import com.numan.wcmp.domain.evidence.health.EvidenceDecryptionService
import com.numan.wcmp.domain.evidence.health.AndroidEvidenceDirectoryProvider
import com.numan.wcmp.domain.evidence.health.EvidenceHealth
import com.numan.wcmp.feature.vault.model.VaultEvidence
import com.numan.wcmp.feature.vault.model.VaultEvidenceType
import com.numan.wcmp.feature.vault.viewmodel.VaultEvidenceUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

import kotlinx.coroutines.CoroutineDispatcher
class EvidencePreviewViewModel(
    private val imageDecoder: SecureImageDecoder,
    private val decryptionService: EvidenceDecryptionService,
    private val integrityService: com.numan.wcmp.domain.evidence.health.EvidenceIntegrityService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow<EvidencePreviewState>(EvidencePreviewState.Idle)
    val uiState: StateFlow<EvidencePreviewState> = _uiState.asStateFlow()

    private val _playbackPosition = MutableStateFlow(0L)
    val playbackPosition: StateFlow<Long> = _playbackPosition.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private var activeDecryptedFile: File? = null
    private var decryptionJob: Job? = null

    private var currentEvidenceId: String? = null

    init {
        // Clear stale temporary files on startup
        decryptionService.cleanupCache()
    }

    fun pausePlayback() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                _isPlaying.value = false
                stopProgressTracking()
            }
        }
    }

    fun loadEvidence(evidenceUiModel: VaultEvidenceUiModel, targetWidth: Int = 1080, targetHeight: Int = 1080) {
        val evidence = evidenceUiModel.evidence

        // Prevent duplicate decryption
        if (currentEvidenceId == evidence.id && 
            (_uiState.value is EvidencePreviewState.Preparing || _uiState.value is EvidencePreviewState.Decrypting)) {
            return
        }

        // Cancel any previous job and cleanup
        decryptionJob?.cancel()
        cleanup()
        
        currentEvidenceId = evidence.id
        _uiState.value = EvidencePreviewState.Preparing

        if (evidence.type == VaultEvidenceType.LOCATION) {
            // Location is plaintext Room metadata, don't try to decrypt a physical file
            _uiState.value = EvidencePreviewState.LocationReady(
                latitude = evidence.latitude ?: 0.0,
                longitude = evidence.longitude ?: 0.0,
                accuracy = evidence.accuracyMeters
            )
            return
        }

        // Security State Handling Before Preview
        when (evidenceUiModel.health) {
            EvidenceHealth.INTEGRITY_ERROR -> {
                _uiState.value = EvidencePreviewState.IntegrityError
                return
            }
            EvidenceHealth.KEY_UNAVAILABLE -> {
                _uiState.value = EvidencePreviewState.KeyUnavailable
                return
            }
            EvidenceHealth.MISSING_FILE -> {
                _uiState.value = EvidencePreviewState.MissingFile
                return
            }
            EvidenceHealth.MIGRATION_REQUIRED -> {
                _uiState.value = EvidencePreviewState.MigrationRequired
                return
            }
            EvidenceHealth.LEGACY -> {
                _uiState.value = EvidencePreviewState.LegacyEvidence
                return
            }
            EvidenceHealth.UNKNOWN -> {
                _uiState.value = EvidencePreviewState.Failed("SECURITY STATE UNKNOWN")
                return
            }
            EvidenceHealth.HEALTHY -> {
                // Proceed to controlled decryption
            }
        }

        _uiState.value = EvidencePreviewState.Decrypting

        decryptionJob = viewModelScope.launch(ioDispatcher) {
            try {
                val tempFile = decryptionService.decryptForViewing(evidence)
                activeDecryptedFile = tempFile

                withContext(Dispatchers.Main) {
                    when (evidence.type) {
                        VaultEvidenceType.PHOTO -> {
                            val bitmap = imageDecoder.decode(tempFile, targetWidth, targetHeight)
                            _uiState.value = EvidencePreviewState.ImageReady(bitmap)
                        }
                        VaultEvidenceType.AUDIO -> {
                            prepareAudioPlayback(tempFile)
                        }
                        else -> {
                            _uiState.value = EvidencePreviewState.Failed("Unsupported evidence type for preview")
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.value = EvidencePreviewState.Failed(e.message ?: "Decryption failed")
                }
            }
        }
    }

    private fun prepareAudioPlayback(audioFile: File) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioFile.absolutePath)
                prepare()
                setOnCompletionListener {
                    _isPlaying.value = false
                    _playbackPosition.value = 0L
                    stopProgressTracking()
                    seekTo(0)
                }
            }
            _uiState.value = EvidencePreviewState.AudioReady(
                audioUri = Uri.fromFile(audioFile),
                durationMs = mediaPlayer?.duration?.toLong()
            )
        } catch (e: Exception) {
            _uiState.value = EvidencePreviewState.Failed("Could not prepare audio playback")
        }
    }

    fun togglePlayPause() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                _isPlaying.value = false
                stopProgressTracking()
            } else {
                player.start()
                _isPlaying.value = true
                startProgressTracking()
            }
        }
    }

    fun seekTo(positionMs: Long) {
        mediaPlayer?.let { player ->
            player.seekTo(positionMs.toInt())
            _playbackPosition.value = positionMs
        }
    }

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (true) {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        _playbackPosition.value = player.currentPosition.toLong()
                    }
                }
                kotlinx.coroutines.delay(100)
            }
        }
    }

    private fun stopProgressTracking() {
        progressJob?.cancel()
    }

    fun cleanup() {
        currentEvidenceId = null
        stopProgressTracking()
        mediaPlayer?.release()
        mediaPlayer = null
        
        _isPlaying.value = false
        _playbackPosition.value = 0L

        activeDecryptedFile?.delete()
        activeDecryptedFile = null
        
        // Also call the service cleanup as a fallback
        decryptionService.cleanupCache()
    }

    override fun onCleared() {
        super.onCleared()
        cleanup()
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val keyManager = DefaultEvidenceKeyManager()
            val directoryProvider = AndroidEvidenceDirectoryProvider(context)
            val decryptionService = ControlledDecryptionService(directoryProvider, keyManager)
            val integrityService = com.numan.wcmp.domain.evidence.health.DefaultEvidenceIntegrityService(directoryProvider, keyManager, com.numan.wcmp.domain.evidence.health.EvidenceHealthEvaluator(context.filesDir, keyManager))
            return EvidencePreviewViewModel(DefaultSecureImageDecoder(), decryptionService, integrityService) as T
        }
    }
}









