package com.numan.wcmp.core.security.evidence

import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files

class EvidenceCryptoTest {

    private lateinit var tempDir: File
    private lateinit var keyManager: EvidenceKeyManager

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
        tempDir = Files.createTempDirectory("evidence_crypto_test").toFile()
        keyManager = FakeEvidenceKeyManager()
    }

    @After
    fun teardown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `encryption round trip succeeds and deletes plaintext`() {
        val plaintextFile = File(tempDir, "plain.txt")
        plaintextFile.writeText("SECRET_EVIDENCE_DATA")
        
        val encryptedFile = File(tempDir, "encrypted.enc")
        
        EvidenceCrypto.encryptFile(keyManager, plaintextFile, encryptedFile)
        
        assertTrue(encryptedFile.exists())
        assertFalse("Plaintext file should be deleted", plaintextFile.exists())
        
        val decryptedFile = File(tempDir, "decrypted.txt")
        EvidenceCrypto.decryptFile(keyManager, encryptedFile, decryptedFile)
        
        assertEquals("SECRET_EVIDENCE_DATA", decryptedFile.readText())
    }

    @Test(expected = EvidenceEncryptionException::class)
    fun `tampered ciphertext throws exception`() {
        val plaintextFile = File(tempDir, "plain.txt")
        plaintextFile.writeText("SECRET")
        val encryptedFile = File(tempDir, "encrypted.enc")
        EvidenceCrypto.encryptFile(keyManager, plaintextFile, encryptedFile)
        
        // Tamper with the ciphertext (e.g., flip a bit at the end)
        val bytes = encryptedFile.readBytes()
        bytes[bytes.size - 1] = (bytes[bytes.size - 1].toInt() xor 1).toByte()
        encryptedFile.writeBytes(bytes)
        
        val decryptedFile = File(tempDir, "decrypted.txt")
        EvidenceCrypto.decryptFile(keyManager, encryptedFile, decryptedFile)
    }
}
