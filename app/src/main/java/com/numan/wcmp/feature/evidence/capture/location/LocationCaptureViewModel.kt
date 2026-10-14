package com.numan.wcmp.feature.evidence.capture.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.numan.wcmp.core.evidence.LocationCaptureSession
import com.numan.wcmp.domain.evidence.EvidenceRecord
import com.numan.wcmp.domain.evidence.EvidenceType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import java.util.UUID

class LocationCaptureViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<LocationCaptureUiState>(LocationCaptureUiState.Idle)
    val uiState: StateFlow<LocationCaptureUiState> = _uiState.asStateFlow()

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var captureJob: Job? = null
    
    companion object {
        private const val LOCATION_TIMEOUT_MS = 15000L
    }

    fun onPermissionGranted() {
        if (_uiState.value is LocationCaptureUiState.Idle || _uiState.value is LocationCaptureUiState.PermissionRequired) {
            _uiState.value = LocationCaptureUiState.Idle
        }
    }

    fun onPermissionDenied() {
        _uiState.value = LocationCaptureUiState.PermissionRequired
    }

    @SuppressLint("MissingPermission")
    fun startCapture(context: Context) {
        if (_uiState.value is LocationCaptureUiState.Acquiring) return
        _uiState.value = LocationCaptureUiState.Acquiring

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        captureJob?.cancel()
        captureJob = viewModelScope.launch {
            try {
                // Timeout wrap
                val location: Location? = kotlinx.coroutines.withTimeoutOrNull(LOCATION_TIMEOUT_MS) {
                    suspendCancellableCoroutine { cont ->
                        fusedLocationClient?.getCurrentLocation(
                            Priority.PRIORITY_HIGH_ACCURACY,
                            null
                        )?.addOnSuccessListener { loc ->
                            if (cont.isActive) cont.resume(loc)
                        }?.addOnFailureListener { e ->
                            if (cont.isActive) cont.resumeWithException(e)
                        }?.addOnCanceledListener {
                            if (cont.isActive) cont.cancel()
                        }
                    }
                }

                if (location != null) {
                    val id = UUID.randomUUID().toString()
                    val record = EvidenceRecord(
                        id = id,
                        type = EvidenceType.LOCATION_STAMP,
                        localUri = "wcmp://location/$id",
                        capturedAtTimestamp = System.currentTimeMillis(),
                        isEncrypted = false,
                        fileSizeInBytes = 0L,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracyMeters = if (location.hasAccuracy()) location.accuracy else null,
                        altitude = if (location.hasAltitude()) location.altitude else null
                    )
                    
                    _uiState.value = LocationCaptureUiState.Success(
                        recordId = id,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracyMeters = if (location.hasAccuracy()) location.accuracy else 0f
                    )
                    LocationCaptureSession.complete(record)
                } else {
                    _uiState.value = LocationCaptureUiState.Failed("LOCATION UNAVAILABLE: Unable to obtain a valid location.")
                    LocationCaptureSession.complete(null)
                }
            } catch (e: Exception) {
                _uiState.value = LocationCaptureUiState.Failed("LOCATION REQUEST FAILED: ${e.message}")
                LocationCaptureSession.complete(null)
            }
        }
    }

    fun cancel() {
        captureJob?.cancel()
        _uiState.value = LocationCaptureUiState.Cancelled
        LocationCaptureSession.complete(null)
    }
}
