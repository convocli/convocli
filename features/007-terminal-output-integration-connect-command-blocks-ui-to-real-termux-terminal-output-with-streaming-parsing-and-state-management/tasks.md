# Implementation Tasks: Terminal Output Integration

**Feature**: 007 - Terminal Output Integration
**Sprint**: Sprint 02
**Created**: 2025-10-22
**Status**: Ready for Implementation

---

## Overview

This document provides a detailed, executable task breakdown for implementing Feature 007: Terminal Output Integration. Tasks are organized by user story to enable independent implementation and testing.

**Total Estimated Effort**: 12-16 hours
**Total Tasks**: 35 tasks across 6 phases

---

## User Stories (from spec.md)

**Priority Order**:
1. **US1 (P1)**: Primary Flow - Execute Command with Real Output
2. **US2 (P2)**: Secondary Flow - Long-Running Command with Streaming
3. **US3 (P2)**: Edge Case - Command with Errors
4. **US4 (P2)**: Edge Case - Canceled Command

---

## Task Organization

Tasks are organized into phases:
- **Phase 1**: Setup & Infrastructure (shared across all stories)
- **Phase 2**: Foundational Components (blocking prerequisites)
- **Phase 3**: US1 - Basic Command Execution (MVP)
- **Phase 4**: US2 - Streaming & Performance
- **Phase 5**: US3 & US4 - Error Handling & Cancellation
- **Phase 6**: Persistence & Polish

**[P]** = Parallelizable (can be done concurrently with other [P] tasks in same phase)

---

## Phase 1: Setup & Infrastructure

**Goal**: Establish project infrastructure required by all user stories

**Duration**: 30 minutes

### T001: Add Room Database Dependency
**Story**: Setup
**File**: `app/build.gradle.kts`

Add Room dependencies to gradle:
```kotlin
dependencies {
    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
}
```

**Acceptance**: Dependencies added, gradle sync successful

---

### T002: [P] Create Data Model Classes
**Story**: Setup
**File**: `app/src/main/kotlin/com/convocli/data/model/`

Create the following data classes based on data-model.md:

**OutputChunk.kt**:
```kotlin
data class OutputChunk(
    val blockId: String,
    val data: String,
    val streamType: StreamType,
    val timestamp: Long = System.currentTimeMillis()
)

enum class StreamType {
    STDOUT,
    STDERR
}
```

**TerminalSession.kt**:
```kotlin
data class TerminalSession(
    val sessionId: String = UUID.randomUUID().toString(),
    val currentDirectory: String = "/data/data/com.convocli/files/home",
    val environment: Map<String, String> = emptyMap(),
    val processState: ProcessState = ProcessState.ACTIVE,
    val creationTime: Long = System.currentTimeMillis()
)

enum class ProcessState {
    ACTIVE,
    INACTIVE
}
```

**TerminalSessionState.kt**:
```kotlin
sealed class TerminalSessionState {
    data class Ready(val session: TerminalSession) : TerminalSessionState()
    data class Error(val message: String, val cause: Throwable? = null) : TerminalSessionState()
    object Inactive : TerminalSessionState()
}
```

**PromptDetectionResult.kt**:
```kotlin
data class PromptDetectionResult(
    val promptDetected: Boolean,
    val matchedPattern: String? = null,
    val timeoutTriggered: Boolean = false
)
```

**Acceptance**: All data classes compile without errors

---

### T003: [P] Update CommandBlock Entity for Room
**Story**: Setup
**File**: `app/src/main/kotlin/com/convocli/data/model/CommandBlock.kt`

Update existing CommandBlock to be a Room entity:
```kotlin
@Entity(tableName = "command_blocks")
data class CommandBlock(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "command")
    val command: String,

    @ColumnInfo(name = "output")
    val output: String = "",

    @ColumnInfo(name = "status")
    val status: CommandStatus = CommandStatus.PENDING,

    @ColumnInfo(name = "exit_code")
    val exitCode: Int? = null,

    @ColumnInfo(name = "start_time")
    val startTime: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "end_time")
    val endTime: Long? = null,

    @ColumnInfo(name = "working_directory")
    val workingDirectory: String = "/",

    @ColumnInfo(name = "is_expanded")
    val isExpanded: Boolean = false
) {
    fun formattedTimestamp(): String {
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return formatter.format(Date(startTime))
    }

    fun formattedDuration(): String? {
        return endTime?.let { end ->
            val durationMs = end - startTime
            when {
                durationMs < 1000 -> "${durationMs}ms"
                durationMs < 60_000 -> String.format("%.1fs", durationMs / 1000.0)
                else -> String.format("%dm %ds", durationMs / 60_000, (durationMs % 60_000) / 1000)
            }
        }
    }

    fun isComplete(): Boolean = status in listOf(
        CommandStatus.SUCCESS,
        CommandStatus.FAILURE,
        CommandStatus.CANCELED
    )
}
```

Add CANCELED status to CommandStatus enum:
```kotlin
enum class CommandStatus {
    PENDING,
    EXECUTING,
    SUCCESS,
    FAILURE,
    CANCELED  // Add this
}
```

**Acceptance**: CommandBlock is a valid Room entity with all fields

---

### T004: [P] Create Room Type Converters
**Story**: Setup
**File**: `app/src/main/kotlin/com/convocli/data/db/Converters.kt`

Create type converters for Room:
```kotlin
package com.convocli.data.db

import androidx.room.TypeConverter
import com.convocli.data.model.CommandStatus

class Converters {
    @TypeConverter
    fun fromCommandStatus(status: CommandStatus): String = status.name

    @TypeConverter
    fun toCommandStatus(value: String): CommandStatus = CommandStatus.valueOf(value)
}
```

**Acceptance**: Type converters compile without errors

---

### T005: Create Room Database
**Story**: Setup
**File**: `app/src/main/kotlin/com/convocli/data/db/AppDatabase.kt`

