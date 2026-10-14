package com.numan.wcmp

import android.app.Application
import com.numan.wcmp.core.background.WcmpWorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class WcmpApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Schedule foundational background maintenance
        val workManager = WcmpWorkManager(this)
        workManager.scheduleInstalledAppsSync()
        workManager.scheduleRepositoryMaintenance()
        
        // Initialize SecurityEngine. Note: it only starts monitoring if requested 
        // (which we do here for testing the engine foundation) and if the user 
        // has granted the necessary accessibility permission.
        val securityEngine = com.numan.wcmp.data.WcmpRepositoryProvider.getSecurityEngine(this)
        securityEngine.start()

        // Auto-seed initial sample evidence if vault is empty so the Evidence tab is immediately verifiable
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val repository = com.numan.wcmp.data.WcmpRepositoryProvider.getSecurityRepository(this@WcmpApplication)
                val existing = repository.observeVaultEvidence().first()
                if (existing.isEmpty()) {
                    com.numan.wcmp.core.evidence.SampleEvidenceSeeder.seed(this@WcmpApplication)
                }
            } catch (e: Exception) {
                // Non-fatal fallback
            }
        }
    }
}
