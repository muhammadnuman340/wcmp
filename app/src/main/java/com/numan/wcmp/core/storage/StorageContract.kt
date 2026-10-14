package com.numan.wcmp.core.storage

import java.io.File

/**
 * Storage Contract for WCMP
 * Defines safe abstraction for encrypted evidence files and local key-value persistence.
 */
interface StorageContract {
    fun getEvidenceDirectory(): File
    fun storeSecureValue(key: String, value: String): Boolean
    fun getSecureValue(key: String, defaultValue: String? = null): String?
    fun clearSecureValue(key: String): Boolean
}
