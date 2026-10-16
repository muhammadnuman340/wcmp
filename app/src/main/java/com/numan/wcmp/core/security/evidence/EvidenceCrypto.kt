package com.numan.wcmp.core.security.evidence

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec

object EvidenceCrypto {

    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val TAG_LENGTH_BIT = 128
    private const val IV_LENGTH_BYTE = 12

    fun encryptFile(
        keyManager: EvidenceKeyManager,
        plaintextFile: File,
        encryptedFile: File,
        version: Int = EvidenceEncryptionVersion.V1
    ) {
        val secretKey = keyManager.getOrCreateKey()
        
        val cipher = Cipher.getInstance(ALGORITHM)
        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        } catch (e: java.security.InvalidAlgorithmParameterException) {
            val fallbackIv = ByteArray(IV_LENGTH_BYTE)
            SecureRandom().nextBytes(fallbackIv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(TAG_LENGTH_BIT, fallbackIv))
        }
        val iv = cipher.iv ?: throw EvidenceEncryptionException("Cipher IV was not generated")
        
        FileOutputStream(encryptedFile).use { out ->
            out.write(version)
            out.write(iv.size)
            out.write(iv)
            
            FileInputStream(plaintextFile).use { input ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    val output = cipher.update(buffer, 0, bytesRead)
                    if (output != null) out.write(output)
                }
            }
            val finalOutput = cipher.doFinal()
            if (finalOutput != null) out.write(finalOutput)
        }
        
        // Ensure plaintext file is deleted securely
        if (plaintextFile.exists()) {
            plaintextFile.delete()
        }
    }

    fun decryptFile(
        keyManager: EvidenceKeyManager,
        encryptedFile: File,
        decryptedFile: File
    ) {
        val secretKey = keyManager.getExistingKey()
            ?: throw EvidenceEncryptionException("KEY_UNAVAILABLE: Decryption key not found in KeyStore")
        
        FileInputStream(encryptedFile).use { input ->
            val version = input.read()
            if (version != EvidenceEncryptionVersion.V1) {
                throw EvidenceEncryptionException("Unsupported encryption version: $version")
            }
            
            val ivLength = input.read()
            if (ivLength != IV_LENGTH_BYTE) {
                throw EvidenceEncryptionException("Invalid IV length: $ivLength")
            }
            
            val iv = ByteArray(ivLength)
            var readLen = input.read(iv)
            if (readLen != IV_LENGTH_BYTE) throw EvidenceEncryptionException("Failed to read IV")
            
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(TAG_LENGTH_BIT, iv))
            
            FileOutputStream(decryptedFile).use { out ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    val output = cipher.update(buffer, 0, bytesRead)
                    if (output != null) out.write(output)
                }
                try {
                    val finalOutput = cipher.doFinal()
                    if (finalOutput != null) out.write(finalOutput)
                } catch (e: Exception) {
                    decryptedFile.delete()
                    throw EvidenceEncryptionException("Integrity check failed or incorrect key", e)
                }
            }
        }
    }

    fun validateIntegrity(
        keyManager: EvidenceKeyManager,
        encryptedFile: File
    ) {
        val secretKey = keyManager.getExistingKey()
            ?: throw EvidenceEncryptionException("KEY_UNAVAILABLE: Decryption key not found in KeyStore")
            
        FileInputStream(encryptedFile).use { input ->
            val version = input.read()
            if (version != EvidenceEncryptionVersion.V1) {
                throw EvidenceEncryptionException("Unsupported encryption version: $version")
            }
            
            val ivLength = input.read()
            if (ivLength != IV_LENGTH_BYTE) {
                throw EvidenceEncryptionException("Invalid IV length: $ivLength")
            }
            
            val iv = ByteArray(ivLength)
            val readLen = input.read(iv)
            if (readLen != IV_LENGTH_BYTE) throw EvidenceEncryptionException("Failed to read IV")
            
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(TAG_LENGTH_BIT, iv))
            
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                cipher.update(buffer, 0, bytesRead)
            }
            try {
                cipher.doFinal()
            } catch (e: Exception) {
                throw EvidenceEncryptionException("Integrity check failed or incorrect key", e)
            }
        }
    }

    fun openDecryptedInputStream(keyManager: EvidenceKeyManager, encryptedFile: File): java.io.InputStream {
        val rawStream = java.io.BufferedInputStream(FileInputStream(encryptedFile), 8 * 1024)
        val version = rawStream.read()
        require(version == EvidenceEncryptionVersion.V1) {
            "Unsupported encryption header version: $version"
        }
        val ivLength = rawStream.read()
        require(ivLength == IV_LENGTH_BYTE) {
            "Invalid GCM IV length: $ivLength"
        }
        val iv = ByteArray(ivLength)
        var bytesRead = 0
        while (bytesRead < ivLength) {
            val read = rawStream.read(iv, bytesRead, ivLength - bytesRead)
            if (read == -1) throw java.security.GeneralSecurityException("Truncated IV header in encrypted evidence file")
            bytesRead += read
        }
        val secretKey = keyManager.getExistingKey() ?: keyManager.getOrCreateKey()
        val cipher = Cipher.getInstance(ALGORITHM).apply {
            init(
                Cipher.DECRYPT_MODE,
                secretKey,
                GCMParameterSpec(TAG_LENGTH_BIT, iv)
            )
        }
        return javax.crypto.CipherInputStream(rawStream, cipher)
    }
}
