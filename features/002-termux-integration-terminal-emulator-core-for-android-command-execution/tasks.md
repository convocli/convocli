# Task Breakdown: Feature 002 - Termux Integration

<!-- Tech Stack Validation: PASSED -->
<!-- Validated against: .specswarm/tech-stack.md v1.0.0 -->
<!-- No prohibited technologies found -->
<!-- 0 unapproved technologies -->

**Feature**: Termux Integration - Terminal Emulator Core for Android Command Execution
**Status**: Ready for Implementation
**Created**: 2025-10-21
**Dependencies**: Feature 001 (Android Project Foundation)

---

## Overview

This document breaks down Feature 002 into actionable, independently testable tasks organized by user story. Each phase represents a complete, deliverable increment that can be tested independently.

**Total Tasks**: 47
**Estimated Duration**: 35-40 hours
**Implementation Strategy**: Incremental delivery by user story

---

## User Story Mapping

This feature enables the following user scenarios from spec.md:

| ID | User Story | Priority | Functional Requirements |
|----|-----------|----------|------------------------|
| US1 | Execute Basic Shell Commands | P1 (Critical) | FR-3, FR-4, FR-5 |
| US2 | Navigate Directory Structure | P1 (Critical) | FR-8 |
| US3 | View File Contents | P1 (Critical) | (Enabled by US1) |
| US4 | Handle Command Errors | P2 (High) | FR-9 |
| US5 | Interactive Learning Environment | P2 (High) | FR-7 |

**Foundational Requirements** (Block all user stories):
- FR-1: Termux Library Integration
- FR-2: PTY Setup
- FR-6: Hilt Dependency Injection Integration

---

## Phase 1: Setup (Project Initialization)

**Goal**: Prepare project for Termux integration

**Duration**: 1-2 hours

**Deliverable**: Project configured and ready for implementation

### T001: Research Termux Library Integration [P]

**Story**: Foundation
**File**: `features/002-.../research.md`
**Duration**: 1 hour

**Description**: Research and document Termux library integration approach.

**Tasks**:
1. Find official Termux terminal-emulator library repository
2. Identify Maven coordinates or GitHub releases
3. Determine exact version to use (stable release)
4. Review library API documentation
5. Verify GPLv3 license compliance
6. Check Android API compatibility (API 26+)
7. Document findings in `research.md`

**Acceptance Criteria**:
- [ ] `research.md` created with library details
- [ ] Maven coordinates or dependency path documented
- [ ] License compatibility verified
- [ ] API compatibility confirmed for API 26+

**Dependencies**: None

---

### T002: Validate PTY Implementation Approach [P]

**Story**: Foundation
**File**: `features/002-.../research.md`
**Duration**: 1 hour

**Description**: Review Termux PTY source code and validate approach.

**Tasks**:
1. Review Termux PTY native implementation (C code)
2. Understand JNI interface for PTY operations
3. Identify file descriptor management requirements
4. Document PTY lifecycle (create, read, write, close)
5. Note any Android-specific considerations
6. Add findings to `research.md`

**Acceptance Criteria**:
- [ ] PTY implementation approach documented
- [ ] JNI interface understood and documented
- [ ] Lifecycle management strategy defined
- [ ] Android-specific gotchas noted

**Dependencies**: None

---

### T003: Create Data Model Documentation

**Story**: Foundation
**File**: `features/002-.../data-model.md`
**Duration**: 30 min

**Description**: Document data entities and their relationships.

**Tasks**:
1. Create `data-model.md`
2. Document `TerminalSession` entity
3. Document `TerminalOutput` entity
4. Document `TerminalError` entity
5. Document `StreamType` enum
6. Add state diagrams for session lifecycle
7. Document relationships between entities

**Acceptance Criteria**:
- [ ] `data-model.md` created
- [ ] All entities documented with attributes
- [ ] State diagrams included
- [ ] Entity relationships clearly defined

**Dependencies**: None

---

## Phase 2: Foundation (Blocking Prerequisites)

**Goal**: Establish core terminal infrastructure required by all user stories

**Duration**: 6-8 hours

**Deliverable**: Terminal session can be created and destroyed

**Checkpoint**: ✅ After this phase, terminal session lifecycle works end-to-end

### T004: Add Termux Dependencies to build.gradle.kts

**Story**: Foundation (FR-1)
**File**: `app/build.gradle.kts`
**Duration**: 30 min

**Description**: Add Termux terminal-emulator library as Gradle dependency.

**Tasks**:
1. Open `app/build.gradle.kts`
2. Add Termux library dependencies (from research.md):
   ```kotlin
   implementation("com.termux:termux-shared:VERSION")
   implementation("com.termux.terminal:terminal-emulator:VERSION")
   implementation("com.termux.terminal:terminal-view:VERSION")
   ```
3. Configure native library inclusion (JNI)
4. Add ABI filters for native libraries:
   ```kotlin
   ndk {
       abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
   }
   ```
5. Sync Gradle
6. Verify compilation succeeds

**Acceptance Criteria**:
- [ ] Termux dependencies added to `build.gradle.kts`
- [ ] Native library configuration added
- [ ] Gradle sync successful
- [ ] Project compiles without errors
- [ ] Native libraries included in APK

**Dependencies**: T001

---

### T005: Add Storage Permissions to AndroidManifest.xml

**Story**: Foundation (FR-1)
**File**: `app/src/main/AndroidManifest.xml`
**Duration**: 15 min

**Description**: Add storage permissions for terminal file access.

**Tasks**:
1. Open `AndroidManifest.xml`
2. Add permissions:
   ```xml
   <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
   <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
                    android:maxSdkVersion="28" />
   ```
3. Document that runtime permission request will be implemented later
4. Verify manifest validates

**Acceptance Criteria**:
- [ ] Storage permissions declared in manifest
- [ ] Manifest validates without errors
- [ ] Permissions scoped correctly (maxSdkVersion)

**Dependencies**: None

---

### T006: Create TerminalSession Data Model

**Story**: Foundation (FR-2, FR-3)
**File**: `app/src/main/kotlin/com/convocli/terminal/model/TerminalSession.kt`
**Duration**: 30 min

**Description**: Define data class for terminal session metadata.

**Tasks**:
1. Create `terminal/model/` directory
2. Create `TerminalSession.kt`
3. Define data class:
   ```kotlin
   data class TerminalSession(
       val sessionId: String,
       val shellPath: String,
       val workingDirectory: String,
       val environment: Map<String, String>,
       val state: SessionState,
       val createdAt: Long
   )

   enum class SessionState {
       RUNNING, STOPPED, ERROR
   }
   ```
4. Add KDoc comments
5. Verify compilation

**Acceptance Criteria**:
- [ ] `TerminalSession.kt` created
- [ ] Data class defined with all required attributes
- [ ] `SessionState` enum defined
- [ ] KDoc comments added
- [ ] File compiles without errors

**Dependencies**: None

---

### T007: Create TerminalOutput Data Model [P]

**Story**: Foundation (FR-5)
**File**: `app/src/main/kotlin/com/convocli/terminal/model/TerminalOutput.kt`
**Duration**: 15 min

