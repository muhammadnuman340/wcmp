package com.numan.wcmp.feature.vault.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.numan.wcmp.core.security.evidence.DefaultEvidenceKeyManager
import com.numan.wcmp.data.WcmpRepositoryProvider
import com.numan.wcmp.domain.evidence.health.ControlledDecryptionService
import com.numan.wcmp.domain.evidence.health.EvidenceHealthEvaluator
import com.numan.wcmp.domain.evidence.health.DefaultEvidenceIntegrityService

class VaultViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VaultViewModel::class.java)) {
            val keyManager = DefaultEvidenceKeyManager()
            @Suppress("UNCHECKED_CAST")
            return VaultViewModel(
                securityRepository = WcmpRepositoryProvider.getSecurityRepository(context),
                integrityService = DefaultEvidenceIntegrityService(com.numan.wcmp.domain.evidence.health.AndroidEvidenceDirectoryProvider(context), keyManager, EvidenceHealthEvaluator(context.filesDir, keyManager)),
                decryptionService = ControlledDecryptionService(
                    com.numan.wcmp.domain.evidence.health.AndroidEvidenceDirectoryProvider(context), 
                    keyManager
                ),
                directoryProvider = com.numan.wcmp.domain.evidence.health.AndroidEvidenceDirectoryProvider(context)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
