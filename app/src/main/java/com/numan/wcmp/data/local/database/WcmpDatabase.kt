package com.numan.wcmp.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.numan.wcmp.data.local.database.dao.ProtectedAppDao
import com.numan.wcmp.data.local.database.dao.SecurityEventDao
import com.numan.wcmp.data.local.database.dao.VaultEvidenceDao
import com.numan.wcmp.data.local.database.entity.ProtectedAppEntity
import com.numan.wcmp.data.local.database.entity.SecurityEventEntity
import com.numan.wcmp.data.local.database.entity.VaultEvidenceEntity

@Database(
    entities = [
        SecurityEventEntity::class,
        VaultEvidenceEntity::class,
        ProtectedAppEntity::class
    ],
    version = 6,
    exportSchema = true
)
abstract class WcmpDatabase : RoomDatabase() {
    abstract fun securityEventDao(): SecurityEventDao
    abstract fun vaultEvidenceDao(): VaultEvidenceDao
    abstract fun protectedAppDao(): ProtectedAppDao
}