**Description**: Define data class for terminal output events.

**Tasks**:
1. Create `TerminalOutput.kt` in `terminal/model/`
2. Define data class:
   ```kotlin
   data class TerminalOutput(
       val text: String,
       val stream: StreamType,
       val timestamp: Long
   )
   ```
3. Add KDoc comments
4. Verify compilation

**Acceptance Criteria**:
- [ ] `TerminalOutput.kt` created
- [ ] Data class defined with text, stream, timestamp
- [ ] KDoc comments added
- [ ] File compiles

**Dependencies**: None

---

### T008: Create StreamType Enum [P]

**Story**: Foundation (FR-5)
**File**: `app/src/main/kotlin/com/convocli/terminal/model/StreamType.kt`
**Duration**: 10 min

**Description**: Define enum for stdout/stderr distinction.

**Tasks**:
1. Create `StreamType.kt` in `terminal/model/`
2. Define enum:
   ```kotlin
   enum class StreamType {
       STDOUT, STDERR
   }
   ```
3. Add KDoc comments
4. Verify compilation

**Acceptance Criteria**:
- [ ] `StreamType.kt` created
- [ ] Enum has STDOUT and STDERR values
- [ ] KDoc comments added
- [ ] File compiles

**Dependencies**: None

---

### T009: Create TerminalError Sealed Class [P]

**Story**: Foundation (FR-9)
**File**: `app/src/main/kotlin/com/convocli/terminal/model/TerminalError.kt`
**Duration**: 20 min

**Description**: Define sealed class hierarchy for error types.

**Tasks**:
1. Create `TerminalError.kt` in `terminal/model/`
2. Define sealed class:
   ```kotlin
   sealed class TerminalError {
       data class InitializationFailed(val reason: String) : TerminalError()
       data class CommandFailed(
           val command: String,
           val exitCode: Int,
           val stderr: String
       ) : TerminalError()
       data class SessionCrashed(val reason: String) : TerminalError()
       data class IOError(val message: String) : TerminalError()
   }
   ```
3. Add KDoc for each error type
4. Verify compilation

**Acceptance Criteria**:
- [ ] `TerminalError.kt` created
- [ ] Sealed class with 4 error types defined
- [ ] KDoc comments for each type
- [ ] File compiles

**Dependencies**: None

---

### T010: Create TerminalRepository Interface

**Story**: Foundation (FR-3)
**File**: `app/src/main/kotlin/com/convocli/terminal/repository/TerminalRepository.kt`
**Duration**: 30 min

**Description**: Define repository contract for terminal operations.

**Tasks**:
1. Create `terminal/repository/` directory
2. Create `TerminalRepository.kt`
3. Define interface:
   ```kotlin
   interface TerminalRepository {
       suspend fun createSession(): Result<String>
       suspend fun executeCommand(sessionId: String, command: String)
       fun observeOutput(sessionId: String): Flow<TerminalOutput>
       fun observeErrors(): SharedFlow<TerminalError>
       suspend fun destroySession(sessionId: String)
       fun getSessionState(sessionId: String): SessionState?
   }
   ```
4. Add comprehensive KDoc
5. Import Result, Flow, SharedFlow

**Acceptance Criteria**:
- [ ] `TerminalRepository.kt` interface created
- [ ] All methods defined with correct signatures
- [ ] Returns `Result` for fallible operations
- [ ] Uses `Flow` for output streaming
- [ ] Comprehensive KDoc added
- [ ] File compiles

**Dependencies**: T006, T007, T009

---

### T011: Create TerminalModule for Hilt DI

**Story**: Foundation (FR-6)
**File**: `app/src/main/kotlin/com/convocli/di/TerminalModule.kt`
**Duration**: 20 min

**Description**: Create Hilt module to provide TerminalRepository.

**Tasks**:
1. Create `TerminalModule.kt` in `di/` directory
2. Define Hilt module:
   ```kotlin
   @Module
   @InstallIn(SingletonComponent::class)
   object TerminalModule {
       @Provides
       @Singleton
       fun provideTerminalRepository(
           @ApplicationContext context: Context
       ): TerminalRepository = TermuxTerminalRepository(context)
   }
   ```
3. Add necessary imports (Hilt, Dagger, Context)
4. Verify compilation

**Acceptance Criteria**:
- [ ] `TerminalModule.kt` created
- [ ] `@Module` and `@InstallIn` annotations added
- [ ] `provideTerminalRepository` method provides singleton
- [ ] Injects `@ApplicationContext`
- [ ] File compiles

**Dependencies**: T010

---

### T012: Implement TermuxTerminalRepository (Stub)

**Story**: Foundation (FR-3)
**File**: `app/src/main/kotlin/com/convocli/terminal/repository/TermuxTerminalRepository.kt`
**Duration**: 1 hour

**Description**: Create stub implementation of TerminalRepository (no Termux integration yet).

**Tasks**:
1. Create `TermuxTerminalRepository.kt` in `terminal/repository/`
2. Implement interface with stubs:
   ```kotlin
   class TermuxTerminalRepository(
       private val context: Context
   ) : TerminalRepository {

       private val sessions = mutableMapOf<String, TerminalSession>()
       private val _output = MutableSharedFlow<TerminalOutput>()
       private val _errors = MutableSharedFlow<TerminalError>()

       override suspend fun createSession(): Result<String> {
           // Stub: Return fake session ID
           val sessionId = UUID.randomUUID().toString()
           sessions[sessionId] = TerminalSession(...)
           return Result.success(sessionId)
       }

       // ... stub other methods
   }
   ```
3. Add KDoc comments
4. Verify compilation and injection works

**Acceptance Criteria**:
- [ ] `TermuxTerminalRepository.kt` created
- [ ] All interface methods implemented (stubs)
- [ ] Session tracking with Map
- [ ] SharedFlow for output and errors
- [ ] File compiles
- [ ] Can be injected via Hilt

**Dependencies**: T010, T011

---

## Phase 3: User Story 1 - Execute Basic Shell Commands (P1)

**Goal**: Enable developers to execute simple commands like `ls`, `echo`, `pwd`

**Duration**: 8-10 hours

**Deliverable**: Basic commands execute and output displays

**Success Criteria** (from spec.md):
- Commands execute successfully: `ls`, `pwd`, `echo`
- Command output is captured in real-time
- Stderr is captured separately from stdout

**Checkpoint**: ✅ After this phase, user can run `echo hello` and see "hello" in output

### T013: Initialize Termux TerminalSession in Repository

**Story**: US1 (FR-2, FR-3)
**File**: `app/src/main/kotlin/com/convocli/terminal/repository/TermuxTerminalRepository.kt`
**Duration**: 2 hours

**Description**: Replace stub with real Termux TerminalSession initialization.

**Tasks**:
1. Import Termux classes:
   ```kotlin
   import com.termux.terminal.TerminalSession
   import com.termux.terminal.TerminalSessionClient
   ```
2. Create `TerminalSessionClient` callback implementation
3. Initialize Termux session in `createSession()`:
   ```kotlin
   val session = TerminalSession(
       shellPath,      // /data/data/com.convocli/files/usr/bin/bash
       workingDir,     // $HOME
       args,           // empty
       envVars,        // default environment
       sessionClient   // callback
   )
   ```
