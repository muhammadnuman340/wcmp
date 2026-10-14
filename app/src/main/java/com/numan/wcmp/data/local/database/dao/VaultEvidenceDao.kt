package com.numan.wcmp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.numan.wcmp.data.local.database.entity.VaultEvidenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultEvidenceDao {

    @Query("SELECT * FROM vault_evidence ORDER BY timestamp DESC")
    fun observeAllEvidence(): Flow<List<VaultEvidenceEntity>>

    @Query("SELECT * FROM vault_evidence WHERE id = :id")
    fun observeEvidenceById(id: String): Flow<VaultEvidenceEntity?>

    @Query("SELECT * FROM vault_evidence WHERE sessionId = :sessionId ORDER BY timestamp DESC")
    fun observeEvidenceBySessionId(sessionId: String): Flow<List<VaultEvidenceEntity>>

    @Query("SELECT * FROM vault_evidence WHERE relatedEventId = :eventId ORDER BY timestamp DESC")
    fun observeEvidenceByRelatedEventId(eventId: String): Flow<List<VaultEvidenceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvidence(evidence: VaultEvidenceEntity): Long

    @Query("DELETE FROM vault_evidence WHERE id = :id")
    suspend fun deleteEvidence(id: String): Int
    
    @Query("DELETE FROM vault_evidence WHERE category = 'MANUAL_TEST'")
    suspend fun deleteTestEvidence(): Int

    @Query("DELETE FROM vault_evidence")
    suspend fun deleteAllEvidence(): Int
}
