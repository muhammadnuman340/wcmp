package com.numan.wcmp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.numan.wcmp.data.local.database.entity.ProtectedAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProtectedAppDao {

    @Query("SELECT * FROM protected_apps ORDER BY appName ASC")
    fun observeAllProtectedApps(): Flow<List<ProtectedAppEntity>>

    @Query("SELECT * FROM protected_apps WHERE isProtected = 1 ORDER BY appName ASC")
    fun observeProtectedAppsOnly(): Flow<List<ProtectedAppEntity>>

    @Query("SELECT * FROM protected_apps WHERE packageName = :packageName LIMIT 1")
    fun observeAppByPackageName(packageName: String): Flow<ProtectedAppEntity?>
    
    @Query("SELECT * FROM protected_apps WHERE packageName = :packageName LIMIT 1")
    suspend fun observeAppByPackageNameSync(packageName: String): ProtectedAppEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: ProtectedAppEntity): Long

    @Update
    suspend fun updateApp(app: ProtectedAppEntity): Int

    @Query("DELETE FROM protected_apps WHERE packageName = :packageName")
    suspend fun deleteApp(packageName: String): Int

    @Query("DELETE FROM protected_apps")
    suspend fun deleteAllApps(): Int
}
