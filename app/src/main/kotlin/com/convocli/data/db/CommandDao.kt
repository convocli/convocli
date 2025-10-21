package com.convocli.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.convocli.data.model.Command
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Command entity.
 *
 * Provides suspend functions for async database operations and Flow for reactive queries.
 */
@Dao
interface CommandDao {

    @Insert
    suspend fun insert(command: Command): Long

    @Update
    suspend fun update(command: Command)

    @Query("SELECT * FROM commands ORDER BY executed_at DESC")
    fun observeAll(): Flow<List<Command>>

    @Query("SELECT * FROM commands WHERE session_id = :sessionId ORDER BY executed_at DESC")
    fun observeBySession(sessionId: String): Flow<List<Command>>

    @Query("SELECT * FROM commands ORDER BY executed_at DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<Command>

    @Query("SELECT * FROM commands WHERE command_text LIKE '%' || :query || '%' ORDER BY executed_at DESC")
    suspend fun search(query: String): List<Command>

    @Query("DELETE FROM commands WHERE executed_at < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long): Int

    @Query("DELETE FROM commands")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM commands")
    suspend fun getCount(): Int
}
