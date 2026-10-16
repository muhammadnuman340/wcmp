package com.numan.wcmp.core.security.evidence

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

interface EvidenceKeyManager {
    /** Returns or creates the encryption key. Use for ENCRYPTION only. */
    fun getOrCreateKey(): SecretKey

    /**
     * Returns the existing key WITHOUT creating a new one.
     * Use for DECRYPTION and health evaluation.
     * Returns null if the key does not exist in the KeyStore.
     */
    fun getExistingKey(): SecretKey?
}

class DefaultEvidenceKeyManager : EvidenceKeyManager {

    companion object {
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val ALIAS_EVIDENCE_KEY_V1 = "WCMP_Evidence_Key_V1"
    }

    private val keyStore: KeyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
        load(null)
    }

    @Synchronized
    override fun getOrCreateKey(): SecretKey {
        runCatching { keyStore.load(null) }
        if (!keyStore.containsAlias(ALIAS_EVIDENCE_KEY_V1)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                KEYSTORE_PROVIDER
            )
            
            val keyGenSpec = KeyGenParameterSpec.Builder(
                ALIAS_EVIDENCE_KEY_V1,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()

            keyGenerator.init(keyGenSpec)
            keyGenerator.generateKey()
        }

        val entry = try {
            keyStore.getEntry(ALIAS_EVIDENCE_KEY_V1, null) as? KeyStore.SecretKeyEntry
        } catch (e: Exception) {
            throw EvidenceEncryptionException("Failed to retrieve key from Android KeyStore", e)
        } ?: throw EvidenceEncryptionException("Failed to retrieve key from Android KeyStore")
            
        return entry.secretKey
    }

    @Synchronized
    override fun getExistingKey(): SecretKey? {
        runCatching { keyStore.load(null) }
        return try {
            if (!keyStore.containsAlias(ALIAS_EVIDENCE_KEY_V1)) {
                return null
            }
            val entry = keyStore.getEntry(ALIAS_EVIDENCE_KEY_V1, null) as? KeyStore.SecretKeyEntry
            entry?.secretKey
        } catch (e: Exception) {
            null
        }
    }
}
