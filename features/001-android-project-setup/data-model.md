# Data Model: Android Project Foundation Setup

> **Feature**: 001-android-project-setup
> **Created**: 2025-10-20
> **Status**: Design Complete

---

## Overview

This document defines the data models for the ConvoCLI Android project foundation. While this feature focuses on infrastructure setup, we establish the initial database schema for command history tracking, which is a core requirement for the terminal application.

---

## Entity Definitions

### 1. Command (Room Entity)

**Purpose**: Stores executed terminal commands for history and replay functionality.

**Schema**:
```kotlin
@Entity(tableName = "commands")
data class Command(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "command_text")
    val commandText: String,

    @ColumnInfo(name = "output")
    val output: String? = null,

    @ColumnInfo(name = "exit_code")
    val exitCode: Int? = null,

    @ColumnInfo(name = "executed_at")
    val executedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "working_directory")
    val workingDirectory: String,

    @ColumnInfo(name = "session_id")
    val sessionId: String? = null
)
```

**Fields**:

| Field | Type | Nullable | Description |
|-------|------|----------|-------------|
| `id` | Long | No | Auto-generated primary key |
| `commandText` | String | No | The actual command executed (e.g., "ls -la") |
| `output` | String | Yes | Command output (null while executing) |
| `exitCode` | Int | Yes | Exit code (0 = success, non-zero = error) |
| `executedAt` | Long | No | Unix timestamp (milliseconds) of execution |
| `workingDirectory` | String | No | Directory where command was executed |
| `sessionId` | String | Yes | Terminal session identifier (for multi-session support) |

**Indexes**:
```kotlin
@Entity(
    tableName = "commands",
    indices = [
        Index(value = ["executed_at"], name = "idx_executed_at"),
        Index(value = ["session_id"], name = "idx_session_id")
    ]
)
```

**Validation Rules**:
- `commandText` must not be blank
- `executedAt` must be positive
- `workingDirectory` must be absolute path

**State Transitions**:
1. **Created**: Command inserted with `output = null`, `exitCode = null`
2. **Executing**: Command is being processed
3. **Completed**: `output` and `exitCode` updated
4. **Failed**: `exitCode != 0`

---

### 2. CommandDao (Room DAO)

**Purpose**: Data access interface for Command entities.

**Operations**:

```kotlin
@Dao
interface CommandDao {

    /**
     * Insert a new command into history.
     */
    @Insert
    suspend fun insert(command: Command): Long

    /**
     * Update command output and exit code after execution.
     */
    @Update
    suspend fun update(command: Command)

    /**
     * Get all commands ordered by most recent first.
     */
    @Query("SELECT * FROM commands ORDER BY executed_at DESC")
    fun observeAll(): Flow<List<Command>>

    /**
     * Get commands for a specific session.
     */
    @Query("SELECT * FROM commands WHERE session_id = :sessionId ORDER BY executed_at DESC")
    fun observeBySession(sessionId: String): Flow<List<Command>>

    /**
     * Get most recent N commands.
     */
    @Query("SELECT * FROM commands ORDER BY executed_at DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<Command>

    /**
     * Search commands by text.
     */
    @Query("SELECT * FROM commands WHERE command_text LIKE '%' || :query || '%' ORDER BY executed_at DESC")
    suspend fun search(query: String): List<Command>

    /**
     * Delete commands older than timestamp (for cleanup).
     */
    @Query("DELETE FROM commands WHERE executed_at < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long): Int

    /**
     * Delete all commands (clear history).
     */
    @Query("DELETE FROM commands")
    suspend fun deleteAll()

    /**
     * Get command count.
     */
    @Query("SELECT COUNT(*) FROM commands")
    suspend fun getCount(): Int
}
```

---

### 3. AppDatabase (Room Database)

**Purpose**: Application database container.

**Definition**:
```kotlin
@Database(
    entities = [Command::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun commandDao(): CommandDao
}
```

**Configuration**:
- **Database Name**: `convocli.db`
- **Version**: 1 (initial version)
- **Export Schema**: false (for MVP; enable for production)

**Future Migrations**:
- Version 2: Add `duration_ms` field to Command
- Version 3: Add `tags` table for command categorization
- Version 4: Add `favorites` support

---

## UI State Models (Not Persisted)

### CommandBlock

**Purpose**: Represents a command block in the UI (Warp-style blocks).

```kotlin
data class CommandBlock(
    val id: String = UUID.randomUUID().toString(),
    val command: String,
    val output: String = "",
    val status: CommandStatus,
    val timestamp: Long = System.currentTimeMillis(),
    val exitCode: Int? = null
)

enum class CommandStatus {
    PENDING,    // Not yet executed
    EXECUTING,  // Currently running
    COMPLETED,  // Finished successfully (exitCode 0)
    FAILED      // Finished with error (exitCode != 0)
}
```

**Mapping to Command Entity**:
- `CommandBlock` is ephemeral UI state
- Converted to `Command` entity for persistence
- Repository layer handles conversion

---

## ViewModel State Models

### TerminalState

**Purpose**: Represents the entire terminal screen state.

```kotlin
data class TerminalState(
    val blocks: List<CommandBlock> = emptyList(),
    val currentInput: String = "",
    val isExecuting: Boolean = false,
    val error: String? = null,
    val workingDirectory: String = "/data/data/com.convocli/files/home"
)
```

**State Transitions**:
```
Idle → Executing (when command starts)
Executing → Idle (when command completes)
Idle → Error (on failure)
Error → Idle (on user dismiss)
```

