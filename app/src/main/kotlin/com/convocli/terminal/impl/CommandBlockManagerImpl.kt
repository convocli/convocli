package com.convocli.terminal.impl

import com.convocli.data.db.CommandBlockDao
import com.convocli.data.model.CommandBlock
import com.convocli.data.model.CommandStatus
import com.convocli.terminal.CommandBlockManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of CommandBlockManager with Room persistence.
 *
 * This manager handles the full lifecycle of command blocks:
 * - Creates blocks with generated UUIDs
 * - Persists to Room database
 * - Updates output and status
 * - Provides reactive observation via Flow
 * - Enforces state transition rules
 */
@Singleton
class CommandBlockManagerImpl @Inject constructor(
    private val dao: CommandBlockDao
) : CommandBlockManager {

    override suspend fun createBlock(command: String, workingDir: String): CommandBlock {
        require(command.isNotEmpty()) { "Command cannot be empty" }
        require(command.length <= 4096) { "Command too long (max 4096 chars)" }

        val block = CommandBlock(
            command = command,
            workingDirectory = workingDir,
            status = CommandStatus.PENDING
        )

        dao.insert(block)
        return block
    }

    override suspend fun updateBlockOutput(id: String, output: String) {
        val block = dao.getById(id) ?: throw IllegalArgumentException("Block not found: $id")
        dao.update(block.copy(output = output))
    }

    override suspend fun updateBlockStatus(id: String, status: CommandStatus, exitCode: Int?) {
        val block = dao.getById(id) ?: throw IllegalArgumentException("Block not found: $id")

        // Validate state transitions
        if (block.isComplete()) {
            throw IllegalStateException("Cannot update status of completed block: $id")
        }

        // Calculate end time for final statuses
        val endTime = if (status in listOf(CommandStatus.SUCCESS, CommandStatus.FAILURE, CommandStatus.CANCELED)) {
            System.currentTimeMillis()
        } else {
            null
        }

        dao.update(
            block.copy(
                status = status,
                exitCode = exitCode,
                endTime = endTime
            )
        )
    }

    override suspend fun toggleExpansion(id: String) {
        val block = dao.getById(id) ?: throw IllegalArgumentException("Block not found: $id")
        dao.update(block.copy(isExpanded = !block.isExpanded))
    }

    override fun observeBlocks(): Flow<List<CommandBlock>> {
        return dao.observeAll()
    }

    override suspend fun getBlockById(id: String): CommandBlock? {
        return dao.getById(id)
    }

    override suspend fun deleteBlock(id: String) {
        dao.deleteById(id)
    }

    override suspend fun deleteAllBlocks() {
        dao.deleteAll()
    }

    override suspend fun getBlockCount(): Int {
        return dao.count()
    }

    override suspend fun getRecentBlocks(limit: Int): List<CommandBlock> {
        return dao.getRecent(limit)
    }
}
