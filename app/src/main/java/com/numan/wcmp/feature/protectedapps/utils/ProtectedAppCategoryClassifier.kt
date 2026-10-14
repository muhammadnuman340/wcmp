package com.numan.wcmp.feature.protectedapps.utils

import com.numan.wcmp.feature.protectedapps.model.ProtectedAppCategory

object ProtectedAppCategoryClassifier {
    
    fun classify(packageName: String, appName: String): ProtectedAppCategory {
        val lowerPackage = packageName.lowercase()
        val lowerName = appName.lowercase()
        
        if (containsAny(lowerPackage, lowerName, "whatsapp", "telegram", "messenger", "sms", "message", "chat", "viber", "signal", "discord", "skype")) {
            return ProtectedAppCategory.COMMUNICATION
        }
        if (containsAny(lowerPackage, lowerName, "gallery", "photo", "video", "music", "spotify", "youtube", "media", "netflix", "camera")) {
            return ProtectedAppCategory.MEDIA
        }
        if (containsAny(lowerPackage, lowerName, "file", "folder", "drive", "explorer", "storage")) {
            return ProtectedAppCategory.FILES
        }
        if (containsAny(lowerPackage, lowerName, "bank", "pay", "finance", "wallet", "cash", "paypal", "venmo", "crypto", "trading")) {
            return ProtectedAppCategory.FINANCE
        }
        if (containsAny(lowerPackage, lowerName, "facebook", "twitter", "instagram", "tiktok", "social", "linkedin", "snapchat", "reddit")) {
            return ProtectedAppCategory.SOCIAL
        }
        if (containsAny(lowerPackage, lowerName, "vault", "secure", "lock", "privacy", "password", "vpn", "authenticator", "shield")) {
            return ProtectedAppCategory.PRIVACY
        }
        
        return ProtectedAppCategory.OTHER
    }
    
    private fun containsAny(pkg: String, name: String, vararg keywords: String): Boolean {
        return keywords.any { pkg.contains(it) || name.contains(it) }
    }
}
