package com.numan.wcmp

import android.app.Application
import com.numan.wcmp.core.background.WcmpWorkManager
import com.numan.wcmp.core.evidence.SampleEvidenceSeeder
import com.numan.wcmp.data.WcmpRepositoryProvider
import com.numan.wcmp.data.local.preferences.WcmpPreferences
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WcmpApplication : Application() {

    private val applicationScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default + CoroutineExceptionHandler { _, _ -> }
    )

    override fun onCreate() {
        super.onCreate()

        val workManager = WcmpWorkManager(this)
        workManager.scheduleInstalledAppsSync()
        workManager.scheduleRepositoryMaintenance()

        applicationScope.launch(Dispatchers.IO) {
            val prefs = WcmpPreferences(applicationContext)
            val shouldStartProtection = prefs.autoProtectionEnabled.first()
            if (shouldStartProtection) {
                WcmpRepositoryProvider.getSecurityEngine(applicationContext).start()
            }

            if (BuildConfig.DEBUG) {
                SampleEvidenceSeeder.seedIfEmpty(applicationContext)
            }
        }
    }
}
