package com.convocli.terminal.service

import com.convocli.data.model.CommandBlock
import com.convocli.data.model.CommandStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.delay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default implementation of CommandBlockManager.
 *
 * Maintains an in-memory list of command blocks with thread-safe access.
 * Uses Mutex for synchronization to prevent concurrent modification issues.
 *
 * Lifecycle:
 * 1. createBlock() → PENDING
 * 2. markExecuting() → EXECUTING
 * 3. appendOutput() → updates output (can be called multiple times)
 * 4. completeBlock() → SUCCESS/FAILURE
 *
 * Thread-safe: All operations protected by mutex.
 */
@Singleton
class CommandBlockManagerImpl @Inject constructor() : CommandBlockManager {

    private val mutex = Mutex()
    private val _blocks = MutableStateFlow<List<CommandBlock>>(emptyList())
    private val blockMap = mutableMapOf<String, CommandBlock>()

    // Output throttling: buffer updates and flush at 60fps
    private val outputBuffer = mutableMapOf<String, StringBuilder>()
    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        // Start output buffer flushing at ~60fps (16ms intervals)
        scope.launch {
            while (true) {
                delay(16)
                flushOutputBuffers()
            }
        }
    }

    private suspend fun flushOutputBuffers() {
        if (outputBuffer.isEmpty()) return

        mutex.withLock {
            outputBuffer.forEach { (blockId, buffer) ->
                if (buffer.isNotEmpty()) {
                    val block = blockMap[blockId] ?: return@forEach
                    val updated = block.copy(output = block.output + buffer.toString())
                    buffer.clear()
                    blockMap[blockId] = updated
                    _blocks.value = _blocks.value.map { if (it.id == blockId) updated else it }
                }
            }
        }
    }

    override fun observeBlocks(): Flow<List<CommandBlock>> {
        return _blocks.asStateFlow()
    }

    override suspend fun createBlock(command: String, workingDirectory: String): String {
        val id = UUID.randomUUID().toString()
        val block = CommandBlock(
            id = id,
            command = command,
            output = "",
            status = CommandStatus.PENDING,
            timestamp = System.currentTimeMillis(),
            executionDuration = null,
            exitCode = null,
            workingDirectory = workingDirectory,
            isExpanded = true
        )

        mutex.withLock {
            blockMap[id] = block
            _blocks.value = _blocks.value + block
        }

        return id
    }

    override suspend fun appendOutput(blockId: String, output: String) {
        // Buffer output for throttled updates at 60fps
        mutex.withLock {
            val buffer = outputBuffer.getOrPut(blockId) { StringBuilder() }
            buffer.append(output)
        }
    }

    override suspend fun markExecuting(blockId: String) {
        mutex.withLock {
            val block = blockMap[blockId] ?: return@withLock
            val updated = block.copy(status = CommandStatus.EXECUTING)
            blockMap[blockId] = updated
            _blocks.value = _blocks.value.map { if (it.id == blockId) updated else it }
        }
    }

    override suspend fun completeBlock(blockId: String, exitCode: Int, duration: Long) {
        // Flush any remaining buffered output before completing
        flushOutputBuffers()

        mutex.withLock {
            val block = blockMap[blockId] ?: return@withLock
            val status = if (exitCode == 0) CommandStatus.SUCCESS else CommandStatus.FAILURE
            val updated = block.copy(
                status = status,
                exitCode = exitCode,
                executionDuration = duration
            )
            blockMap[blockId] = updated
            _blocks.value = _blocks.value.map { if (it.id == blockId) updated else it }

            // Clean up buffer
            outputBuffer.remove(blockId)
        }
    }

    override suspend fun cancelBlock(blockId: String) {
        // Flush any remaining buffered output before cancelling
        flushOutputBuffers()

        mutex.withLock {
            val block = blockMap[blockId] ?: return@withLock
            val now = System.currentTimeMillis()
            val duration = now - block.timestamp
            val updated = block.copy(
                status = CommandStatus.FAILURE,
                exitCode = 130,  // SIGINT
                executionDuration = duration,
                output = block.output + "\n\nCancelled by user"
            )
            blockMap[blockId] = updated
            _blocks.value = _blocks.value.map { if (it.id == blockId) updated else it }

            // Clean up buffer
            outputBuffer.remove(blockId)
        }
    }

    override suspend fun toggleExpansion(blockId: String) {
        mutex.withLock {
            val block = blockMap[blockId] ?: return@withLock
            val updated = block.copy(isExpanded = !block.isExpanded)
            blockMap[blockId] = updated
            _blocks.value = _blocks.value.map { if (it.id == blockId) updated else it }
        }
    }

    override suspend fun getBlock(blockId: String): CommandBlock? {
        return mutex.withLock {
            blockMap[blockId]
        }
    }

    override suspend fun clearBlocks() {
        mutex.withLock {
            blockMap.clear()
            _blocks.value = emptyList()
        }
    }
}
