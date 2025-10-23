# Implementation Tasks: Command Blocks UI

<!-- Tech Stack Validation: PASSED -->
<!-- Validated against: /memory/tech-stack.md v1.0.0 -->
<!-- No prohibited technologies found -->
<!-- 0 unapproved technologies require runtime validation -->

**Feature ID**: 004
**Feature**: Command Blocks UI
**Created**: 2025-10-22
**Status**: Ready for Implementation
**Total Estimated Effort**: 16-20 hours (2-3 days)

---

## Task Summary

**Total Tasks**: 45
**Phases**: 7
- Phase 1: Setup (1 task)
- Phase 2: Foundational (12 tasks)
- Phase 3: Scenario 1 - Execute Simple Command (9 tasks)
- Phase 4: Scenario 2 - Multiple Commands (6 tasks)
- Phase 5: Scenario 3 - Block Actions (7 tasks)
- Phase 6: Scenario 4 - Long-Running Commands (6 tasks)
- Phase 7: Polish & Integration (4 tasks)

**Parallel Opportunities**: 15+ tasks can run in parallel within phases

---

## Implementation Strategy

### User Story Priority Order

This feature implements 4 user scenarios in priority order:

1. **[S1] Scenario 1: Execute Simple Command** (P1 - MVP)
   - Goal: Basic command execution with visual feedback
   - Test Criteria: User can type command, see output in card, verify success/failure
   - Blocking: None (foundational scenario)

2. **[S2] Scenario 2: Multiple Commands** (P2)
   - Goal: Command history with efficient scrolling
   - Test Criteria: Execute 50+ commands, scroll smoothly, expand/collapse works
   - Blocking: Requires S1 complete

3. **[S3] Scenario 3: Block Actions** (P3)
   - Goal: Interact with command results (copy, re-run, edit)
   - Test Criteria: All 5 actions work reliably
   - Blocking: Requires S1 complete

4. **[S4] Scenario 4: Long-Running Commands** (P4)
   - Goal: Cancel long-running commands
   - Test Criteria: Can cancel builds/installs, SIGINT works
   - Blocking: Requires S1 complete

### MVP Scope

**Minimum Viable Product** = Foundational + Scenario 1
- Estimated effort: ~10 hours
- Delivers core value: command blocks UI with basic execution
- Allows early testing and feedback

---

## Phase 1: Setup

**Purpose**: Project-level configuration

### T001: Create feature directory structure [Setup]
**File**: N/A (directory creation)
**Description**: Create all required directories for Feature 004
**Steps**:
1. Create `app/src/main/kotlin/com/convocli/data/model/`
2. Create `app/src/main/kotlin/com/convocli/terminal/util/`
3. Create `app/src/main/kotlin/com/convocli/terminal/service/`
4. Create `app/src/main/kotlin/com/convocli/ui/components/`
5. Create `app/src/test/kotlin/com/convocli/data/model/`
6. Create `app/src/test/kotlin/com/convocli/terminal/`
7. Create `app/src/test/kotlin/com/convocli/ui/`
8. Create `app/src/androidTest/kotlin/com/convocli/ui/`

**Acceptance**: All directories exist and are tracked in git

**Estimated Effort**: 5 minutes

---

## Phase 2: Foundational Tasks

**Purpose**: Core infrastructure that ALL scenarios depend on
**Must Complete Before**: Any scenario implementation can begin

### T002: [Research] R0.1 - Shell Prompt Detection Patterns [Foundational]
**File**: `features/004-command-blocks-ui/research.md`
**Description**: Research common shell prompt formats in Termux/bash
**Steps**:
1. Test default Termux bash prompt format
2. Identify common PS1 customizations
3. Catalog prompt patterns: `$ `, `# `, `user@host:~$ `, `> `
4. Create regex patterns for each
5. Document findings in research.md

**Acceptance**:
- List of 5+ prompt patterns documented
- Regex patterns cover 95%+ of default configurations
- Patterns tested against real Termux output

**Estimated Effort**: 30 minutes

---

### T003: [Research] R0.2 - ANSI Color Code Parsing [Foundational]
**File**: `features/004-command-blocks-ui/research.md`
**Description**: Catalog ANSI escape sequences used in command output
**Steps**:
1. Run ls, grep, git, npm commands in Termux
2. Capture output and identify ANSI codes used
3. Document basic 16 colors (30-37, 90-97)
4. Document formatting codes (bold=1, italic=3, underline=4)
5. Map ANSI colors to Material 3 palette
6. Document unsupported sequences to strip

**Acceptance**:
- Basic 16 colors cataloged
- Bold/italic/underline codes documented
- Material 3 color mapping defined
- Handles 95%+ of common command output

**Estimated Effort**: 45 minutes

---

### T004: [Research] R0.3 - Material 3 Card Performance [Foundational] [P]
**File**: `features/004-command-blocks-ui/research.md`
**Description**: Benchmark Material 3 Card performance with long content
**Steps**:
1. Create prototype Compose app with LazyColumn
2. Generate 50 test CommandBlock items with 100+ lines each
3. Measure scroll FPS using Android Profiler
4. Test with 1000-line outputs
5. Document performance characteristics
6. Identify optimization strategies if needed

**Acceptance**:
- 60fps scroll maintained with 50+ blocks
- Performance benchmarks documented
- Optimization strategies noted (if needed)

**Estimated Effort**: 1 hour

---

### T005: [Research] R0.4 - Command Boundary Edge Cases [Foundational] [P]
**File**: `features/004-command-blocks-ui/research.md`
**Description**: Identify edge cases for prompt detection
**Steps**:
1. Test multi-line commands (heredocs, backslash continuations)
2. Test commands that output prompts in strings
3. Test custom PS1 configurations
4. Test commands with no output
5. Document edge cases and handling strategies

