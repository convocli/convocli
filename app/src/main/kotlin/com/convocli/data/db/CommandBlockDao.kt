package com.convocli.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.convocli.data.model.CommandBlock
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for CommandBlock entity.
 *
 * Provides queries for command block persistence and retrieval.
 * All suspend functions run on background threads automatically via Room.
 * Flow queries observe changes and update automatically.
 */
@Dao
interface CommandBlockDao {
    /**
     * Observe all command blocks in chronological order (newest first).
     *
     * Returns a Flow that automatically updates when blocks change.
     * Ideal for UI observation with LaunchedEffect.
     */
    @Query("SELECT * FROM command_blocks ORDER BY start_time DESC")
    fun observeAll(): Flow<List<CommandBlock>>

    /**
     * Get a specific command block by ID.
     *
     * @param id The command block ID
     * @return The command block, or null if not found
     */
    @Query("SELECT * FROM command_blocks WHERE id = :id")
    suspend fun getById(id: String): CommandBlock?

    /**
     * Insert a new command block.
     *
     * Uses REPLACE strategy to handle duplicate IDs.
     *
     * @param block The command block to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(block: CommandBlock)

    /**
     * Update an existing command block.
     *
     * @param block The command block with updated values
     */
    @Update
    suspend fun update(block: CommandBlock)

    /**
     * Delete a specific command block.
     *
     * @param id The command block ID to delete
     */
    @Query("DELETE FROM command_blocks WHERE id = :id")
    suspend fun deleteById(id: String)

    /**
     * Delete all command blocks (clear history).
     */
    @Query("DELETE FROM command_blocks")
    suspend fun deleteAll()

    /**
     * Get the total count of command blocks.
     *
     * @return Number of blocks in the database
     */
    @Query("SELECT COUNT(*) FROM command_blocks")
    suspend fun count(): Int

    /**
     * Get the most recent N command blocks.
     *
     * @param limit Maximum number of blocks to return
     * @return List of recent command blocks (newest first)
     */
    @Query("SELECT * FROM command_blocks ORDER BY start_time DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<CommandBlock>
}
