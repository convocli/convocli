# Implementation Plan: Terminal Output Integration

**Feature**: 007 - Terminal Output Integration
**Sprint**: Sprint 02
**Created**: 2025-10-22
**Status**: Planning

---

## Executive Summary

This feature integrates the Command Blocks UI (Feature 004) with the Termux terminal emulator core (Feature 002) to transform ConvoCLI from a UI mockup into a functional terminal emulator. The implementation focuses on real-time output streaming, ANSI color preservation, command lifecycle management, and session persistence.

**Estimated Effort**: 12-16 hours
**Risk Level**: Medium (integration complexity, performance tuning)
**Dependencies**: Features 002 (Termux) and 004 (Command Blocks UI) must be complete

---

## Architecture Overview

### Current State Analysis

**Existing Components** (from Sprint 01):
- ✅ `CommandBlockCard.kt` - UI component for displaying command blocks
- ✅ `CommandInputBar.kt` - Input component for command entry
- ✅ `CommandBlockViewModel.kt` - ViewModel with simulated data
- ✅ `CommandBlock` data model - Status, output, timestamps
- ✅ `CommandStatus` enum - PENDING, EXECUTING, SUCCESS, FAILURE
- ✅ Hilt DI setup with AppModule

**Terminal Core** (Feature 002):
- ✅ Termux fork integrated
- ✅ `TerminalSession.java` - Session management
- ✅ `TerminalEmulator.java` - VT-100 emulation
- ✅ PTY interface for process execution

**Missing Components** (to be built):
- ❌ Terminal output capture and routing
- ❌ Real-time streaming infrastructure
- ❌ ANSI color parsing and rendering
- ❌ Prompt detection for command completion
- ❌ Exit code capture
- ❌ Session state persistence
- ❌ Working directory tracking
- ❌ Command cancellation mechanism

### Target Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    UI Layer (Compose)                   │
│  ┌──────────────────┐         ┌────────────────────┐   │
│  │ CommandBlockCard │◄────────┤ CommandBlockScreen │   │
│  │  (with ANSI      │         │  (LazyColumn of    │   │
│  │   colored text)  │         │   command blocks)  │   │
│  └────────▲─────────┘         └──────────▲─────────┘   │
└───────────┼────────────────────────────────┼───────────┘
            │ StateFlow<List<CommandBlock>>  │
            │                                 │
┌───────────┴─────────────────────────────────┴───────────┐
│                 ViewModel Layer                         │
│  ┌──────────────────────────────────────────────────┐   │
│  │          CommandBlockViewModel                   │   │
│  │  • Command execution orchestration               │   │
│  │  • State management (MVI pattern)                │   │
│  │  • ANSI parsing via AnsiColorParser              │   │
│  │  • Lifecycle management                          │   │
│  └──────────────────┬──────────────┬────────────────┘   │
└─────────────────────┼──────────────┼────────────────────┘
                      │              │
        ┌─────────────▼──────┐  ┌───▼──────────────────┐
        │  TerminalRepository│  │ CommandBlockManager  │
        │  • Terminal I/O    │  │ • Block lifecycle    │
        │  • Session mgmt    │  │ • Persistence (Room) │
        │  • Output streaming│  │ • History management │
        └─────────┬──────────┘  └──────────────────────┘
                  │
┌─────────────────┴─────────────────────────────────────┐
│              Terminal Integration Layer                │
│  ┌────────────────────────────────────────────────┐   │
│  │         TerminalOutputProcessor                │   │
│  │  • Output chunking and buffering               │   │
│  │  • Stream routing (stdout/stderr)              │   │
│  │  • Performance throttling (60fps max)          │   │
│  └────────────────┬───────────────────────────────┘   │
│                   │                                    │
│  ┌────────────────▼───────────────────────────────┐   │
│  │         PromptDetector                         │   │
│  │  • Pattern matching for bash/zsh/sh prompts    │   │
│  │  • Timeout fallback (2s silence = complete)    │   │
│  │  • False positive prevention                   │   │
│  └────────────────────────────────────────────────┘   │
│                                                        │
│  ┌─────────────────────────────────────────────────┐  │
│  │         WorkingDirectoryTracker                 │  │
│  │  • Parse PWD from environment                   │  │
│  │  • Detect 'cd' commands                         │  │
│  │  • Maintain directory state                     │  │
│  └─────────────────────────────────────────────────┘  │
└────────────────────────┬───────────────────────────────┘
                         │
