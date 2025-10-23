# Data Model: Terminal Output Integration

**Feature**: 007 - Terminal Output Integration
**Created**: 2025-10-22

---

## Overview

This document defines all data entities, their relationships, and state transitions for the Terminal Output Integration feature. The data model supports real-time command execution, output streaming, ANSI color preservation, and session persistence.

---

## Entity Diagram

```
┌─────────────────────────────────────────────────────────┐
│                    CommandBlock                         │
│  ┌───────────────────────────────────────────────────┐  │
│  │ id: String                                        │  │
│  │ command: String                                   │  │
│  │ output: String                                    │  │
│  │ status: CommandStatus                             │  │
│  │ exitCode: Int?                                    │  │
│  │ startTime: Long                                   │  │
│  │ endTime: Long?                                    │  │
│  │ workingDirectory: String                          │  │
│  │ isExpanded: Boolean                               │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────┬───────────────────────────────────┘
                      │
                      │ 1:N
                      │
┌─────────────────────▼───────────────────────────────────┐
│                   OutputChunk                           │
│  ┌───────────────────────────────────────────────────┐  │
│  │ blockId: String                                   │  │
│  │ data: String (raw text with ANSI)                 │  │
│  │ streamType: StreamType (STDOUT/STDERR)            │  │
│  │ timestamp: Long                                   │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│                  TerminalSession                        │
│  ┌───────────────────────────────────────────────────┐  │
│  │ sessionId: String                                 │  │
│  │ currentDirectory: String                          │  │
│  │ environment: Map<String, String>                  │  │
│  │ processState: ProcessState                        │  │
│  │ creationTime: Long                                │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

---

## Core Entities

### CommandBlock

**Purpose**: Represents a single command execution with its output, status, and metadata

**Data Class**:
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
)
```

**Field Definitions**:

| Field | Type | Description | Constraints |
|-------|------|-------------|-------------|
| `id` | String | Unique identifier | UUID format, Primary key |
| `command` | String | Command text entered by user | Non-empty, max 4096 chars |
| `output` | String | Combined stdout+stderr output | Can be empty, max 10MB |
| `status` | CommandStatus | Current execution status | Enum: PENDING, EXECUTING, SUCCESS, FAILURE, CANCELED |
| `exitCode` | Int? | Exit code if completed | Nullable, 0-255 range |
| `startTime` | Long | Unix timestamp (ms) when created | Required, >0 |
| `endTime` | Long? | Unix timestamp (ms) when completed | Nullable, >= startTime |
| `workingDirectory` | String | Directory when command was executed | Absolute path |
| `isExpanded` | Boolean | UI expansion state for long output | Default: false |

**Computed Properties**:
```kotlin
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
```

**State Transitions**:
```
PENDING → EXECUTING → SUCCESS
              ↓
            FAILURE
              ↓
           CANCELED
```

Valid transitions:
- PENDING → EXECUTING (when command starts)
- EXECUTING → SUCCESS (exit code 0, prompt detected)
- EXECUTING → FAILURE (exit code ≠ 0, prompt detected)
- EXECUTING → CANCELED (user cancels or app restart)

Invalid transitions (prevented):
- SUCCESS → any other state (final)
- FAILURE → any other state (final)
- CANCELED → any other state (final)

---

### CommandStatus

**Purpose**: Enum representing command execution lifecycle states

**Definition**:
```kotlin
enum class CommandStatus {
    /** Command created but not yet sent to terminal */
    PENDING,

    /** Command sent to terminal, output streaming */
    EXECUTING,

    /** Command completed successfully (exit code 0) */
    SUCCESS,

    /** Command completed with error (exit code ≠ 0) */
    FAILURE,

    /** Command was canceled by user or system */
    CANCELED
}
```

**UI Representation**:

| Status | Icon | Color | Description |
|--------|------|-------|-------------|
| PENDING | Schedule | OnSurfaceVariant | Waiting to execute |
| EXECUTING | HourglassEmpty | Primary | Currently running |
| SUCCESS | CheckCircle | Green (0xFF4CAF50) | Completed successfully |
| FAILURE | Error | Error | Completed with errors |
| CANCELED | Cancel | OnSurfaceVariant | User or system canceled |

---

### OutputChunk

**Purpose**: Represents a fragment of terminal output as it streams

**Data Class**:
```kotlin
data class OutputChunk(
    val blockId: String,
    val data: String,
    val streamType: StreamType,
    val timestamp: Long = System.currentTimeMillis()
)
```

**Field Definitions**:

| Field | Type | Description | Constraints |
|-------|------|-------------|-------------|
| `blockId` | String | Associated command block ID | Must match existing CommandBlock.id |
| `data` | String | Raw output text with ANSI codes | Can be empty, max 4KB per chunk |
| `streamType` | StreamType | Source stream (stdout/stderr) | STDOUT or STDERR |
| `timestamp` | Long | Unix timestamp (ms) when generated | Required, >0 |

**Notes**:
- OutputChunk is ephemeral (not persisted to database)
- Used for streaming only
- Chunks are concatenated into CommandBlock.output
- ANSI codes preserved in data field

