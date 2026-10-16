package com.numan.wcmp.data.local.database

import android.content.Context
import androidx.room.Room

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object WcmpDatabaseProvider {
    @Volatile
    private var INSTANCE: WcmpDatabase? = null

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Drop old table and recreate since we are changing primary key and schema
            // We lose sample data but it was just fake data anyway.
            db.execSQL("DROP TABLE IF EXISTS `protected_apps`")
            db.execSQL("CREATE TABLE IF NOT EXISTS `protected_apps` (`packageName` TEXT NOT NULL, `appName` TEXT NOT NULL, `isSystemApp` INTEGER NOT NULL, `category` TEXT NOT NULL, `isProtected` INTEGER NOT NULL, `versionName` TEXT, `versionCode` INTEGER, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`packageName`))")
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `vault_evidence` ADD COLUMN `latitude` REAL")
            db.execSQL("ALTER TABLE `vault_evidence` ADD COLUMN `longitude` REAL")
            db.execSQL("ALTER TABLE `vault_evidence` ADD COLUMN `accuracyMeters` REAL")
            db.execSQL("ALTER TABLE `vault_evidence` ADD COLUMN `altitude` REAL")
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `vault_evidence` ADD COLUMN `isEncrypted` INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE `vault_evidence` ADD COLUMN `encryptionVersion` INTEGER")
        }
    }

    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `security_events` ADD COLUMN `sessionId` TEXT")
            db.execSQL("ALTER TABLE `security_events` ADD COLUMN `packageName` TEXT")
            db.execSQL("ALTER TABLE `security_events` ADD COLUMN `applicationLabel` TEXT")
            db.execSQL("ALTER TABLE `security_events` ADD COLUMN `status` TEXT")
            db.execSQL("ALTER TABLE `security_events` ADD COLUMN `metadata` TEXT")
            db.execSQL("ALTER TABLE `vault_evidence` ADD COLUMN `sessionId` TEXT")
        }
    }

    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_security_events_timestamp` ON `security_events` (`timestamp`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_security_events_sessionId` ON `security_events` (`sessionId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_security_events_packageName` ON `security_events` (`packageName`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_vault_evidence_timestamp` ON `vault_evidence` (`timestamp`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_vault_evidence_sessionId` ON `vault_evidence` (`sessionId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_vault_evidence_relatedEventId` ON `vault_evidence` (`relatedEventId`)")
        }
    }

    fun getDatabase(context: Context): WcmpDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                WcmpDatabase::class.java,
                "wcmp_database"
            )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
            INSTANCE = instance
            instance
        }
    }
}
