package com.numan.wcmp.domain.protection

import android.content.Context
import android.content.Intent
import com.numan.wcmp.feature.applock.AppLockActivity

class DefaultAppLockPresenter(private val context: Context) : AppLockPresenter {
    override fun presentLockScreen(packageName: String, applicationName: String?) {
        val intent = Intent(context, AppLockActivity::class.java).apply {
            putExtra("EXTRA_PACKAGE_NAME", packageName)
            if (applicationName != null) {
                putExtra("EXTRA_APPLICATION_NAME", applicationName)
            }
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                Intent.FLAG_ACTIVITY_NO_ANIMATION
        }
        context.startActivity(intent)
    }
}