**Acceptance**:
- 10+ edge cases identified and documented
- Graceful degradation strategy for each
- No false positives in test cases

**Estimated Effort**: 45 minutes

---

### T006: [Research] R0.5 - SIGINT Signal Handling [Foundational]
**File**: `features/004-command-blocks-ui/research.md`
**Description**: Verify PTY interface supports SIGINT for cancellation
**Steps**:
1. Review Termux PTY implementation code
2. Test sending SIGINT to running process
3. Test with long-running commands (sleep 1000, large builds)
4. Verify exit codes (130 for SIGINT, 143 for SIGTERM)
5. Document implementation approach

**Acceptance**:
- SIGINT successfully interrupts processes
- Exit codes documented
- Implementation approach defined
- Works with sleep, npm install, git clone

**Estimated Effort**: 45 minutes

---

### T007: Implement CommandBlock data model [Foundational]
**File**: `app/src/main/kotlin/com/convocli/data/model/CommandBlock.kt`
**Description**: Create immutable data class for CommandBlock entity
**Implementation**:
```kotlin
@Serializable
data class CommandBlock(
    val id: String,
    val command: String,
    val output: String,
    val status: CommandStatus,
    val timestamp: Long,
    val executionDuration: Long?,
    val exitCode: Int?,
    val workingDirectory: String,
    val isExpanded: Boolean = true
)

enum class CommandStatus {
    PENDING, EXECUTING, SUCCESS, FAILURE
}
```

**Dependencies**: T001 (directory structure)
**Acceptance**:
- Data class compiles without errors
- All properties have correct types
- Kotlinx Serialization annotations present
- Default value for isExpanded works

**Estimated Effort**: 15 minutes

---

### T008: Write CommandBlock unit tests [Foundational]
**File**: `app/src/test/kotlin/com/convocli/data/model/CommandBlockTest.kt`
**Description**: Test CommandBlock data class
**Test Cases**:
1. Data class properties
2. Kotlinx Serialization encode/decode
3. Default values (isExpanded = true)
4. CommandStatus enum values

**Dependencies**: T007
**Acceptance**: All tests pass, 100% coverage of CommandBlock

**Estimated Effort**: 20 minutes

---

### T009: Implement PromptDetector interface [Foundational]
**File**: `app/src/main/kotlin/com/convocli/terminal/util/PromptDetector.kt`
**Description**: Create interface for shell prompt detection
**Implementation**: See plan.md section "PromptDetector Interface"

**Dependencies**: T001, T002 (research complete)
**Acceptance**: Interface compiles, matches contract in plan.md

**Estimated Effort**: 10 minutes

---

### T010: Implement PromptDetectorImpl [Foundational]
**File**: `app/src/main/kotlin/com/convocli/terminal/util/PromptDetectorImpl.kt`
**Description**: Implement prompt detection logic
**Implementation**: See plan.md section "1.2 PromptDetector Implementation"
**Steps**:
1. Implement detectsPrompt() using patterns from R0.1
2. Implement getPromptPatterns() returning default patterns
3. Implement stripPrompt() to remove trailing prompt
4. Use patterns: `^[$#]\s`, `^\w+@[\w-]+:[~/].*[$#]\s`, `^>\s`

**Dependencies**: T009, T002 (research)
**Acceptance**:
- All interface methods implemented
- Uses patterns from research
- Handles common prompt formats

**Estimated Effort**: 30 minutes

---

### T011: Write PromptDetector unit tests [Foundational]
**File**: `app/src/test/kotlin/com/convocli/terminal/util/PromptDetectorTest.kt`
**Description**: Test prompt detection logic
**Test Cases**:
1. Detects `$ ` prompt
2. Detects `# ` prompt
3. Detects `user@host:~$ ` prompt
4. Detects `> ` prompt
5. stripPrompt() removes trailing prompt correctly
6. Edge cases from R0.4 (prompts in output, multi-line)
7. False positive prevention

**Dependencies**: T010
**Acceptance**: All tests pass, 100% coverage, edge cases handled

**Estimated Effort**: 30 minutes

---

### T012: Implement AnsiColorParser interface [Foundational] [P]
**File**: `app/src/main/kotlin/com/convocli/terminal/util/AnsiColorParser.kt`
**Description**: Create interface for ANSI code parsing
**Implementation**: See plan.md section "AnsiColorParser Interface"

**Dependencies**: T001, T003 (research complete)
**Acceptance**: Interface compiles, matches contract in plan.md

**Estimated Effort**: 10 minutes

---

### T013: Implement AnsiColorParserImpl [Foundational] [P]
**File**: `app/src/main/kotlin/com/convocli/terminal/util/AnsiColorParserImpl.kt`
**Description**: Parse ANSI codes and convert to Material 3 styled text
**Implementation**: See plan.md section "1.3 AnsiColorParser Implementation"
**Steps**:
1. Implement parse() to convert ANSI to AnnotatedString
2. Handle color codes 30-37 (basic) and 90-97 (bright)
3. Handle formatting: bold (1), italic (3), underline (4)
4. Implement stripAnsiCodes() using regex
5. Implement mapColorCode() using Material 3 palette

**Dependencies**: T012, T003 (research)
**Acceptance**:
- Parses basic ANSI colors correctly
- Handles bold, italic, underline
- Strips codes cleanly
- Maps to Material 3 colors

**Estimated Effort**: 1 hour

---

