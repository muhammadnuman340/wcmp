package com.numan.wcmp.domain.protection

sealed interface AppLockDecision {
    data object Allow : AppLockDecision
    data object RequireAuthentication : AppLockDecision
    data object Ignore : AppLockDecision
}