┌────────────────────────▼───────────────────────────────┐
│                Termux Core (Java)                      │
│  ┌─────────────────────────────────────────────────┐   │
│  │  TerminalSession                                │   │
│  │  • Process execution                            │   │
│  │  • PTY management                               │   │
│  │  • Output stream callbacks                      │   │
│  └─────────────────────────────────────────────────┘   │
│                                                        │
│  ┌─────────────────────────────────────────────────┐   │
│  │  TerminalEmulator                               │   │
│  │  • VT-100/ANSI emulation                        │   │
│  │  • Screen buffer management                     │   │
│  │  • Character encoding                           │   │
│  └─────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────┘
```

---

## Component Design

### 1. TerminalRepository

**Purpose**: Abstract terminal session management and provide reactive output streams

**Responsibility**:
- Create and manage terminal sessions
- Execute commands
- Stream output to consumers
- Handle session lifecycle
- Expose working directory state

**Interface**:
```kotlin
interface TerminalRepository {
    // Session management
    fun createSession(): Flow<TerminalSessionState>
    suspend fun destroySession()

    // Command execution
    suspend fun executeCommand(command: String, blockId: String)
    suspend fun cancelCommand(blockId: String)

    // Output observation
    fun observeOutput(): Flow<OutputChunk>

    // State observation
    val workingDirectory: StateFlow<String>
    val isSessionActive: StateFlow<Boolean>
}
```

**Implementation Notes**:
- Wrap Termux TerminalSession with Kotlin-friendly API
- Use Channels for output streaming
- Coroutine-based lifecycle management
- Single session for Sprint 02

### 2. TerminalOutputProcessor

**Purpose**: Process raw terminal output into structured chunks for UI consumption

**Responsibility**:
- Buffer output for performance (batch updates at 60fps max)
- Route stdout/stderr to appropriate streams
- Detect binary output
- Implement backpressure handling

**Interface**:
```kotlin
class TerminalOutputProcessor {
    suspend fun processOutput(
        rawOutput: String,
        streamType: StreamType,
        blockId: String
    ): List<OutputChunk>

    fun startBuffering()
    fun flushBuffer(): List<OutputChunk>
}
```

**Implementation Details**:
- Buffer size: 16ms (~60fps)
- Max chunk size: 4KB per update
- Binary detection: Check for non-printable characters
- Interleave stdout/stderr by timestamp

### 3. AnsiColorParser

**Purpose**: Parse ANSI color codes and convert to Compose-renderable annotations

**Responsibility**:
- Parse ANSI escape sequences (colors, bold, underline)
- Generate AnnotatedString with SpanStyle
- Handle invalid sequences gracefully
- Support 16-color palette (ANSI standard)

**Interface**:
```kotlin
class AnsiColorParser {
    fun parseAnsiString(rawText: String): AnnotatedString
    fun stripAnsiCodes(rawText: String): String
}
```

**ANSI Code Support**:
- Foreground colors: 30-37 (standard), 90-97 (bright)
- Background colors: 40-47 (standard), 100-107 (bright)
- Text styles: Bold (1), Underline (4), Reset (0)
- Out of scope: 256-color (38;5;n), true-color (38;2;r;g;b)

**Color Mapping**:
```kotlin
val ansiColorMap = mapOf(
    30 to Color.Black,
    31 to Color.Red,
    32 to Color.Green,
    33 to Color.Yellow,
    34 to Color.Blue,
    35 to Color.Magenta,
    36 to Color.Cyan,
    37 to Color.White,
    // ... bright colors 90-97
)
```

### 4. PromptDetector

**Purpose**: Detect command completion by recognizing shell prompts

**Responsibility**:
- Match against pre-configured prompt patterns
- Implement timeout-based fallback
- Prevent false positives from mid-command output
- Signal command completion to ViewModel

**Interface**:
```kotlin
class PromptDetector {
    fun detectPrompt(output: String): PromptDetectionResult
    fun updateLastOutputTime()
    fun checkTimeout(): Boolean  // Returns true if 2s silence elapsed
}
```

**Prompt Patterns** (Sprint 02):
```kotlin
val bashPrompt = Regex("""^\$ """)
val rootPrompt = Regex("""^# """)
val zshPrompt = Regex("""^%\s""")
val generalPrompt = Regex("""^[\w@\-]+[:#\$%]\s""")
```

**Timeout Logic**:
- Start timer on first output
- Reset timer on new output
- If 2 seconds of silence: mark command complete
- Prevents hanging for custom prompts

### 5. WorkingDirectoryTracker

**Purpose**: Track and expose current working directory

**Responsibility**:
- Extract PWD from terminal environment
- Detect `cd` commands
- Update directory state
- Provide directory for command block headers

**Interface**:
```kotlin
class WorkingDirectoryTracker {
    val currentDirectory: StateFlow<String>

