package com.numan.wcmp.domain.security

data class ForegroundApp(
    val packageName: String,
    val applicationName: String? = null,
    val observedAt: Long = System.currentTimeMillis()
)
