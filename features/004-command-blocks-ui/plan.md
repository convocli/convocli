# Implementation Plan: Command Blocks UI

**Feature ID**: 004
**Feature**: Command Blocks UI
**Created**: 2025-10-22
**Status**: Planning Complete
**Estimated Effort**: 16-20 hours (2-3 days)

---

## Overview

This implementation plan translates the Command Blocks UI specification into a concrete technical design for transforming ConvoCLI's terminal from a traditional text interface into a modern, conversational chat-like experience using Material 3 cards. Each command and its output become discrete, interactive blocks that users can scroll, copy, re-run, and manipulate like messages in a messaging app.

**Critical Impact**: This is THE defining product differentiator that makes ConvoCLI "Warp 2.0 for Android." Without this feature, ConvoCLI remains a generic terminal. With it, the product vision becomes reality.

---

## Technical Context

### Architecture Components

**New Components to Implement**:

1. **CommandBlock** (Data Model)
   - Immutable data class representing a single command execution
   - Properties: id, command, output, status, timestamp, executionDuration, exitCode, workingDirectory, isExpanded
   - Serializable with Kotlinx Serialization
   - Location: `data/model/CommandBlock.kt`

2. **CommandBlockManager** (Service)
   - Orchestrates command block lifecycle
   - Detects command boundaries using shell prompt patterns
   - Creates CommandBlock when user submits command
   - Updates block as output streams in
   - Transitions block to completed state when done
   - Maintains chronological sequence of blocks
   - Dependencies: TermuxTerminalRepository, OutputStreamProcessor
   - Location: `terminal/service/CommandBlockManager.kt`

3. **PromptDetector** (Utility)
   - Parses terminal output to detect shell prompts
   - Recognizes patterns: `$ `, `# `, `user@host:~$ `
   - Determines command boundaries
   - Location: `terminal/util/PromptDetector.kt`

4. **AnsiColorParser** (Utility)
   - Parses ANSI escape sequences
   - Converts ANSI colors to Material 3 theme colors
   - Handles basic formatting: bold, italic, underline
   - Strips or converts unsupported sequences
   - Location: `terminal/util/AnsiColorParser.kt`

5. **CommandBlockViewModel** (ViewModel)
   - Manages UI state for command blocks interface
   - Exposes StateFlow<List<CommandBlock>> to UI
   - Handles user actions: execute, copy, re-run, edit, expand/collapse, cancel
   - Triggers commands through TerminalViewModel
   - Manages scroll state and auto-scroll behavior
   - Formats timestamps and durations for display
   - Dependencies: CommandBlockManager, TerminalViewModel
   - Location: `ui/viewmodels/CommandBlockViewModel.kt`

6. **CommandBlocksScreen** (Compose UI)
   - Main screen composable for blocks interface
   - LazyColumn of CommandBlock cards
   - Bottom input bar for command entry
   - Auto-scroll to newest block
   - Empty state when no commands
   - Location: `ui/screens/CommandBlocksScreen.kt`

7. **CommandBlockCard** (Compose Component)
   - Material 3 Card displaying single command block
   - Command header with timestamp
   - Output section (expandable/collapsible)
   - Status indicator (pending, executing, success, failure)
   - Cancel button (for executing blocks)
   - Action buttons: Copy Command, Copy Output, Re-run, Edit & Run
   - Long-press context menu
   - Location: `ui/components/CommandBlockCard.kt`

8. **CommandInputBar** (Compose Component)
   - Fixed bottom input field
   - Multi-line text input support
   - Send button
   - Integration with CommandBlockViewModel
   - Location: `ui/components/CommandInputBar.kt`

**Modified Components**:

9. **TerminalViewModel** (Enhancement)
   - Add signal sending capability for command cancellation (SIGINT)
   - Location: `terminal/viewmodel/TerminalViewModel.kt`

10. **TermuxTerminalRepository** (Enhancement)
    - Add cancelCommand() method that sends SIGINT to running process
    - Location: `terminal/repository/TermuxTerminalRepository.kt`

### Technology Decisions

**UI Framework**: Jetpack Compose 1.9.3
- Modern declarative UI
- **Status**: ✅ APPROVED (core technology)
- **Purpose**: All UI components

**Design System**: Material Design 3
- Material 3 Card component for command blocks
- Material icons for status indicators and actions
- MaterialTheme.colorScheme for colors
- **Status**: ✅ APPROVED (core technology)
- **Purpose**: Design language and components

**State Management**: StateFlow / SharedFlow
- CommandBlockViewModel exposes StateFlow<List<CommandBlock>>
- Reactive UI updates
- **Status**: ✅ APPROVED (core technology)
- **Purpose**: Reactive state management

**Architecture Pattern**: MVI (Model-View-Intent)
- Unidirectional data flow
- ViewModel as state container
- **Status**: ✅ APPROVED (architecture pattern)
- **Purpose**: State management pattern

**Data Persistence**: Room + DataStore
- Room for CommandBlock history (future enhancement)
- DataStore for UI preferences (expansion states, scroll position)
- **Status**: ✅ APPROVED (core technology)
- **Purpose**: Local data storage (minimal use in MVP)

**Serialization**: Kotlinx Serialization
- CommandBlock data class serialization
- **Status**: ✅ APPROVED (core technology)
- **Purpose**: Data model serialization

**Dependency Injection**: Hilt
- ViewModel injection
- Repository injection
- **Status**: ✅ APPROVED (core technology)
- **Purpose**: Dependency injection