---

## Settings (DataStore)

**Purpose**: User preferences and app settings.

**Schema** (Proto DataStore):
```proto
syntax = "proto3";

message Settings {
  bool dark_mode_enabled = 1;
  string default_shell = 2;
  int32 font_size = 3;
  int32 max_history_size = 4;
  bool enable_command_autocomplete = 5;
}
```

**Or Preferences DataStore** (simpler for MVP):
```kotlin
object SettingsKeys {
    val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
    val DEFAULT_SHELL = stringPreferencesKey("default_shell")
    val FONT_SIZE = intPreferencesKey("font_size")
    val MAX_HISTORY_SIZE = intPreferencesKey("max_history_size")
}
```

**Default Values**:
- `dark_mode_enabled`: System default
- `default_shell`: "/data/data/com.convocli/files/usr/bin/bash"
- `font_size`: 14 (sp)
- `max_history_size`: 1000

---

## Data Flow Architecture

### Command Execution Flow

```
User Input
    ↓
ViewModel.executeCommand(command: String)
    ↓
Repository.executeCommand(command)
    ↓
1. Save to DB (Command entity with output=null)
    ↓
2. Execute via Termux
    ↓
3. Stream output
    ↓
4. Update DB (output + exitCode)
    ↓
5. Emit Flow update
    ↓
ViewModel updates State
    ↓
UI recomposes
```

### Data Layer Responsibilities

**Repository Layer**:
- Coordinates between Termux and Room
- Handles command execution lifecycle
- Provides Flow streams for reactive UI
- Manages error handling

**DAO Layer**:
- Direct database operations only
- No business logic
- Suspending functions for async DB ops

**ViewModel Layer**:
- Converts Command entities to UI state (CommandBlock)
- Manages UI state machine
- Handles user intents

---

## Validation & Constraints

### Database Constraints

**Command Table**:
- `command_text` NOT NULL, length > 0
- `executed_at` > 0
- `working_directory` NOT NULL
- `exit_code` between -255 and 255 (if not null)

### Business Rules

**History Management**:
- Maximum 1000 commands in history (configurable)
- Auto-delete commands older than 30 days
- Background cleanup worker runs daily

**Command Limits**:
- Maximum command length: 4096 characters
- Maximum output length: 1MB (truncate if exceeded)
- Maximum concurrent commands: 5 per session

---

## Migration Strategy

### Version 1 → Version 2 (Future)

**Changes**:
- Add `duration_ms` field to Command
- Add `tags` table

**Migration**:
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE commands ADD COLUMN duration_ms INTEGER")
        database.execSQL("""
            CREATE TABLE tags (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                color TEXT NOT NULL
            )
        """)
    }
}
```

---

## Performance Considerations

### Indexing Strategy

**Current Indexes**:
- `executed_at`: For recent commands query (DESC sort)
- `session_id`: For session filtering

**Future Indexes**:
- FTS (Full-Text Search) on `command_text` for advanced search
- Composite index on `(session_id, executed_at)` if needed

### Query Optimization

**Pagination**:
- Use `LIMIT` for initial load (last 100 commands)
- Lazy load older commands on scroll

**Flow vs Suspend**:
- Use Flow for observing command list (reactive updates)
- Use suspend for one-time operations (insert, delete)

### Data Cleanup

**Automatic Cleanup** (WorkManager):
```kotlin
class CleanupWorker : CoroutineWorker() {
    override suspend fun doWork(): Result {
        val thirtyDaysAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)
        commandDao.deleteOlderThan(thirtyDaysAgo)
        return Result.success()
    }
}
```

**Schedule**:
- Run daily at 3 AM
- Keep at least 100 most recent commands (even if older than 30 days)

---

## Testing Considerations

### Test Data Builders

```kotlin
fun commandEntity(
    id: Long = 0,
    commandText: String = "ls -la",
    output: String? = null,
    exitCode: Int? = null,
    executedAt: Long = System.currentTimeMillis(),
    workingDirectory: String = "/home",
    sessionId: String? = null
) = Command(id, commandText, output, exitCode, executedAt, workingDirectory, sessionId)
```

### Database Testing

**In-Memory Database**:
```kotlin
@RunWith(AndroidJUnit4::class)
class CommandDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var commandDao: CommandDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        commandDao = database.commandDao()
    }

    @After
    fun tearDown() {
        database.close()
    }
}
```

---

## Summary

### Entities Defined

1. **Command** (Room Entity)
   - Persisted command history
   - Schema version 1
   - Indexed by `executed_at` and `session_id`

2. **CommandBlock** (UI Model)
   - Transient UI state
   - Not persisted
   - Converted from Command entities

3. **Settings** (DataStore)
   - User preferences
   - Key-value storage
   - No complex schema needed for MVP

### Data Flow Established

- **Repository Pattern**: Single source of truth
- **Reactive Updates**: Flow-based observation
- **Type Safety**: Room compile-time validation
- **Lifecycle Aware**: ViewModel + StateFlow

### Ready for Implementation

All data models, validation rules, and flow patterns are defined. Implementation can proceed with confidence.

---

## References

- [Room Documentation](https://developer.android.com/training/data-storage/room)
- [DataStore Documentation](https://developer.android.com/topic/libraries/architecture/datastore)
- [Feature Spec](./spec.md) - FR-7 Room Database Configuration
- [Constitution](/.specswarm/constitution.md) - Architecture patterns
