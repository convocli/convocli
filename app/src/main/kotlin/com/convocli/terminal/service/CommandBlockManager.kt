package com.convocli.terminal.service

import com.convocli.data.model.CommandBlock
import kotlinx.coroutines.flow.Flow

/**
 * Manages the lifecycle of command blocks: creation, updates, and completion.
 *
 * This service bridges the terminal output stream to the command block data model,
 * orchestrating the creation, updating, and completion of command blocks as commands
 * are executed in the terminal.
 *
 * Responsibilities:
 * - Create new command blocks when user submits commands
 * - Update blocks as output streams from the terminal
 * - Detect command completion using prompt patterns
 * - Maintain chronological sequence of blocks
 * - Provide observable stream of blocks for UI
 *
 * Thread-safety: All methods must be safe to call from multiple coroutines.
 */
interface CommandBlockManager {

    /**
     * Observable stream of all command blocks in chronological order.
     *
     * Emits a new list whenever blocks are added or updated.
     * UI can collect this flow to reactively display command blocks.
     *
     * @return Flow emitting list of CommandBlock in execution order (oldest first)
     */
    fun observeBlocks(): Flow<List<CommandBlock>>

    /**
     * Creates a new command block when user submits a command.
     *
     * Block starts in PENDING status and will transition to EXECUTING
     * when the command begins execution.
     *
     * @param command The command text to execute
     * @param workingDirectory Current working directory
     * @return ID of the created block (UUID)
     */
    suspend fun createBlock(command: String, workingDirectory: String): String

    /**
     * Appends output to the currently executing block.
     *
     * Called as output streams from the terminal. Updates the block's output
     * property with new text.
     *
     * @param blockId ID of the block to update
     * @param output New output text to append
     */
    suspend fun appendOutput(blockId: String, output: String)

    /**
     * Marks a block as currently executing.
     *
     * Transitions status from PENDING → EXECUTING.
     *
     * @param blockId ID of the block
     */
    suspend fun markExecuting(blockId: String)

    /**
     * Completes a command block with final state.
     *
     * Transitions status to SUCCESS (exit code 0) or FAILURE (non-zero).
     * Sets execution duration and final exit code.
     *
     * @param blockId ID of the block
     * @param exitCode Process exit code
     * @param duration Execution time in milliseconds
     */
    suspend fun completeBlock(blockId: String, exitCode: Int, duration: Long)

    /**
     * Cancels a currently executing block.
     *
     * Transitions to FAILURE status with "Cancelled by user" indication.
     * Sets exit code to 130 (SIGINT).
     *
     * @param blockId ID of the block to cancel
     */
    suspend fun cancelBlock(blockId: String)

    /**
     * Toggles the expansion state of a block's output.
     *
     * Switches between expanded (show all output) and collapsed (show summary).
     *
     * @param blockId ID of the block
     */
    suspend fun toggleExpansion(blockId: String)

    /**
     * Gets a specific block by ID.
     *
     * @param blockId ID of the block
     * @return CommandBlock or null if not found
     */
    suspend fun getBlock(blockId: String): CommandBlock?

    /**
     * Clears all command blocks.
     *
     * Useful for testing or implementing a "clear history" feature.
     */
    suspend fun clearBlocks()
}