**Async Operations**: Kotlin Coroutines + Flow
- Command execution monitoring
- Output streaming
- **Status**: ✅ APPROVED (core technology)
- **Purpose**: Asynchronous operations

### Android-Specific Considerations

**Compose Performance**:
- LazyColumn for efficient list rendering (virtualization)
- `key` parameter for stable item identity
- `remember` for expensive computations
- `derivedStateOf` for derived state
- Target: 60fps scroll performance

**Memory Management**:
- Unlimited command history for MVP
- Monitor memory usage in production
- Consider pruning in Phase 2 if needed
- LazyColumn handles off-screen item recycling

**Touch Targets**:
- Minimum 48dp x 48dp for all interactive elements
- Appropriate spacing between blocks
- Long-press gestures for context menu

**Text Rendering**:
- Monospace font for command and output
- Material Typography scale
- Appropriate text sizes for mobile screens

**Lifecycle Awareness**:
- ViewModel survives configuration changes
- State restoration on process death (if needed)
- Proper coroutine cancellation on ViewModel clear

**Signal Handling** (Command Cancellation):
- SIGINT (signal 2) to interrupt processes
- PTY interface already supports signals
- Clean process termination
- Exit codes: 130 (SIGINT) or 143 (SIGTERM)

### Integration Points

**Feature 002: Terminal Emulator Core**:
- TermuxTerminalRepository.observeOutput() → stream of terminal output
- TermuxTerminalRepository.executeCommand() → execute user commands
- TerminalSession → represents active terminal session
- OutputStreamProcessor → processes output stream
- WorkingDirectoryTracker → tracks current directory

**Feature 003: Bootstrap Installation**:
- Requires functional bash shell
- Bootstrap must be installed before blocks can execute commands
- Working directory from bootstrap environment

**Existing ViewModels**:
- TerminalViewModel → delegates command execution to it
- CommandBlockViewModel wraps TerminalViewModel

**Existing Data Models**:
- TerminalOutput → source of command output stream
- TerminalError → error handling integration

---

## Tech Stack Compliance Report

### ✅ Approved Technologies (already in stack)

All technologies required for this feature are already approved in tech-stack.md:

- **Jetpack Compose** 1.9.3 - Declarative UI framework
- **Material Design 3** - Design system and components
- **StateFlow / SharedFlow** - Reactive state management
- **ViewModel** - Lifecycle-aware state containers
- **Kotlin Coroutines** - Asynchronous operations
- **Room** (future use) - Local database for history
- **DataStore** (future use) - UI preferences storage
- **Kotlinx Serialization** - Data model serialization
- **Hilt** - Dependency injection

### ➕ New Technologies

**None** - All required technologies already approved in tech-stack.md v1.0.0

### ⚠️ Conflicting Technologies

**None** - No conflicts detected

### ❌ Prohibited Technologies

**None used** - Plan complies with all prohibitions

---

## Constitution Check

Validating plan against `.specswarm/constitution.md` (v1.0.0):

### ✅ Principle 1: Kotlin Coding Standards

**Compliance**:
- All code will follow Kotlin Coding Conventions
- ktlint will enforce formatting
- PascalCase for classes (CommandBlock, CommandBlockManager)
- camelCase for functions (executeCommand, parseAnsiCodes)
- KDoc required for all public APIs

### ✅ Principle 2: Jetpack Compose Patterns

**Compliance**:
- Composables in PascalCase (CommandBlockCard, CommandBlocksScreen)
- State hoisting pattern used
- No business logic in composables (all in ViewModel)
- remember and derivedStateOf for performance
- LazyColumn for efficient lists

### ✅ Principle 3: MVI Architecture

**Compliance**:
- Unidirectional data flow: View → Intent → ViewModel → State → View
- ViewModel as single source of truth
- StateFlow for state, no mutable state in UI
- Clear separation: UI (Composables) → ViewModel → Repository → Service

### ✅ Principle 4: Dependency Injection with Hilt

**Compliance**:
- All ViewModels use @HiltViewModel
- Repositories and services injected via constructor
- No manual instantiation
- Singleton services appropriately scoped

### ✅ Principle 5: Technology Stack Adherence

**Compliance**:
- No prohibited technologies used
- All libraries from approved list
- No XML layouts (100% Compose)
- No deprecated patterns (LiveData, RxJava, etc.)
- StateFlow instead of LiveData
- Coroutines instead of RxJava

### ✅ Principle 6: Performance Standards

**Compliance**:
- 60fps target for UI
- LazyColumn virtualization for large lists
- Output throttling for rapid streams
- No blocking main thread
- Efficient ANSI parsing

### ✅ Principle 7: Accessibility

**Compliance**:
- Touch targets ≥ 48dp
- Content descriptions for all interactive elements
- TalkBack navigation support
- Not color-only status indicators (icons + color)
- Material 3 contrast ratios

### ✅ Principle 8: Testing Requirements

**To Implement**:
- Unit tests for CommandBlockManager (prompt detection, block lifecycle)
- Unit tests for AnsiColorParser
- Unit tests for PromptDetector
- Unit tests for CommandBlockViewModel (state transitions)
- Compose UI tests for CommandBlocksScreen
- Integration tests for command execution → block creation flow

**Target Coverage**: 80%+ for ViewModels and services

---

## Data Model Design

### CommandBlock

**Purpose**: Represents a single command execution as a displayable unit

**File**: `app/src/main/kotlin/com/convocli/data/model/CommandBlock.kt`

