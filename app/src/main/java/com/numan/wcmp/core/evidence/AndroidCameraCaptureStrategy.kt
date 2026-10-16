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

class AndroidCameraCaptureStrategy(
    private val context: Context
) : EvidenceCaptureStrategy {

    override fun isCapabilityAvailable(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    override suspend fun capture(request: EvidenceCaptureRequest): EvidenceCaptureResult {
        if (!isCapabilityAvailable()) {
            return EvidenceCaptureResult.CapabilityUnavailable
        }

        // PLATFORM LIMITATION DOCUMENTATION:
        // Android restricts background camera launches aggressively.
        // A genuine background camera capture without a visible Activity or foreground service
        // is generally blocked on modern Android versions to protect user privacy.
        // We use CameraCaptureSession to suspend until the foreground CameraCaptureActivity returns a result.
        
        CameraCaptureSession.prepareSession(request.trigger.name, request.relatedEventId)
        
        try {
            EvidenceCaptureService.startCapture(
                context = context,
                eventIdStr = request.relatedEventId,
                trigger = request.trigger.name
            )
        } catch (e: Exception) {
            CameraCaptureSession.complete(null)
            return EvidenceCaptureResult.Failure("Failed to start EvidenceCaptureService: ${e.message}")
        }
        
        val record = CameraCaptureSession.awaitResult()
        
        return if (record != null) {
            EvidenceCaptureResult.Success(record)
        } else {
            EvidenceCaptureResult.Failure("Capture cancelled or failed.")
        }
    }
}