4. Store session reference
5. Configure PTY (default size: 80x24)
6. Add error handling for initialization failures
7. Test session creation succeeds

**Acceptance Criteria**:
- [ ] Termux `TerminalSession` initialized correctly
- [ ] PTY created successfully
- [ ] Shell process spawned
- [ ] Session client callback registered
- [ ] Returns `Result.success` with session ID
- [ ] Returns `Result.failure` on errors
- [ ] Integration test passes: session creation

**Dependencies**: T004, T012

---

### T014: Implement PTY Output Streaming

**Story**: US1 (FR-5)
**File**: `app/src/main/kotlin/com/convocli/terminal/service/OutputStreamProcessor.kt`
**Duration**: 3 hours

**Description**: Read output from PTY and emit via Flow.

**Tasks**:
1. Create `terminal/service/` directory
2. Create `OutputStreamProcessor.kt`
3. Implement background coroutine to read PTY:
   ```kotlin
   class OutputStreamProcessor(
       private val session: TerminalSession,
       private val outputFlow: MutableSharedFlow<TerminalOutput>
   ) {
       fun start() {
           scope.launch(Dispatchers.IO) {
               // Read from session.terminalOutput
               // Emit to outputFlow
           }
       }
   }
   ```
4. Parse terminal output (text extraction)
5. Handle ANSI escape sequences (basic - strip or parse)
6. Emit output chunks to SharedFlow
7. Handle backpressure (buffer strategy)
8. Implement stop() to cancel coroutine

**Acceptance Criteria**:
- [ ] `OutputStreamProcessor.kt` created
- [ ] Reads output from Termux session
- [ ] Emits `TerminalOutput` events via Flow
- [ ] ANSI sequences handled (at minimum, stripped)
- [ ] Backpressure handled correctly
- [ ] Coroutine cancels on stop()
- [ ] Integration test: Output streaming works

**Dependencies**: T013

---

### T015: Integrate OutputStreamProcessor into Repository

**Story**: US1 (FR-5)
**File**: `app/src/main/kotlin/com/convocli/terminal/repository/TermuxTerminalRepository.kt`
**Duration**: 1 hour

**Description**: Wire OutputStreamProcessor into repository.

**Tasks**:
1. Create `OutputStreamProcessor` instance per session
2. Start processor when session created
3. Connect processor output to `_output` SharedFlow
4. Implement `observeOutput()` to return filtered Flow by sessionId
5. Stop processor when session destroyed
6. Test output flow end-to-end

**Acceptance Criteria**:
- [ ] Processor instantiated in `createSession()`
- [ ] Processor started automatically
- [ ] Output flows to `_output` SharedFlow
- [ ] `observeOutput()` filters by sessionId
- [ ] Processor stopped in `destroySession()`
- [ ] Integration test: Output received after command

**Dependencies**: T014

---

### T016: Implement Command Execution (Write to PTY)

**Story**: US1 (FR-4)
**File**: `app/src/main/kotlin/com/convocli/terminal/repository/TermuxTerminalRepository.kt`
**Duration**: 1 hour

**Description**: Write commands to PTY stdin for execution.

**Tasks**:
1. Implement `executeCommand()`:
   ```kotlin
   override suspend fun executeCommand(sessionId: String, command: String) {
       val session = sessions[sessionId] ?: error("Session not found")
       session.write(command + "\n")
   }
   ```
2. Validate command is non-empty
3. Append newline character
4. Handle write errors (broken pipe)
5. Emit error events if write fails
6. Test command execution

**Acceptance Criteria**:
- [ ] `executeCommand()` writes to PTY stdin
- [ ] Newline appended to command
- [ ] Empty commands rejected
- [ ] Write errors caught and emitted to `_errors`
- [ ] Integration test: `echo hello` outputs "hello"

**Dependencies**: T013

---

### T017: Create TerminalViewModel (Basic)

**Story**: US1 (FR-3)
**File**: `app/src/main/kotlin/com/convocli/terminal/viewmodel/TerminalViewModel.kt`
**Duration**: 2 hours

**Description**: Create ViewModel to manage terminal UI state.

**Tasks**:
1. Create `terminal/viewmodel/` directory
2. Create `TerminalViewModel.kt`:
   ```kotlin
   @HiltViewModel
   class TerminalViewModel @Inject constructor(
       private val terminalRepository: TerminalRepository
   ) : ViewModel() {

       private val _output = MutableStateFlow<List<String>>(emptyList())
       val output: StateFlow<List<String>> = _output.asStateFlow()

       private val _isExecuting = MutableStateFlow(false)
       val isExecuting: StateFlow<Boolean> = _isExecuting.asStateFlow()

       private var sessionId: String? = null

       init {
           createSession()
       }

       // ... methods
   }
   ```
3. Implement `createSession()` on init
4. Implement `executeCommand(command: String)`
5. Collect output from repository and update `_output`
6. Manage execution state (`_isExecuting`)
7. Handle errors
8. Test ViewModel logic

**Acceptance Criteria**:
- [ ] `TerminalViewModel.kt` created
- [ ] `@HiltViewModel` annotation added
- [ ] Repository injected via Hilt
- [ ] Session created on init
- [ ] `executeCommand()` delegates to repository
- [ ] Output collected and exposed via StateFlow
- [ ] Execution state tracked
- [ ] Unit test: ViewModel state updates correctly

**Dependencies**: T010, T016

---

### T018: Write Integration Test for Basic Command Execution

**Story**: US1 (Testing)
**File**: `app/src/androidTest/kotlin/com/convocli/terminal/TerminalIntegrationTest.kt`
**Duration**: 1 hour

**Description**: Test end-to-end command execution with real PTY.

**Tasks**:
1. Create `androidTest/kotlin/com/convocli/terminal/` directory
2. Create `TerminalIntegrationTest.kt`
3. Test session creation
4. Test simple command execution:
   ```kotlin
   @Test
   fun executeSimpleCommand() = runTest {
       val sessionId = repository.createSession().getOrThrow()
       repository.executeCommand(sessionId, "echo hello")

       val output = repository.observeOutput(sessionId).first()
       assertTrue(output.text.contains("hello"))
   }
   ```
5. Test multiple commands sequentially
6. Test command chaining (`echo a && echo b`)
7. Run tests on emulator

**Acceptance Criteria**:
- [ ] `TerminalIntegrationTest.kt` created
- [ ] Tests run on real Android emulator
- [ ] Session creation test passes
- [ ] `echo hello` test passes
- [ ] Multiple commands test passes
- [ ] Tests verify actual output correctness

**Dependencies**: T016

---

## Phase 4: User Story 2 - Navigate Directory Structure (P1)

**Goal**: Enable directory navigation with `cd` and `pwd`

**Duration**: 2-3 hours

**Deliverable**: Working directory changes persist across commands

**Success Criteria** (from spec.md):
- `cd /some/path` changes working directory
- Subsequent commands execute in correct directory
- `pwd` displays current working directory

