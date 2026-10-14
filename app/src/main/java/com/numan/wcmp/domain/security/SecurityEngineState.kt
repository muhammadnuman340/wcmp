package com.numan.wcmp.domain.security

enum class SecurityEngineState {
    DISABLED,
    STARTING,
    RUNNING,
    WAITING_FOR_ACCESS,
    ERROR
}
