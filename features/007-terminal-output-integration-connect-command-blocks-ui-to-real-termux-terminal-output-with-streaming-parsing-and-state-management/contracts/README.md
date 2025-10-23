# Contracts: Terminal Output Integration

This directory contains interface definitions (contracts) for Feature 007 components.

## Purpose

Contracts define:
- **API boundaries** between components
- **Expected behavior** via documentation
- **Type safety** for implementations
- **Testing interfaces** for mocking

All implementations must adhere to these contracts.

---

## Contracts Overview

### 1. TerminalRepository.kt

**Purpose**: Terminal session management and command execution

**Responsibilities**:
- Create/destroy terminal sessions
- Execute commands via Termux core
- Stream output to consumers
- Track working directory
- Capture exit codes

**Key Methods**:
- `createSession()`: Initialize terminal session
- `executeCommand(command, blockId)`: Run command
- `cancelCommand(blockId)`: Terminate running command
- `observeOutput()`: Stream output chunks
- `workingDirectory`: Current directory state
- `isSessionActive`: Session status

**Implementation**: `TerminalRepositoryImpl.kt` (to be created in Phase 1)

---

### 2. CommandBlockManager.kt

**Purpose**: Command block lifecycle and persistence

**Responsibilities**:
- Create and update command blocks
- Persist to Room database
- Provide historical queries
- Manage expansion state

**Key Methods**:
- `createBlock(command, workingDir)`: Create new block
- `updateBlockOutput(id, output)`: Update output during streaming
- `updateBlockStatus(id, status, exitCode)`: Update completion status
- `observeBlocks()`: Reactive list of all blocks
- `toggleExpansion(id)`: Collapse/expand long output

**Implementation**: `CommandBlockManagerImpl.kt` (to be created in Phase 1)

---

### 3. AnsiColorParser.kt

**Purpose**: Parse ANSI escape sequences for Compose rendering

**Responsibilities**:
- Parse ANSI color codes (16-color palette)
- Parse text styles (bold, underline)
- Generate AnnotatedString with styling
- Strip invalid sequences

**Key Methods**:
- `parseAnsiString(rawText)`: Parse and style text
- `stripAnsiCodes(rawText)`: Remove ANSI codes
- `containsAnsiCodes(text)`: Detection helper

**Implementation**: `AnsiColorParserImpl.kt` (to be created in Phase 2)

**See**: research.md Section 1 for ANSI parsing details

---

### 4. PromptDetector.kt

**Purpose**: Detect command completion via shell prompt recognition

**Responsibilities**:
- Match shell prompts (bash, zsh, sh)
- Timeout fallback (2s silence)
- Prevent false positives

**Key Methods**:
- `detectPrompt(output)`: Check for prompt in output
- `updateLastOutputTime()`: Track output for timeout
- `checkTimeout()`: Verify if timeout elapsed
- `reset()`: Reset state for new command

**Implementation**: `PromptDetectorImpl.kt` (to be created in Phase 3)

**See**: research.md Section 2 for prompt patterns and detection strategy

---

### 5. WorkingDirectoryTracker.kt

**Purpose**: Track current working directory

**Responsibilities**:
- Extract PWD from terminal environment
- Detect and handle `cd` commands
- Resolve relative paths
- Validate directory existence

**Key Methods**:
- `currentDirectory`: StateFlow of current path
- `updateFromEnvironment(session)`: Extract PWD
- `handleCdCommand(command)`: Parse and apply cd
- `isValidDirectory(path)`: Validation

**Implementation**: `WorkingDirectoryTrackerImpl.kt` (to be created in Phase 3)

**See**: research.md Section 4 for working directory tracking details

---

### 6. TerminalOutputProcessor.kt

**Purpose**: Process raw output into structured chunks

**Responsibilities**:
- Buffer output for performance (60fps)
- Route stdout/stderr
- Detect binary output
- Implement backpressure

**Key Methods**:
- `processOutput(rawOutput, streamType, blockId)`: Process and chunk
- `startBuffering()`: Begin buffering
- `flushBuffer()`: Emit buffered chunks
- `isBinaryOutput(text)`: Binary detection
- `createBinaryPlaceholder(sizeBytes)`: Binary placeholder

**Implementation**: `TerminalOutputProcessorImpl.kt` (to be created in Phase 1)

**See**: research.md Section 3 for buffering strategy

---

## Implementation Guidelines

### Dependency Injection

All contracts are interfaces intended for Hilt injection:

```kotlin
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

    // ... other bindings
}
```

### Testing

Contracts enable easy mocking for tests:

```kotlin
class FakeTerminalRepository : TerminalRepository {
    override fun createSession() = flow {
        emit(TerminalSessionState.Ready(mockSession))
    }
    // ... implement other methods
}

@Test
fun testCommandExecution() {
    val fakeRepo = FakeTerminalRepository()
    val viewModel = CommandBlockViewModel(fakeRepo, ...)
    // Test with controlled fake
}
```

### Naming Convention

- **Contract**: `SomethingInterface.kt` → Just interface name
- **Implementation**: `SomethingImpl.kt`
- **Fake/Mock**: `FakeSomething.kt` for tests

---

## Implementation Order

Based on plan.md phases:

**Phase 1** (Terminal Integration):
1. TerminalRepository
2. TerminalOutputProcessor

**Phase 2** (ANSI Colors):
3. AnsiColorParser

**Phase 3** (Lifecycle):
4. PromptDetector
5. WorkingDirectoryTracker
6. CommandBlockManager (with Room)

---

## See Also

- **plan.md**: Overall implementation plan
- **data-model.md**: Entity definitions and schemas
- **research.md**: Technical decisions and research
