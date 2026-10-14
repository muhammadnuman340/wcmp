package com.numan.wcmp.data.local

import com.numan.wcmp.data.model.ProtectionRuleEntity
import com.numan.wcmp.data.model.SecurityEventEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interface defining local persistence operations for WCMP.
 */
interface LocalDataSource {
    fun getAllEvents(): Flow<List<SecurityEventEntity>>
    suspend fun insertEvent(event: SecurityEventEntity)
    suspend fun clearAllEvents()
    fun getProtectionRules(): Flow<List<ProtectionRuleEntity>>
    suspend fun updateRule(rule: ProtectionRuleEntity)
}
