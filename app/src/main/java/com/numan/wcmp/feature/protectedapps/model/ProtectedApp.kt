package com.numan.wcmp.feature.protectedapps.model

data class ProtectedApp(
    val packageName: String,
    val name: String,
    val isSystemApp: Boolean = false,
    val isProtected: Boolean = false,
    val category: ProtectedAppCategory = ProtectedAppCategory.OTHER,
    val versionName: String? = null,
    val versionCode: Long? = null
) {
    val id: String get() = packageName // Backward compatibility for UI
}

enum class ProtectedAppCategory {
    ALL,
    PROTECTED,
    UNPROTECTED,
    PRIVACY,
    COMMUNICATION,
    MEDIA,
    FILES,
    FINANCE,
    SOCIAL,
    OTHER
}