### T014: Write AnsiColorParser unit tests [Foundational] [P]
**File**: `app/src/test/kotlin/com/convocli/terminal/util/AnsiColorParserTest.kt`
**Description**: Test ANSI parsing logic
**Test Cases**:
1. Parse basic colors (red, green, blue, etc.)
2. Parse bright colors
3. Parse bold text
4. Parse italic text
5. Parse combined (bold + color)
6. Strip ANSI codes completely
7. mapColorCode returns correct Material 3 colors
8. Handle unknown/unsupported codes gracefully

**Dependencies**: T013
**Acceptance**: All tests pass, 100% coverage, handles edge cases

**Estimated Effort**: 45 minutes

---

### T015: Implement CommandBlockManager interface [Foundational]
**File**: `app/src/main/kotlin/com/convocli/terminal/service/CommandBlockManager.kt`
**Description**: Create interface for command block lifecycle management
**Implementation**: See plan.md section "CommandBlockManager Interface"

**Dependencies**: T007 (CommandBlock model exists)
**Acceptance**: Interface compiles, matches contract in plan.md

**Estimated Effort**: 15 minutes

---

### T016: Implement CommandBlockManagerImpl [Foundational]
**File**: `app/src/main/kotlin/com/convocli/terminal/service/CommandBlockManagerImpl.kt`
**Description**: Implement command block lifecycle orchestration
**Implementation**: See plan.md section "1.4 CommandBlockManager Implementation"
**Steps**:
1. Create @Singleton class with @Inject constructor
2. Maintain MutableStateFlow<List<CommandBlock>>
3. Implement createBlock() to add new block
4. Implement appendOutput() to update active block
5. Implement markExecuting() to transition PENDING → EXECUTING
6. Implement completeBlock() to transition to SUCCESS/FAILURE
7. Implement cancelBlock() for cancellation
8. Implement toggleExpansion() for UI state
9. Listen to TermuxTerminalRepository.observeOutput()
10. Detect prompts using PromptDetector

**Dependencies**: T015, T010 (PromptDetector), existing TermuxTerminalRepository
**Acceptance**:
- All interface methods implemented
- Thread-safe block updates
- Integrates with TermuxTerminalRepository
- Uses PromptDetector for boundaries

**Estimated Effort**: 2 hours

---

### T017: Write CommandBlockManager unit tests [Foundational]
**File**: `app/src/test/kotlin/com/convocli/terminal/service/CommandBlockManagerTest.kt`
**Description**: Test command block lifecycle
**Test Cases**:
1. createBlock() adds block to list
2. markExecuting() transitions status correctly
3. appendOutput() updates block output
4. completeBlock() sets exit code and duration
5. cancelBlock() transitions to FAILURE
6. toggleExpansion() toggles isExpanded flag
7. observeBlocks() emits updated list on changes
8. Prompt detection triggers block completion
9. Concurrent access is thread-safe

**Dependencies**: T016
**Test Strategy**: Use fake TermuxTerminalRepository

**Acceptance**: All tests pass, 80%+ coverage

**Estimated Effort**: 1.5 hours

---

### T018: Implement CommandBlockViewModel [Foundational]
**File**: `app/src/main/kotlin/com/convocli/ui/viewmodels/CommandBlockViewModel.kt`
**Description**: Create ViewModel for UI state management
**Implementation**: See plan.md section "2.1 CommandBlockViewModel"
**Steps**:
1. Create @HiltViewModel with @Inject constructor
2. Define CommandBlocksUiState data class
3. Create MutableStateFlow<CommandBlocksUiState>
4. Observe CommandBlockManager.observeBlocks()
5. Observe TerminalViewModel.isExecuting
6. Observe TerminalViewModel.currentDirectory
7. Implement executeCommand() to create block and execute
8. Implement copyCommand(), copyOutput(), rerunCommand()
9. Implement editAndRun(), cancelCommand(), toggleExpansion()

**Dependencies**: T016 (CommandBlockManager), existing TerminalViewModel
**Acceptance**:
- ViewModel compiles
- All user actions implemented
- StateFlow exposed to UI
- Integrates with both managers

**Estimated Effort**: 1.5 hours

---

### T019: Write CommandBlockViewModel unit tests [Foundational]
**File**: `app/src/test/kotlin/com/convocli/ui/viewmodels/CommandBlockViewModelTest.kt`
**Description**: Test ViewModel state transitions and user actions
**Test Cases**:
1. executeCommand() creates block and delegates to TerminalViewModel
2. State updates when blocks change
3. copyCommand() action works
4. copyOutput() action works
5. rerunCommand() executes same command
6. editAndRun() returns command text
7. cancelCommand() cancels and marks failed
8. toggleExpansion() toggles block state
9. isExecuting state syncs with TerminalViewModel

**Dependencies**: T018
**Test Strategy**: Use fake CommandBlockManager and TerminalViewModel

**Acceptance**: All tests pass, 80%+ coverage

**Estimated Effort**: 1 hour

---

**Checkpoint**: Foundational Phase Complete
- All core services implemented and tested
- Ready to build UI for Scenario 1

---

## Phase 3: Scenario 1 - Execute Simple Command (MVP)

**Goal**: User can type a command, execute it, and see output in a Material 3 card
**Test Criteria**:
- User types "ls -la", taps send
- Command block appears with command text
- Output displays in card below command
- Success indicator shows (green checkmark)
- Timestamp displays correctly

