package com.numan.wcmp.core.evidence

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.numan.wcmp.domain.evidence.EvidenceCaptureRequest
import com.numan.wcmp.domain.evidence.EvidenceCaptureResult
import com.numan.wcmp.domain.evidence.EvidenceCaptureStrategy
import kotlinx.coroutines.withTimeoutOrNull

class AndroidCameraCaptureStrategy(
    private val context: Context
) : EvidenceCaptureStrategy {

    override fun isCapabilityAvailable(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    override suspend fun capture(request: EvidenceCaptureRequest): EvidenceCaptureResult {
        if (!isCapabilityAvailable()) {
            return EvidenceCaptureResult.CapabilityUnavailable
        }

        CameraCaptureSession.prepareSession(request.trigger.name, request.relatedEventId)

        try {
            EvidenceCaptureService.startCapture(
                context = context,
                eventIdStr = request.relatedEventId,
                trigger = request.trigger.name
            )
        } catch (e: Exception) {
            CameraCaptureSession.complete(null)
            return EvidenceCaptureResult.Failure(
                "Failed to start EvidenceCaptureService: ${e.message}"
            )
        }

        val record = withTimeoutOrNull(10_000L) {
            CameraCaptureSession.awaitResult()
        }

        return if (record != null) {
            EvidenceCaptureResult.Success(record)
        } else {
            CameraCaptureSession.complete(null)
            EvidenceCaptureResult.Failure("Camera capture timed out or failed.")
        }
    }
}
