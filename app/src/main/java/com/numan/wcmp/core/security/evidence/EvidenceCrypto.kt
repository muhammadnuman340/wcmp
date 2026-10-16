package com.numan.wcmp.core.security.evidence

import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.RandomAccessFile
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.spec.GCMParameterSpec

object EvidenceCrypto {

    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val TAG_LENGTH_BIT = 128
    private const val IV_LENGTH_BYTE = 12
    const val MIN_ENCRYPTED_FILE_SIZE_BYTES = 1 + 1 + IV_LENGTH_BYTE + (TAG_LENGTH_BIT / 8) // 30 bytes

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
            out.fd.sync()
        }

        secureDelete(plaintextFile)
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

            val iv = readExactBytes(input, ivLength)

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
                    secureDelete(decryptedFile)
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

        if (encryptedFile.length() < MIN_ENCRYPTED_FILE_SIZE_BYTES) {
            throw EvidenceEncryptionException("Encrypted file is truncated (< $MIN_ENCRYPTED_FILE_SIZE_BYTES bytes)")
        }

        FileInputStream(encryptedFile).use { input ->
            val version = input.read()
            if (version != EvidenceEncryptionVersion.V1) {
                throw EvidenceEncryptionException("Unsupported encryption version: $version")
            }

            val ivLength = input.read()
            if (ivLength != IV_LENGTH_BYTE) {
                throw EvidenceEncryptionException("Invalid IV length: $ivLength")
            }

            val iv = readExactBytes(input, ivLength)

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

    fun openDecryptedInputStream(keyManager: EvidenceKeyManager, encryptedFile: File): InputStream {
        val secretKey = keyManager.getExistingKey()
            ?: throw EvidenceEncryptionException("KEY_UNAVAILABLE: Decryption key not found in KeyStore")

        val rawStream = BufferedInputStream(FileInputStream(encryptedFile), 8 * 1024)
        try {
            val version = rawStream.read()
            require(version == EvidenceEncryptionVersion.V1) {
                "Unsupported encryption header version: $version"
            }
            val ivLength = rawStream.read()
            require(ivLength == IV_LENGTH_BYTE) {
                "Invalid GCM IV length: $ivLength"
            }
            val iv = readExactBytes(rawStream, ivLength)
            val cipher = Cipher.getInstance(ALGORITHM).apply {
                init(
                    Cipher.DECRYPT_MODE,
                    secretKey,
                    GCMParameterSpec(TAG_LENGTH_BIT, iv)
                )
            }
            return CipherInputStream(rawStream, cipher)
        } catch (e: Exception) {
            runCatching { rawStream.close() }
            throw e
        }
    }

    fun secureDelete(file: File): Boolean {
        if (!file.exists()) return true
        runCatching {
            val length = file.length()
            if (length in 1..(10 * 1024 * 1024)) {
                RandomAccessFile(file, "rws").use { raf ->
                    val zeros = ByteArray(8192)
                    var remaining = length
                    while (remaining > 0) {
                        val toWrite = minOf(remaining, zeros.size.toLong()).toInt()
                        raf.write(zeros, 0, toWrite)
                        remaining -= toWrite
                    }
                }
            }
        }
        return file.delete()
    }

    private fun readExactBytes(input: InputStream, length: Int): ByteArray {
        val buffer = ByteArray(length)
        var totalRead = 0
        while (totalRead < length) {
            val read = input.read(buffer, totalRead, length - totalRead)
            if (read == -1) {
                throw EvidenceEncryptionException("Failed to read IV: expected $length bytes, got $totalRead")
            }
            totalRead += read
        }
        return buffer
    }
}