---

### StreamType

**Purpose**: Distinguishes between standard output and error streams

**Definition**:
```kotlin
enum class StreamType {
    /** Standard output stream */
    STDOUT,

    /** Standard error stream */
    STDERR
}
```

**Usage**:
- STDOUT: Normal command output (default color)
- STDERR: Error output (displayed in red text)
- Interleaved chronologically by timestamp

---

### TerminalSession

**Purpose**: Represents an active terminal emulator session

**Data Class**:
```kotlin
data class TerminalSession(
    val sessionId: String = UUID.randomUUID().toString(),
    val currentDirectory: String = "/data/data/com.convocli/files/home",
    val environment: Map<String, String> = emptyMap(),
    val processState: ProcessState = ProcessState.ACTIVE,
    val creationTime: Long = System.currentTimeMillis()
)
```

**Field Definitions**:

| Field | Type | Description | Constraints |
|-------|------|-------------|-------------|
| `sessionId` | String | Unique session identifier | UUID format |
| `currentDirectory` | String | Current working directory | Absolute path, must exist |
| `environment` | Map<String, String> | Environment variables | Key-value pairs |
| `processState` | ProcessState | Session process state | ACTIVE or INACTIVE |
| `creationTime` | Long | Unix timestamp (ms) when created | Required, >0 |

**Notes**:
- Single session for Sprint 02
- Session is transient (not persisted)
- Recreated on app restart
- Working directory tracked separately by WorkingDirectoryTracker

---

### ProcessState

**Purpose**: Represents terminal process lifecycle state

**Definition**:
```kotlin
enum class ProcessState {
    /** Process is running and accepting commands */
    ACTIVE,

    /** Process has terminated or crashed */
    INACTIVE
}
```

---

## View Models

### CommandBlockState

**Purpose**: UI state for command blocks screen

**Data Class**:
```kotlin
data class CommandBlockState(
    val blocks: List<CommandBlock> = emptyList(),
    val isExecuting: Boolean = false,
    val workingDirectory: String = "/",
    val sessionActive: Boolean = false,
    val error: String? = null
)
```

**Field Definitions**:

| Field | Type | Description |
|-------|------|-------------|
| `blocks` | List<CommandBlock> | All command blocks (historical + current) |
| `isExecuting` | Boolean | True if any block is EXECUTING |
| `workingDirectory` | String | Current working directory for new commands |
| `sessionActive` | Boolean | True if terminal session is active |
| `error` | String? | Error message for user display (null = no error) |

**Computed Properties**:
```kotlin
fun hasBlocks(): Boolean = blocks.isNotEmpty()

fun executingBlock(): CommandBlock? = blocks.firstOrNull { it.status == CommandStatus.EXECUTING }

fun recentBlocks(count: Int = 50): List<CommandBlock> = blocks.takeLast(count)
```

---

### CommandBlockIntent

**Purpose**: User actions/intents for MVI pattern

**Sealed Class**:
```kotlin
sealed class CommandBlockIntent {
    data class ExecuteCommand(val command: String) : CommandBlockIntent()
    data class CancelCommand(val blockId: String) : CommandBlockIntent()
    data class ToggleExpansion(val blockId: String) : CommandBlockIntent()
    data class CopyCommand(val blockId: String) : CommandBlockIntent()
    data class CopyOutput(val blockId: String) : CommandBlockIntent()
    data class RerunCommand(val blockId: String) : CommandBlockIntent()
    data class DeleteBlock(val blockId: String) : CommandBlockIntent()
    object ClearHistory : CommandBlockIntent()
}
```

---

### CommandBlockEffect

**Purpose**: One-time side effects for UI

**Sealed Class**:
```kotlin
sealed class CommandBlockEffect {
    data class ShowToast(val message: String) : CommandBlockEffect()
    data class ShowSnackbar(val message: String, val action: String? = null) : CommandBlockEffect()
    object ScrollToBottom : CommandBlockEffect()
    object ClearFocus : CommandBlockEffect()
}
```

---

## Repository Models

### TerminalSessionState

**Purpose**: Represents terminal session creation result

**Sealed Class**:
```kotlin
sealed class TerminalSessionState {
    data class Ready(val session: TerminalSession) : TerminalSessionState()
    data class Error(val message: String, val cause: Throwable? = null) : TerminalSessionState()
    object Inactive : TerminalSessionState()
}
```

---

### PromptDetectionResult

**Purpose**: Result of prompt detection analysis

**Data Class**:
```kotlin
data class PromptDetectionResult(
    val promptDetected: Boolean,
    val matchedPattern: String? = null,
    val timeoutTriggered: Boolean = false
)
```

**Field Definitions**:

| Field | Type | Description |
|-------|------|-------------|
| `promptDetected` | Boolean | True if prompt was detected |
| `matchedPattern` | String? | Regex pattern that matched (null if timeout) |
| `timeoutTriggered` | Boolean | True if detection via 2s timeout fallback |

---

## Database Schema

### command_blocks Table

