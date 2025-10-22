# Data Model: Termux Integration

**Feature**: 002 - Termux Integration - Terminal Emulator Core
**Date**: 2025-10-21
**Status**: Design Complete

---

## Overview

This document defines the data entities and their relationships for the Termux terminal integration feature. The model supports terminal session management, command execution, output streaming, and error handling.

---

## Entity Diagram

```
┌──────────────────────┐
│  TerminalSession     │
│                      │
│  - sessionId         │
│  - shellPath         │
│  - workingDirectory  │◄───────┐
│  - environment       │        │
│  - state             │        │
│  - createdAt         │        │
└──────────┬───────────┘        │
           │                    │
           │ 1                  │
           │                    │
           │                    │
           │ *                  │
┌──────────▼───────────┐        │
│  TerminalOutput      │        │
│                      │        │
│  - text              │        │
│  - stream            │────────┼────► StreamType
│  - timestamp         │        │      - STDOUT
│  - sessionId         │────────┘      - STDERR
└──────────────────────┘
           │
           │ *
           │
┌──────────▼───────────┐
│  TerminalError       │
│  (sealed class)      │
│                      │
│  Subtypes:           │
│  - InitializationFailed
│  - CommandFailed     │
│  - SessionCrashed    │
│  - IOError           │
└──────────────────────┘
```

---

## Entities

### 1. TerminalSession

**Purpose**: Represents an active terminal session with a shell process

**Type**: Data Class

**Definition**:
```kotlin
data class TerminalSession(
    val sessionId: String,
    val shellPath: String,
    val workingDirectory: String,
    val environment: Map<String, String>,
    val state: SessionState,
    val createdAt: Long
)
```

**Attributes**:

| Attribute | Type | Description | Example |
|-----------|------|-------------|---------|
| `sessionId` | String | Unique identifier (UUID) | `"550e8400-e29b-41d4-a716-446655440000"` |
| `shellPath` | String | Absolute path to shell executable | `"/data/data/com.convocli/files/usr/bin/bash"` |
| `workingDirectory` | String | Current working directory | `"/data/data/com.convocli/files/home"` |
| `environment` | Map<String, String> | Environment variables | `{"HOME": "/data/...", "PATH": "..."}` |
| `state` | SessionState | Current session state | `SessionState.RUNNING` |
| `createdAt` | Long | Unix timestamp (milliseconds) | `1729540800000L` |

**Lifecycle States**:
```kotlin
enum class SessionState {
    RUNNING,   // Session active, shell process alive
    STOPPED,   // Session terminated normally
    ERROR      // Session crashed or failed
}
```

**State Transitions**:
```
    [NEW]
      │
      │ createSession()
      ▼
   RUNNING ──────► STOPPED
      │              ▲
      │ error        │
      ▼              │
    ERROR ───────────┘
       destroySession()
```

**Business Rules**:
- `sessionId` must be globally unique (use UUID)
- `shellPath` must exist and be executable
- `workingDirectory` must be a valid directory path
- `environment` must include at minimum: HOME, PATH, SHELL
- `createdAt` is immutable once set

**Relationships**:
- **Has Many** `TerminalOutput` (1:N)
- **May Have** `TerminalError` (1:0..1 at any given time)

---

### 2. TerminalOutput

**Purpose**: Represents a chunk of output from command execution

**Type**: Data Class

**Definition**:
```kotlin
data class TerminalOutput(
    val text: String,
    val stream: StreamType,
    val timestamp: Long
)
```

**Attributes**:

| Attribute | Type | Description | Example |
|-----------|------|-------------|---------|
| `text` | String | Output text content | `"total 24\ndrwxr-xr-x  3 user user 4096..."` |
| `stream` | StreamType | Output stream type | `StreamType.STDOUT` |
| `timestamp` | Long | Unix timestamp (milliseconds) | `1729540801234L` |

**Stream Types**:
```kotlin
enum class StreamType {
    STDOUT,  // Standard output
    STDERR   // Standard error
}
```

**Business Rules**:
- `text` may contain ANSI escape sequences (colors, cursor movement)
- `text` may be empty string (valid for some outputs)
- `timestamp` is set when output received from PTY
- Each output chunk is immutable

**Relationships**:
- **Belongs To** `TerminalSession` (N:1)

**Usage Pattern**:
```kotlin
// Emitted via Flow for reactive streaming
val output: Flow<TerminalOutput> = repository.observeOutput(sessionId)

output.collect { chunk ->
    when (chunk.stream) {
        StreamType.STDOUT -> displayNormal(chunk.text)
        StreamType.STDERR -> displayError(chunk.text)
    }
}
```