```kotlin
@Serializable
data class CommandBlock(
    val id: String,                          // Unique identifier (UUID)
    val command: String,                      // Command text entered by user
    val output: String,                       // Combined stdout/stderr output
    val status: CommandStatus,                // Execution state
    val timestamp: Long,                      // Unix timestamp (milliseconds)
    val executionDuration: Long?,             // Time taken (milliseconds), null if pending/executing
    val exitCode: Int?,                       // Process exit code, null until complete
    val workingDirectory: String,             // Directory where command was executed
    val isExpanded: Boolean = true            // Whether block output is expanded
)

enum class CommandStatus {
    PENDING,      // Command submitted but not yet executing
    EXECUTING,    // Command currently running
    SUCCESS,      // Completed with exit code 0
    FAILURE       // Completed with non-zero exit code or cancelled
}
```

**Relationships**:
- Managed by CommandBlockManager
- Displayed by CommandBlockCard composable
- Persisted in Room database (Phase 2)

**Validation Rules**:
- `id` must be unique (UUID format)
- `command` must not be empty
- `timestamp` > 0
- `executionDuration` ≥ 0 when present
- `exitCode` 0-255 when present
- `workingDirectory` must be valid path

**State Transitions**:
```
PENDING → EXECUTING → (SUCCESS | FAILURE)
```

---

## Contracts & Interfaces

### CommandBlockManager Interface

**Purpose**: Service interface for managing command block lifecycle

**File**: `features/004-command-blocks-ui/contracts/CommandBlockManager.kt`

```kotlin
/**
 * Manages the lifecycle of command blocks: creation, updates, and completion.
 * Bridges terminal output stream to command block data model.
 */
interface CommandBlockManager {

    /**
     * Observable stream of all command blocks in chronological order.
     * Emits new list whenever blocks are added or updated.
     */
    fun observeBlocks(): Flow<List<CommandBlock>>

    /**
     * Creates a new command block when user submits a command.
     * Block starts in PENDING status.
     *
     * @param command The command text to execute
     * @param workingDirectory Current working directory
     * @return ID of the created block
     */
    suspend fun createBlock(command: String, workingDirectory: String): String

    /**
     * Appends output to the currently executing block.
     * Called as output streams from the terminal.
     *
     * @param blockId ID of the block to update
     * @param output New output to append
     */
    suspend fun appendOutput(blockId: String, output: String)

    /**
     * Marks a block as executing.
     * Transitions from PENDING to EXECUTING status.
     *
     * @param blockId ID of the block
     */
    suspend fun markExecuting(blockId: String)

    /**
     * Completes a command block with final state.
     *
     * @param blockId ID of the block
     * @param exitCode Process exit code (0 = success, non-zero = failure)
     * @param duration Execution time in milliseconds
     */
    suspend fun completeBlock(blockId: String, exitCode: Int, duration: Long)

    /**
     * Cancels a currently executing block.
     * Transitions to FAILURE status with "Cancelled by user" indication.
     *
     * @param blockId ID of the block to cancel
     */
    suspend fun cancelBlock(blockId: String)

    /**
     * Toggles the expansion state of a block's output.
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
     * Clears all command blocks (for testing or reset).
     */
    suspend fun clearBlocks()
}
```

### PromptDetector Interface

**Purpose**: Detects shell prompts in terminal output to identify command boundaries

**File**: `features/004-command-blocks-ui/contracts/PromptDetector.kt`

```kotlin
/**
 * Detects shell prompts in terminal output to determine command boundaries.
 * Recognizes common prompt formats used by bash and other shells.
 */
interface PromptDetector {

    /**
     * Analyzes terminal output to detect if it ends with a shell prompt.
     *
     * @param output Terminal output text to analyze
     * @return true if output ends with a recognized prompt pattern
     */
    fun detectsPrompt(output: String): Boolean

    /**
     * Extracts prompt patterns from configuration or defaults.
     *
     * @return List of regex patterns that match shell prompts
     */
    fun getPromptPatterns(): List<Regex>

    /**
     * Strips prompt from the end of output if present.
     *
     * @param output Terminal output that may end with prompt
     * @return Output with trailing prompt removed
     */
    fun stripPrompt(output: String): String
}
```

### AnsiColorParser Interface

**Purpose**: Parses ANSI escape sequences and converts to styled text

**File**: `features/004-command-blocks-ui/contracts/AnsiColorParser.kt`

```kotlin
/**
 * Parses ANSI escape sequences in terminal output and converts to styled spans.
 * Supports basic 16 colors and formatting (bold, italic, underline).
 */
interface AnsiColorParser {

    /**
     * Parses ANSI-encoded text and returns styled annotation string for Compose.
     *
     * @param ansiText Raw terminal output with ANSI escape codes
     * @return AnnotatedString with appropriate styling applied
     */
    fun parse(ansiText: String): AnnotatedString

    /**
     * Strips all ANSI escape sequences from text.
     * Useful for copying plain text to clipboard.
     *
     * @param ansiText Text containing ANSI codes
     * @return Plain text with all escape sequences removed
     */
    fun stripAnsiCodes(ansiText: String): String

    /**
     * Maps ANSI color code to Material 3 theme color.
     *
     * @param ansiCode ANSI color code (0-15 for basic colors)
     * @param isDark Whether dark theme is active
     * @return Color from Material 3 palette
     */
    fun mapColorCode(ansiCode: Int, isDark: Boolean): Color
}
```