```sql
CREATE TABLE command_blocks (
    id TEXT PRIMARY KEY NOT NULL,
    command TEXT NOT NULL,
    output TEXT NOT NULL DEFAULT '',
    status TEXT NOT NULL,
    exit_code INTEGER,
    start_time INTEGER NOT NULL,
    end_time INTEGER,
    working_directory TEXT NOT NULL,
    is_expanded INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_start_time ON command_blocks(start_time);
CREATE INDEX idx_status ON command_blocks(status);
```

**Indexes**:
- `idx_start_time`: For chronological queries (most recent first)
- `idx_status`: For filtering by status (EXECUTING, SUCCESS, etc.)

**Room DAO**:
```kotlin
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
}
```

---

## Type Converters (Room)

**CommandStatusConverter**:
```kotlin
class Converters {
    @TypeConverter
    fun fromCommandStatus(status: CommandStatus): String = status.name

    @TypeConverter
    fun toCommandStatus(value: String): CommandStatus = CommandStatus.valueOf(value)
}
```

---

## ANSI Parsing Models

### AnsiColorSpan

**Purpose**: Represents ANSI color styling for AnnotatedString

**Data Class**:
```kotlin
data class AnsiColorSpan(
    val start: Int,
    val end: Int,
    val style: SpanStyle
)
```

**Field Definitions**:

| Field | Type | Description |
|-------|------|-------------|
| `start` | Int | Start index in string |
| `end` | Int | End index in string |
| `style` | SpanStyle | Compose SpanStyle (color, fontWeight, etc.) |

**Example**:
```kotlin
// For: "\033[31mRed Text\033[0m"
// Produces:
AnsiColorSpan(
    start = 0,
    end = 8,
    style = SpanStyle(color = Color.Red)
)
```

---

### AnsiCode

**Purpose**: Represents parsed ANSI escape sequence

**Data Class**:
```kotlin
data class AnsiCode(
    val code: Int,
    val type: AnsiCodeType
)

enum class AnsiCodeType {
    FOREGROUND_COLOR,
    BACKGROUND_COLOR,
    TEXT_STYLE,
    RESET
}
```

**Supported Codes**:

| Code | Type | Effect |
|------|------|--------|
| 0 | RESET | Reset all formatting |
| 1 | TEXT_STYLE | Bold |
| 4 | TEXT_STYLE | Underline |
| 30-37 | FOREGROUND_COLOR | Standard colors |
| 40-47 | BACKGROUND_COLOR | Standard colors |
| 90-97 | FOREGROUND_COLOR | Bright colors |
| 100-107 | BACKGROUND_COLOR | Bright colors |

---

## Working Directory Model

### DirectoryInfo

**Purpose**: Represents current directory state with metadata

**Data Class**:
```kotlin
data class DirectoryInfo(
    val path: String,
    val isValid: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis()
)
```

**Field Definitions**:

| Field | Type | Description |
|-------|------|-------------|
| `path` | String | Absolute directory path |
| `isValid` | Boolean | True if directory exists and is accessible |
| `lastUpdated` | Long | Unix timestamp (ms) of last update |

---

## Validation Rules

### CommandBlock Validation

```kotlin
fun CommandBlock.validate(): ValidationResult {
    return when {
        command.isEmpty() -> ValidationResult.Error("Command cannot be empty")
        command.length > 4096 -> ValidationResult.Error("Command too long (max 4096 chars)")
        output.length > 10_000_000 -> ValidationResult.Error("Output too large (max 10MB)")
        startTime <= 0 -> ValidationResult.Error("Invalid start time")
        endTime != null && endTime < startTime -> ValidationResult.Error("End time before start time")
        exitCode != null && exitCode !in 0..255 -> ValidationResult.Error("Invalid exit code")
        else -> ValidationResult.Valid
    }
}

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}
```

---

## Migration Strategy

### Version 1 (Initial)

```kotlin
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

### Future Migrations

**Version 2**: Add `stderr_output` column for separate stderr storage
**Version 3**: Add `session_id` FK for multiple sessions
**Version 4**: Add `tags` field for command categorization

---

## Memory Management

### Output Size Limits

```kotlin
const val MAX_OUTPUT_SIZE = 10_000_000  // 10MB
const val MAX_OUTPUT_LINES = 10_000
const val CHUNK_SIZE_LIMIT = 4_096  // 4KB per chunk

fun trimOutput(output: String): String {
    val lines = output.lines()
    return if (lines.size > MAX_OUTPUT_LINES) {
        val header = lines.take(5_000).joinToString("\n")
        val footer = lines.takeLast(5_000).joinToString("\n")
        "$header\n\n[... ${lines.size - MAX_OUTPUT_LINES} lines truncated ...]\n\n$footer"
    } else {
        output.take(MAX_OUTPUT_SIZE)
    }
}
```

---

## Summary

This data model provides:
- ✅ Complete entity definitions with validation
- ✅ Room database schema for persistence
- ✅ State management for MVI pattern
- ✅ ANSI parsing models for colored output
- ✅ Working directory tracking
- ✅ Memory management constraints
- ✅ Clear state transitions and lifecycle

All entities are designed to support real-time streaming, ANSI color preservation, and session persistence as specified in the functional requirements.
