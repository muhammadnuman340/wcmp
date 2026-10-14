package com.numan.wcmp.data.repository

import com.numan.wcmp.data.local.apps.InstalledAppDataSource
import com.numan.wcmp.data.local.database.dao.ProtectedAppDao
import com.numan.wcmp.data.local.database.entity.ProtectedAppEntity
import com.numan.wcmp.feature.protectedapps.model.ProtectedApp
import com.numan.wcmp.feature.protectedapps.model.ProtectedAppCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class ProtectedAppsRepositoryTest {

    class FakeProtectedAppDao : ProtectedAppDao {
        val database = mutableMapOf<String, ProtectedAppEntity>()

        override fun observeAllProtectedApps(): Flow<List<ProtectedAppEntity>> = flowOf(database.values.toList())
        override fun observeProtectedAppsOnly(): Flow<List<ProtectedAppEntity>> = flowOf(database.values.filter { it.isProtected })
        override fun observeAppByPackageName(packageName: String): Flow<ProtectedAppEntity?> = flowOf(database[packageName])
        override suspend fun observeAppByPackageNameSync(packageName: String): ProtectedAppEntity? = database[packageName]
        
        override suspend fun insertApp(app: ProtectedAppEntity): Long {
            database[app.packageName] = app
            return 1L
        }
        
        override suspend fun updateApp(app: ProtectedAppEntity): Int {
            database[app.packageName] = app
            return 1
        }
        
        override suspend fun deleteApp(packageName: String): Int {
            database.remove(packageName)
            return 1
        }
        
        override suspend fun deleteAllApps(): Int {
            database.clear()
            return 1
        }
    }

    @Test
    fun `refreshing apps merges correctly and preserves existing protection`() = runBlocking {
        val dao = FakeProtectedAppDao()
        val appList = mutableListOf<ProtectedApp>()
        val dataSource = mock<InstalledAppDataSource> {
            on { getInstalledApps() } doReturn appList
        }
        val repository = DefaultProtectedAppsRepository(dataSource, dao)

        // Seed DB with an existing protected app
        dao.insertApp(ProtectedAppEntity(
            packageName = "com.whatsapp",
            appName = "WhatsApp",
            isSystemApp = false,
            category = "COMMUNICATION",
            isProtected = true,
            versionName = null,
            versionCode = null,
            createdAt = 1234,
            updatedAt = 1234
        ))

        // DataSource returns same app but natively it's unprotected by default
        appList.add(ProtectedApp(
            packageName = "com.whatsapp",
            name = "WhatsApp",
            isSystemApp = false,
            isProtected = false, // Freshly discovered apps default to false
            category = ProtectedAppCategory.COMMUNICATION
        ))
        
        // Also a brand new app
        appList.add(ProtectedApp(
            packageName = "com.newapp",
            name = "New App",
            isSystemApp = false,
            isProtected = false,
            category = ProtectedAppCategory.OTHER
        ))

        repository.refreshInstalledApps()

        val whatsapp = dao.observeAppByPackageNameSync("com.whatsapp")
        val newapp = dao.observeAppByPackageNameSync("com.newapp")

        // Assertion: Preserved protection
        assertTrue("Existing protected app should remain protected", whatsapp!!.isProtected)
        // Assertion: Defaults
        assertFalse("New app should default to unprotected", newapp!!.isProtected)
    }

    @Test
    fun `bulk protection sets status on multiple packages`() = runBlocking {
        val dao = FakeProtectedAppDao()
        val dataSource = mock<InstalledAppDataSource> {
            on { getInstalledApps() } doReturn emptyList()
        }
        val repository = DefaultProtectedAppsRepository(dataSource, dao)

        dao.insertApp(ProtectedAppEntity(
            packageName = "com.app1",
            appName = "App 1",
            isSystemApp = false,
            category = "OTHER",
            isProtected = false,
            createdAt = 0,
            updatedAt = 0
        ))
        
        dao.insertApp(ProtectedAppEntity(
            packageName = "com.app2",
            appName = "App 2",
            isSystemApp = false,
            category = "OTHER",
            isProtected = false,
            createdAt = 0,
            updatedAt = 0
        ))

        repository.setProtected(listOf("com.app1", "com.app2"), true)

        assertTrue(dao.observeAppByPackageNameSync("com.app1")!!.isProtected)
        assertTrue(dao.observeAppByPackageNameSync("com.app2")!!.isProtected)
    }
}