### T020: Implement StatusIndicator composable [S1]
**File**: `app/src/main/kotlin/com/convocli/ui/components/StatusIndicator.kt`
**Description**: Composable for status icons
**Implementation**: See plan.md section "StatusIndicator" in CommandBlockCard
**Steps**:
1. Create @Composable function
2. Map CommandStatus → (Icon, Color):
   - PENDING → Schedule icon, onSurfaceVariant
   - EXECUTING → HourglassEmpty, primary
   - SUCCESS → CheckCircle, Green
   - FAILURE → Error, Red
3. Display Icon with appropriate tint

**Dependencies**: Phase 2 complete
**Acceptance**: All 4 statuses render correctly, accessible

**Estimated Effort**: 20 minutes

---

### T021: Implement CommandBlockCard composable [S1]
**File**: `app/src/main/kotlin/com/convocli/ui/components/CommandBlockCard.kt`
**Description**: Material 3 Card displaying single command block
**Implementation**: See plan.md section "3.1 CommandBlockCard"
**Steps**:
1. Create Material 3 Card with elevation
2. Display command text in monospace font
3. Display timestamp (relative format)
4. Display StatusIndicator
5. Display output text (monospace)
6. Display execution duration when available
7. Add action buttons row (placeholder for now)
8. Add long-press detection for context menu (future)

**Dependencies**: T020 (StatusIndicator)
**Acceptance**:
- Card renders command and output
- Status indicator shows correctly
- Timestamp and duration display
- Material 3 styling applied

**Estimated Effort**: 1.5 hours

---

### T022: Write CommandBlockCard UI tests [S1]
**File**: `app/src/androidTest/kotlin/com/convocli/ui/components/CommandBlockCardTest.kt`
**Description**: Compose UI tests for CommandBlockCard
**Test Cases**:
1. Card displays command text
2. Card displays output text
3. Status indicator shows for each status
4. Timestamp renders correctly
5. Execution duration renders when present
6. PENDING state shows loading indicator
7. EXECUTING state shows animated indicator
8. SUCCESS state shows green checkmark
9. FAILURE state shows red error icon

**Dependencies**: T021
**Acceptance**: All tests pass, visual states verified

**Estimated Effort**: 45 minutes

---

### T023: Implement CommandInputBar composable [S1]
**File**: `app/src/main/kotlin/com/convocli/ui/components/CommandInputBar.kt`
**Description**: Fixed bottom input field for command entry
**Implementation**: See plan.md section "3.2 CommandInputBar"
**Steps**:
1. Create Surface with elevation (fixed bottom)
2. Add TextField with monospace font
3. Support multi-line input (maxLines = 5)
4. Add Send IconButton
5. Handle keyboard actions (ImeAction.Send)
6. Clear input after submit
7. Callback onCommandSubmit(String)

**Dependencies**: Phase 2 complete
**Acceptance**:
- Input field accepts text
- Send button triggers callback
- Enter key submits command
- Input clears after submit
- Multi-line support works

**Estimated Effort**: 1 hour

---

### T024: Write CommandInputBar UI tests [S1]
**File**: `app/src/androidTest/kotlin/com/convocli/ui/components/CommandInputBarTest.kt`
**Description**: Compose UI tests for input bar
**Test Cases**:
1. TextField accepts text input
2. Send button is clickable
3. Clicking send triggers callback with text
4. Enter key triggers send action
5. Input clears after submission
6. Multi-line input works

**Dependencies**: T023
**Acceptance**: All tests pass, interactions verified

**Estimated Effort**: 30 minutes

---

### T025: Implement EmptyState composable [S1]
**File**: `app/src/main/kotlin/com/convocli/ui/components/EmptyState.kt`
**Description**: Welcoming empty state when no commands exist
**Implementation**: See plan.md section "EmptyState" in CommandBlocksScreen
**Steps**:
1. Center-aligned Column
2. Terminal icon (64dp, tinted)
3. "No commands yet" title
4. Example commands text (monospace)
5. Material 3 typography and colors

**Dependencies**: None (can be parallel with other UI)
**Acceptance**:
- Renders centered content
- Material 3 styling
- Helpful example commands shown

**Estimated Effort**: 20 minutes

---

### T026: Implement CommandBlocksScreen composable [S1]
**File**: `app/src/main/kotlin/com/convocli/ui/screens/CommandBlocksScreen.kt`
**Description**: Main screen with LazyColumn of command blocks
**Implementation**: See plan.md section "3.3 CommandBlocksScreen"
**Steps**:
1. Create @Composable function with hiltViewModel()
2. Collect uiState from ViewModel
3. Create Scaffold with bottomBar (CommandInputBar)
4. Show EmptyState when blocks.isEmpty()
5. Show LazyColumn with items(blocks, key = { it.id })
6. Render CommandBlockCard for each block
7. Wire up all callbacks (copy, rerun, edit, cancel, toggle)
8. Auto-scroll to bottom when new block added

**Dependencies**: T021 (CommandBlockCard), T023 (CommandInputBar), T025 (EmptyState), T018 (ViewModel)
**Acceptance**:
- Screen renders correctly
- Empty state shows when no blocks
- LazyColumn renders blocks
- Input bar fixed at bottom
- Auto-scroll works

**Estimated Effort**: 1.5 hours

---

### T027: Write CommandBlocksScreen UI tests [S1]
**File**: `app/src/androidTest/kotlin/com/convocli/ui/screens/CommandBlocksScreenTest.kt`
**Description**: Compose UI tests for main screen
**Test Cases**:
1. Empty state displays when no commands
2. LazyColumn renders blocks
3. Input bar is always visible
4. Typing and sending command creates block
5. Auto-scroll to newest block works
6. Multiple blocks render in order

**Dependencies**: T026
**Acceptance**: All tests pass, end-to-end flow verified

