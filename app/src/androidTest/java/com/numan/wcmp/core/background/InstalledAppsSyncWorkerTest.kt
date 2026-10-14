package com.numan.wcmp.core.background.workers

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.numan.wcmp.data.WcmpRepositoryProvider
import com.numan.wcmp.data.local.apps.InstalledAppDataSource
import com.numan.wcmp.data.local.database.dao.ProtectedAppDao
import com.numan.wcmp.data.local.database.entity.ProtectedAppEntity
import com.numan.wcmp.data.repository.ProtectedAppsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class InstalledAppsSyncWorkerTest {

    private lateinit var context: Context
    
    // Fake Repository to verify delegation
    class FakeProtectedAppsRepository(
        appDataSource: InstalledAppDataSource,
        protectedAppDao: ProtectedAppDao
    ) : ProtectedAppsRepository(appDataSource, protectedAppDao) {
        var refreshCalled = false
        var shouldThrow = false

        override suspend fun refreshInstalledApps() {
            if (shouldThrow) throw RuntimeException("Simulated failure")
            refreshCalled = true
        }
    }

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }
    
    @After
    fun teardown() {
        WcmpRepositoryProvider.setProtectedAppsRepositoryForTest(null)
    }

    @Test
    fun `successful sync returns Result success and delegates to repository`() = runBlocking {
        // Setup fake
        val fakeRepo = FakeProtectedAppsRepository(
            object : InstalledAppDataSource(context) {
                override fun getInstalledApps() = emptyList<com.numan.wcmp.feature.protectedapps.model.ProtectedApp>()
            },
            object : ProtectedAppDao {
                override fun observeAllProtectedApps() = flowOf(emptyList<ProtectedAppEntity>())
                override fun observeProtectedAppsOnly() = flowOf(emptyList<ProtectedAppEntity>())
                override fun observeAppByPackageName(packageName: String) = flowOf(null)
                override suspend fun observeAppByPackageNameSync(packageName: String) = null
                override suspend fun insertApp(app: ProtectedAppEntity) {}
                override suspend fun updateApp(app: ProtectedAppEntity) {}
                override suspend fun deleteApp(packageName: String) {}
                override suspend fun deleteAllApps() {}
            }
        )
        WcmpRepositoryProvider.setProtectedAppsRepositoryForTest(fakeRepo)

        // Build worker
        val worker = TestListenableWorkerBuilder<InstalledAppsSyncWorker>(context).build()

        // Execute
        val result = worker.doWork()

        // Assert
        assertEquals(true, fakeRepo.refreshCalled)
        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun `failure during sync returns Result retry`() = runBlocking {
        // Setup fake to throw exception
        val fakeRepo = FakeProtectedAppsRepository(
            object : InstalledAppDataSource(context) {
                override fun getInstalledApps() = emptyList<com.numan.wcmp.feature.protectedapps.model.ProtectedApp>()
            },
            object : ProtectedAppDao {
                override fun observeAllProtectedApps() = flowOf(emptyList<ProtectedAppEntity>())
                override fun observeProtectedAppsOnly() = flowOf(emptyList<ProtectedAppEntity>())
                override fun observeAppByPackageName(packageName: String) = flowOf(null)
                override suspend fun observeAppByPackageNameSync(packageName: String) = null
                override suspend fun insertApp(app: ProtectedAppEntity) {}
                override suspend fun updateApp(app: ProtectedAppEntity) {}
                override suspend fun deleteApp(packageName: String) {}
                override suspend fun deleteAllApps() {}
            }
        )
        fakeRepo.shouldThrow = true
        WcmpRepositoryProvider.setProtectedAppsRepositoryForTest(fakeRepo)

        val worker = TestListenableWorkerBuilder<InstalledAppsSyncWorker>(context).build()
        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.retry(), result)
    }
}
