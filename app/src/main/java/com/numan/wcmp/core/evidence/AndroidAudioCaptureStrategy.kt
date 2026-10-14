package com.numan.wcmp.core.evidence

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.numan.wcmp.domain.evidence.EvidenceCaptureRequest
import com.numan.wcmp.domain.evidence.EvidenceCaptureResult
import com.numan.wcmp.domain.evidence.EvidenceCaptureStrategy
import com.numan.wcmp.domain.evidence.EvidenceRecord
import com.numan.wcmp.domain.evidence.EvidenceType
import java.util.UUID

class AndroidAudioCaptureStrategy(
    private val context: Context
) : EvidenceCaptureStrategy {

    override fun isCapabilityAvailable(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    override suspend fun capture(request: EvidenceCaptureRequest): EvidenceCaptureResult {
        if (!isCapabilityAvailable()) {
            return EvidenceCaptureResult.CapabilityUnavailable
        }

        // PLATFORM LIMITATION DOCUMENTATION:
        // Android restricts background microphone use.
        // We use AudioCaptureSession to suspend until the foreground AudioCaptureActivity returns a result.
        
        AudioCaptureSession.prepareSession(request.trigger.name, request.relatedEventId)
        
        val intent = android.content.Intent().apply {
            setClassName(context, "com.numan.wcmp.feature.evidence.capture.audio.AudioCaptureActivity")
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            AudioCaptureSession.complete(null)
            return EvidenceCaptureResult.Failure("Failed to launch AudioCaptureActivity: ${e.message}")
        }
        
        val record = AudioCaptureSession.awaitResult()
        
        return if (record != null) {
            EvidenceCaptureResult.Success(record)
        } else {
            EvidenceCaptureResult.Failure("Audio capture cancelled or failed.")
        }
    }
}