**Checkpoint**: ✅ After this phase, user can `cd` and subsequent commands work in new directory

### T019: Set Default Environment Variables

**Story**: US2 (FR-8)
**File**: `app/src/main/kotlin/com/convocli/terminal/repository/TermuxTerminalRepository.kt`
**Duration**: 1 hour

**Description**: Configure default environment variables for terminal session.

**Tasks**:
1. Define default environment variables:
   ```kotlin
   private fun getDefaultEnvironment(): Map<String, String> {
       val appDir = context.filesDir.absolutePath
       return mapOf(
           "HOME" to "$appDir/home",
           "PATH" to "$appDir/usr/bin:/system/bin",
           "SHELL" to "$appDir/usr/bin/bash",
           "TMPDIR" to "$appDir/usr/tmp",
           "PREFIX" to "$appDir/usr",
           "TERM" to "xterm-256color",
           "LANG" to "en_US.UTF-8"
       )
   }
   ```
2. Pass environment to Termux session on creation
3. Test `echo $HOME` displays correct path
4. Test `echo $PATH` shows correct paths

**Acceptance Criteria**:
- [ ] Default environment variables defined
- [ ] Passed to Termux session on creation
- [ ] `$HOME` points to app private directory
- [ ] `$PATH` includes app binaries
- [ ] Integration test: `echo $HOME` outputs correct path

**Dependencies**: T013

---

### T020: Configure Initial Working Directory

**Story**: US2 (FR-8)
**File**: `app/src/main/kotlin/com/convocli/terminal/repository/TermuxTerminalRepository.kt`
**Duration**: 30 min

**Description**: Set initial working directory to $HOME.

**Tasks**:
1. Create `$HOME` directory if not exists:
   ```kotlin
   val homeDir = File(context.filesDir, "home")
   homeDir.mkdirs()
   ```
2. Pass `homeDir.absolutePath` as working directory to Termux session
3. Test session starts in $HOME
4. Test `pwd` outputs $HOME path

**Acceptance Criteria**:
- [ ] `$HOME` directory created on first launch
- [ ] Session initialized with $HOME as working directory
- [ ] `pwd` command outputs $HOME path
- [ ] Integration test: Initial directory is $HOME

**Dependencies**: T019

---

### T021: Track Working Directory Changes

**Story**: US2 (FR-8)
**File**: `app/src/main/kotlin/com/convocli/terminal/service/WorkingDirectoryTracker.kt`
**Duration**: 1 hour

**Description**: Monitor `cd` commands and track current working directory.

**Tasks**:
1. Create `WorkingDirectoryTracker.kt` in `service/`
2. Parse command input for `cd` patterns
3. Update tracked working directory:
   ```kotlin
   class WorkingDirectoryTracker {
       private val _currentDirectory = MutableStateFlow<String>("")
       val currentDirectory: StateFlow<String> = _currentDirectory.asStateFlow()

       fun onCommand(command: String) {
           if (command.startsWith("cd ")) {
               val path = command.substringAfter("cd ").trim()
               _currentDirectory.value = resolvePath(path)
           }
       }
   }
   ```
4. Handle relative paths (`cd ..`, `cd subdir`)
5. Handle absolute paths (`cd /storage/emulated/0`)
6. Handle `cd ~` (home directory)
7. Test directory tracking accuracy

**Acceptance Criteria**:
- [ ] `WorkingDirectoryTracker.kt` created
- [ ] Detects `cd` commands
- [ ] Updates current directory state
- [ ] Handles relative paths correctly
- [ ] Handles absolute paths correctly
- [ ] Handles `cd ~` for home
- [ ] Unit test: Directory tracking logic

**Dependencies**: None

---

### T022: Integrate WorkingDirectoryTracker into Repository

**Story**: US2 (FR-8)
**File**: `app/src/main/kotlin/com/convocli/terminal/repository/TermuxTerminalRepository.kt`
**Duration**: 30 min

**Description**: Wire directory tracker into repository.

**Tasks**:
1. Instantiate `WorkingDirectoryTracker` in repository
2. Call `tracker.onCommand()` in `executeCommand()`
3. Expose `currentDirectory` Flow from repository
4. Update ViewModel to expose current directory
5. Test directory tracking end-to-end

**Acceptance Criteria**:
- [ ] Tracker instantiated in repository
- [ ] `onCommand()` called for each command
- [ ] Current directory exposed via Flow
- [ ] ViewModel tracks current directory
- [ ] Integration test: `cd` then `pwd` shows new directory

**Dependencies**: T021

---

## Phase 5: User Story 3 - View File Contents (P1)

**Goal**: Enable file viewing with `cat`, `less`, etc.

**Duration**: 1 hour

**Deliverable**: File content commands work correctly

**Success Criteria** (from spec.md):
- `cat` streams file contents line-by-line
- Output formatting preserved
- Pipe commands work (`cat file.txt | grep pattern`)

**Checkpoint**: ✅ After this phase, user can view file contents

### T023: Test File Viewing Commands

**Story**: US3 (Testing)
**File**: `app/src/androidTest/kotlin/com/convocli/terminal/FileCommandsTest.kt`
**Duration**: 1 hour

**Description**: Verify file viewing commands work correctly.

**Tasks**:
1. Create `FileCommandsTest.kt`
2. Create test file in $HOME:
   ```kotlin
   @Before
   fun setupTestFile() {
       val testFile = File(homeDir, "test.txt")
       testFile.writeText("line1\nline2\nline3\n")
   }
   ```
3. Test `cat test.txt` displays file contents
4. Test `cat` with large file (1000+ lines)
5. Test pipe: `cat test.txt | grep line2`
6. Test redirect: `echo "content" > output.txt`
7. Verify output correctness

**Acceptance Criteria**:
- [ ] `FileCommandsTest.kt` created
- [ ] `cat` command test passes
- [ ] Large file test passes (no crash)
- [ ] Pipe command test passes
- [ ] Redirect test passes
- [ ] Output matches expected content

**Dependencies**: T018 (US1 complete)

---

## Phase 6: User Story 4 - Handle Command Errors (P2)

**Goal**: Gracefully handle invalid commands and errors

**Duration**: 3-4 hours

**Deliverable**: Errors display clearly, session continues

**Success Criteria** (from spec.md):
- Invalid commands show "command not found"
- Failed commands return non-zero exit codes
- Error messages are user-readable
- Session remains active after errors

**Checkpoint**: ✅ After this phase, errors are handled gracefully

### T024: Implement Stderr Capture

**Story**: US4 (FR-9)
**File**: `app/src/main/kotlin/com/convocli/terminal/service/OutputStreamProcessor.kt`
**Duration**: 1 hour

**Description**: Capture stderr output separately from stdout.

**Tasks**:
1. Modify `OutputStreamProcessor` to distinguish streams
2. Check Termux session for stderr API
3. Emit `TerminalOutput` with `StreamType.STDERR`
4. Test stderr capture with invalid command
5. Verify stderr appears separately in output

**Acceptance Criteria**:
- [ ] Stderr captured separately from stdout
- [ ] `TerminalOutput` includes `StreamType.STDERR`
- [ ] Invalid command produces stderr output
- [ ] Integration test: Stderr is captured

