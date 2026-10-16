package com.numan.wcmp.core.evidence

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import androidx.core.content.ContextCompat
import com.numan.wcmp.domain.evidence.EvidenceCaptureRequest
import com.numan.wcmp.domain.evidence.EvidenceCaptureResult
import com.numan.wcmp.domain.evidence.EvidenceCaptureStrategy
import com.numan.wcmp.domain.evidence.EvidenceRecord
import com.numan.wcmp.domain.evidence.EvidenceType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class AndroidAudioCaptureStrategy(
    private val context: Context
) : EvidenceCaptureStrategy {

    override fun isCapabilityAvailable(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    override suspend fun capture(request: EvidenceCaptureRequest): EvidenceCaptureResult =
        withContext(Dispatchers.IO) {
            if (!isCapabilityAvailable()) {
                return@withContext EvidenceCaptureResult.CapabilityUnavailable
            }

            val id = UUID.randomUUID().toString()
            val timestamp = System.currentTimeMillis()
            // Store temporary plaintext recording in cacheDir, never inside filesDir/evidence
            val audioFile = File(context.cacheDir, "temp_audio_${timestamp}_$id.m4a")
            var recorder: MediaRecorder? = null

            try {
                recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder(context.applicationContext)
                } else {
                    @Suppress("DEPRECATION")
                    MediaRecorder()
                }

                recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
                recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                recorder.setAudioEncodingBitRate(128_000)
                recorder.setAudioSamplingRate(44_100)
                recorder.setOutputFile(audioFile.absolutePath)
                recorder.prepare()
                recorder.start()

                delay(5000L)

                recorder.stop()
                recorder.release()
                recorder = null

                if (audioFile.exists() && audioFile.length() > 0L) {
                    val record = EvidenceRecord(
                        id = id,
                        type = EvidenceType.AUDIO_CLIP,
                        localUri = "file://${audioFile.absolutePath}",
                        capturedAtTimestamp = timestamp,
                        isEncrypted = false,
                        fileSizeInBytes = audioFile.length()
                    )
                    EvidenceCaptureResult.Success(record)
                } else {
                    audioFile.delete()
                    EvidenceCaptureResult.Failure("Recorded audio file is empty or was not created.")
                }
            } catch (ce: CancellationException) {
                runCatching { recorder?.stop() }
                runCatching { recorder?.release() }
                if (audioFile.exists()) audioFile.delete()
                throw ce
            } catch (e: Exception) {
                runCatching { recorder?.stop() }
                runCatching { recorder?.release() }
                if (audioFile.exists()) audioFile.delete()
                EvidenceCaptureResult.Failure("Audio capture failed: ${e.message}")
            }
        }
}