---

## Component Design Details

### Phase 0: Research & Unknown Resolution

**Research Tasks** (see `research.md`):

1. **R0.1: Shell Prompt Detection Patterns**
   - **Question**: What are the most common shell prompt formats in Termux/bash?
   - **Approach**: Analyze default Termux prompts, common PS1 customizations
   - **Output**: List of regex patterns to detect prompts reliably
   - **Success Criteria**: Patterns cover 95%+ of default configurations

2. **R0.2: ANSI Color Code Parsing**
   - **Question**: What ANSI escape sequences are commonly used in command output?
   - **Approach**: Test with ls, grep, git, npm commands; catalog codes used
   - **Output**: Supported ANSI codes list, mapping to Material 3 colors
   - **Success Criteria**: Handles basic 16 colors + bold/italic/underline

3. **R0.3: Material 3 Card Performance with Long Content**
   - **Question**: How does Material 3 Card perform with thousands of lines of text?
   - **Approach**: Prototype with test data, measure rendering time and scroll FPS
   - **Output**: Performance benchmarks, optimization strategies if needed
   - **Success Criteria**: Smooth 60fps scroll with 50+ blocks containing 100+ line output

4. **R0.4: Command Boundary Detection Edge Cases**
   - **Question**: What edge cases exist for prompt detection (prompt in output, custom PS1, etc.)?
   - **Approach**: Test with edge cases: multi-line commands, prompts in strings, heredocs
   - **Output**: Edge case handling strategy
   - **Success Criteria**: Graceful degradation for edge cases, no false positives

5. **R0.5: SIGINT Signal Handling in PTY**
   - **Question**: How to send SIGINT to a running process through the PTY interface?
   - **Approach**: Review Termux PTY implementation, test signal sending
   - **Output**: Implementation approach for cancellation
   - **Success Criteria**: Can successfully interrupt long-running commands (sleep, build)

### Phase 1: Data Model & Services

**1.1 CommandBlock Data Model**

**File**: `app/src/main/kotlin/com/convocli/data/model/CommandBlock.kt`

**Implementation**:
- Immutable data class with all required properties
- Kotlinx Serialization annotations
- Extension functions for formatting (e.g., `formattedTimestamp()`, `formattedDuration()`)
- Default values where appropriate (`isExpanded = true`)

**Testing**:
- Unit tests for data class properties
- Serialization/deserialization tests
- Extension function tests

---

**1.2 PromptDetector Implementation**

**File**: `app/src/main/kotlin/com/convocli/terminal/util/PromptDetectorImpl.kt`

**Implementation**:
```kotlin
class PromptDetectorImpl : PromptDetector {

    private val defaultPatterns = listOf(
        Regex("""^[$#]\s"""),                    // "$ " or "# "
        Regex("""^\w+@[\w-]+:[~/].*[$#]\s"""),  // "user@host:~/path$ "
        Regex("""^>\s"""),                        // "> "
    )

    override fun detectsPrompt(output: String): Boolean {
        val lastLine = output.lines().lastOrNull() ?: return false
        return defaultPatterns.any { it.containsMatchIn(lastLine) }
    }

    override fun getPromptPatterns(): List<Regex> = defaultPatterns

    override fun stripPrompt(output: String): String {
        val lines = output.lines().toMutableList()
        if (lines.isNotEmpty() && detectsPrompt(output)) {
            lines.removeLast()
        }
        return lines.joinToString("\n")
    }
}
```

**Testing**:
- Unit tests with various prompt formats
- Edge cases: prompts in command output, multi-line prompts
- False positive prevention

---

**1.3 AnsiColorParser Implementation**

**File**: `app/src/main/kotlin/com/convocli/terminal/util/AnsiColorParserImpl.kt`

**Implementation**:
```kotlin
class AnsiColorParserImpl(
    private val materialColors: ColorScheme
) : AnsiColorParser {

    // ANSI escape sequence regex: ESC[...m
    private val ansiPattern = Regex("""\u001B\[([0-9;]+)m""")

    override fun parse(ansiText: String): AnnotatedString {
        val builder = AnnotatedString.Builder()
        var currentIndex = 0
        var currentColor: Color? = null
        var isBold = false
        var isItalic = false

        ansiPattern.findAll(ansiText).forEach { match ->
            // Append text before this escape code
            builder.append(ansiText.substring(currentIndex, match.range.first))

            // Parse escape code and update style
            val codes = match.groupValues[1].split(";").map { it.toIntOrNull() ?: 0 }
            codes.forEach { code ->
                when (code) {
                    0 -> { // Reset
                        currentColor = null
                        isBold = false
                        isItalic = false
                    }
                    1 -> isBold = true
                    3 -> isItalic = true
                    in 30..37 -> currentColor = mapColorCode(code - 30, false) // Foreground
                    in 90..97 -> currentColor = mapColorCode(code - 90 + 8, false) // Bright foreground
                }
            }

            // Apply current style
            if (currentColor != null || isBold || isItalic) {
                builder.pushStyle(
                    SpanStyle(
                        color = currentColor ?: Color.Unspecified,
                        fontWeight = if (isBold) FontWeight.Bold else null,
                        fontStyle = if (isItalic) FontStyle.Italic else null
                    )
                )
            }

            currentIndex = match.range.last + 1
        }

        // Append remaining text
        builder.append(ansiText.substring(currentIndex))

        return builder.toAnnotatedString()
    }

    override fun stripAnsiCodes(ansiText: String): String {
        return ansiPattern.replace(ansiText, "")
    }

    override fun mapColorCode(ansiCode: Int, isDark: Boolean): Color {
        // Map ANSI 16 colors to Material 3 palette
        return when (ansiCode) {
            0 -> if (isDark) Color.White else Color.Black  // Black/White
            1 -> Color.Red
            2 -> Color.Green
            3 -> Color.Yellow
            4 -> Color.Blue
            5 -> Color.Magenta
            6 -> Color.Cyan
            7 -> if (isDark) Color.Black else Color.White  // White/Black
            else -> Color.Unspecified
        }
    }
}
```

