package com.numan.wcmp.domain.protection

import android.content.Context
import android.content.Intent

class DefaultAppLockPresenter(private val context: Context) : AppLockPresenter {
    override fun presentLockScreen(packageName: String, applicationName: String?) {
        // Start the WCMP Lock Activity
        // We use full class name string here to avoid circular dependency before we create the UI package, 
        // or we just define an explicit Intent action/component.
        val intent = Intent(context, Class.forName("com.numan.wcmp.feature.applock.AppLockActivity")).apply {
            putExtra("EXTRA_PACKAGE_NAME", packageName)
            if (applicationName != null) {
                putExtra("EXTRA_APPLICATION_NAME", applicationName)
            }
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        context.startActivity(intent)
    }
}
