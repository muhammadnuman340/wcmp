package com.numan.wcmp.domain.protection

data class AppLockSession(
    val packageName: String,
    val authenticatedAt: Long = System.currentTimeMillis()
)