**Testing**:
- Unit tests with various ANSI sequences
- Color mapping tests
- Strip codes functionality
- Complex formatting (bold + color)

---

**1.4 CommandBlockManager Implementation**

**File**: `app/src/main/kotlin/com/convocli/terminal/service/CommandBlockManagerImpl.kt`

**Implementation Strategy**:
1. Maintains `MutableStateFlow<List<CommandBlock>>`
2. Listens to `TermuxTerminalRepository.observeOutput()`
3. Uses `PromptDetector` to identify command boundaries
4. Creates new block when command submitted
5. Updates active block as output streams
6. Completes block when prompt detected
7. Thread-safe updates using synchronized or Mutex

**Key Logic**:
```kotlin
@Singleton
class CommandBlockManagerImpl @Inject constructor(
    private val promptDetector: PromptDetector,
    private val terminalRepository: TermuxTerminalRepository
) : CommandBlockManager {

    private val _blocks = MutableStateFlow<List<CommandBlock>>(emptyList())
    private var activeBlockId: String? = null
    private val blockMap = mutableMapOf<String, CommandBlock>()

    init {
        // Listen to terminal output and update active block
        terminalRepository.observeOutput().onEach { output ->
            activeBlockId?.let { blockId ->
                appendOutput(blockId, output.text)

                // Check if command completed (prompt detected)
                if (promptDetector.detectsPrompt(output.text)) {
                    completeBlock(blockId, exitCode = 0, duration = calculateDuration(blockId))
                    activeBlockId = null
                }
            }
        }.launchIn(CoroutineScope(Dispatchers.Default))
    }

    override fun observeBlocks(): Flow<List<CommandBlock>> = _blocks.asStateFlow()

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

        blockMap[id] = block
        _blocks.value = _blocks.value + block
        activeBlockId = id

        return id
    }

    // ... other methods
}
```

**Testing**:
- Unit tests with fake TerminalRepository
- Block lifecycle tests
- Prompt detection integration
- Concurrent access tests

---

### Phase 2: ViewModel Layer

**2.1 CommandBlockViewModel**

**File**: `app/src/main/kotlin/com/convocli/ui/viewmodels/CommandBlockViewModel.kt`

**State**:
```kotlin
data class CommandBlocksUiState(
    val blocks: List<CommandBlock> = emptyList(),
    val isExecuting: Boolean = false,
    val currentDirectory: String = "/data/data/com.convocli/files/home",
    val error: String? = null
)
```

**Implementation**:
```kotlin
@HiltViewModel
class CommandBlockViewModel @Inject constructor(
    private val commandBlockManager: CommandBlockManager,
    private val terminalViewModel: TerminalViewModel,
    private val terminalRepository: TermuxTerminalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommandBlocksUiState())
    val uiState: StateFlow<CommandBlocksUiState> = _uiState.asStateFlow()

    init {
        // Observe blocks from manager
        viewModelScope.launch {
            commandBlockManager.observeBlocks().collect { blocks ->
                _uiState.update { it.copy(blocks = blocks) }
            }
        }

        // Observe executing state
        viewModelScope.launch {
            terminalViewModel.isExecuting.collect { executing ->
                _uiState.update { it.copy(isExecuting = executing) }
            }
        }

        // Observe current directory
        viewModelScope.launch {
            terminalViewModel.currentDirectory.collect { dir ->
                _uiState.update { it.copy(currentDirectory = dir) }
            }
        }
    }

    fun executeCommand(command: String) {
        viewModelScope.launch {
            val blockId = commandBlockManager.createBlock(
                command = command,
                workingDirectory = _uiState.value.currentDirectory
            )

            commandBlockManager.markExecuting(blockId)
            terminalViewModel.executeCommand(command)
        }
    }

    fun copyCommand(blockId: String) {
        viewModelScope.launch {
            val block = commandBlockManager.getBlock(blockId) ?: return@launch
            // Copy to clipboard (use ClipboardManager)
        }
    }

    fun copyOutput(blockId: String) {
        viewModelScope.launch {
            val block = commandBlockManager.getBlock(blockId) ?: return@launch
            // Copy to clipboard
        }
    }

    fun rerunCommand(blockId: String) {
        viewModelScope.launch {
            val block = commandBlockManager.getBlock(blockId) ?: return@launch
            executeCommand(block.command)
        }
    }

    fun editAndRun(blockId: String): String {
        viewModelScope.launch {
            val block = commandBlockManager.getBlock(blockId) ?: return@launch ""
            return@launch block.command
        }
        return ""
    }

    fun cancelCommand(blockId: String) {
        viewModelScope.launch {
            terminalRepository.cancelCommand() // Sends SIGINT
            commandBlockManager.cancelBlock(blockId)
        }
    }

    fun toggleExpansion(blockId: String) {
        viewModelScope.launch {
            commandBlockManager.toggleExpansion(blockId)
        }
    }
}
```

