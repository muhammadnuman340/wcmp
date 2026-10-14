package com.numan.wcmp.core.security.evidence

object EvidenceEncryptionVersion {
    const val V1 = 1
}

class EvidenceEncryptionException(message: String, cause: Throwable? = null) : Exception(message, cause)