**Dependencies**: T014

---

### T025: Detect Command Failures (Exit Codes)

**Story**: US4 (FR-9)
**File**: `app/src/main/kotlin/com/convocli/terminal/service/CommandMonitor.kt`
**Duration**: 2 hours

**Description**: Monitor command execution and detect failures.

**Tasks**:
1. Create `CommandMonitor.kt` in `service/`
2. Track commands sent to terminal
3. Monitor for command completion (prompt return)
4. Capture exit codes (if available from Termux)
5. Emit `TerminalError.CommandFailed` on non-zero exit:
   ```kotlin
   if (exitCode != 0) {
       _errors.emit(TerminalError.CommandFailed(
           command = lastCommand,
           exitCode = exitCode,
           stderr = capturedStderr
       ))
   }
   ```
6. Test with failing command (`ls /nonexistent`)

**Acceptance Criteria**:
- [ ] `CommandMonitor.kt` created
- [ ] Exit codes captured (if possible)
- [ ] Command failures detected
- [ ] `TerminalError.CommandFailed` emitted
- [ ] Integration test: Failed command emits error

**Dependencies**: T024

---

### T026: Implement PTY Error Detection

**Story**: US4 (FR-9)
**File**: `app/src/main/kotlin/com/convocli/terminal/service/OutputStreamProcessor.kt`
**Duration**: 1 hour

**Description**: Detect and handle PTY I/O errors.

**Tasks**:
1. Wrap PTY read operations in try-catch
2. Detect broken pipe (process died)
3. Detect read/write timeouts
4. Emit `TerminalError.IOError` on failure:
   ```kotlin
   catch (e: IOException) {
       _errors.emit(TerminalError.IOError(e.message ?: "Unknown I/O error"))
   }
   ```
5. Test with simulated session crash
6. Verify error emission

**Acceptance Criteria**:
- [ ] PTY read wrapped in error handling
- [ ] Broken pipe detected
- [ ] Timeouts detected
- [ ] `TerminalError.IOError` emitted
- [ ] Error includes descriptive message

**Dependencies**: T014

---

### T027: Display Errors in ViewModel

**Story**: US4 (FR-9)
**File**: `app/src/main/kotlin/com/convocli/terminal/viewmodel/TerminalViewModel.kt`
**Duration**: 1 hour

**Description**: Collect errors from repository and expose to UI.

**Tasks**:
1. Add error StateFlow to ViewModel:
   ```kotlin
   private val _errors = MutableStateFlow<TerminalError?>(null)
   val errors: StateFlow<TerminalError?> = _errors.asStateFlow()
   ```
2. Collect errors from repository
3. Transform errors to user-friendly messages
4. Emit to UI
5. Add method to clear errors
6. Test error display logic

**Acceptance Criteria**:
- [ ] Errors exposed via StateFlow
- [ ] Errors collected from repository
- [ ] User-friendly error messages generated
- [ ] Errors can be cleared
- [ ] Unit test: Error transformation logic

**Dependencies**: T025, T026

---

### T028: Write Error Handling Integration Tests

**Story**: US4 (Testing)
**File**: `app/src/androidTest/kotlin/com/convocli/terminal/ErrorHandlingTest.kt`
**Duration**: 1 hour

**Description**: Test error scenarios end-to-end.

**Tasks**:
1. Create `ErrorHandlingTest.kt`
2. Test invalid command:
   ```kotlin
   @Test
   fun invalidCommand() {
       repository.executeCommand(sessionId, "invalidcmd123")
       val error = repository.observeErrors().first()
       assertTrue(error is TerminalError.CommandFailed)
   }
   ```
3. Test permission denied (access restricted file)
4. Test file not found
5. Test session crash recovery
6. Verify session continues after errors

**Acceptance Criteria**:
- [ ] `ErrorHandlingTest.kt` created
- [ ] Invalid command test passes
- [ ] Permission denied test passes
- [ ] File not found test passes
- [ ] Session remains active after errors

**Dependencies**: T027

---

## Phase 7: User Story 5 - Interactive Learning Environment (P2)

**Goal**: Session persists across app lifecycle events

**Duration**: 4-5 hours

**Deliverable**: Sessions survive backgrounding and rotation

**Success Criteria** (from spec.md):
- Session persists when app backgrounded
- Session survives device rotation
- Working directory restored after restart

**Checkpoint**: ✅ After this phase, sessions are robust

### T029: Implement Session State Tracking

**Story**: US5 (FR-7)
**File**: `app/src/main/kotlin/com/convocli/terminal/service/TerminalSessionManager.kt`
**Duration**: 2 hours

**Description**: Track session state through lifecycle events.

**Tasks**:
1. Create `TerminalSessionManager.kt` in `service/`
2. Track session state (RUNNING, STOPPED, ERROR)
3. Implement state transitions:
   ```kotlin
   class TerminalSessionManager {
       private val _state = MutableStateFlow<SessionState>(SessionState.STOPPED)
       val state: StateFlow<SessionState> = _state.asStateFlow()

       fun start() { _state.value = SessionState.RUNNING }
       fun stop() { _state.value = SessionState.STOPPED }
       fun error(reason: String) { _state.value = SessionState.ERROR }
   }
   ```
4. Emit state changes via Flow
5. Test state transitions
6. Unit test state management

**Acceptance Criteria**:
- [ ] `TerminalSessionManager.kt` created
- [ ] Session state tracked correctly
- [ ] State transitions implemented
- [ ] State exposed via StateFlow
- [ ] Unit test: State transitions

**Dependencies**: None

---

### T030: Handle App Backgrounding

**Story**: US5 (FR-7)
**File**: `app/src/main/kotlin/com/convocli/terminal/repository/TermuxTerminalRepository.kt`
**Duration**: 1 hour

**Description**: Pause/resume I/O monitoring when app backgrounds.

**Tasks**:
1. Implement `pauseSession()` method:
   ```kotlin
   fun pauseSession(sessionId: String) {
       outputProcessor?.pause()
   }
   ```
2. Implement `resumeSession()` method
3. Hook into Android lifecycle callbacks (via ViewModel or Application)
4. Test session pause/resume
5. Verify no data loss during pause

**Acceptance Criteria**:
- [ ] `pauseSession()` method implemented
- [ ] `resumeSession()` method implemented
- [ ] I/O monitoring paused during background
- [ ] Session resumes correctly on foreground
- [ ] No data loss during pause
- [ ] Integration test: Backgrounding test

**Dependencies**: T029

---

### T031: Handle Configuration Changes (Rotation)

**Story**: US5 (FR-7)
**File**: `app/src/main/kotlin/com/convocli/terminal/viewmodel/TerminalViewModel.kt`
**Duration**: 1 hour

**Description**: Ensure session survives device rotation.

**Tasks**:
1. Verify ViewModel is scoped correctly (survives rotation)
2. Test rotation with active session
3. Verify output history preserved
4. Verify working directory preserved
5. Test multiple rotations
6. Integration test rotation scenario