---

### 3. TerminalError

**Purpose**: Represents errors during terminal operations

**Type**: Sealed Class (discriminated union)

**Definition**:
```kotlin
sealed class TerminalError {
    /**
     * Session initialization failed
     * Causes: PTY creation failure, shell not found, permission denied
     */
    data class InitializationFailed(
        val reason: String
    ) : TerminalError()

    /**
     * Command execution failed
     * Causes: Invalid command, permission denied, file not found
     */
    data class CommandFailed(
        val command: String,
        val exitCode: Int,
        val stderr: String
    ) : TerminalError()

    /**
     * Session crashed unexpectedly
     * Causes: Broken pipe, process killed, resource exhaustion
     */
    data class SessionCrashed(
        val reason: String
    ) : TerminalError()

    /**
     * I/O error during PTY operations
     * Causes: Read timeout, write failure, connection lost
     */
    data class IOError(
        val message: String
    ) : TerminalError()
}
```

**Error Subtypes**:

| Type | Trigger | Typical Causes | Recovery |
|------|---------|----------------|----------|
| `InitializationFailed` | `createSession()` | PTY failure, shell not found | Show error dialog, prevent commands |
| `CommandFailed` | `executeCommand()` | Invalid command, permissions | Display stderr, continue session |
| `SessionCrashed` | Any time | Process died, broken pipe | Offer session restart |
| `IOError` | Read/write ops | Timeouts, pipe errors | Log error, attempt recovery |

**Business Rules**:
- Errors are non-recoverable state transitions
- Each error includes context for debugging
- Errors are emitted via `SharedFlow` for UI observation
- Multiple errors can occur for a single session

**Relationships**:
- **May Be Associated With** `TerminalSession` (0..1:1 at error time)

**Usage Pattern**:
```kotlin
// Emitted via SharedFlow for error handling
val errors: SharedFlow<TerminalError> = repository.observeErrors()

errors.collect { error ->
    when (error) {
        is TerminalError.InitializationFailed -> {
            showDialog("Failed to create terminal: ${error.reason}")
        }
        is TerminalError.CommandFailed -> {
            displayErrorMessage("Command '${error.command}' failed (exit ${error.exitCode}): ${error.stderr}")
        }
        is TerminalError.SessionCrashed -> {
            showRestartDialog("Session crashed: ${error.reason}")
        }
        is TerminalError.IOError -> {
            logError("I/O error: ${error.message}")
        }
    }
}
```

---

### 4. StreamType

**Purpose**: Distinguishes between standard output and standard error

**Type**: Enum

**Definition**:
```kotlin
enum class StreamType {
    STDOUT,
    STDERR
}
```

**Usage**:
- Determines UI rendering (normal text vs error text)
- Enables separate handling of command output and errors
- Used in `TerminalOutput.stream` field

---

## Relationships

### Session ↔ Output (1:N)

```kotlin
// One session produces many output events
val session: TerminalSession = repository.createSession().getOrThrow()
val outputs: Flow<TerminalOutput> = repository.observeOutput(session.sessionId)

// Each output is tagged with timestamp
outputs.collect { output ->
    println("[${output.timestamp}] ${output.text}")
}
```

**Cardinality**: 1 session : N outputs (0 to ∞)

**Lifecycle**:
- Outputs start when session is RUNNING
- Outputs continue until session is STOPPED or ERROR
- Old outputs are not persisted (future enhancement: Room database)

### Session ↔ Error (1:0..1)

```kotlin
// One session may emit multiple errors over time
val errors: SharedFlow<TerminalError> = repository.observeErrors()

errors.collect { error ->
    // Handle error (not necessarily fatal)
    if (error is TerminalError.SessionCrashed) {
        // Fatal error - session is dead
        currentSession.state = SessionState.ERROR
    }
}
```

**Cardinality**: 1 session : 0..N errors (over lifetime)

**Lifecycle**:
- Errors can occur at any time
- Some errors are fatal (InitializationFailed, SessionCrashed)
- Some errors are recoverable (CommandFailed, IOError)

---

## Data Flow

### Command Execution Flow

```
User Input
    │
    │ (1) executeCommand(sessionId, "ls -la")
    ▼
TerminalRepository
    │
    │ (2) Write to PTY stdin
    ▼
Native PTY
    │
    │ (3) Forward to shell process
    ▼
Bash Shell
    │
    │ (4) Execute command
    │ (5) Generate output
    ▼
Native PTY
    │
    │ (6) Read from PTY stdout/stderr
    ▼
OutputStreamProcessor
    │
    │ (7) Parse and emit TerminalOutput
    ▼
SharedFlow<TerminalOutput>
    │
    │ (8) Collect in ViewModel
    ▼
StateFlow<List<String>>
    │
    │ (9) Observe in UI
    ▼
Compose UI (Display)
```