    suspend fun updateFromEnvironment(session: TerminalSession)
    suspend fun handleCdCommand(command: String)
}
```

**Implementation**:
- Parse PWD environment variable from session
- Regex to detect `cd <path>` commands
- Resolve relative paths (., .., ~)
- Initial value: /data/data/com.convocli/files/home

### 6. CommandBlockManager

**Purpose**: Manage command block lifecycle and persistence

**Responsibility**:
- Create command blocks
- Update block state (status, output, exit code)
- Persist blocks to Room database
- Provide history query API
- Handle block expansion state

**Interface**:
```kotlin
interface CommandBlockManager {
    suspend fun createBlock(command: String, workingDir: String): CommandBlock
    suspend fun updateBlockOutput(id: String, output: String)
    suspend fun updateBlockStatus(id: String, status: CommandStatus, exitCode: Int?)
    suspend fun toggleExpansion(id: String)
    fun observeBlocks(): Flow<List<CommandBlock>>
    suspend fun deleteBlock(id: String)
}
```

**Database Schema** (Room):
```kotlin
@Entity(tableName = "command_blocks")
data class CommandBlockEntity(
    @PrimaryKey val id: String,
    val command: String,
    val output: String,
    val status: CommandStatus,
    val exitCode: Int?,
    val startTime: Long,
    val endTime: Long?,
    val workingDirectory: String,
    val isExpanded: Boolean
)
```

---

## Data Flow

### 1. Command Execution Flow

```
User Input → CommandInputBar
    ↓
CommandBlockViewModel.executeCommand(cmd)
    ↓
1. Create CommandBlock (PENDING status)
2. Add to StateFlow<List<CommandBlock>>
    ↓
TerminalRepository.executeCommand(cmd, blockId)
    ↓
Termux TerminalSession.write(cmd + "\n")
    ↓
[Process executes, output generated]
    ↓
TerminalSession callback: onTextChanged()
    ↓
TerminalOutputProcessor.processOutput()
    ↓ (buffered at 60fps)
OutputChunk emitted to Flow
    ↓
CommandBlockViewModel collects output
    ↓
1. Parse ANSI codes → AnnotatedString
2. Update block output in StateFlow
3. PromptDetector checks for completion
    ↓
UI Recomposition with updated output
```

### 2. Command Completion Flow

```
PromptDetector.detectPrompt(latestOutput)
    ↓
Prompt pattern matched OR timeout (2s silence)
    ↓
Emit completion signal
    ↓
CommandBlockViewModel receives signal
    ↓
1. Capture exit code from session
2. Update block status (SUCCESS/FAILURE)
3. Set endTime
4. Persist to database
    ↓
UI updates with final status indicator
```

### 3. Cancellation Flow

```
User taps cancel button
    ↓
CommandBlockViewModel.cancelCommand(blockId)
    ↓