**Testing**:
- Unit tests with fake CommandBlockManager and TerminalViewModel
- State transition tests
- User action tests (execute, copy, re-run, cancel)

---

### Phase 3: UI Components

**3.1 CommandBlockCard**

**File**: `app/src/main/kotlin/com/convocli/ui/components/CommandBlockCard.kt`

**Design**:
```kotlin
@Composable
fun CommandBlockCard(
    block: CommandBlock,
    onCopyCommand: () -> Unit,
    onCopyOutput: () -> Unit,
    onRerun: () -> Unit,
    onEditAndRun: () -> Unit,
    onCancel: () -> Unit,
    onToggleExpansion: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { /* Show context menu */ }
                )
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header: Command + Timestamp + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Command text
                Text(
                    text = block.command,
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                // Status indicator
                StatusIndicator(status = block.status)

                // Cancel button (only for executing blocks)
                if (block.status == CommandStatus.EXECUTING) {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Cancel, "Cancel command")
                    }
                }
            }

            // Timestamp + Duration
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = formatTimestamp(block.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                block.executionDuration?.let { duration ->
                    Text(
                        text = "• ${formatDuration(duration)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Output section
            if (block.output.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                val lines = block.output.lines()
                val shouldCollapse = lines.size > 20

                if (shouldCollapse && !block.isExpanded) {
                    // Collapsed view: first 10 + last 5 lines
                    Text(
                        text = buildCollapsedOutput(lines),
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    TextButton(onClick = onToggleExpansion) {
                        Text("Show ${lines.size} lines")
                    }
                } else {
                    // Expanded view: all output
                    Text(
                        text = block.output,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (shouldCollapse) {
                        TextButton(onClick = onToggleExpansion) {
                            Text("Show less")
                        }
                    }
                }
            } else if (block.status == CommandStatus.EXECUTING) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            }

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onCopyCommand) {
                    Icon(Icons.Default.ContentCopy, "Copy command")
                }
                IconButton(onClick = onCopyOutput) {
                    Icon(Icons.Default.Assignment, "Copy output")
                }
                IconButton(onClick = onRerun) {
                    Icon(Icons.Default.Refresh, "Re-run")
                }
                IconButton(onClick = onEditAndRun) {
                    Icon(Icons.Default.Edit, "Edit & run")
                }
            }
        }
    }
}

@Composable
private fun StatusIndicator(status: CommandStatus) {
    val (icon, color) = when (status) {
        CommandStatus.PENDING -> Icons.Default.Schedule to MaterialTheme.colorScheme.onSurfaceVariant
        CommandStatus.EXECUTING -> Icons.Default.HourglassEmpty to MaterialTheme.colorScheme.primary
        CommandStatus.SUCCESS -> Icons.Default.CheckCircle to Color.Green
        CommandStatus.FAILURE -> Icons.Default.Error to Color.Red
    }

    Icon(
        imageVector = icon,
        contentDescription = status.name,
        tint = color,
        modifier = Modifier.size(20.dp)
    )
}
```

**Testing**:
- Compose UI tests for different block states
- Interaction tests (tap, long-press)
- Accessibility tests (TalkBack)

---

**3.2 CommandInputBar**

**File**: `app/src/main/kotlin/com/convocli/ui/components/CommandInputBar.kt`

**Design**:
```kotlin
@Composable
fun CommandInputBar(
    onCommandSubmit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var commandText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            // Multi-line text field
            TextField(
                value = commandText,
                onValueChange = { commandText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Enter command...") },
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                ),
                maxLines = 5,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send,
                    capitalization = KeyboardCapitalization.None,
                    autoCorrect = false
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (commandText.isNotBlank()) {
                            onCommandSubmit(commandText)
                            commandText = ""
                            focusManager.clearFocus()
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Send button
            FilledIconButton(
                onClick = {
                    if (commandText.isNotBlank()) {
                        onCommandSubmit(commandText)
                        commandText = ""
                        focusManager.clearFocus()
                    }
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, "Execute command")
            }
        }
    }
}
```

**Testing**:
- UI tests for text input
- Send button interaction
- Keyboard actions (Enter key)

---

**3.3 CommandBlocksScreen**

**File**: `app/src/main/kotlin/com/convocli/ui/screens/CommandBlocksScreen.kt`

**Design**:
```kotlin
@Composable
fun CommandBlocksScreen(
    viewModel: CommandBlockViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new block added
    LaunchedEffect(uiState.blocks.size) {
        if (uiState.blocks.isNotEmpty()) {
            listState.animateScrollToItem(uiState.blocks.size - 1)
        }
    }

    Scaffold(
        bottomBar = {
            CommandInputBar(
                onCommandSubmit = { command ->
                    viewModel.executeCommand(command)
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.blocks.isEmpty()) {
                // Empty state
                EmptyState()
            } else {
                // Command blocks list
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = uiState.blocks,
                        key = { it.id }
                    ) { block ->
                        CommandBlockCard(
                            block = block,
                            onCopyCommand = { viewModel.copyCommand(block.id) },
                            onCopyOutput = { viewModel.copyOutput(block.id) },
                            onRerun = { viewModel.rerunCommand(block.id) },
                            onEditAndRun = {
                                val command = viewModel.editAndRun(block.id)
                                // Populate input field with command
                            },
                            onCancel = { viewModel.cancelCommand(block.id) },
                            onToggleExpansion = { viewModel.toggleExpansion(block.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Terminal,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No commands yet",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Try: ls, pwd, or echo 'Hello World'",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Monospace
        )
    }
}
```

