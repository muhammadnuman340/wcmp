package com.numan.wcmp.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "protected_apps")
data class ProtectedAppEntity(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean = false,
    val category: String,
    val isProtected: Boolean,
    val versionName: String? = null,
    val versionCode: Long? = null,
    val createdAt: Long,
    val updatedAt: Long
)
