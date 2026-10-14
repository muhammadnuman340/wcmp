package com.numan.wcmp.feature.applock

import androidx.lifecycle.ViewModel
import com.numan.wcmp.domain.protection.AppLockController
import com.numan.wcmp.domain.protection.AuthenticationResult

class AppLockViewModel(
    private val appLockController: AppLockController
) : ViewModel() {

    fun onAuthenticationResult(packageName: String, result: AuthenticationResult) {
        appLockController.onAuthenticationCompleted(packageName, result)
    }
}
