package com.numan.wcmp.core.evidence

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.numan.wcmp.core.notifications.NotificationChannels
import com.numan.wcmp.domain.evidence.EvidenceRecord
import com.numan.wcmp.domain.evidence.EvidenceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.util.UUID
import kotlin.coroutines.resume

class EvidenceCaptureService : LifecycleService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val eventId = intent?.getLongExtra(EXTRA_EVENT_ID, -1L) ?: -1L
        val notification = NotificationChannels.createSilentSecurityNotification(this)

        val hasCameraPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasCameraPermission) {
            CameraCaptureSession.complete(null)
            stopSelf(startId)
            return START_NOT_STICKY
        }

        val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
        } else {
            0
        }

        try {
            ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, serviceType)
        } catch (e: Exception) {
            CameraCaptureSession.complete(null)
            stopSelf(startId)
            return START_NOT_STICKY
        }

        lifecycleScope.launch(Dispatchers.Main) {
            var capturedFile: File? = null
            try {
                capturedFile = captureFrontPhoto(eventId)
                if (capturedFile != null && capturedFile.exists() && capturedFile.length() > 0L) {
                    val record = EvidenceRecord(
                        id = UUID.randomUUID().toString(),
                        type = EvidenceType.FRONT_CAMERA_PHOTO,
                        localUri = "file://${capturedFile.absolutePath}",
                        capturedAtTimestamp = System.currentTimeMillis(),
                        isEncrypted = false,
                        fileSizeInBytes = capturedFile.length()
                    )
                    val delivered = CameraCaptureSession.complete(record)
                    if (!delivered) {
                        capturedFile.delete()
                    }
                } else {
                    capturedFile?.delete()
                    CameraCaptureSession.complete(null)
                }
            } catch (e: Exception) {
                capturedFile?.delete()
                CameraCaptureSession.complete(null)
            } finally {
                ServiceCompat.stopForeground(
                    this@EvidenceCaptureService,
                    ServiceCompat.STOP_FOREGROUND_REMOVE
                )
                stopSelf(startId)
            }
        }

        return START_NOT_STICKY
    }

    private suspend fun captureFrontPhoto(eventId: Long): File? {
        val provider = suspendCancellableCoroutine<ProcessCameraProvider?> { cont ->
            val future = ProcessCameraProvider.getInstance(this)
            future.addListener({
                try {
                    if (cont.isActive) cont.resume(future.get())
                } catch (e: Exception) {
                    if (cont.isActive) cont.resume(null)
                }
            }, ContextCompat.getMainExecutor(this))
        } ?: return null

        val imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        return try {
            provider.unbindAll()
            provider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_FRONT_CAMERA,
                imageCapture
            )

            // Allow sensor 3A (auto-exposure / auto-white-balance) to settle before shutter
            delay(350L)

            val outputFile = File(
                cacheDir,
                "intruder_${eventId}_${System.currentTimeMillis()}.jpg"
            )
            val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

            suspendCancellableCoroutine { cont ->
                cont.invokeOnCancellation {
                    provider.unbindAll()
                    if (outputFile.exists()) outputFile.delete()
                }
                imageCapture.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(this),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(results: ImageCapture.OutputFileResults) {
                            provider.unbindAll()
                            if (cont.isActive) {
                                cont.resume(outputFile)
                            } else {
                                outputFile.delete()
                            }
                        }

                        override fun onError(exc: ImageCaptureException) {
                            provider.unbindAll()
                            if (outputFile.exists()) outputFile.delete()
                            if (cont.isActive) {
                                cont.resume(null)
                            }
                        }
                    }
                )
            }
        } catch (e: Exception) {
            provider.unbindAll()
            null
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 4001
        private const val EXTRA_EVENT_ID = "extra_event_id"
        private const val EXTRA_EVENT_ID_STR = "extra_event_id_str"
        private const val EXTRA_TRIGGER = "extra_trigger"

        fun startCapture(
            context: Context,
            eventId: Long = -1L,
            eventIdStr: String? = null,
            trigger: String = "PROTECTED_APP_ACCESS"
        ) {
            val intent = Intent(context, EvidenceCaptureService::class.java).apply {
                putExtra(EXTRA_EVENT_ID, eventId)
                if (eventIdStr != null) putExtra(EXTRA_EVENT_ID_STR, eventIdStr)
                putExtra(EXTRA_TRIGGER, trigger)
            }
            ContextCompat.startForegroundService(context, intent)
        }
    }
}