**Estimated Effort**: 1 hour

---

### T028: Integration test: Command execution → Block creation [S1]
**File**: `app/src/androidTest/kotlin/com/convocli/integration/CommandExecutionFlowTest.kt`
**Description**: End-to-end test of command execution creating blocks
**Test Flow**:
1. Launch CommandBlocksScreen
2. Type "echo 'Hello World'" in input
3. Tap send button
4. Verify CommandBlock appears with EXECUTING status
5. Wait for output to stream
6. Verify block transitions to SUCCESS
7. Verify output contains "Hello World"
8. Verify timestamp and duration display

**Dependencies**: T026 (screen), T018 (ViewModel), T016 (Manager)
**Acceptance**: Full flow works end-to-end with real terminal

**Estimated Effort**: 1 hour

---

**Checkpoint**: Scenario 1 Complete (MVP Achieved)
- User can execute commands and see results in blocks
- Basic UI working end-to-end
- Ready to enhance with additional scenarios

---

## Phase 4: Scenario 2 - Multiple Commands (Command History)

**Goal**: Execute multiple commands, scroll through history, expand/collapse blocks
**Test Criteria**:
- Execute 50+ commands
- Scroll smoothly (60fps)
- Expand/collapse long output
- Performance remains good

### T029: Implement block expansion logic in CommandBlockCard [S2]
**File**: `app/src/main/kotlin/com/convocli/ui/components/CommandBlockCard.kt` (modify)
**Description**: Add expansion/collapse UI for long output
**Steps**:
1. Check if output.lines().size > 20
2. If collapsed: show first 10 lines + "..." + last 5 lines
3. Show TextButton "Show N lines" when collapsed
4. Show TextButton "Show less" when expanded
5. Call onToggleExpansion callback on button click

**Dependencies**: T021 (CommandBlockCard exists)
**Acceptance**:
- Long output collapses by default
- Show more/less buttons work
- Collapsed view shows first/last lines
- Line count displayed

**Estimated Effort**: 45 minutes

---

### T030: Add expansion tests to CommandBlockCard [S2]
**File**: `app/src/androidTest/kotlin/com/convocli/ui/components/CommandBlockCardTest.kt` (modify)
**Description**: Test expansion/collapse functionality
**Test Cases**:
1. Output > 20 lines: collapsed by default
2. "Show N lines" button appears when collapsed
3. Tapping button expands block
4. "Show less" button appears when expanded
5. Tapping collapses block
6. Collapsed view shows correct lines (first 10 + last 5)

**Dependencies**: T029
**Acceptance**: All expansion tests pass

**Estimated Effort**: 30 minutes

---

### T031: Optimize LazyColumn performance [S2]
**File**: `app/src/main/kotlin/com/convocli/ui/screens/CommandBlocksScreen.kt` (modify)
**Description**: Ensure smooth scrolling with many blocks
**Steps**:
1. Use stable keys for LazyColumn items: `key = { it.id }`
2. Use `remember` for expensive computations
3. Use `derivedStateOf` for derived state
4. Test with 50+ blocks (100+ lines each)
5. Measure FPS during scroll with Android Profiler

**Dependencies**: T026 (CommandBlocksScreen exists), T004 (research)
**Acceptance**:
- 60fps scroll with 50+ blocks
- No janky frames
- Memory usage stable

**Estimated Effort**: 45 minutes

---

### T032: Implement output throttling in CommandBlockManager [S2]
**File**: `app/src/main/kotlin/com/convocli/terminal/service/CommandBlockManagerImpl.kt` (modify)
**Description**: Throttle rapid output updates for performance
**Steps**:
1. Buffer output in StringBuilder
2. Flush buffer max 60 times/second (every 16ms)
3. Use `debounce(16)` on output flow
4. Test with rapid output commands (yes, cat large file)

**Dependencies**: T016 (CommandBlockManagerImpl exists)
**Acceptance**:
- Output updates throttled to ~60fps
- UI remains responsive during rapid output
- No output loss

**Estimated Effort**: 30 minutes

---

### T033: Performance test: 50+ blocks scrolling [S2]
**File**: `app/src/androidTest/kotlin/com/convocli/performance/ScrollPerformanceTest.kt`
**Description**: Benchmark scroll performance
**Test Steps**:
1. Create 50 CommandBlocks with 100+ lines each
2. Render in LazyColumn
3. Perform scroll gesture
4. Measure FPS using FrameMetrics or Profiler
5. Verify 60fps maintained

**Dependencies**: T031 (optimizations applied)
**Acceptance**:
- 60fps scroll performance
- No dropped frames
- Memory usage < 50MB

**Estimated Effort**: 45 minutes

---

### T034: Performance test: Memory usage with 100 blocks [S2]
**File**: `app/src/androidTest/kotlin/com/convocli/performance/MemoryUsageTest.kt`
**Description**: Verify memory efficiency
**Test Steps**:
1. Create 100 CommandBlocks (varying output sizes)
2. Render in LazyColumn
3. Measure memory consumption using Android Profiler
4. Verify no memory leaks with LeakCanary

**Dependencies**: T026 (screen exists)
**Acceptance**:
- Memory usage < 50MB for 100 blocks
- No memory leaks detected
- LazyColumn recycles off-screen items

**Estimated Effort**: 30 minutes

---

**Checkpoint**: Scenario 2 Complete
- Command history works smoothly
- Performance optimized for many blocks
- Expansion/collapse enhances readability

---

## Phase 5: Scenario 3 - Block Actions (Copy, Re-run, Edit)