TerminalRepository.cancelCommand(blockId)
    ↓
1. Send SIGTERM to process
2. Wait 2 seconds
3. If still running: send SIGKILL
    ↓
Update block status to CANCELED
    ↓
Partial output preserved in UI
```

### 4. Persistence & Restoration Flow

```
App backgrounded or closed
    ↓
CommandBlockManager persists all blocks to Room
    ↓
[App restart]
    ↓
ViewModel init: load blocks from database
    ↓
Blocks with EXECUTING status → transition to CANCELED
    ↓
Restore StateFlow with historical blocks
    ↓
UI displays command history
```

---

## State Management (MVI Pattern)

### ViewModel State

```kotlin
data class CommandBlockState(
    val blocks: List<CommandBlock> = emptyList(),
    val isExecuting: Boolean = false,
    val workingDirectory: String = "/",
    val sessionActive: Boolean = false
)

sealed class CommandBlockIntent {
    data class ExecuteCommand(val command: String) : CommandBlockIntent()
    data class CancelCommand(val blockId: String) : CommandBlockIntent()
    data class ToggleExpansion(val blockId: String) : CommandBlockIntent()
    data class CopyCommand(val blockId: String) : CommandBlockIntent()
    data class CopyOutput(val blockId: String) : CommandBlockIntent()
    data class RerunCommand(val blockId: String) : CommandBlockIntent()
}