**Acceptance Criteria**:
- [ ] ViewModel survives rotation (scope correct)
- [ ] Session ID persists across rotation
- [ ] Output history preserved
- [ ] Working directory preserved
- [ ] Integration test: Rotation test passes

**Dependencies**: T017, T030

---

### T032: Implement Session Cleanup

**Story**: US5 (FR-7)
**File**: `app/src/main/kotlin/com/convocli/terminal/repository/TermuxTerminalRepository.kt`
**Duration**: 1 hour

**Description**: Clean up resources when session destroyed.

**Tasks**:
1. Implement `destroySession()` fully:
   ```kotlin
   override suspend fun destroySession(sessionId: String) {
       val session = sessions.remove(sessionId) ?: return

       outputProcessor?.stop()
       termuxSession.finishIfRunning()  // Send SIGHUP
       // Close PTY file descriptors (handled by Termux)

       _state.value = SessionState.STOPPED
   }
   ```
2. Stop output processor
3. Terminate shell process (SIGHUP)
4. Close PTY file descriptors
5. Remove from session map
6. Cancel coroutines
7. Test cleanup prevents leaks

**Acceptance Criteria**:
- [ ] `destroySession()` fully implemented
- [ ] Output processor stopped
- [ ] Shell process terminated
- [ ] PTY closed
- [ ] Session removed from map
- [ ] No memory leaks (LeakCanary)
- [ ] Unit test: Cleanup logic

**Dependencies**: T013, T029

---

### T033: Write Lifecycle Integration Tests

**Story**: US5 (Testing)
**File**: `app/src/androidTest/kotlin/com/convocli/terminal/LifecycleTest.kt`
**Duration**: 1 hour

**Description**: Test session persistence through lifecycle events.

**Tasks**:
1. Create `LifecycleTest.kt`
2. Test app backgrounding:
   ```kotlin
   @Test
   fun sessionSurvivesBackgrounding() {
       repository.executeCommand(sessionId, "echo test")
       // Simulate backgrounding
       repository.pauseSession(sessionId)
       // Foreground
       repository.resumeSession(sessionId)
       // Verify session still works
   }
   ```
3. Test device rotation (recreate activity)
4. Test multiple lifecycle events
5. Verify session state throughout

**Acceptance Criteria**:
- [ ] `LifecycleTest.kt` created
- [ ] Backgrounding test passes
- [ ] Rotation test passes
- [ ] Multiple events test passes
- [ ] Session state verified throughout

**Dependencies**: T030, T031, T032

---

## Phase 8: Polish & Cross-Cutting Concerns

**Goal**: Complete testing, documentation, and polish

**Duration**: 8-10 hours

**Deliverable**: Production-ready feature

**Checkpoint**: ✅ Feature complete and ready for merge

### T034: Write Comprehensive Unit Tests for Repository

**Story**: Testing
**File**: `app/src/test/kotlin/com/convocli/terminal/repository/TermuxTerminalRepositoryTest.kt`
**Duration**: 2 hours

**Description**: Unit test repository logic with mocked Termux.

**Tasks**:
1. Create `TermuxTerminalRepositoryTest.kt` in `test/`
2. Mock Termux `TerminalSession`
3. Test `createSession()` success and failure
4. Test `executeCommand()` writes correctly
5. Test `observeOutput()` filters by sessionId
6. Test `destroySession()` cleanup
7. Test error emission
8. Achieve 80%+ coverage

**Acceptance Criteria**:
- [ ] `TermuxTerminalRepositoryTest.kt` created
- [ ] Termux session mocked
- [ ] All repository methods tested
- [ ] Success and failure paths tested
- [ ] 80%+ code coverage
- [ ] All tests pass

**Dependencies**: T012

---

### T035: Write Unit Tests for TerminalViewModel [P]

**Story**: Testing
**File**: `app/src/test/kotlin/com/convocli/terminal/viewmodel/TerminalViewModelTest.kt`
**Duration**: 2 hours

**Description**: Unit test ViewModel state management logic.

**Tasks**:
1. Create `TerminalViewModelTest.kt` in `test/`
2. Mock `TerminalRepository`
3. Test session creation on init
4. Test `executeCommand()` logic
5. Test output collection and state updates
6. Test error handling and display
7. Test command input validation
8. Use Turbine for Flow testing

**Acceptance Criteria**:
- [ ] `TerminalViewModelTest.kt` created
- [ ] Repository mocked
- [ ] All ViewModel methods tested
- [ ] StateFlow updates verified
- [ ] Error handling tested
- [ ] All tests pass

**Dependencies**: T017

---

### T036: Write Unit Tests for OutputStreamProcessor [P]

**Story**: Testing
**File**: `app/src/test/kotlin/com/convocli/terminal/service/OutputStreamProcessorTest.kt`
**Duration**: 1 hour

**Description**: Unit test output streaming logic.

**Tasks**:
1. Create `OutputStreamProcessorTest.kt` in `test/`
2. Mock Termux session with fake output
3. Test output parsing
4. Test ANSI sequence handling
5. Test backpressure handling
6. Test coroutine cancellation
7. Verify emissions to Flow

**Acceptance Criteria**:
- [ ] `OutputStreamProcessorTest.kt` created
- [ ] Fake output used for testing
- [ ] Output parsing tested
- [ ] ANSI handling tested
- [ ] Backpressure tested
- [ ] All tests pass

**Dependencies**: T014

---

### T037: Performance Test - Command Execution Latency

**Story**: Testing
**File**: `app/src/androidTest/kotlin/com/convocli/terminal/PerformanceTest.kt`
**Duration**: 1 hour

**Description**: Measure and verify command execution performance.

**Tasks**:
1. Create `PerformanceTest.kt`
2. Test command execution latency:
   ```kotlin
   @Test
   fun commandLatency() {
       val start = System.currentTimeMillis()
       repository.executeCommand(sessionId, "echo test")
       val output = repository.observeOutput(sessionId).first()
       val duration = System.currentTimeMillis() - start

       assertTrue(duration < 200, "Latency: ${duration}ms")
   }
   ```
3. Test output streaming latency
4. Test with simple commands (ls, pwd, echo)
5. Verify < 200ms for simple commands (success criterion)

**Acceptance Criteria**:
- [ ] `PerformanceTest.kt` created
- [ ] Command latency < 200ms
- [ ] Output streaming latency < 100ms
- [ ] Performance tests pass

**Dependencies**: T018

---

### T038: Performance Test - Large Output Handling [P]

**Story**: Testing
**File**: `app/src/androidTest/kotlin/com/convocli/terminal/PerformanceTest.kt`
**Duration**: 1 hour

**Description**: Test handling of large output volumes.

**Tasks**:
1. Add tests to `PerformanceTest.kt`
2. Test streaming 10,000 lines:
   ```kotlin
   @Test
   fun largeOutputStreaming() {
       repository.executeCommand(sessionId, "seq 1 10000")
       val outputs = repository.observeOutput(sessionId).take(10000).toList()
       assertEquals(10000, outputs.size)
   }
   ```
3. Monitor memory usage during streaming
4. Verify memory < 50MB (success criterion)
5. Test no UI freezing (use UI test with assertions)
6. Verify backpressure handling works

