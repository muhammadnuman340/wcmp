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
import java.nio.file.Files

class EvidenceMissingFileTest {

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
        tempDir = Files.createTempDirectory("evidence_missing_test").toFile()
        
        keyManager = FakeEvidenceKeyManager()
        evaluator = EvidenceHealthEvaluator(tempDir, keyManager)
    }

    @Test
    fun `evaluating evidence where file is deleted returns MISSING_FILE`() {
        val evidence = VaultEvidence(
            id = "test_123",
            title = "Missing Photo",
            type = VaultEvidenceType.PHOTO,
            relatedEvent = "Event",
            timestamp = "Now",
            size = "1MB",
            category = VaultCategory.PHOTOS,
            isEncrypted = true,
            encryptionVersion = 1
        )
        
        // File does not exist in tempDir/evidence/evidence_test_123.enc
        
        val health = evaluator.evaluateHealth(evidence)
        assertEquals(EvidenceHealth.MISSING_FILE, health)
    }
}
