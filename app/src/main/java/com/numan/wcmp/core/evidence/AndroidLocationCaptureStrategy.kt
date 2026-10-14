package com.numan.wcmp.core.evidence

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.numan.wcmp.domain.evidence.EvidenceCaptureRequest
import com.numan.wcmp.domain.evidence.EvidenceCaptureResult
import com.numan.wcmp.domain.evidence.EvidenceCaptureStrategy

class AndroidLocationCaptureStrategy(
    private val context: Context
) : EvidenceCaptureStrategy {

    override fun isCapabilityAvailable(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override suspend fun capture(request: EvidenceCaptureRequest): EvidenceCaptureResult {
        if (!isCapabilityAvailable()) {
            return EvidenceCaptureResult.CapabilityUnavailable
        }

        LocationCaptureSession.prepareSession(request.trigger.name, request.relatedEventId)
        
        val intent = Intent().apply {
            setClassName(context, "com.numan.wcmp.feature.evidence.capture.location.LocationCaptureActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            LocationCaptureSession.complete(null)
            return EvidenceCaptureResult.Failure("Failed to launch LocationCaptureActivity: ${e.message}")
        }
        
        val record = LocationCaptureSession.awaitResult()
        
        return if (record != null) {
            EvidenceCaptureResult.Success(record)
        } else {
            EvidenceCaptureResult.Failure("Location capture cancelled or failed.")
        }
    }
}