**Acceptance Criteria**:
- [ ] 10,000 line test passes
- [ ] Memory usage < 50MB during streaming
- [ ] No UI freezing detected
- [ ] Backpressure handled correctly

**Dependencies**: T037

---

### T039: Performance Test - Memory Leak Detection [P]

**Story**: Testing
**File**: `app/src/androidTest/kotlin/com/convocli/terminal/PerformanceTest.kt`
**Duration**: 1 hour

**Description**: Verify no memory leaks after repeated operations.

**Tasks**:
1. Add test to `PerformanceTest.kt`
2. Install LeakCanary in test environment
3. Test repeated session create/destroy (100 times)
4. Test 1000 consecutive commands
5. Trigger GC and check for leaks
6. Verify no leaks detected

**Acceptance Criteria**:
- [ ] Repeated operations test added
- [ ] LeakCanary integrated in tests
- [ ] No memory leaks after 100 session cycles
- [ ] No leaks after 1000 commands
- [ ] Test passes

**Dependencies**: T037

---

### T040: Add KDoc Comments to All Public APIs

**Story**: Documentation
**Files**: All `.kt` files in `terminal/`
**Duration**: 2 hours

**Description**: Add comprehensive KDoc to all public classes and methods.

**Tasks**:
1. Review all files in `terminal/` package
2. Add KDoc to classes:
   ```kotlin
   /**
    * Repository for terminal operations.
    *
    * Provides access to terminal session lifecycle, command execution,
    * and output streaming. All operations are thread-safe and use
    * Kotlin coroutines for asynchronous execution.
    *
    * @see TerminalSession
    * @see TerminalOutput
    */
   interface TerminalRepository { ... }
   ```
3. Add KDoc to all public methods
4. Add parameter descriptions
5. Add return value descriptions
6. Add usage examples in comments
7. Verify KDoc renders correctly

**Acceptance Criteria**:
- [ ] All public classes have KDoc
- [ ] All public methods have KDoc
- [ ] Parameters documented
- [ ] Return values documented
- [ ] Usage examples included
- [ ] KDoc renders correctly

**Dependencies**: All implementation tasks

---

### T041: Write quickstart.md

**Story**: Documentation
**File**: `features/002-.../quickstart.md`
**Duration**: 1 hour

**Description**: Create developer quickstart guide.

**Tasks**:
1. Create `quickstart.md`
2. Add setup instructions
3. Add basic usage examples:
   ```kotlin
   // Creating a terminal session
   val repository: TerminalRepository = ...
   val sessionId = repository.createSession().getOrThrow()

   // Executing a command
   repository.executeCommand(sessionId, "ls -la")

   // Observing output
   repository.observeOutput(sessionId).collect { output ->
       println(output.text)
   }
   ```
4. Add common troubleshooting scenarios
5. Add testing instructions
6. Add links to API docs

**Acceptance Criteria**:
- [ ] `quickstart.md` created
- [ ] Setup instructions included
- [ ] Usage examples included
- [ ] Troubleshooting section added
- [ ] Testing instructions added
- [ ] Links to docs added

**Dependencies**: T040

---

### T042: Update data-model.md with Implementation Details

**Story**: Documentation
**File**: `features/002-.../data-model.md`
**Duration**: 30 min

**Description**: Update data model doc with final implementation.

**Tasks**:
1. Update `data-model.md` with actual class definitions
2. Add state diagram for session lifecycle
3. Document Flow-based APIs
4. Add relationship diagrams
5. Add code examples

**Acceptance Criteria**:
- [ ] `data-model.md` updated
- [ ] State diagrams added
- [ ] Flow APIs documented
- [ ] Relationship diagrams included
- [ ] Code examples added

**Dependencies**: T003, All implementation tasks

---

### T043: Run Manual Testing Checklist

**Story**: Testing
**File**: `features/002-.../plan.md` (Manual Testing Checklist section)
**Duration**: 2 hours

**Description**: Execute manual testing checklist from plan.md.

**Tasks**:
1. Review checklist in plan.md
2. Execute each functional test:
   - [ ] Execute simple commands (ls, pwd, echo)
   - [ ] Navigate directories (cd, pwd)
   - [ ] View file contents (cat, less)
   - [ ] Run complex commands (pipes, redirects)
   - [ ] Handle invalid commands
3. Execute lifecycle tests:
   - [ ] Session survives rotation
   - [ ] Session persists when backgrounded
   - [ ] Session cleaned up on termination
4. Execute performance tests:
   - [ ] Commands respond instantly
   - [ ] Large outputs stream smoothly
   - [ ] No UI freezing
5. Execute edge case tests:
   - [ ] Binary output
   - [ ] Rapid command execution
   - [ ] Long-running commands
6. Document results

**Acceptance Criteria**:
- [ ] All checklist items executed
- [ ] Results documented
- [ ] Any issues identified and resolved
- [ ] Feature meets all success criteria

**Dependencies**: All implementation tasks

---

### T044: Update COMPLETION_SUMMARY.md

**Story**: Documentation
**File**: `features/002-.../COMPLETION_SUMMARY.md`
**Duration**: 1 hour

**Description**: Write completion summary and retrospective.

**Tasks**:
1. Create `COMPLETION_SUMMARY.md`
2. Summarize feature implementation
3. Document what was built:
   - Terminal session management
   - Command execution
   - Output streaming
   - Error handling
   - Lifecycle management
4. Document metrics:
   - Lines of code
   - Test coverage
   - Performance metrics
5. Document lessons learned
6. Document future improvements
7. Document known limitations

**Acceptance Criteria**:
- [ ] `COMPLETION_SUMMARY.md` created
- [ ] Implementation summarized
- [ ] Metrics documented
- [ ] Lessons learned included
- [ ] Future improvements listed

**Dependencies**: T043

---

### T045: Verify All Success Criteria from spec.md

**Story**: Validation
**File**: `features/002-.../checklists/requirements.md`
**Duration**: 1 hour

**Description**: Validate all success criteria are met.

**Tasks**:
1. Review success criteria from spec.md
2. Verify SC-1: Command Execution Performance
   - [ ] Simple commands < 200ms
   - [ ] Output appears < 100ms
3. Verify SC-2: Session Stability
   - [ ] Sessions run 1+ hour
   - [ ] 1000+ commands without failure
   - [ ] Survive backgrounding and rotation
4. Verify SC-3: Output Handling Capacity
   - [ ] Stream 10,000+ lines
   - [ ] Memory < 50MB
   - [ ] Outputs up to 10MB
5. Verify SC-4: User Experience
   - [ ] < 100ms perceived delay
   - [ ] Clear error messages
   - [ ] No crashes
6. Verify SC-5: Linux Environment Completeness
   - [ ] Standard commands work
   - [ ] Environment variables set
   - [ ] Working directory persists
7. Document verification results

**Acceptance Criteria**:
- [ ] All success criteria verified
- [ ] Test results documented
- [ ] Any gaps identified and addressed

**Dependencies**: T043

---

### T046: Update PRE_MERGE_CHECKLIST.md

