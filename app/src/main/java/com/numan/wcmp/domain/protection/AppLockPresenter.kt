package com.numan.wcmp.domain.protection

interface AppLockPresenter {
    fun presentLockScreen(packageName: String, applicationName: String?)
}
