package com.numan.wcmp.core.security.evidence

import com.numan.wcmp.domain.evidence.health.EvidenceHealth
import com.numan.wcmp.domain.evidence.health.EvidenceHealthEvaluator
import com.numan.wcmp.feature.vault.model.VaultCategory
import com.numan.wcmp.feature.vault.model.VaultEvidence
import com.numan.wcmp.feature.vault.model.VaultEvidenceType
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files

class VaultEvidenceStateTest {

    private lateinit var tempDir: File
    private lateinit var keyManager: EvidenceKeyManager
    private lateinit var evaluator: EvidenceHealthEvaluator

    class FakeEvidenceKeyManager : EvidenceKeyManager {
        private var key: javax.crypto.SecretKey? = null
        override fun getOrCreateKey(): javax.crypto.SecretKey {
            if (key == null) {
                val keyGenerator = javax.crypto.KeyGenerator.getInstance("AES")
                keyGenerator.init(256)
                key = keyGenerator.generateKey()
            }
            return key!!
        }
        override fun getExistingKey(): javax.crypto.SecretKey? = key
    }

    @Before
    fun setup() {
        tempDir = Files.createTempDirectory("evidence_state_test").toFile()
        
        keyManager = FakeEvidenceKeyManager()
        evaluator = EvidenceHealthEvaluator(tempDir, keyManager)
        
        File(tempDir, "evidence").mkdirs()
    }

    @Test
    fun `legacy evidence evaluates as LEGACY`() {
        val evidence = VaultEvidence(
            id = "test_legacy",
            title = "Old Photo",
            type = VaultEvidenceType.PHOTO,
            relatedEvent = "Event",
            timestamp = "Now",
            size = "1MB",
            category = VaultCategory.PHOTOS,
            isEncrypted = false,
            encryptionVersion = null
        )
        
        assertEquals(EvidenceHealth.LEGACY, evaluator.evaluateHealth(evidence))
    }

    @Test
    fun `valid encrypted evidence evaluates as HEALTHY`() {
        // Pre-create the key (simulates key being created during original encryption)
        keyManager.getOrCreateKey()
        
        val evidence = VaultEvidence(
            id = "test_healthy",
            title = "New Photo",
            type = VaultEvidenceType.PHOTO,
            relatedEvent = "Event",
            timestamp = "Now",
            size = "1MB",
            category = VaultCategory.PHOTOS,
            isEncrypted = true,
            encryptionVersion = 1
        )
        
        val encryptedFile = File(tempDir, "evidence/evidence_test_healthy.enc")
        FileOutputStream(encryptedFile).use { out ->
            out.write(EvidenceEncryptionVersion.V1) // version
            out.write(12) // iv length
            out.write(ByteArray(12)) // iv
            out.write("ciphertext".toByteArray()) // ciphertext
        }
        
        assertEquals(EvidenceHealth.HEALTHY, evaluator.evaluateHealth(evidence))
    }

    @Test
    fun `corrupted IV length evaluates as INTEGRITY_ERROR`() {
        // Pre-create the key (simulates key being created during original encryption)
        keyManager.getOrCreateKey()

        val evidence = VaultEvidence(
            id = "test_corrupt",
            title = "Corrupt Photo",
            type = VaultEvidenceType.PHOTO,
            relatedEvent = "Event",
            timestamp = "Now",
            size = "1MB",
            category = VaultCategory.PHOTOS,
            isEncrypted = true,
            encryptionVersion = 1
        )
        
        val encryptedFile = File(tempDir, "evidence/evidence_test_corrupt.enc")
        FileOutputStream(encryptedFile).use { out ->
            out.write(EvidenceEncryptionVersion.V1) // version
            out.write(99) // BAD iv length
            out.write(ByteArray(99)) // iv
        }
        
        assertEquals(EvidenceHealth.INTEGRITY_ERROR, evaluator.evaluateHealth(evidence))
    }
}
