package com.numan.wcmp

import android.app.Application
import android.util.Log
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
        SupervisorJob() + Dispatchers.Default +
        CoroutineExceptionHandler { _, throwable ->
            if (BuildConfig.DEBUG) {
                Log.e("WcmpApplication", "Unhandled background initialization error", throwable)
            }
        }
    )

    override fun onCreate() {
        super.onCreate()
        
        // Schedule foundational background maintenance
        val workManager = WcmpWorkManager(this)
        workManager.scheduleInstalledAppsSync()
        workManager.scheduleRepositoryMaintenance()
        
        // Initialize SecurityEngine only if the user has auto-protection enabled.
        applicationScope.launch(Dispatchers.IO) {
            try {
                val prefs = WcmpPreferences(this@WcmpApplication)
                if (prefs.autoProtectionEnabled.first()) {
                    WcmpRepositoryProvider.getSecurityEngine(this@WcmpApplication).start()
                }
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) {
                    Log.e("WcmpApplication", "Failed to initialize SecurityEngine state", e)
                }
            }
        }

        // Auto-seed initial sample evidence if vault is empty (DEBUG mode only)
        if (BuildConfig.DEBUG) {
            applicationScope.launch(Dispatchers.IO) {
                try {
                    SampleEvidenceSeeder.seedIfEmpty(this@WcmpApplication)
                } catch (e: Exception) {
                    if (BuildConfig.DEBUG) {
                        Log.e("WcmpApplication", "Failed to seed sample evidence", e)
                    }
                }
            }
        }
    }
}