**Goal**: Interact with command results without retyping
**Test Criteria**:
- All 5 actions work (copy command, copy output, copy all, re-run, edit)
- Visual confirmation for each action
- Actions are discoverable

### T035: Implement clipboard copy functionality [S3]
**File**: `app/src/main/kotlin/com/convocli/ui/viewmodels/CommandBlockViewModel.kt` (modify)
**Description**: Add clipboard integration for copy actions
**Steps**:
1. Inject ClipboardManager into ViewModel
2. Implement copyCommand() to copy command text only
3. Implement copyOutput() to copy output text only (strip ANSI)
4. Use AnsiColorParser.stripAnsiCodes() for plain text
5. Show toast/snackbar confirmation

**Dependencies**: T018 (ViewModel exists), T013 (AnsiColorParser)
**Acceptance**:
- Copy actions put text on clipboard
- ANSI codes stripped from output
- Confirmation shown to user

**Estimated Effort**: 30 minutes

---

### T036: Add action buttons to CommandBlockCard [S3]
**File**: `app/src/main/kotlin/com/convocli/ui/components/CommandBlockCard.kt` (modify)
**Description**: Add action buttons row to card
**Steps**:
1. Add Row with IconButtons at bottom of card
2. ContentCopy icon → onCopyCommand callback
3. Assignment icon → onCopyOutput callback
4. Refresh icon → onRerun callback
5. Edit icon → onEditAndRun callback
6. Set content descriptions for accessibility

**Dependencies**: T021 (CommandBlockCard exists)
**Acceptance**:
- 4 action buttons displayed
- Icons are clear and accessible
- Callbacks wired correctly
- Touch targets ≥ 48dp

**Estimated Effort**: 30 minutes

---

### T037: Implement re-run functionality [S3]
**File**: `app/src/main/kotlin/com/convocli/ui/viewmodels/CommandBlockViewModel.kt` (modify)
**Description**: Execute same command again when re-run tapped
**Steps**:
1. In rerunCommand(), get block by ID
2. Extract command text
3. Call executeCommand(block.command)
4. New block created with fresh execution

**Dependencies**: T018 (ViewModel exists)
**Acceptance**:
- Re-run executes command again
- New block appears
- Old block unchanged

**Estimated Effort**: 15 minutes

---

### T038: Implement edit & run functionality [S3]
**File**: `app/src/main/kotlin/com/convocli/ui/viewmodels/CommandBlockViewModel.kt` (modify)
**File**: `app/src/main/kotlin/com/convocli/ui/screens/CommandBlocksScreen.kt` (modify)
**Description**: Populate input field with command for editing
**Steps**:
1. In editAndRun(), return block.command
2. In CommandBlocksScreen, when editAndRun callback triggered:
   - Populate CommandInputBar with returned command
   - Focus input field
   - User can edit and submit

**Dependencies**: T018 (ViewModel), T026 (Screen)
**Acceptance**:
- Edit & run populates input field
- User can modify command
- Submitting executes modified command

**Estimated Effort**: 30 minutes

---

### T039: Add action button tests to CommandBlockCard [S3]
**File**: `app/src/androidTest/kotlin/com/convocli/ui/components/CommandBlockCardTest.kt` (modify)
**Description**: Test action button interactions
**Test Cases**:
1. Copy command button triggers callback
2. Copy output button triggers callback
3. Re-run button triggers callback
4. Edit & run button triggers callback
5. All buttons have proper content descriptions
6. Touch targets are at least 48dp

**Dependencies**: T036 (action buttons added)
**Acceptance**: All action tests pass, accessible

**Estimated Effort**: 30 minutes

---

### T040: Integration test: Copy and re-run flow [S3]
**File**: `app/src/androidTest/kotlin/com/convocli/integration/BlockActionsFlowTest.kt`
**Description**: End-to-end test of block actions
**Test Flow**:
1. Execute command "pwd"
2. Wait for completion
3. Tap "Copy Output" button
4. Verify clipboard contains output
5. Tap "Re-run" button
6. Verify new block created with same command
7. Tap "Edit & Run"
8. Verify input field populated
9. Modify command and submit
10. Verify new block with modified command

**Dependencies**: T035-T038 (all actions implemented)
**Acceptance**: Full action flow works end-to-end

**Estimated Effort**: 45 minutes

---

### T041: Add long-press context menu (optional enhancement) [S3]
**File**: `app/src/main/kotlin/com/convocli/ui/components/CommandBlockCard.kt` (modify)
**Description**: Alternative access to actions via long-press
**Steps**:
1. Add pointerInput modifier with detectTapGestures
2. On long press, show DropdownMenu with actions
3. Menu items: Copy Command, Copy Output, Copy All, Re-run, Edit & Run
4. Trigger same callbacks as action buttons

**Dependencies**: T036 (action buttons exist)
**Acceptance**:
- Long-press shows context menu
- All actions work via menu
- Menu dismisses correctly

**Estimated Effort**: 45 minutes

---

**Checkpoint**: Scenario 3 Complete
- All block actions implemented
- Users can copy, re-run, and edit commands
- Interaction model is intuitive

---

## Phase 6: Scenario 4 - Long-Running Commands (Cancellation)

**Goal**: Cancel long-running commands like builds or installations
**Test Criteria**:
- Cancel button appears on executing blocks
- Tapping cancel sends SIGINT
- Block transitions to FAILURE with "Cancelled by user"
- Exit code 130 or 143 shown

