package com.numan.wcmp.core.evidence

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
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
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    override suspend fun capture(request: EvidenceCaptureRequest): EvidenceCaptureResult {
        if (!isCapabilityAvailable()) {
            return EvidenceCaptureResult.CapabilityUnavailable
        }

        val client = LocationServices.getFusedLocationProviderClient(context.applicationContext)
        val priority = if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Priority.PRIORITY_HIGH_ACCURACY
        } else {
            Priority.PRIORITY_BALANCED_POWER_ACCURACY
        }

        val currentLocation: Location? = withTimeoutOrNull(8_000L) {
            suspendCancellableCoroutine { cont ->
                val tokenSource = CancellationTokenSource()
                cont.invokeOnCancellation {
                    tokenSource.cancel()
                }
                client.getCurrentLocation(priority, tokenSource.token)
                    .addOnSuccessListener { loc ->
                        if (cont.isActive) cont.resume(loc)
                    }
                    .addOnFailureListener {
                        if (cont.isActive) cont.resume(null)
                    }
                    .addOnCanceledListener {
                        if (cont.isActive) cont.resume(null)
                    }
            }
        }

        val finalLocation: Location? = currentLocation ?: withTimeoutOrNull(2_000L) {
            suspendCancellableCoroutine { cont ->
                client.lastLocation
                    .addOnSuccessListener { lastLoc ->
                        if (cont.isActive) cont.resume(lastLoc)
                    }
                    .addOnFailureListener {
                        if (cont.isActive) cont.resume(null)
                    }
            }
        }

        return if (finalLocation != null) {
            val record = EvidenceRecord(
                id = UUID.randomUUID().toString(),
                type = EvidenceType.LOCATION_STAMP,
                localUri = "geo:${finalLocation.latitude},${finalLocation.longitude}",
                capturedAtTimestamp = System.currentTimeMillis(),
                isEncrypted = false,
                fileSizeInBytes = 0L,
                latitude = finalLocation.latitude,
                longitude = finalLocation.longitude,
                accuracyMeters = if (finalLocation.hasAccuracy()) finalLocation.accuracy else null,
                altitude = if (finalLocation.hasAltitude()) finalLocation.altitude else null
            )
            EvidenceCaptureResult.Success(record)
        } else {
            EvidenceCaptureResult.Failure("Failed to acquire location fix.")
        }
    }
}
