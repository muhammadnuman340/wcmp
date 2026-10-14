package com.numan.wcmp.core.security

import kotlinx.coroutines.flow.StateFlow

/**
 * Foundation contract for system protection monitoring.
 * Strictly decoupled from platform-specific APIs to ensure architectural cleanliness and testability.
 */
interface SecurityContract {
    val protectionState: StateFlow<ProtectionState>

    suspend fun armProtection(): Result<Unit>
    suspend fun disarmProtection(): Result<Unit>
    fun isProtectionActive(): Boolean
}