### T042: Implement SIGINT signal sending in TermuxTerminalRepository [S4]
**File**: `app/src/main/kotlin/com/convocli/terminal/repository/TermuxTerminalRepositoryImpl.kt` (modify)
**Description**: Add cancelCommand() method to send SIGINT
**Steps**:
1. Add cancelCommand() method to interface
2. Get current TerminalSession
3. Send SIGINT (signal 2) to process via PTY
4. Use TerminalSession.write("\u0003") or direct signal method
5. Verify with research findings from R0.5

**Dependencies**: T006 (research), existing TermuxTerminalRepository
**Acceptance**:
- cancelCommand() method implemented
- SIGINT sent to running process
- Process interrupts successfully

**Estimated Effort**: 45 minutes

---

### T043: Add Cancel button to CommandBlockCard [S4]
**File**: `app/src/main/kotlin/com/convocli/ui/components/CommandBlockCard.kt` (modify)
**Description**: Show Cancel button for EXECUTING blocks
**Steps**:
1. Check if block.status == CommandStatus.EXECUTING
2. If true, show Cancel IconButton next to status indicator
3. Wire to onCancel callback
4. Disable button once clicked (prevent multiple signals)
5. Show brief loading state during cancellation

**Dependencies**: T021 (CommandBlockCard exists)
**Acceptance**:
- Cancel button appears only for EXECUTING blocks
- Button triggers callback
- Visual feedback during cancellation

**Estimated Effort**: 30 minutes

---

### T044: Implement cancelBlock in CommandBlockManager [S4]
**File**: `app/src/main/kotlin/com/convocli/terminal/service/CommandBlockManagerImpl.kt` (modify)
**Description**: Handle block cancellation state
**Steps**:
1. Implement cancelBlock(blockId) method
2. Get block by ID
3. Update status to FAILURE
4. Set output to include "Cancelled by user" message
5. Set exitCode to 130 (SIGINT standard)
6. Calculate executionDuration from timestamp to now

**Dependencies**: T016 (CommandBlockManagerImpl exists)
**Acceptance**:
- cancelBlock transitions to FAILURE
- Exit code 130 set
- Cancellation message shown

**Estimated Effort**: 20 minutes

---

### T045: Wire cancelCommand in ViewModel [S4]
**File**: `app/src/main/kotlin/com/convocli/ui/viewmodels/CommandBlockViewModel.kt` (modify)
**Description**: Connect UI cancel action to signal sending
**Steps**:
1. In cancelCommand(blockId):
   - Call terminalRepository.cancelCommand() to send SIGINT
   - Call commandBlockManager.cancelBlock(blockId) to update state
2. Handle any errors gracefully

**Dependencies**: T042 (SIGINT in repo), T044 (cancelBlock in manager), T018 (ViewModel)
**Acceptance**:
- cancelCommand sends signal and updates block
- No crashes on cancellation

**Estimated Effort**: 15 minutes

---

### T046: Integration test: Cancel long-running command [S4]
**File**: `app/src/androidTest/kotlin/com/convocli/integration/CancellationFlowTest.kt`
**Description**: End-to-end cancellation test
**Test Flow**:
1. Execute "sleep 1000" command
2. Verify block status is EXECUTING
3. Verify Cancel button appears
4. Wait 2 seconds
5. Tap Cancel button
6. Verify SIGINT sent
7. Verify block transitions to FAILURE
8. Verify exit code 130 shown
9. Verify "Cancelled by user" message appears
10. Verify process actually stopped

**Dependencies**: T042-T045 (cancellation implemented)
**Acceptance**: Full cancellation flow works, process stops

**Estimated Effort**: 45 minutes

---

### T047: Add cancellation tests to CommandBlockManager [S4]
**File**: `app/src/test/kotlin/com/convocli/terminal/service/CommandBlockManagerTest.kt` (modify)
**Description**: Unit test cancellation logic
**Test Cases**:
1. cancelBlock sets status to FAILURE
2. cancelBlock sets exit code 130
3. cancelBlock includes "Cancelled" message
4. cancelBlock calculates duration correctly

**Dependencies**: T044 (cancelBlock implemented)
**Acceptance**: All cancellation unit tests pass

**Estimated Effort**: 20 minutes

---

**Checkpoint**: Scenario 4 Complete
- Command cancellation fully implemented
- Users can interrupt long-running commands
- SIGINT signal handling verified

---

## Phase 7: Polish & Integration

**Purpose**: Final testing, accessibility, documentation

### T048: End-to-end integration testing [Polish]
**File**: `app/src/androidTest/kotlin/com/convocli/integration/FullFeatureIntegrationTest.kt`
**Description**: Comprehensive integration test covering all scenarios
**Test Flow**:
1. Launch app
2. Execute simple command (S1)
3. Execute multiple commands (S2)
4. Expand/collapse blocks (S2)
5. Copy output (S3)
6. Re-run command (S3)
7. Edit & run (S3)
8. Execute long-running command (S4)
9. Cancel it (S4)
10. Verify all states and transitions

**Dependencies**: All scenarios complete
**Acceptance**: Full feature works end-to-end without errors

**Estimated Effort**: 1 hour

---

### T049: Accessibility review (TalkBack) [Polish]
**File**: N/A (manual testing + fixes)
**Description**: Verify screen reader support
**Steps**:
1. Enable TalkBack on Android device
2. Navigate through CommandBlocksScreen
3. Verify all interactive elements announced
4. Verify content descriptions present
5. Verify focus order is logical
6. Fix any accessibility issues found

**Dependencies**: All UI complete
**Acceptance**:
- TalkBack users can navigate all elements
- All buttons/actions announced clearly
- Focus order makes sense
- No accessibility violations

**Estimated Effort**: 45 minutes

---

