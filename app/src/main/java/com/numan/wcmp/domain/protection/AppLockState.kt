package com.numan.wcmp.domain.protection

enum class AppLockState {
    DISABLED,
    WAITING_FOR_PROTECTED_APP,
    LOCK_REQUIRED,
    AUTHENTICATING,
    UNLOCKED,
    DENIED,
    ERROR
}
