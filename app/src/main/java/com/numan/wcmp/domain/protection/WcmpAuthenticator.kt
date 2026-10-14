package com.numan.wcmp.domain.protection

interface WcmpAuthenticator {
    suspend fun authenticate(): AuthenticationResult
}