### Error Handling Flow

```
PTY Operation
    │
    │ Exception thrown (IOException, etc.)
    ▼
Repository Catch Block
    │
    │ Create appropriate TerminalError
    ▼
SharedFlow<TerminalError>
    │
    │ Emit error
    ▼
ViewModel Error Collector
    │
    │ Transform to user-friendly message
    ▼
StateFlow<TerminalError?>
    │
    │ Observe in UI
    ▼
Error Dialog / Snackbar
```

---

## Repository Interface

The data model is accessed through the `TerminalRepository` interface:

```kotlin
interface TerminalRepository {
    /**
     * Create a new terminal session
     * @return Result containing sessionId on success, error on failure
     */
    suspend fun createSession(): Result<String>

    /**
     * Execute a command in the specified session
     * @param sessionId The session to execute in
     * @param command The command string to execute
     */
    suspend fun executeCommand(sessionId: String, command: String)

    /**
     * Observe output from a specific session
     * @param sessionId The session to observe
     * @return Flow of terminal output events
     */
    fun observeOutput(sessionId: String): Flow<TerminalOutput>

    /**
     * Observe errors from all sessions
     * @return SharedFlow of error events
     */
    fun observeErrors(): SharedFlow<TerminalError>

    /**
     * Destroy a terminal session and clean up resources
     * @param sessionId The session to destroy
     */
    suspend fun destroySession(sessionId: String)

    /**
     * Get the current state of a session
     * @param sessionId The session to check
     * @return Current state or null if session not found
     */
    fun getSessionState(sessionId: String): SessionState?
}
```

---

## ViewModel State

The ViewModel transforms repository data for UI consumption:

```kotlin
@HiltViewModel
class TerminalViewModel @Inject constructor(
    private val repository: TerminalRepository
) : ViewModel() {

    // UI state
    private val _output = MutableStateFlow<List<String>>(emptyList())
    val output: StateFlow<List<String>> = _output.asStateFlow()

    private val _isExecuting = MutableStateFlow(false)
    val isExecuting: StateFlow<Boolean> = _isExecuting.asStateFlow()

    private val _errors = MutableStateFlow<TerminalError?>(null)
    val errors: StateFlow<TerminalError?> = _errors.asStateFlow()

    private val _currentDirectory = MutableStateFlow("/data/data/com.convocli/files/home")
    val currentDirectory: StateFlow<String> = _currentDirectory.asStateFlow()

    // Session management
    private var sessionId: String? = null

    init {
        createSession()
    }

    private fun createSession() {
        viewModelScope.launch {
            repository.createSession()
                .onSuccess { id ->
                    sessionId = id
                    collectOutput(id)
                    collectErrors()
                }
                .onFailure { error ->
                    _errors.value = TerminalError.InitializationFailed(error.message ?: "Unknown error")
                }
        }
    }

    // ... other methods
}
```

---

## Future Enhancements

### Persistence (Feature 003+)

```kotlin
@Entity(tableName = "command_history")
data class CommandHistoryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val command: String,
    val output: String,
    val exitCode: Int,
    val timestamp: Long,
    val sessionId: String
)
```

### Multi-Session Support

```kotlin
// Repository manages multiple sessions
class TermuxTerminalRepository {
    private val sessions = mutableMapOf<String, TerminalSession>()

    suspend fun createSession(): Result<String> {
        val id = UUID.randomUUID().toString()
        val session = initializeSession(id)
        sessions[id] = session
        return Result.success(id)
    }
}
```

### Working Directory Tracking

```kotlin
data class TerminalSession(
    // ... existing fields
    var currentWorkingDirectory: String // Mutable, updated on 'cd' commands
)
```

---

## Summary

**Entities**: 4 (TerminalSession, TerminalOutput, TerminalError, StreamType)
**Relationships**: 2 (Session→Output, Session→Error)
**State Machine**: SessionState (RUNNING, STOPPED, ERROR)
**Reactive Streams**: 2 (Flow<TerminalOutput>, SharedFlow<TerminalError>)

**Design Principles**:
- ✅ Immutable data classes (except session state)
- ✅ Reactive streams via Kotlin Flow
- ✅ Type-safe error handling with sealed classes
- ✅ Clear separation of concerns (data vs logic)

---

**Status**: ✅ Design Complete
**Next Steps**: Implement data models (Phase 2: T006-T009)
