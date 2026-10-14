package com.numan.wcmp.data.local.apps

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.numan.wcmp.feature.protectedapps.model.ProtectedApp
import com.numan.wcmp.feature.protectedapps.utils.ProtectedAppCategoryClassifier

interface InstalledAppDataSource {
    fun getInstalledApps(): List<ProtectedApp>
}

class DefaultInstalledAppDataSource(private val context: Context) : InstalledAppDataSource {
    
    override fun getInstalledApps(): List<ProtectedApp> {
        val pm = context.packageManager
        val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(0L))
        } else {
            pm.getInstalledPackages(0)
        }
        
        return packages.mapNotNull { packageInfo ->
            val appInfo = packageInfo.applicationInfo ?: return@mapNotNull null
            
            // Skip the WCMP app itself
            if (packageInfo.packageName == context.packageName) return@mapNotNull null
            
            val appName = pm.getApplicationLabel(appInfo).toString()
            val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            
            val category = ProtectedAppCategoryClassifier.classify(packageInfo.packageName, appName)
            
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
            
            ProtectedApp(
                packageName = packageInfo.packageName,
                name = appName,
                isSystemApp = isSystemApp,
                isProtected = false,
                category = category,
                versionName = packageInfo.versionName,
                versionCode = versionCode
            )
        }
    }
}