**Story**: Validation
**File**: `features/002-.../PRE_MERGE_CHECKLIST.md`
**Duration**: 30 min

**Description**: Complete pre-merge checklist.

**Tasks**:
1. Create/update `PRE_MERGE_CHECKLIST.md`
2. Verify all tasks completed
3. Verify all tests pass
4. Verify code quality (ktlint, no warnings)
5. Verify documentation complete
6. Verify feature meets spec requirements
7. Verify constitution compliance
8. Check all items off

**Acceptance Criteria**:
- [ ] `PRE_MERGE_CHECKLIST.md` completed
- [ ] All items checked
- [ ] Feature ready for merge

**Dependencies**: T044, T045

---

### T047: Run /specswarm:complete to Merge Feature

**Story**: Completion
**File**: N/A (Git operation)
**Duration**: 15 min

**Description**: Execute SpecSwarm completion workflow.

**Tasks**:
1. Verify all tasks completed
2. Verify all tests pass
3. Verify documentation complete
4. Run `/specswarm:complete`
5. Review merge results
6. Verify feature merged to sprint-01

**Acceptance Criteria**:
- [ ] `/specswarm:complete` executed
- [ ] Feature merged to sprint-01
- [ ] Feature branch deleted (automatic)
- [ ] Git history clean

**Dependencies**: T046

---

## Task Execution Order

### Sequential Dependencies

Tasks must be executed in dependency order. Key dependency chains:

1. **Foundation Chain**:
   - T001 → T004 → T013 → T016 (Termux integration)
   - T003 → T006-T009 → T010 → T012 (Data models)

2. **Core Functionality Chain**:
   - T013 → T014 → T015 → T016 (Command execution)
   - T016 → T017 (ViewModel)

3. **Advanced Features Chain**:
   - T019 → T020 (Environment setup)
   - T021 → T022 (Directory tracking)
   - T024 → T025 → T027 (Error handling)

4. **Lifecycle Chain**:
   - T029 → T030 → T031 → T032 (Session management)

5. **Testing Chain**:
   - Implementation tasks → Testing tasks
   - T034-T039 can run in parallel [P]

### Parallel Execution Opportunities

Tasks marked [P] can be executed in parallel:

**Phase 1 (Setup)**:
- T001, T002, T003 can all run in parallel

**Phase 2 (Foundation)**:
- T007, T008, T009 can run in parallel (after T006)
- T005 can run anytime

**Phase 8 (Polish)**:
- T034, T035, T036 can run in parallel
- T037, T038, T039 can run in parallel

**Maximum Parallelization**:
- Phase 1: 3 tasks in parallel
- Phase 2: 3 tasks in parallel
- Phase 8: 3-6 tasks in parallel

---

## Implementation Strategy

### MVP Scope (Minimum Viable Product)

**Goal**: Execute basic commands and see output

**MVP Includes**:
- Phase 1: Setup
- Phase 2: Foundation
- Phase 3: User Story 1 (Execute Basic Shell Commands)

**MVP Excludes** (defer to post-MVP):
- User Story 2-5
- Advanced error handling
- Performance optimization
- Comprehensive testing

**MVP Duration**: 10-12 hours

**MVP Success**: User can open app, type `echo hello`, see "hello" output

### Incremental Delivery Plan

1. **Sprint 1: MVP** (Phase 1-3)
   - Deliverable: Basic command execution
   - Demo: `echo`, `ls`, `pwd` commands work

2. **Sprint 2: Navigation & Files** (Phase 4-5)
   - Deliverable: Directory navigation and file viewing
   - Demo: `cd`, `cat`, `grep` commands work

3. **Sprint 3: Error Handling** (Phase 6)
   - Deliverable: Graceful error handling
   - Demo: Invalid commands handled cleanly

4. **Sprint 4: Lifecycle** (Phase 7)
   - Deliverable: Robust session management
   - Demo: App survives rotation and backgrounding

5. **Sprint 5: Polish** (Phase 8)
   - Deliverable: Production-ready feature
   - Demo: All tests pass, docs complete

---

## Testing Coverage

### Unit Tests (19 test files)

**Coverage Target**: 80%+

**Test Files**:
- Repository: `TermuxTerminalRepositoryTest.kt`
- ViewModel: `TerminalViewModelTest.kt`
- Service: `OutputStreamProcessorTest.kt`, `TerminalSessionManagerTest.kt`, `WorkingDirectoryTrackerTest.kt`, `CommandMonitorTest.kt`
- Models: Tests for data classes (if complex logic)

### Integration Tests (5 test files)

**Test Files**:
- `TerminalIntegrationTest.kt` (US1)
- `FileCommandsTest.kt` (US3)
- `ErrorHandlingTest.kt` (US4)
- `LifecycleTest.kt` (US5)
- `PerformanceTest.kt` (Performance)

### Manual Tests

**Checklist**: See plan.md Manual Testing Checklist

---

## Dependencies Graph

```
Phase 1 (Setup)
├── T001 [P]
├── T002 [P]
└── T003 [P]

Phase 2 (Foundation)
├── T004 ← T001
├── T005
├── T006 ← T003
├── T007 [P] ← T006
├── T008 [P] ← T006
├── T009 [P] ← T006
├── T010 ← T006, T007, T009
├── T011 ← T010
└── T012 ← T010, T011

Phase 3 (US1)
├── T013 ← T004, T012
├── T014 ← T013
├── T015 ← T014
├── T016 ← T013
├── T017 ← T010, T016
└── T018 ← T016

Phase 4 (US2)
├── T019 ← T013
├── T020 ← T019
├── T021
└── T022 ← T021

Phase 5 (US3)
└── T023 ← T018

Phase 6 (US4)
├── T024 ← T014
├── T025 ← T024
├── T026 ← T014
├── T027 ← T025, T026
└── T028 ← T027

Phase 7 (US5)
├── T029
├── T030 ← T029
├── T031 ← T017, T030
├── T032 ← T013, T029
└── T033 ← T030, T031, T032

Phase 8 (Polish)
├── T034 [P] ← T012
├── T035 [P] ← T017
├── T036 [P] ← T014
├── T037 [P] ← T018
├── T038 [P] ← T037
├── T039 [P] ← T037
├── T040 ← All implementation
├── T041 ← T040
├── T042 ← T003, All implementation
├── T043 ← All implementation
├── T044 ← T043
├── T045 ← T043
├── T046 ← T044, T045
└── T047 ← T046
```

---

## Summary

**Total Tasks**: 47
**Total Duration**: 35-40 hours
**Test Coverage Target**: 80%+
**Parallel Opportunities**: 12 tasks can run in parallel

**MVP Path** (10-12 hours):
1. T001-T003 (Setup)
2. T004-T012 (Foundation)
3. T013-T018 (US1: Basic Commands)

**Full Feature Path** (35-40 hours):
- All 47 tasks in dependency order
- Incremental delivery by user story
- Comprehensive testing and documentation

**Next Command**: `/specswarm:implement` to begin implementation

---

**Status**: ✅ Tasks ready for implementation
**Generated**: 2025-10-21
**Feature**: 002 - Termux Integration