**Testing**:
- UI tests for empty state
- UI tests for list rendering
- Auto-scroll behavior
- Interaction tests

---

## Testing Strategy

### Unit Tests

**CommandBlock Data Model**:
- Property validation
- Serialization/deserialization
- Extension functions (formatting)

**PromptDetectorImpl**:
- Various prompt formats
- Edge cases (prompts in output, multi-line)
- False positive prevention

**AnsiColorParserImpl**:
- ANSI code parsing
- Color mapping
- Stripping codes
- Complex formatting

**CommandBlockManagerImpl**:
- Block creation and lifecycle
- Prompt detection integration
- Output appending
- Concurrent access safety

**CommandBlockViewModel**:
- State transitions
- User actions (execute, copy, cancel)
- Integration with manager and terminal ViewModel

### Integration Tests

**Command Execution → Block Creation Flow**:
1. User submits command via UI
2. CommandBlockViewModel creates block via Manager
3. TerminalViewModel executes command
4. Output streams to Manager
5. Block updates in real-time
6. Prompt detected → block completes
7. UI updates to show completed block

**Multi-Block Interaction**:
- Execute multiple commands
- Verify chronological order
- Verify independent status tracking
- Verify scroll behavior

**Cancellation Flow**:
- Start long-running command
- Tap cancel button
- Verify SIGINT sent
- Verify block transitions to FAILURE
- Verify exit code 130 or 143

### Compose UI Tests

**CommandBlockCard**:
- Status indicator rendering
- Output expansion/collapse
- Action button interactions
- Long-press context menu

**CommandInputBar**:
- Text input
- Send button
- Keyboard actions
- Multi-line support

**CommandBlocksScreen**:
- Empty state display
- List rendering with multiple blocks
- Auto-scroll to newest block
- Input field integration

### Performance Tests

**Scroll Performance**:
- 50+ blocks with 100+ lines each
- Measure FPS during scroll
- Target: 60fps maintained

**Memory Usage**:
- 100 blocks with various output sizes
- Measure memory consumption
- Target: <50MB for 100 blocks

**Output Streaming**:
- Rapid output (1000+ lines/second)
- Verify throttling works
- Verify UI remains responsive

---

## Implementation Phases

### Phase 0: Research (2-3 hours)

**R0.1 - R0.5**: Complete all research tasks
- Output: research.md with decisions documented
- Blockers: None (can start immediately)

### Phase 1: Data Model & Services (6-8 hours)

**Day 1, Part 1** (3-4 hours):
- T1.1: CommandBlock data model + tests
- T1.2: PromptDetector interface + implementation + tests
- T1.3: AnsiColorParser interface + implementation + tests

**Day 1, Part 2** (3-4 hours):
- T1.4: CommandBlockManager interface
- T1.5: CommandBlockManagerImpl (core logic)
- T1.6: CommandBlockManagerImpl tests

### Phase 2: ViewModel Layer (3-4 hours)

**Day 2, Part 1** (3-4 hours):
- T2.1: CommandBlockViewModel implementation
- T2.2: CommandBlockViewModel tests
- T2.3: TerminalRepository cancellation enhancement (SIGINT)

### Phase 3: UI Components (5-6 hours)

**Day 2, Part 2** (2-3 hours):
- T3.1: CommandBlockCard composable
- T3.2: StatusIndicator composable
- T3.3: CommandBlockCard UI tests

**Day 3, Part 1** (3 hours):
- T3.4: CommandInputBar composable
- T3.5: CommandInputBar UI tests
- T3.6: CommandBlocksScreen composable
- T3.7: EmptyState composable
- T3.8: CommandBlocksScreen UI tests

### Phase 4: Integration & Polish (2-3 hours)

**Day 3, Part 2** (2-3 hours):
- T4.1: End-to-end integration testing
- T4.2: Performance testing and optimization
- T4.3: Accessibility review (TalkBack)
- T4.4: Bug fixes and polish
- T4.5: Documentation (KDoc completion)

---

## Dependencies & Prerequisites

### Upstream Dependencies (MUST be complete)

✅ **Feature 002: Terminal Emulator Core**
- TermuxTerminalRepository with observeOutput() and executeCommand()
- TerminalViewModel with state management
- OutputStreamProcessor for output stream handling
- Status: Complete (merged to main)

✅ **Feature 003: Bootstrap Installation**
- Functional bash shell and Linux environment
- Working directory tracking
- Status: Complete (merged to main)

### Parallel Work (Can develop concurrently)

None - This feature is self-contained

### Downstream Dependencies (Features that depend on this)

**Feature 005: Smart Command Cards** (Week 5, Month 2):
- Enhances CommandBlock with command type detection
- Adds icons based on command type
- Builds on CommandBlock data model

**Feature 006: Chat-Like Input Enhancement** (Week 6, Month 2):
- Enhances CommandInputBar with history swipe-up
- Integrates with CommandBlockViewModel

**Feature 007: Touch Gestures** (Week 7, Month 2):
- Adds swipe gestures to CommandBlockCard
- Swipe right → copy, swipe left → delete

---

## Risk Analysis & Mitigation

### Risk 1: Prompt Detection Accuracy

**Risk**: Shell prompts vary; detection may fail or produce false positives

