package com.numan.wcmp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.numan.wcmp.data.local.database.entity.SecurityEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SecurityEventDao {

    @Query("SELECT * FROM security_events ORDER BY timestamp DESC")
    fun observeAllEvents(): Flow<List<SecurityEventEntity>>

    @Query("SELECT * FROM security_events ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecentEvents(limit: Int): Flow<List<SecurityEventEntity>>

    @Query("SELECT * FROM security_events WHERE id = :id")
    fun observeEventById(id: String): Flow<SecurityEventEntity?>

    @Query("SELECT * FROM security_events WHERE sessionId = :sessionId ORDER BY timestamp DESC")
    fun observeEventsBySessionId(sessionId: String): Flow<List<SecurityEventEntity>>

    @Query("SELECT * FROM security_events WHERE packageName = :packageName ORDER BY timestamp DESC")
    fun observeEventsByPackage(packageName: String): Flow<List<SecurityEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: SecurityEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<SecurityEventEntity>): List<Long>

    @Query("DELETE FROM security_events WHERE id = :id")
    suspend fun deleteEvent(id: String): Int

    @Query("DELETE FROM security_events")
    suspend fun deleteAllEvents(): Int
}
