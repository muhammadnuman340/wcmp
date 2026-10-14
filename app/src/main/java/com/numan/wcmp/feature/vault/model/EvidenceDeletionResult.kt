package com.numan.wcmp.feature.vault.model

sealed interface EvidenceDeletionResult {
    data object Success : EvidenceDeletionResult
    data object FileMissing : EvidenceDeletionResult
    data class Failed(val reason: String) : EvidenceDeletionResult
}