**Likelihood**: Medium
**Impact**: High (blocks won't form correctly)

**Mitigation**:
- R0.1 research task documents common patterns
- Support configurable prompt patterns (Phase 2)
- Fallback: timeout-based completion if no prompt detected
- Test with default Termux bash (well-defined prompt)

**Contingency**: If accuracy <90%, add manual "New Command" button

---

### Risk 2: ANSI Parsing Complexity

**Risk**: ANSI escape sequences are complex; parser may fail on edge cases

**Likelihood**: Medium
**Impact**: Low (cosmetic issue, not functional)

**Mitigation**:
- R0.2 research catalogs common codes used
- Focus on basic 16 colors + formatting (covers 95% of cases)
- Gracefully degrade unknown codes (strip them)
- Test with real command output (ls, grep, git)

**Contingency**: Accept 5% edge case failure rate for MVP; enhance parser in Phase 2 if needed

---

### Risk 3: Performance with Large Output

**Risk**: Commands with thousands of lines may cause lag or memory issues

**Likelihood**: Medium
**Impact**: Medium (poor UX)

**Mitigation**:
- R0.3 research benchmarks performance
- Auto-collapse output >20 lines
- LazyColumn virtualization for efficient rendering
- Output throttling (max 60 updates/second)
- Test with npm install, large file cats

**Contingency**: If performance issues persist, add output line limit (e.g., keep last 10,000 lines)

---

### Risk 4: Signal Handling for Cancellation

**Risk**: SIGINT may not work correctly through PTY interface

**Likelihood**: Low (PTY supports signals)
**Impact**: High (users can't cancel commands)

**Mitigation**:
- R0.5 research verifies PTY signal support
- Test cancellation with long-running commands (sleep 1000)
- Termux already supports Ctrl+C, so PTY interface supports it

**Contingency**: If SIGINT fails, provide "Force Close Session" that recreates terminal session

---

### Risk 5: Integration with Existing Terminal

**Risk**: Bridging TermuxTerminalRepository to block paradigm may introduce bugs

**Likelihood**: Low (terminal layer is tested)
**Impact**: High (core functionality broken)

**Mitigation**:
- CommandBlockManager wraps existing repository
- Existing Feature 002 integration tests still pass
- Incremental rollout on feature branch
- Comprehensive integration tests

**Contingency**: Keep traditional terminal as fallback mode (out of scope for this feature)

---

## Timeline Estimate

**Total Duration**: 2-3 days (16-20 hours)

**Phase Breakdown**:
- Phase 0 (Research): 2-3 hours
- Phase 1 (Services): 6-8 hours
- Phase 2 (ViewModel): 3-4 hours
- Phase 3 (UI): 5-6 hours
- Phase 4 (Integration): 2-3 hours

**Critical Path**: Sequential (Research → Services → ViewModel → UI → Integration)

**Parallel Opportunities**:
- PromptDetector and AnsiColorParser can be developed in parallel
- UI components can be developed in parallel once ViewModel is done

---

## Success Criteria

### Functional Completeness

✅ All 13 functional requirements implemented (FR-1 through FR-13)
✅ All 4 user scenarios work end-to-end
✅ Command execution → block creation → output display → completion
✅ All block actions work: copy, re-run, edit, cancel, expand/collapse
✅ Cancellation sends SIGINT and marks block as FAILURE

### Performance Targets

✅ 60fps scroll performance with 50+ blocks
✅ New blocks appear within 16ms (60fps)
✅ Output updates within 100ms of emission
✅ Memory: <50MB for 100 blocks
✅ UI remains responsive during command execution

### Quality Gates

✅ 80%+ code coverage (ViewModels and services)
✅ All unit tests pass
✅ All integration tests pass
✅ All Compose UI tests pass
✅ No memory leaks detected (LeakCanary)
✅ ktlint passes (no formatting violations)
✅ Accessibility: TalkBack navigation works

### User Experience

✅ 90%+ users can distinguish command from output
✅ Touch interactions feel natural and responsive
✅ Material 3 theming applied correctly (light/dark mode)
✅ Error states are clear and actionable
✅ Empty state is welcoming and helpful

---

## Deliverables

### Code

1. **Data Models**: `CommandBlock.kt`, `CommandStatus.kt`
2. **Services**: `CommandBlockManagerImpl.kt`, `PromptDetectorImpl.kt`, `AnsiColorParserImpl.kt`
3. **ViewModels**: `CommandBlockViewModel.kt`
4. **UI Components**: `CommandBlockCard.kt`, `CommandInputBar.kt`, `CommandBlocksScreen.kt`
5. **Contracts**: Interfaces in `features/004-.../contracts/`

### Tests

1. **Unit Tests**: CommandBlock, PromptDetector, AnsiColorParser, CommandBlockManager, CommandBlockViewModel
2. **Integration Tests**: Command execution flow, cancellation flow
3. **Compose UI Tests**: CommandBlockCard, CommandInputBar, CommandBlocksScreen

### Documentation

1. **research.md**: Research findings from Phase 0
2. **data-model.md**: CommandBlock entity specification
3. **contracts/**: Interface contracts for services
4. **quickstart.md**: How to use Command Blocks UI

### Artifacts

1. **Implementation Plan**: This document (plan.md)
2. **Task List**: Generated in tasks.md (next step)
3. **Test Coverage Report**: Generated after implementation
4. **Performance Benchmarks**: Scroll FPS, memory usage

---

**Next Step**: `/specswarm:tasks` to generate executable task breakdown

**Status**: ✅ Planning Complete - Ready for Task Generation
