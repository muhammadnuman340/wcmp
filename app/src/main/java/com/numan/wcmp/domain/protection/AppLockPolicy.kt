package com.numan.wcmp.domain.protection

interface AppLockPolicy {
    fun requiresAuthentication(
        packageName: String,
        isProtected: Boolean,
        isAppLockEnabled: Boolean,
        currentSession: AppLockSession?
    ): AppLockDecision
}

class DefaultAppLockPolicy : AppLockPolicy {
    override fun requiresAuthentication(
        packageName: String,
        isProtected: Boolean,
        isAppLockEnabled: Boolean,
        currentSession: AppLockSession?
    ): AppLockDecision {
        if (!isProtected) return AppLockDecision.Ignore
        if (!isAppLockEnabled) return AppLockDecision.Allow
        
        if (currentSession?.packageName == packageName) {
            return AppLockDecision.Allow
        }
        
        return AppLockDecision.RequireAuthentication
    }
}