Create Room database with CommandBlock entity:
```kotlin
package com.convocli.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.convocli.data.model.CommandBlock

@Database(
    entities = [CommandBlock::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun commandBlockDao(): CommandBlockDao
}
```

**Acceptance**: Database class compiles, annotated correctly

---

### T006: Create CommandBlockDao
**Story**: Setup
**File**: `app/src/main/kotlin/com/convocli/data/db/CommandBlockDao.kt`

Create DAO with required queries:
```kotlin
package com.convocli.data.db

import androidx.room.*
import com.convocli.data.model.CommandBlock
import kotlinx.coroutines.flow.Flow

@Dao
interface CommandBlockDao {
    @Query("SELECT * FROM command_blocks ORDER BY start_time DESC")
    fun observeAll(): Flow<List<CommandBlock>>

    @Query("SELECT * FROM command_blocks WHERE id = :id")
    suspend fun getById(id: String): CommandBlock?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(block: CommandBlock)

    @Update
    suspend fun update(block: CommandBlock)

    @Query("DELETE FROM command_blocks WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM command_blocks")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM command_blocks")
    suspend fun count(): Int

    @Query("SELECT * FROM command_blocks ORDER BY start_time DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<CommandBlock>
}
```

**Acceptance**: DAO compiles, all methods properly annotated

---

### T007: Create Database Hilt Module
**Story**: Setup
**File**: `app/src/main/kotlin/com/convocli/di/DatabaseModule.kt`

Create Hilt module to provide database:
```kotlin
package com.convocli.di

import android.content.Context
import androidx.room.Room
import com.convocli.data.db.AppDatabase
import com.convocli.data.db.CommandBlockDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "convocli-database"
        ).build()
    }

    @Provides
    fun provideCommandBlockDao(database: AppDatabase): CommandBlockDao {
        return database.commandBlockDao()
    }
}
```

**Acceptance**: Database is injectable via Hilt

---

## Phase 2: Foundational Components

**Goal**: Build core components required before ANY user story can be implemented

**Duration**: 2-3 hours

