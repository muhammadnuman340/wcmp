package com.numan.wcmp.core.evidence

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.numan.wcmp.domain.evidence.EvidenceCaptureRequest
import com.numan.wcmp.domain.evidence.EvidenceCaptureResult
import com.numan.wcmp.domain.evidence.EvidenceCaptureStrategy
import com.numan.wcmp.domain.evidence.EvidenceRecord
import com.numan.wcmp.domain.evidence.EvidenceType
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.UUID
import kotlin.coroutines.resume

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

    @SuppressLint("MissingPermission")
    override suspend fun capture(request: EvidenceCaptureRequest): EvidenceCaptureResult {
        if (!isCapabilityAvailable()) {
            return EvidenceCaptureResult.CapabilityUnavailable
        }

        val client = LocationServices.getFusedLocationProviderClient(context)
        val location: Location? = withTimeoutOrNull(10000L) {
            suspendCancellableCoroutine { cont ->
                client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { loc ->
                        if (cont.isActive) cont.resume(loc)
                    }
                    .addOnFailureListener {
                        client.lastLocation
                            .addOnSuccessListener { lastLoc ->
                                if (cont.isActive) cont.resume(lastLoc)
                            }
                            .addOnFailureListener {
                                if (cont.isActive) cont.resume(null)
                            }
                    }
            }
        }

        return if (location != null) {
            val record = EvidenceRecord(
                id = UUID.randomUUID().toString(),
                type = EvidenceType.LOCATION_STAMP,
                localUri = "geo:${location.latitude},${location.longitude}",
                capturedAtTimestamp = System.currentTimeMillis(),
                fileSizeInBytes = 0L,
                latitude = location.latitude,
                longitude = location.longitude,
                accuracyMeters = location.accuracy,
                altitude = location.altitude
            )
            EvidenceCaptureResult.Success(record)
        } else {
            EvidenceCaptureResult.Failure("Failed to acquire location fix.")
        }
    }
}
