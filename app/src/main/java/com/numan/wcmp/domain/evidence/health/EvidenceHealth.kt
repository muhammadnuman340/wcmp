package com.numan.wcmp.domain.evidence.health

enum class EvidenceHealth {
    HEALTHY,            // Encrypted, key available, file exists
    MISSING_FILE,       // File doesn't exist on disk
    INTEGRITY_ERROR,    // GCM authentication failed during decryption
    LEGACY,             // Plaintext file that was never encrypted
    MIGRATION_REQUIRED, // Might need format update
    KEY_UNAVAILABLE,    // Keystore lost the key
    UNKNOWN             // Unexpected state
}