sealed class CommandBlockEffect {
    data class ShowToast(val message: String) : CommandBlockEffect()
    object ScrollToBottom : CommandBlockEffect()
}
```

### ViewModel Implementation Structure

```kotlin
@HiltViewModel
class CommandBlockViewModel @Inject constructor(
    private val terminalRepository: TerminalRepository,
    private val commandBlockManager: CommandBlockManager,
    private val ansiColorParser: AnsiColorParser,
    private val clipboardManager: ClipboardManager
) : ViewModel() {

    private val _state = MutableStateFlow(CommandBlockState())
    val state: StateFlow<CommandBlockState> = _state.asStateFlow()

    private val _effects = Channel<CommandBlockEffect>()
    val effects: Flow<CommandBlockEffect> = _effects.receiveAsFlow()

    init {
        observeCommandBlocks()
        observeTerminalOutput()
        observeWorkingDirectory()
    }

    fun processIntent(intent: CommandBlockIntent) {
        when (intent) {
            is CommandBlockIntent.ExecuteCommand -> executeCommand(intent.command)
            is CommandBlockIntent.CancelCommand -> cancelCommand(intent.blockId)
            // ... other intents
        }
    }
}
```

---

## Implementation Phases

### Phase 1: Terminal Integration (4-5 hours)

**Tasks**:
1. Create `TerminalRepository` with session management
   - Wrap Termux TerminalSession
   - Implement command execution
   - Setup output streaming via Flow
   - **Deliverable**: Repository with basic command execution

2. Create `TerminalOutputProcessor`
   - Implement buffering (16ms batches)
   - Route stdout/stderr
   - Handle binary detection
   - **Deliverable**: Processed output chunks

3. Integrate output streaming into ViewModel
   - Collect output Flow
   - Update command block output
   - **Deliverable**: Real output appears in UI

**Validation**:
- Execute `echo "Hello World"` → output displays
- Execute `ls -la` → directory listing displays
- Execute long command → output streams progressively

### Phase 2: ANSI Color Support (3-4 hours)

**Tasks**:
1. Implement `AnsiColorParser`
   - Parse ANSI escape sequences
   - Generate AnnotatedString with colors
   - Handle text styles (bold, underline)
   - Strip invalid sequences
   - **Deliverable**: Colored text rendering

2. Update CommandBlockCard UI
   - Render AnnotatedString instead of plain String
   - Support stderr coloring (red text)
   - **Deliverable**: Colored output in UI

3. Test color rendering
   - `ls --color=always` → colored directories
   - `echo -e "\033[31mRed\033[0m"` → red text
   - **Deliverable**: Color fidelity validation

**Validation**:
- Colored ls output renders correctly
- Bold/underline text styles work
- Invalid codes don't corrupt display

### Phase 3: Command Lifecycle Management (3-4 hours)

**Tasks**:
1. Implement `PromptDetector`
   - Pattern matching for bash/zsh/sh
   - Timeout fallback logic
   - False positive prevention
   - **Deliverable**: Command completion detection

2. Implement exit code capture
   - Extract exit status from terminal
   - Update block status (SUCCESS/FAILURE)
   - **Deliverable**: Status indicators work

3. Implement command cancellation
   - SIGTERM → SIGKILL escalation
   - Update block to CANCELED status
   - **Deliverable**: Cancel button works

4. Implement `WorkingDirectoryTracker`
   - Parse PWD from environment
   - Detect cd commands
   - **Deliverable**: Working directory in headers

**Validation**:
- `echo "test"` → auto-completes, shows SUCCESS
- `cat nonexistent` → auto-completes, shows FAILURE
- `ping google.com` → cancel works, shows CANCELED
- `cd /tmp && pwd` → working directory updates

### Phase 4: Persistence & Polish (2-3 hours)

**Tasks**:
1. Implement `CommandBlockManager` with Room
   - Define database schema
   - Implement DAO
   - Add persistence operations
   - **Deliverable**: Command history persists

2. Session restoration
   - Load blocks on app start
   - Handle EXECUTING → CANCELED transition
   - **Deliverable**: History survives restarts

3. Performance optimization
   - Profile output streaming
   - Optimize recomposition
   - Implement output truncation (10,000 lines)
   - **Deliverable**: Smooth performance

4. Error handling & edge cases
   - Binary output detection
   - Memory limits
   - Process crashes
   - **Deliverable**: Robust error handling

**Validation**:
- Command history persists across app restarts
- `tail -f` streams smoothly without lag
- Binary output shows placeholder
- App doesn't crash on process failures

---

## Testing Strategy

### Unit Tests

**TerminalRepository** (`TerminalRepositoryTest.kt`):
- Session creation returns valid state
- Command execution writes to terminal
- Output streaming emits chunks
- Cancellation sends termination signal

**AnsiColorParser** (`AnsiColorParserTest.kt`):
- Standard colors (30-37) parse correctly
- Bright colors (90-97) parse correctly
- Bold/underline styles apply
- Invalid sequences are stripped
- Mixed codes work correctly

**PromptDetector** (`PromptDetectorTest.kt`):
- Bash prompt "$ " detected
- Root prompt "# " detected
- zsh prompt "% " detected
- Timeout triggers after 2s silence
- Mid-command prompt-like output ignored

**WorkingDirectoryTracker** (`WorkingDirectoryTrackerTest.kt`):
- PWD extraction works
- `cd /tmp` updates directory
- `cd ..` resolves correctly
- `cd ~` expands to home

### Integration Tests

**Terminal → ViewModel** (`CommandExecutionIntegrationTest.kt`):
- Execute command → block appears
- Output streams → UI updates
- Command completes → status updates
- Exit code captured correctly

**Persistence** (`PersistenceIntegrationTest.kt`):
- Command blocks persist to database
- History loads on ViewModel init
- EXECUTING blocks transition to CANCELED

### UI Tests (Compose)

**CommandBlockScreen** (`CommandBlockScreenTest.kt`):
- Enter command → block appears
- Output displays with colors
- Cancel button works
- Status indicators display correctly
- Expansion toggle works

### Manual Testing Checklist

- [ ] Basic commands: `echo`, `ls`, `pwd`, `date`
- [ ] Colored output: `ls --color=always`
- [ ] Long-running: `sleep 10 && echo done`
- [ ] Errors: `cat nonexistent.txt`
- [ ] Cancellation: `ping google.com` → cancel
- [ ] Working directory: `cd /tmp && ls`
- [ ] Persistence: Execute commands → restart app
- [ ] Performance: `npm install` (high-volume output)
- [ ] Binary output: `cat /bin/ls`

---

## Performance Considerations

### Output Buffering

**Problem**: High-frequency terminal output could cause UI lag

**Solution**:
- Buffer output chunks
- Flush at 60fps max (16ms intervals)
- Batch recompositions

**Implementation**:
```kotlin
private val outputBuffer = mutableListOf<OutputChunk>()
private val bufferFlushJob = viewModelScope.launch {
    while (isActive) {
        delay(16) // 60fps
        if (outputBuffer.isNotEmpty()) {
            val chunks = outputBuffer.toList()
            outputBuffer.clear()
            processChunks(chunks)
        }
    }
}
```

### Memory Management

**Problem**: Very long output could cause OOM

**Solution**:
- Limit output to 10,000 lines per block
- Truncate with "show more" option
- Automatically collapse old blocks

**Implementation**:
```kotlin
fun trimOutput(output: String): String {
    val lines = output.lines()
    return if (lines.size > 10_000) {
        val header = lines.take(5_000).joinToString("\n")
        val footer = lines.takeLast(5_000).joinToString("\n")
        "$header\n\n[... ${lines.size - 10_000} lines truncated ...]\n\n$footer"
    } else {
        output
    }
}
```

### Compose Optimization

**Key Patterns**:
- Stable keys for LazyColumn items: `key = { it.id }`
- Remember ANSI parsing: `remember(rawOutput) { parser.parse(rawOutput) }`
- Avoid lambda allocations in composition
- Use derivedStateOf for computed state

---

## Risk Mitigation

### Risk 1: Prompt Detection Failures

**Mitigation**: Timeout fallback (2s silence = complete)

**Implementation**:
```kotlin
private var lastOutputTime = System.currentTimeMillis()

