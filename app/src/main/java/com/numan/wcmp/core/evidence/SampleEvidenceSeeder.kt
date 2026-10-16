package com.numan.wcmp.core.evidence

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.numan.wcmp.core.security.evidence.DefaultEvidenceKeyManager
import com.numan.wcmp.core.security.evidence.EvidenceCrypto
import com.numan.wcmp.core.security.evidence.EvidenceEncryptionVersion
import com.numan.wcmp.data.WcmpRepositoryProvider
import com.numan.wcmp.domain.evidence.health.AndroidEvidenceDirectoryProvider
import com.numan.wcmp.feature.vault.model.VaultCategory
import com.numan.wcmp.feature.vault.model.VaultEvidence
import com.numan.wcmp.feature.vault.model.VaultEvidenceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Seeds verified, cryptographically valid sample evidence (Photo, Audio, Location)
 * into encrypted storage and Room DB so the Evidence Vault is immediately populated
 * and fully verifiable.
 */
object SampleEvidenceSeeder {

    suspend fun seedIfEmpty(context: Context): Int = withContext(Dispatchers.IO) {
        val repository = WcmpRepositoryProvider.getSecurityRepository(context)
        val existing = repository.observeVaultEvidence().first()
        if (existing.isEmpty()) {
            seed(context)
        } else {
            0
        }
    }

    suspend fun seed(context: Context): Int = withContext(Dispatchers.IO) {
        val repository = WcmpRepositoryProvider.getSecurityRepository(context)
        val directoryProvider = AndroidEvidenceDirectoryProvider(context.applicationContext)
        val keyManager = DefaultEvidenceKeyManager()
        val evidenceDir = directoryProvider.getEvidenceDirectory()
        if (!evidenceDir.exists()) evidenceDir.mkdirs()

        val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        val now = System.currentTimeMillis()
        var createdCount = 0

        // 1. Seed Real Valid Photo Evidence (Encrypted JPEG)
        val photoId = UUID.randomUUID().toString()
        val plainPhotoFile = File(context.cacheDir, "sample_photo_$photoId.jpg")
        val bitmap = Bitmap.createBitmap(400, 300, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            color = Color.rgb(20, 30, 48)
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, 400f, 300f, paint)
        paint.color = Color.rgb(0, 230, 118) // Terminal Green
        paint.textSize = 22f
        paint.isAntiAlias = true
        canvas.drawText("WCMP SECURITY SURVEILLANCE", 24f, 80f, paint)
        paint.color = Color.WHITE
        paint.textSize = 16f
        canvas.drawText("Trigger: Unauthorized Access", 24f, 130f, paint)
        canvas.drawText("Timestamp: ${dateFormat.format(Date(now))}", 24f, 170f, paint)
        canvas.drawText("Status: AES-256 Encrypted", 24f, 210f, paint)

        FileOutputStream(plainPhotoFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        bitmap.recycle()

        val encPhotoFile = File(evidenceDir, "evidence_$photoId.enc")
        EvidenceCrypto.encryptFile(
            keyManager = keyManager,
            plaintextFile = plainPhotoFile,
            encryptedFile = encPhotoFile,
            version = EvidenceEncryptionVersion.V1
        )

        val photoEvidence = VaultEvidence(
            id = photoId,
            title = "Intruder Front Camera Capture",
            type = VaultEvidenceType.PHOTO,
            relatedEvent = "Unauthorized App Unlock Attempt",
            timestamp = dateFormat.format(Date(now - 300000)),
            size = "${(encPhotoFile.length() / 1024).coerceAtLeast(1)} KB",
            category = VaultCategory.PHOTOS,
            isEncrypted = true,
            encryptionVersion = EvidenceEncryptionVersion.V1
        )
        repository.insertVaultEvidence(photoEvidence)
        createdCount++

        // 2. Seed Real Valid Audio Evidence (Encrypted Audio Payload)
        val audioId = UUID.randomUUID().toString()
        val plainAudioFile = File(context.cacheDir, "sample_audio_$audioId.bin")
        plainAudioFile.writeBytes("WCMP_AUDIO_SURVEILLANCE_STREAM_${UUID.randomUUID()}".toByteArray())
        val encAudioFile = File(evidenceDir, "evidence_$audioId.enc")
        EvidenceCrypto.encryptFile(
            keyManager = keyManager,
            plaintextFile = plainAudioFile,
            encryptedFile = encAudioFile,
            version = EvidenceEncryptionVersion.V1
        )

        val audioEvidence = VaultEvidence(
            id = audioId,
            title = "Ambient Microphone Audio Clip",
            type = VaultEvidenceType.AUDIO,
            relatedEvent = "Background Tamper Sensor Alert",
            timestamp = dateFormat.format(Date(now - 120000)),
            size = "${(encAudioFile.length() / 1024).coerceAtLeast(1)} KB",
            category = VaultCategory.AUDIO,
            isEncrypted = true,
            encryptionVersion = EvidenceEncryptionVersion.V1
        )
        repository.insertVaultEvidence(audioEvidence)
        createdCount++

        // 3. Seed Real Valid Location Evidence (GPS Coordinates in Room DB)
        val locId = UUID.randomUUID().toString()
        val locEvidence = VaultEvidence(
            id = locId,
            title = "Incident Geolocation Fix",
            type = VaultEvidenceType.LOCATION,
            relatedEvent = "Perimeter Breach Trigger",
            timestamp = dateFormat.format(Date(now)),
            size = "1.2 KB",
            category = VaultCategory.OTHER,
            latitude = 33.6844,
            longitude = 73.0479,
            accuracyMeters = 3.8f,
            altitude = 512.0,
            isEncrypted = false
        )
        repository.insertVaultEvidence(locEvidence)
        createdCount++

        // Update session manager evidence count if active
        val sessionManager = WcmpRepositoryProvider.getSecuritySessionManager()
        repeat(createdCount) {
            sessionManager.recordEvidence()
        }

        createdCount
    }
}
