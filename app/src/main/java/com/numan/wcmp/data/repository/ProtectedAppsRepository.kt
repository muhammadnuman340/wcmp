package com.numan.wcmp.data.repository

import com.numan.wcmp.data.local.apps.InstalledAppDataSource
import com.numan.wcmp.data.local.database.dao.ProtectedAppDao
import com.numan.wcmp.data.mapper.toDomainModel
import com.numan.wcmp.data.mapper.toEntity
import com.numan.wcmp.feature.protectedapps.model.ProtectedApp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface ProtectedAppsRepository {
    fun observeInstalledApps(): Flow<List<ProtectedApp>>
    fun observeProtectedAppsOnly(): Flow<List<ProtectedApp>>
    suspend fun refreshInstalledApps()
    suspend fun setProtected(packageName: String, isProtected: Boolean)
    suspend fun setProtected(packageNames: List<String>, isProtected: Boolean)
}

class DefaultProtectedAppsRepository(
    private val appDataSource: InstalledAppDataSource,
    private val protectedAppDao: ProtectedAppDao
) : ProtectedAppsRepository {
    override fun observeInstalledApps(): Flow<List<ProtectedApp>> {
        return protectedAppDao.observeAllProtectedApps().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun observeProtectedAppsOnly(): Flow<List<ProtectedApp>> {
        return protectedAppDao.observeProtectedAppsOnly().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun refreshInstalledApps() {
        val installedApps = appDataSource.getInstalledApps()
        val currentSavedApps = protectedAppDao.observeAllProtectedApps()

        installedApps.forEach { app ->
            // Update DB with discovered app.
            // But we must NOT overwrite isProtected if it is true in the DB already.
            val existing = protectedAppDao.observeAppByPackageNameSync(app.packageName)
            if (existing != null) {
                protectedAppDao.insertApp(app.copy(isProtected = existing.isProtected).toEntity())
            } else {
                protectedAppDao.insertApp(app.toEntity())
            }
        }
        
        // Note: we don't aggressively delete apps that are uninstalled,
        // to maintain protection state if the user reinstalls, or we could.
        // For WCMP, keeping the protected state of missing apps is safer for now.
    }

    override suspend fun setProtected(packageName: String, isProtected: Boolean) {
        val existing = protectedAppDao.observeAppByPackageNameSync(packageName)
        if (existing != null) {
            protectedAppDao.updateApp(existing.copy(isProtected = isProtected))
        }
    }

    override suspend fun setProtected(packageNames: List<String>, isProtected: Boolean) {
        packageNames.forEach {
            setProtected(it, isProtected)
        }
    }
}