fun checkCompletion(): Boolean {
    val promptDetected = detector.detectPrompt(latestOutput)
    val timeoutTriggered = System.currentTimeMillis() - lastOutputTime > 2000
    return promptDetected || (timeoutTriggered && latestOutput.isNotBlank())
}
```

### Risk 2: Performance with High-Volume Output

**Mitigation**: Output buffering, throttling, truncation

**Monitoring**:
- Log output chunk sizes
- Measure recomposition frequency
- Alert if >60 updates/second

### Risk 3: Process Termination Failures

**Mitigation**: Escalating termination

**Implementation**:
```kotlin
suspend fun cancelCommand(blockId: String) {
    process.destroy() // SIGTERM
    delay(2000)
    if (process.isAlive) {
        process.destroyForcibly() // SIGKILL
        delay(500)
        if (process.isAlive) {
            Log.e(TAG, "Failed to terminate process")
            // Update UI with warning
        }
    }
}
```

---

## Dependencies

### Feature Dependencies
- ✅ Feature 002: Termux Integration (complete)
- ✅ Feature 004: Command Blocks UI (complete)

### Tech Stack Dependencies
- ✅ Jetpack Compose 1.9.3
- ✅ Kotlin Coroutines
- ✅ StateFlow/SharedFlow
- ✅ Hilt dependency injection
- ⏳ Room database (to be added)

---

## Success Criteria

### Functional
- [ ] Commands execute and display real output
- [ ] ANSI colors render correctly
- [ ] Command completion detected automatically
- [ ] Exit status (success/failure) displayed
- [ ] Cancel button terminates commands
- [ ] Working directory tracked and displayed
- [ ] Command history persists across restarts

### Performance
- [ ] Output latency <100ms for 95% of commands
- [ ] UI remains responsive during streaming
- [ ] No memory leaks or excessive growth
- [ ] Session restore <2 seconds

### Quality
- [ ] Zero output loss or corruption
- [ ] No output mixing between commands
- [ ] Graceful handling of edge cases (binary output, crashes)

---

## Next Steps

After plan approval:
1. Generate `data-model.md` with detailed entity definitions
2. Generate `contracts/` directory with interface specifications
3. Generate `research.md` for ANSI parsing and prompt detection
4. Run `/specswarm:tasks` to create detailed task breakdown
5. Begin Phase 1 implementation