### T050: Performance benchmarking and optimization [Polish]
**File**: `features/004-command-blocks-ui/performance-report.md`
**Description**: Final performance validation
**Benchmarks**:
1. Scroll FPS with 50+ blocks (target: 60fps)
2. Memory usage with 100 blocks (target: <50MB)
3. Command execution latency (target: <16ms to create block)
4. Output update latency (target: <100ms)
5. App startup time impact

**Dependencies**: All implementation complete
**Acceptance**:
- All performance targets met
- Benchmarks documented
- No performance regressions

**Estimated Effort**: 1 hour

---

### T051: Documentation and KDoc completion [Polish]
**File**: All implementation files
**Description**: Complete inline documentation
**Steps**:
1. Add KDoc to all public classes and methods
2. Document complex logic with inline comments
3. Update README if needed
4. Create `features/004-command-blocks-ui/quickstart.md` for users
5. Document known limitations

**Dependencies**: All implementation complete
**Acceptance**:
- All public APIs documented
- Complex logic explained
- User guide created

**Estimated Effort**: 1 hour

---

**Checkpoint**: Feature 004 Complete
- All 13 functional requirements implemented
- All 4 user scenarios working
- Performance targets met
- Accessible and documented

---

## Dependency Graph

```
Setup Phase (T001)
    ↓
Foundational Phase
    Research (T002-T006) [can run in parallel]
        ↓
    Core Models (T007-T008)
        ↓
    Utilities (T009-T014) [PromptDetector and AnsiColorParser in parallel]
        ↓
    Services (T015-T017)
        ↓
    ViewModel (T018-T019)
        ↓
Scenario 1 (T020-T028) [UI components can be parallel until screen integration]
    ↓
Scenario 2 (T029-T034) [builds on S1]
    ↓
Scenario 3 (T035-T041) [builds on S1]
    ↓
Scenario 4 (T042-T047) [builds on S1]
    ↓
Polish (T048-T051)
```

---

## Parallel Execution Opportunities

### Phase 2: Foundational
**Parallel Group 1** (Research - can all run simultaneously):
- T002: Prompt detection research
- T003: ANSI parsing research
- T004: Performance research
- T005: Edge cases research
- T006: SIGINT research

**Parallel Group 2** (Utilities - after research):
- T009-T011: PromptDetector (interface + impl + tests)
- T012-T014: AnsiColorParser (interface + impl + tests)

### Phase 3: Scenario 1
**Parallel Group 3** (UI components):
- T020: StatusIndicator
- T023: CommandInputBar
- T025: EmptyState

### Phase 5: Scenario 3
**Parallel Group 4** (Actions):
- T035: Clipboard functionality
- T036: Action buttons UI
- T037: Re-run logic
- T038: Edit & run logic

---

## Success Criteria Checklist

### Functional Completeness
- [ ] FR-1: CommandBlock data model ✓
- [ ] FR-2: Block display with all metadata ✓
- [ ] FR-3: Prompt detection and boundaries ✓
- [ ] FR-4: ANSI color parsing ✓
- [ ] FR-5: Scrollable history ✓
- [ ] FR-6: Block expansion/collapse ✓
- [ ] FR-7: All 5 block actions ✓
- [ ] FR-8: Status indicators ✓
- [ ] FR-9: Material 3 design ✓
- [ ] FR-10: Command input integration ✓
- [ ] FR-11: Empty state ✓
- [ ] FR-12: Performance optimization ✓
- [ ] FR-13: Command cancellation ✓

### Performance Targets
- [ ] 60fps scroll with 50+ blocks
- [ ] <16ms to create new block
- [ ] <100ms output update latency
- [ ] <50MB memory for 100 blocks
- [ ] UI responsive during execution

### Quality Gates
- [ ] 80%+ code coverage (services + ViewModels)
- [ ] All unit tests pass
- [ ] All integration tests pass
- [ ] All Compose UI tests pass
- [ ] No memory leaks (LeakCanary)
- [ ] ktlint passes
- [ ] TalkBack navigation works

### User Experience
- [ ] 90%+ distinguish command from output
- [ ] Touch interactions responsive
- [ ] Light/dark mode support
- [ ] Error states clear
- [ ] Empty state helpful

---

## Estimated Timeline

**Day 1** (8 hours):
- Phase 1: Setup (30 min)
- Phase 2: Foundational (6.5 hours)
  - Research: 3 hours
  - Models & Utilities: 2.5 hours
  - Services: 1 hour (partial)

**Day 2** (8 hours):
- Phase 2: Foundational (2 hours)
  - Services completion: 1 hour
  - ViewModel: 1 hour
- Phase 3: Scenario 1 (5 hours)
- Phase 4: Scenario 2 (1 hour partial)

**Day 3** (4-6 hours):
- Phase 4: Scenario 2 (1 hour)
- Phase 5: Scenario 3 (1.5 hours)
- Phase 6: Scenario 4 (1.5 hours)
- Phase 7: Polish (2 hours)

**Total**: 20-22 hours over 2.5-3 days

---

## Notes

- **MVP Completion**: After Phase 3 (Scenario 1), you have a working command blocks UI
- **Testing Strategy**: Tests written alongside implementation (not deferred)
- **Constitution Compliance**: All tasks follow coding standards, MVI pattern, Hilt DI
- **Tech Stack**: All technologies pre-approved in tech-stack.md v1.0.0
- **Risk Mitigation**: Research phase addresses all identified risks early

---

**Next Step**: Begin implementation with T001 (Setup)
**Ready to Start**: `/specswarm:implement` to execute tasks

**Status**: ✅ Tasks Generated - Ready for Implementation