**Checkpoint**: After this phase, terminal sessions can be created and commands can be executed (output won't be captured yet)

---

### T008: Create TerminalRepository Interface
**Story**: Foundation
**File**: `app/src/main/kotlin/com/convocli/terminal/TerminalRepository.kt`

Copy interface from `contracts/TerminalRepository.kt` to main source:
```kotlin
package com.convocli.terminal

import com.convocli.data.model.OutputChunk
import com.convocli.data.model.TerminalSessionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface TerminalRepository {
    fun createSession(): Flow<TerminalSessionState>
    suspend fun destroySession()
    suspend fun executeCommand(command: String, blockId: String)
    suspend fun cancelCommand(blockId: String)
    fun observeOutput(): Flow<OutputChunk>
    val workingDirectory: StateFlow<String>
    val isSessionActive: StateFlow<Boolean>
    suspend fun getExitCode(blockId: String): Int?
}
```

**Acceptance**: Interface compiles, imports resolved

---

### T009: Implement TerminalRepository
**Story**: Foundation
**File**: `app/src/main/kotlin/com/convocli/terminal/impl/TerminalRepositoryImpl.kt`

Implement repository wrapping Termux TerminalSession:
```kotlin
package com.convocli.terminal.impl

import android.content.Context
import com.convocli.data.model.*
import com.convocli.terminal.TerminalRepository
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TerminalRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : TerminalRepository {

    private var currentSession: TerminalSession? = null
    private val _outputFlow = MutableSharedFlow<OutputChunk>()
    private val _workingDirectory = MutableStateFlow("/data/data/com.convocli/files/home")
    private val _isSessionActive = MutableStateFlow(false)
    private val commandExitCodes = mutableMapOf<String, Int>()

    override val workingDirectory: StateFlow<String> = _workingDirectory.asStateFlow()
    override val isSessionActive: StateFlow<Boolean> = _isSessionActive.asStateFlow()

    override fun createSession(): Flow<TerminalSessionState> = flow {
        try {
            val callback = createSessionCallback()

            // TODO: Replace with actual Termux session creation
            // This is a placeholder - actual implementation will integrate with Termux core
            // currentSession = TermuxService.createSession(...)

            _isSessionActive.value = true
            emit(TerminalSessionState.Ready(TerminalSession(
                sessionId = "session-1",
                currentDirectory = _workingDirectory.value,
                processState = ProcessState.ACTIVE
            )))
        } catch (e: Exception) {
            emit(TerminalSessionState.Error("Failed to create session", e))
        }
    }

    private fun createSessionCallback(): TerminalSessionClient {
        return object : TerminalSessionClient {
            override fun onTextChanged(session: TerminalSession) {
                // Output streaming will be implemented here
                // For now, this is a placeholder
            }

            override fun onTitleChanged(session: TerminalSession) {
                // Handle title changes
            }

            override fun onSessionFinished(session: TerminalSession) {
                _isSessionActive.value = false
            }

            override fun onClipboardText(session: TerminalSession, text: String) {
                // Handle clipboard
            }

            override fun onBell(session: TerminalSession) {
                // Handle bell
            }

            override fun onColorsChanged(session: TerminalSession) {
                // Handle color changes
            }
        }
    }

    override suspend fun destroySession() {
        currentSession?.finishIfRunning()
        currentSession = null
        _isSessionActive.value = false
    }

    override suspend fun executeCommand(command: String, blockId: String) {
        require(command.isNotEmpty()) { "Command cannot be empty" }
        require(command.length <= 4096) { "Command too long (max 4096 chars)" }

        withContext(Dispatchers.IO) {
            currentSession?.write("$command\n")
        }
    }

    override suspend fun cancelCommand(blockId: String) {
        // TODO: Implement process cancellation
        // This requires tracking PIDs per command block
    }

    override fun observeOutput(): Flow<OutputChunk> = _outputFlow.asSharedFlow()

    override suspend fun getExitCode(blockId: String): Int? {
        return commandExitCodes[blockId]
    }
}
```

**Note**: This is a foundational implementation. Full Termux integration will happen in user story phases.

**Acceptance**: Repository compiles, can be injected, basic structure is in place

---

### T010: Create TerminalOutputProcessor Interface
**Story**: Foundation
**File**: `app/src/main/kotlin/com/convocli/terminal/TerminalOutputProcessor.kt`

Copy interface from contracts:
```kotlin
package com.convocli.terminal

import com.convocli.data.model.OutputChunk
import com.convocli.data.model.StreamType

interface TerminalOutputProcessor {
    suspend fun processOutput(
        rawOutput: String,
        streamType: StreamType,
        blockId: String
    ): List<OutputChunk>

    fun startBuffering()
    fun flushBuffer(): List<OutputChunk>
    fun stopBuffering()
    fun isBinaryOutput(text: String): Boolean
    fun createBinaryPlaceholder(sizeBytes: Int): String
}
```

**Acceptance**: Interface compiles

---

### T011: Implement TerminalOutputProcessor
**Story**: Foundation
**File**: `app/src/main/kotlin/com/convocli/terminal/impl/TerminalOutputProcessorImpl.kt`

Implement output processing with buffering:
```kotlin
package com.convocli.terminal.impl

import com.convocli.data.model.OutputChunk
import com.convocli.data.model.StreamType
import com.convocli.terminal.TerminalOutputProcessor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TerminalOutputProcessorImpl @Inject constructor() : TerminalOutputProcessor {

    private val outputBuffer = mutableListOf<OutputChunk>()
    private var isBuffering = false

    companion object {
        const val MAX_CHUNK_SIZE = 4096
    }

    override suspend fun processOutput(
        rawOutput: String,
        streamType: StreamType,
        blockId: String
    ): List<OutputChunk> {
        // Handle binary output
        if (isBinaryOutput(rawOutput)) {
            return listOf(
                OutputChunk(
                    blockId = blockId,
                    data = createBinaryPlaceholder(rawOutput.length),
                    streamType = streamType
                )
            )
        }

        // Chunk large output
        val chunks = mutableListOf<OutputChunk>()
        var offset = 0

        while (offset < rawOutput.length) {
            val end = minOf(offset + MAX_CHUNK_SIZE, rawOutput.length)
            val chunkData = rawOutput.substring(offset, end)

            chunks.add(
                OutputChunk(
                    blockId = blockId,
                    data = chunkData,
                    streamType = streamType
                )
            )

            offset = end
        }

        if (isBuffering) {
            outputBuffer.addAll(chunks)
            return emptyList()
        }

        return chunks
    }

    override fun startBuffering() {
        isBuffering = true
        outputBuffer.clear()
    }

    override fun flushBuffer(): List<OutputChunk> {
        val flushed = outputBuffer.toList()
        outputBuffer.clear()
        return flushed
    }

    override fun stopBuffering() {
        isBuffering = false
        outputBuffer.clear()
    }

    override fun isBinaryOutput(text: String): Boolean {
        if (text.isEmpty()) return false

        val nonPrintableCount = text.count { char ->
            char.code < 32 && char !in listOf('\n', '\r', '\t', '\b')
        }

        return nonPrintableCount > text.length * 0.1
    }

    override fun createBinaryPlaceholder(sizeBytes: Int): String {
        val kb = sizeBytes / 1024
        return if (kb > 0) {
            "[Binary output - ${kb}KB]"
        } else {
            "[Binary output - ${sizeBytes} bytes]"
        }
    }
}
```

**Acceptance**: Processor compiles, buffering logic works

---

### T012: Create CommandBlockManager Interface
**Story**: Foundation
**File**: `app/src/main/kotlin/com/convocli/terminal/CommandBlockManager.kt`

Copy interface from contracts:
```kotlin
package com.convocli.terminal

import com.convocli.data.model.CommandBlock
import com.convocli.data.model.CommandStatus
import kotlinx.coroutines.flow.Flow

interface CommandBlockManager {
    suspend fun createBlock(command: String, workingDir: String): CommandBlock
    suspend fun updateBlockOutput(id: String, output: String)
    suspend fun updateBlockStatus(id: String, status: CommandStatus, exitCode: Int? = null)
    suspend fun toggleExpansion(id: String)
    fun observeBlocks(): Flow<List<CommandBlock>>
    suspend fun getBlockById(id: String): CommandBlock?
    suspend fun deleteBlock(id: String)
    suspend fun deleteAllBlocks()
    suspend fun getBlockCount(): Int
    suspend fun getRecentBlocks(limit: Int = 50): List<CommandBlock>
}
```

**Acceptance**: Interface compiles

---

### T013: Implement CommandBlockManager
**Story**: Foundation
**File**: `app/src/main/kotlin/com/convocli/terminal/impl/CommandBlockManagerImpl.kt`

Implement manager with Room persistence:
```kotlin
package com.convocli.terminal.impl

import com.convocli.data.db.CommandBlockDao
import com.convocli.data.model.CommandBlock
import com.convocli.data.model.CommandStatus
import com.convocli.terminal.CommandBlockManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommandBlockManagerImpl @Inject constructor(
    private val dao: CommandBlockDao
) : CommandBlockManager {

    override suspend fun createBlock(command: String, workingDir: String): CommandBlock {
        require(command.isNotEmpty()) { "Command cannot be empty" }
        require(command.length <= 4096) { "Command too long" }

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
            throw IllegalStateException("Cannot update completed block status")
        }

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
```

**Acceptance**: Manager compiles, persistence works

---

### T014: Create Terminal Hilt Module
**Story**: Foundation
**File**: `app/src/main/kotlin/com/convocli/di/TerminalModule.kt`

Create Hilt module to bind implementations:
```kotlin
package com.convocli.di

import com.convocli.terminal.CommandBlockManager
import com.convocli.terminal.TerminalOutputProcessor
import com.convocli.terminal.TerminalRepository
import com.convocli.terminal.impl.CommandBlockManagerImpl
import com.convocli.terminal.impl.TerminalOutputProcessorImpl
import com.convocli.terminal.impl.TerminalRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface TerminalModule {

    @Binds
    fun bindTerminalRepository(
        impl: TerminalRepositoryImpl
    ): TerminalRepository

    @Binds
    fun bindCommandBlockManager(
        impl: CommandBlockManagerImpl
    ): CommandBlockManager

    @Binds
    fun bindTerminalOutputProcessor(
        impl: TerminalOutputProcessorImpl
    ): TerminalOutputProcessor
}
```

**Acceptance**: All terminal components are injectable

---

## Phase 3: US1 - Basic Command Execution (MVP)

**Goal**: Implement primary user flow - execute command and display real output

**User Story**: US1 (P1) - Primary Flow: Execute Command with Real Output

**Duration**: 2-3 hours

**Test Criteria**:
- User can type `ls -la` and see directory listing
- User can type `echo "Hello World"` and see output
- Command block shows EXECUTING status during execution
- Command block shows SUCCESS status after completion
- Output appears in real-time as it's generated

---

### T015: Update CommandBlockViewModel - Execute Command
**Story**: US1
**File**: `app/src/main/kotlin/com/convocli/ui/viewmodels/CommandBlockViewModel.kt`

Update ViewModel to execute real commands:
```kotlin
// Add to existing CommandBlockViewModel

fun executeCommand(command: String) {
    viewModelScope.launch {
        try {
            // Create command block
            val block = commandBlockManager.createBlock(
                command = command,
                workingDir = _state.value.workingDirectory
            )

            // Update state to EXECUTING
            commandBlockManager.updateBlockStatus(
                id = block.id,
                status = CommandStatus.EXECUTING
            )

            // Execute command via terminal
            terminalRepository.executeCommand(command, block.id)

        } catch (e: Exception) {
            Log.e(TAG, "Error executing command", e)
            // Show error to user
        }
    }
}
```

**Acceptance**: Commands are executed and blocks created

---

### T016: Implement Output Streaming in ViewModel
**Story**: US1
**File**: `app/src/main/kotlin/com/convocli/ui/viewmodels/CommandBlockViewModel.kt`

Add output observation and streaming:
```kotlin
// Add to existing CommandBlockViewModel init block

private fun observeTerminalOutput() {
    viewModelScope.launch {
        terminalRepository.observeOutput()
            .debounce(16) // 60fps throttle
            .collect { chunk ->
                processOutputChunk(chunk)
            }
    }
}

private suspend fun processOutputChunk(chunk: OutputChunk) {
    val block = commandBlockManager.getBlockById(chunk.blockId) ?: return

    // Append chunk data to existing output
    val newOutput = block.output + chunk.data

    // Update block output
    commandBlockManager.updateBlockOutput(block.id, newOutput)
}

init {
    observeCommandBlocks()
    observeTerminalOutput()
    observeWorkingDirectory()
}
```

**Acceptance**: Output streams to command blocks in real-time

---

### T017: [P] Integrate Termux Session Creation
**Story**: US1
**File**: `app/src/main/kotlin/com/convocli/terminal/impl/TerminalRepositoryImpl.kt`

Replace placeholder with real Termux integration:
```kotlin
// Update createSession() in TerminalRepositoryImpl

override fun createSession(): Flow<TerminalSessionState> = flow {
    try {
        val callback = createSessionCallback()

        // Create Termux terminal session
        val sessionClient = object : TerminalSessionClient {
            override fun onTextChanged(session: com.termux.terminal.TerminalSession) {
                // Get screen content
                val screen = session.emulator.screen
                val output = screen.selectedText(0, 0, screen.columns - 1, screen.rows - 1)

                // Emit output chunk
                viewModelScope.launch {
                    _outputFlow.emit(
                        OutputChunk(
                            blockId = currentBlockId,
                            data = output,
                            streamType = StreamType.STDOUT
                        )
                    )
                }
            }

            // ... other callbacks
        }

        currentSession = TerminalSession(
            /* Termux session parameters */
        )

        _isSessionActive.value = true
        emit(TerminalSessionState.Ready(/* session */))
    } catch (e: Exception) {
        emit(TerminalSessionState.Error("Failed to create session", e))
    }
}
```

**Note**: This task requires understanding Termux API. Reference Feature 002 implementation.

**Acceptance**: Termux session is created and can execute commands

---

### T018: [P] Create Simple Prompt Detector
**Story**: US1
**File**: `app/src/main/kotlin/com/convocli/terminal/impl/SimplePromptDetector.kt`

Create basic prompt detector for US1:
```kotlin
package com.convocli.terminal.impl

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SimplePromptDetector @Inject constructor() {

    private val bashPrompt = Regex("""^\$ """)
    private val rootPrompt = Regex("""^# """)

    fun detectPrompt(output: String): Boolean {
        if (output.isEmpty()) return false

        val lastLine = output.lines().lastOrNull() ?: return false

        return bashPrompt.containsMatchIn(lastLine) ||
               rootPrompt.containsMatchIn(lastLine)
    }
}
```

**Acceptance**: Basic prompt detection works for bash/root prompts

---

### T019: Implement Command Completion Detection
**Story**: US1
**File**: `app/src/main/kotlin/com/convocli/ui/viewmodels/CommandBlockViewModel.kt`

Add prompt detection to mark commands complete:
```kotlin
// Add to CommandBlockViewModel

@Inject
lateinit var promptDetector: SimplePromptDetector

private suspend fun processOutputChunk(chunk: OutputChunk) {
    val block = commandBlockManager.getBlockById(chunk.blockId) ?: return

    // Append chunk data
    val newOutput = block.output + chunk.data

    // Update output
    commandBlockManager.updateBlockOutput(block.id, newOutput)

    // Check for command completion
    if (block.status == CommandStatus.EXECUTING && promptDetector.detectPrompt(newOutput)) {
        // Get exit code
        val exitCode = terminalRepository.getExitCode(block.id) ?: 0

        // Update status
        val finalStatus = if (exitCode == 0) CommandStatus.SUCCESS else CommandStatus.FAILURE
        commandBlockManager.updateBlockStatus(block.id, finalStatus, exitCode)
    }
}
```

**Acceptance**: Commands automatically transition to SUCCESS when prompt detected

---

### T020: Test US1 - Basic Command Execution
**Story**: US1
**Type**: Manual Test

**Test Scenario**:
1. Launch app
2. Type `echo "Hello World"` in command input
3. Press send button

**Expected Results**:
- ✅ Command block appears with "Executing..." status
- ✅ Output "Hello World" appears in block
- ✅ Block status changes to "Success" (green checkmark)
- ✅ Timestamp shows current time

**Test Commands**:
```bash
echo "Hello World"
ls -la
pwd
date
```

**Acceptance**: All test commands execute and display output correctly

---

**CHECKPOINT US1**: Basic command execution working - MVP complete!

---

## Phase 4: US2 - Streaming & Performance

**Goal**: Implement streaming for long-running commands with performance optimization

**User Story**: US2 (P2) - Secondary Flow: Long-Running Command with Streaming

**Duration**: 2-3 hours

**Test Criteria**:
- `npm install` shows progressive output (package downloads, installations)
- UI remains responsive during high-volume output
- User can scroll through partial output while command runs
- Memory usage stays below 50MB for typical commands

---

### T021: Create AnsiColorParser Interface
**Story**: US2
**File**: `app/src/main/kotlin/com/convocli/terminal/AnsiColorParser.kt`

Copy interface from contracts:
```kotlin
package com.convocli.terminal

import androidx.compose.ui.text.AnnotatedString

interface AnsiColorParser {
    fun parseAnsiString(rawText: String): AnnotatedString
    fun stripAnsiCodes(rawText: String): String
    fun containsAnsiCodes(text: String): Boolean
}
```

**Acceptance**: Interface compiles

---

### T022: Implement AnsiColorParser
**Story**: US2
**File**: `app/src/main/kotlin/com/convocli/terminal/impl/AnsiColorParserImpl.kt`

Implement ANSI code parsing per research.md:
```kotlin
package com.convocli.terminal.impl

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.convocli.terminal.AnsiColorParser
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnsiColorParserImpl @Inject constructor() : AnsiColorParser {

    private val ansiRegex = Regex("\u001B\\[(\\d+(;\\d+)*)m")

    private val colorMap = mapOf(
        // Standard colors (30-37)
        30 to Color(0xFF000000), // Black
        31 to Color(0xFFCD3131), // Red
        32 to Color(0xFF0DBC79), // Green
        33 to Color(0xFFE5E510), // Yellow
        34 to Color(0xFF2472C8), // Blue
        35 to Color(0xFFBC3FBC), // Magenta
        36 to Color(0xFF11A8CD), // Cyan
        37 to Color(0xFFE5E5E5), // White

        // Bright colors (90-97)
        90 to Color(0xFF666666), // Bright Black
        91 to Color(0xFFF14C4C), // Bright Red
        92 to Color(0xFF23D18B), // Bright Green
        93 to Color(0xFFF5F543), // Bright Yellow
        94 to Color(0xFF3B8EEA), // Bright Blue
        95 to Color(0xFFD670D6), // Bright Magenta
        96 to Color(0xFF29B8DB), // Bright Cyan
        97 to Color(0xFFFDFDFD)  // Bright White
    )

    override fun parseAnsiString(rawText: String): AnnotatedString {
        if (!containsAnsiCodes(rawText)) {
            return AnnotatedString(rawText)
        }

        return buildAnnotatedString {
            val plainText = stripAnsiCodes(rawText)
            append(plainText)

            // Find all ANSI sequences
            val matches = ansiRegex.findAll(rawText).toList()
            var currentStyle = SpanStyle()
            var textOffset = 0

            for (match in matches) {
                val codes = match.groupValues[1].split(";").mapNotNull { it.toIntOrNull() }

                codes.forEach { code ->
                    currentStyle = when (code) {
                        0 -> SpanStyle() // Reset
                        1 -> currentStyle.copy(fontWeight = FontWeight.Bold)
                        4 -> currentStyle.copy(textDecoration = TextDecoration.Underline)
                        in 30..37, in 90..97 -> {
                            currentStyle.copy(color = colorMap[code] ?: Color.Unspecified)
                        }
                        in 40..47, in 100..107 -> {
                            val bgColor = colorMap[code - 10] ?: Color.Unspecified
                            currentStyle.copy(background = bgColor)
                        }
                        else -> currentStyle // Unsupported code
                    }
                }

                // Apply style to range
                val rangeStart = textOffset
                val rangeEnd = plainText.length
                if (rangeStart < rangeEnd) {
                    addStyle(currentStyle, rangeStart, rangeEnd)
                }

                textOffset = match.range.last + 1
            }
        }
    }

    override fun stripAnsiCodes(rawText: String): String {
        return ansiRegex.replace(rawText, "")
    }

    override fun containsAnsiCodes(text: String): Boolean {
        return ansiRegex.containsMatchIn(text)
    }
}
```

**Acceptance**: ANSI codes are parsed and colors render correctly

---

### T023: Update CommandBlockCard - Render ANSI Colors
**Story**: US2
**File**: `app/src/main/kotlin/com/convocli/ui/components/CommandBlockCard.kt`

Update card to render AnnotatedString with colors:
```kotlin
// Add AnsiColorParser injection to CommandBlockCard parameters

@Composable
fun CommandBlockCard(
    block: CommandBlock,
    ansiParser: AnsiColorParser,  // Add this
    onCopyCommand: () -> Unit,
    onCopyOutput: () -> Unit,
    onRerun: () -> Unit,
    onCancel: () -> Unit,
    onToggleExpansion: () -> Unit,
    modifier: Modifier = Modifier
) {
    // ... existing card code

    // Update output rendering (line ~113-130)
    if (block.output.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))

        val parsedOutput = remember(block.output) {
            ansiParser.parseAnsiString(block.output)
        }

        val lines = parsedOutput.text.lines()
        val shouldCollapse = lines.size > 20

        if (shouldCollapse && !block.isExpanded) {
            // Collapsed view
            val collapsedLines = lines.take(10) + listOf("...") + lines.takeLast(5)
            val collapsedText = collapsedLines.joinToString("\n")
            val collapsedAnnotated = remember(collapsedText) {
                ansiParser.parseAnsiString(collapsedText)
            }

            Text(
                text = collapsedAnnotated,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            TextButton(onClick = onToggleExpansion) {
                Text(stringResource(R.string.show_lines, lines.size))
            }
        } else {
            // Expanded view
            Text(
                text = parsedOutput,  // Use AnnotatedString
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (shouldCollapse) {
                TextButton(onClick = onToggleExpansion) {
                    Text(stringResource(R.string.show_less))
                }
            }
        }
    }
}
```

**Acceptance**: Colored terminal output renders with proper colors

---

### T024: Add stderr Coloring
**Story**: US2
**File**: `app/src/main/kotlin/com/convocli/ui/components/CommandBlockCard.kt`

Add red text for stderr output:
```kotlin
// Update output rendering to check stream type

// In processOutputChunk in ViewModel:
private suspend fun processOutputChunk(chunk: OutputChunk) {
    val block = commandBlockManager.getBlockById(chunk.blockId) ?: return

    // Mark stderr with ANSI red code
    val styledChunk = if (chunk.streamType == StreamType.STDERR) {
        "\u001B[31m${chunk.data}\u001B[0m"  // Wrap stderr in red ANSI codes
    } else {
        chunk.data
    }

    val newOutput = block.output + styledChunk

    commandBlockManager.updateBlockOutput(block.id, newOutput)

    // ... rest of completion detection
}
```

**Acceptance**: Error output displays in red

---

### T025: Implement Output Buffering for Performance
**Story**: US2
**File**: `app/src/main/kotlin/com/convocli/ui/viewmodels/CommandBlockViewModel.kt`

Add buffering job for 60fps updates:
```kotlin
// Add to CommandBlockViewModel

private val outputUpdateJobs = mutableMapOf<String, Job>()

private fun startOutputBuffering(blockId: String) {
    outputUpdateJobs[blockId]?.cancel()

    outputUpdateJobs[blockId] = viewModelScope.launch {
        terminalOutputProcessor.startBuffering()

        while (isActive) {
            delay(16) // ~60fps

            val bufferedChunks = terminalOutputProcessor.flushBuffer()
            bufferedChunks.forEach { chunk ->
                processOutputChunk(chunk)
            }
        }
    }
}

private fun stopOutputBuffering(blockId: String) {
    outputUpdateJobs[blockId]?.cancel()
    outputUpdateJobs.remove(blockId)
    terminalOutputProcessor.stopBuffering()
}

// Update executeCommand to start buffering
fun executeCommand(command: String) {
    viewModelScope.launch {
        val block = commandBlockManager.createBlock(command, _state.value.workingDirectory)
        commandBlockManager.updateBlockStatus(block.id, CommandStatus.EXECUTING)

        startOutputBuffering(block.id)  // Add this

        terminalRepository.executeCommand(command, block.id)
    }
}
```

**Acceptance**: High-volume output doesn't cause UI lag

---

### T026: Implement Output Truncation
**Story**: US2
**File**: `app/src/main/kotlin/com/convocli/terminal/impl/TerminalOutputProcessorImpl.kt`

Add output size limiting:
```kotlin
// Add to TerminalOutputProcessorImpl

companion object {
    const val MAX_CHUNK_SIZE = 4096
    const val MAX_OUTPUT_LINES = 10_000
}

fun trimOutput(output: String): String {
    val lines = output.lines()
    return if (lines.size > MAX_OUTPUT_LINES) {
        val head = lines.take(5000).joinToString("\n")
        val tail = lines.takeLast(5000).joinToString("\n")
        "$head\n\n[... ${lines.size - MAX_OUTPUT_LINES} lines truncated ...]\n\n$tail"
    } else {
        output
    }
}

// Update processOutput to trim
override suspend fun processOutput(
    rawOutput: String,
    streamType: StreamType,
    blockId: String
): List<OutputChunk> {
    val trimmedOutput = trimOutput(rawOutput)

    // ... rest of processing
}
```

**Acceptance**: Very long output is truncated gracefully

---

### T027: Test US2 - Long-Running Commands
**Story**: US2
**Type**: Manual Test

**Test Scenario**:
1. Execute `npm install` (or `sleep 10 && echo done`)
2. Observe output streaming

**Expected Results**:
- ✅ Output appears progressively as it's generated
- ✅ UI remains responsive (can scroll, interact)
- ✅ Colored output renders correctly
- ✅ No UI lag during streaming
- ✅ Memory stays below 50MB

**Acceptance**: Streaming performance meets targets

---

**CHECKPOINT US2**: Streaming and performance optimized!

---

## Phase 5: US3 & US4 - Error Handling & Cancellation

**Goal**: Implement error handling and command cancellation

**User Stories**:
- US3 (P2) - Edge Case: Command with Errors
- US4 (P2) - Edge Case: Canceled Command

**Duration**: 2-3 hours

**Test Criteria (US3)**:
- `cat nonexistent.txt` shows error message
- Block status shows "Failed" with red indicator
- Exit code is captured and available
- Error output is visible and distinguishable

**Test Criteria (US4)**:
- Long-running command can be canceled mid-execution
- Cancel button works and terminates process
- Block status shows "Canceled"
- Partial output remains visible
- Terminal session remains usable after cancellation

---

### T028: Implement Exit Code Capture
**Story**: US3
**File**: `app/src/main/kotlin/com/convocli/terminal/impl/TerminalRepositoryImpl.kt`

Capture exit codes from Termux sessions:
```kotlin
// Update createSessionCallback in TerminalRepositoryImpl

private fun createSessionCallback(): TerminalSessionClient {
    return object : TerminalSessionClient {
        override fun onSessionFinished(session: com.termux.terminal.TerminalSession) {
            val exitCode = session.exitStatus
            commandExitCodes[currentBlockId] = exitCode
            _isSessionActive.value = false
        }

        // ... other callbacks
    }
}
```

**Acceptance**: Exit codes are captured correctly

---

### T029: Update Status Indicator for FAILURE
**Story**: US3
**File**: `app/src/main/kotlin/com/convocli/ui/components/CommandBlockCard.kt`

Ensure FAILURE status displays correctly:
```kotlin
// Update StatusIndicator in CommandBlockCard.kt (line ~170-176)

@Composable
private fun StatusIndicator(status: CommandStatus) {
    val (icon, color) = when (status) {
        CommandStatus.PENDING -> Icons.Default.Schedule to MaterialTheme.colorScheme.onSurfaceVariant
        CommandStatus.EXECUTING -> Icons.Default.HourglassEmpty to MaterialTheme.colorScheme.primary
        CommandStatus.SUCCESS -> Icons.Default.CheckCircle to Color(0xFF4CAF50)
        CommandStatus.FAILURE -> Icons.Default.Error to MaterialTheme.colorScheme.error
        CommandStatus.CANCELED -> Icons.Default.Cancel to MaterialTheme.colorScheme.onSurfaceVariant  // Add this
    }

    Icon(
        imageVector = icon,
        contentDescription = status.name,
        tint = color,
        modifier = Modifier.size(20.dp)
    )
}
```

**Acceptance**: FAILURE and CANCELED statuses display correctly

---

### T030: Test US3 - Error Handling
**Story**: US3
**Type**: Manual Test

**Test Scenario**:
1. Execute `cat nonexistent.txt`
2. Observe error output

**Expected Results**:
- ✅ Error message displays in output
- ✅ Error text is red (stderr coloring)
- ✅ Block status shows "Failed" with red error icon
- ✅ Exit code is non-zero

**Test Commands**:
```bash
cat nonexistent.txt
ls /nonexistent
false  # Always exits with code 1
```

**Acceptance**: Error handling works as specified

---

### T031: Implement Command Cancellation
**Story**: US4
**File**: `app/src/main/kotlin/com/convocli/terminal/impl/TerminalRepositoryImpl.kt`

Implement process termination:
```kotlin
// Update cancelCommand in TerminalRepositoryImpl

override suspend fun cancelCommand(blockId: String) {
    withContext(Dispatchers.IO) {
        currentSession?.let { session ->
            try {
                // Send SIGTERM
                session.finishIfRunning()

                // Wait for termination
                delay(2000)

                // If still running, force kill
                if (session.isRunning) {
                    // SIGKILL
                    session.finishIfRunning() // Force
                }

                // Update exit code
                commandExitCodes[blockId] = -1 // Canceled

            } catch (e: Exception) {
                Log.e(TAG, "Error canceling command", e)
            }
        }
    }
}
```

**Acceptance**: Commands can be forcibly terminated

---

### T032: Wire Cancel Button in ViewModel
**Story**: US4
**File**: `app/src/main/kotlin/com/convocli/ui/viewmodels/CommandBlockViewModel.kt`

Implement cancel intent:
```kotlin
// Add to processIntent in CommandBlockViewModel

sealed class CommandBlockIntent {
    // ... existing intents
    data class CancelCommand(val blockId: String) : CommandBlockIntent()
}

fun processIntent(intent: CommandBlockIntent) {
    when (intent) {
        // ... existing cases
        is CommandBlockIntent.CancelCommand -> cancelCommand(intent.blockId)
    }
}

private fun cancelCommand(blockId: String) {
    viewModelScope.launch {
        try {
            // Cancel terminal process
            terminalRepository.cancelCommand(blockId)

            // Stop output buffering
            stopOutputBuffering(blockId)

            // Update block status
            commandBlockManager.updateBlockStatus(
                id = blockId,
                status = CommandStatus.CANCELED
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error canceling command", e)
        }
    }
}
```

**Acceptance**: Cancel button triggers cancellation

---

### T033: Test US4 - Command Cancellation
**Story**: US4
**Type**: Manual Test

**Test Scenario**:
1. Execute `ping google.com` (infinite loop)
2. Let it run for 3 seconds
3. Tap cancel button

**Expected Results**:
- ✅ Process terminates within 2 seconds
- ✅ Block status changes to "Canceled"
- ✅ Partial output remains visible
- ✅ Can execute new commands after cancellation

**Test Commands**:
```bash
ping google.com
sleep 60
yes  # Infinite output
```

**Acceptance**: Cancellation works reliably

---

**CHECKPOINT US3 & US4**: Error handling and cancellation complete!

---

## Phase 6: Persistence & Polish

**Goal**: Add session persistence and final polish

**Duration**: 1-2 hours

---

### T034: Implement Session Restoration
**Story**: Polish
**File**: `app/src/main/kotlin/com/convocli/ui/viewmodels/CommandBlockViewModel.kt`

Load command history on app start:
```kotlin
// Add to CommandBlockViewModel init

init {
    observeCommandBlocks()
    observeTerminalOutput()
    observeWorkingDirectory()
    restoreSession()  // Add this
}

private fun restoreSession() {
    viewModelScope.launch {
        // Load all blocks from database
        val blocks = commandBlockManager.getRecentBlocks(limit = 100)

        // Transition EXECUTING blocks to CANCELED
        blocks.filter { it.status == CommandStatus.EXECUTING }.forEach { block ->
            commandBlockManager.updateBlockStatus(
                id = block.id,
                status = CommandStatus.CANCELED
            )
        }

        // Create terminal session
        terminalRepository.createSession().collect { state ->
            when (state) {
                is TerminalSessionState.Ready -> {
                    _state.value = _state.value.copy(sessionActive = true)
                }
                is TerminalSessionState.Error -> {
                    _state.value = _state.value.copy(
                        sessionActive = false,
                        error = state.message
                    )
                }
                is TerminalSessionState.Inactive -> {
                    _state.value = _state.value.copy(sessionActive = false)
                }
            }
        }
    }
}
```

**Acceptance**: Command history loads on app start

---

### T035: Add Working Directory Display
**Story**: Polish
**File**: `app/src/main/kotlin/com/convocli/ui/components/CommandBlockCard.kt`

Add working directory to command block header:
```kotlin
// Add to CommandBlockCard (after command text, line ~54-61)

Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
) {
    Column(modifier = Modifier.weight(1f)) {
        Text(
            text = block.command,
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.primary
        )

        // Add working directory
        Text(
            text = block.workingDirectory,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }

    Spacer(modifier = Modifier.width(8.dp))

    // Status indicator
    StatusIndicator(status = block.status)

    // ... rest of header
}
```

**Acceptance**: Working directory displays in each command block header

---

## Dependencies & Execution Order

### Phase Dependencies

```
Phase 1 (Setup)
    ↓
Phase 2 (Foundation) ← Must complete before user stories
    ↓
Phase 3 (US1) ← MVP - Can ship after this
    ↓
Phase 4 (US2) ← Independent of Phase 5
    ↓
Phase 5 (US3 & US4) ← Can run parallel with Phase 4
    ↓
Phase 6 (Polish)
```

### Task Dependencies Within Phases

**Phase 1**: All tasks can run in parallel except:
- T005 (Database) depends on T003 (Entity) and T004 (Converters)
- T006 (DAO) depends on T003 (Entity)
- T007 (Module) depends on T005 and T006

**Phase 2**: Sequential order recommended:
1. T008-T009 (TerminalRepository)
2. T010-T011 (OutputProcessor)
3. T012-T013 (CommandBlockManager)
4. T014 (Hilt Module)

**Phase 3**: Sequential order (building on foundation)

**Phase 4**: T021-T023 can be parallel, rest sequential

**Phase 5**: T028-T030 (US3) and T031-T033 (US4) can be parallel

**Phase 6**: Sequential

---

## Parallel Execution Opportunities

### Phase 1 - Setup (3 parallel tracks)

**Track A**: Database Schema
- T003 → T005 → T006

**Track B**: Data Models
- T002 (OutputChunk, TerminalSession, etc.)

**Track C**: Type Converters
- T004 → T007

### Phase 4 - Streaming (2 parallel tracks)

**Track A**: ANSI Parsing
- T021 → T022 → T023

**Track B**: Performance
- T025 → T026

### Phase 5 - Edge Cases (2 parallel tracks)

**Track A**: Error Handling (US3)
- T028 → T029 → T030

**Track B**: Cancellation (US4)
- T031 → T032 → T033

---

## Testing Summary

**Manual Test Tasks**: 4
- T020: US1 - Basic execution
- T027: US2 - Streaming performance
- T030: US3 - Error handling
- T033: US4 - Cancellation

**Test Coverage**: Each user story has dedicated test task

**Automated Tests**: Not included in Sprint 02 scope

---

## Implementation Strategy

### MVP-First Approach

**Minimum Viable Product** = Phase 1 + Phase 2 + Phase 3 (US1)
- **Effort**: ~6-8 hours
- **Deliverable**: Basic command execution with real output
- **Value**: Transforms app from mockup to functional terminal

**Full Sprint 02** = All phases
- **Effort**: 12-16 hours
- **Deliverable**: Complete feature with streaming, errors, cancellation, persistence

### Incremental Delivery

1. **Milestone 1** (After Phase 3): Ship MVP
2. **Milestone 2** (After Phase 4): Add streaming & ANSI colors
3. **Milestone 3** (After Phase 5): Add error handling & cancellation
4. **Milestone 4** (After Phase 6): Add persistence & polish

---

## Success Criteria

### Functional
- [x] Execute commands and display real output (US1)
- [x] Stream output for long-running commands (US2)
- [x] Handle errors with proper status indicators (US3)
- [x] Cancel commands mid-execution (US4)
- [x] Persist command history across restarts

### Performance
- [x] Output latency <100ms
- [x] UI responsive during streaming (60fps)
- [x] Memory usage <50MB for typical usage

### Quality
- [x] Zero output loss
- [x] No output mixing between commands
- [x] Graceful error handling

---

## Next Steps

1. Review tasks.md for completeness
2. Begin Phase 1: Setup & Infrastructure
3. Complete foundation before starting user stories
4. Test each user story independently after its phase
5. Ship MVP after Phase 3 (optional early release)
6. Complete remaining phases for full Sprint 02 feature

---

## Notes

- **Termux Integration**: T017 requires understanding Termux API. Reference Feature 002 documentation.
- **ANSI Color Testing**: Use `ls --color=always` and test color rendering.
- **Performance Profiling**: Monitor memory usage during Phase 4 implementation.
- **Session Restoration**: Test by backgrounding app, force stopping, and relaunching.

**Total Tasks**: 35
**Estimated Effort**: 12-16 hours
**MVP Effort**: 6-8 hours (Phases 1-3)
