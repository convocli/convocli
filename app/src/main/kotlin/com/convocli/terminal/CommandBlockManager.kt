package com.convocli.terminal

import com.convocli.data.model.CommandBlock
import com.convocli.data.model.CommandStatus
import kotlinx.coroutines.flow.Flow

/**
 * Contract for managing command block lifecycle and persistence.
 *
 * This manager handles:
 * - Command block creation and updates
 * - Database persistence via Room
 * - Historical command queries
 * - Expansion state management
 *
 * All operations are suspend functions for async database access.
 * Observations use Flow for reactive updates.
 */
interface CommandBlockManager {

    /**
     * Creates a new command block in PENDING status.
     *
     * The block is immediately persisted to the database and emitted
     * via observeBlocks() flow.
     *
     * @param command The command text entered by user
     * @param workingDir The current working directory
     * @return The created CommandBlock with generated ID
     *
     * @throws IllegalArgumentException if command is empty or too long
     */
    suspend fun createBlock(command: String, workingDir: String): CommandBlock

    /**
     * Updates the output of a command block.
     *
     * This is called frequently during command execution as output streams in.
     * Implementation should optimize for frequent writes (consider buffering).
     *
     * @param id The command block ID
     * @param output The new complete output (replaces existing)
     *
     * @throws IllegalArgumentException if block ID doesn't exist
     */
    suspend fun updateBlockOutput(id: String, output: String)

    /**
     * Updates the status of a command block.
     *
     * Also sets endTime when transitioning to a final status (SUCCESS, FAILURE, CANCELED).
     *
     * @param id The command block ID
     * @param status The new status
     * @param exitCode The exit code (required for SUCCESS/FAILURE, null for others)
     *
     * @throws IllegalArgumentException if block ID doesn't exist
     * @throws IllegalStateException if transition is invalid (e.g., SUCCESS → EXECUTING)
     */
    suspend fun updateBlockStatus(id: String, status: CommandStatus, exitCode: Int? = null)

    /**
     * Toggles the expansion state of a command block.
     *
     * Used for collapsing/expanding long output in the UI.
     *
     * @param id The command block ID
     *
     * @throws IllegalArgumentException if block ID doesn't exist
     */
    suspend fun toggleExpansion(id: String)

    /**
     * Observes all command blocks in chronological order (newest first).
     *
     * Emits the complete list whenever any block is created, updated, or deleted.
     * UI should collect this flow and render the list.
     *
     * @return Flow emitting list of all command blocks
     */
    fun observeBlocks(): Flow<List<CommandBlock>>

    /**
     * Retrieves a specific command block by ID.
     *
     * @param id The command block ID
     * @return The command block, or null if not found
     */
    suspend fun getBlockById(id: String): CommandBlock?

    /**
     * Deletes a command block from the database.
     *
     * @param id The command block ID
     *
     * @throws IllegalArgumentException if block ID doesn't exist
     */
    suspend fun deleteBlock(id: String)

    /**
     * Deletes all command blocks (clear history).
     *
     * This is a destructive operation that cannot be undone.
     */
    suspend fun deleteAllBlocks()

    /**
     * Returns the count of command blocks in the database.
     *
     * @return Total number of blocks
     */
    suspend fun getBlockCount(): Int

    /**
     * Retrieves the most recent N command blocks.
     *
     * Used for limiting UI display or pagination.
     *
     * @param limit Maximum number of blocks to return
     * @return List of command blocks (newest first)
     */
    suspend fun getRecentBlocks(limit: Int = 50): List<CommandBlock>
}
