# Bug 008: Terminal Output Not Streaming - Loading Spinner Runs Forever

**Status**: Active
**Created**: 2025-10-23
**Priority**: Critical
**Severity**: Critical
**Blocks**: MVP testing, all terminal functionality

## Symptoms

After implementing Phase 3 (Terminal MVP), when submitting any command like `echo "Hello World"`:

- Command block is created
- Loading spinner starts (EXECUTING status)
- **Spinner runs forever with no response**
- No output ever appears in the command block
- Block never transitions to SUCCESS/FAILURE
- UI becomes stuck waiting for completion

## Reproduction Steps

1. Build and install the app: `./gradlew installDebug`
2. Launch ConvoCLI
3. Type `echo "Hello World"` in the command input
4. Press send/execute button

**Expected Behavior**:
- Spinner shows briefly
- Output "Hello World" appears
- Block transitions to SUCCESS status (green checkmark)
- Prompt appears for next command

**Actual Behavior**:
- Spinner runs indefinitely
- No output ever appears
- Block stays in EXECUTING status forever
- App appears frozen/unresponsive

## Root Cause Analysis

**ULTRATHINKING PERFORMED** - Deep multi-layer analysis revealed MULTIPLE bugs:

### Bug #1: Thread Visibility Issue (PRIMARY CAUSE of infinite spinner)

**Location**: `TerminalRepositoryImpl.kt:51`

```kotlin
private var currentBlockId: String? = null  // ❌ NOT @Volatile!
```

**Problem**:
- `currentBlockId` is set on `Dispatchers.IO` thread in `executeCommand()`
- `currentBlockId` is read on Termux callback thread in `onTextChanged()`
- Without `@Volatile`, the callback thread may NEVER see the updated value
- Result: `blockId` is null in callback → no OutputChunks emitted → no UI updates → infinite spinner

**Evidence**: In `TerminalRepositoryImpl.kt:228-240`:
```kotlin
val blockId = currentBlockId  // ← May be null due to thread visibility!
if (blockId != null) {
    val chunk = OutputChunk(blockId = blockId, ...)
    _outputFlow.tryEmit(chunk)
} // ← If null, no chunks emitted!
```

**Impact**: **CRITICAL** - Without volatile, inter-thread visibility is not guaranteed by Java Memory Model. The Termux callback thread may continue seeing `null` indefinitely, causing zero output chunks to be emitted.

### Bug #2: Output Duplication - Using Full Transcript

**Location**: `TerminalRepositoryImpl.kt:224` and `CommandBlockViewModel.kt:113`

**Problem**:
```kotlin
// TerminalRepositoryImpl.kt:224
val text = screen.transcriptText  // ← Returns ENTIRE terminal history!

// CommandBlockViewModel.kt:113
val newOutput = block.output + chunk.data  // ← Appends entire history to existing!
```

**Flow**:
1. First `onTextChanged`: chunk.data = "$ echo Hello\nHello\n$ "
2. Block output becomes: "" + "$ echo Hello\nHello\n$ " = "$ echo Hello\nHello\n$ "
3. Second `onTextChanged`: chunk.data = "$ echo Hello\nHello\n$ echo World\nWorld\n$ "
4. Block output becomes: "$ echo Hello\nHello\n$ " + "$ echo Hello\nHello\n$ echo World\nWorld\n$ "
5. Result: **Exponential duplication** of all previous output!

**Impact**: **HIGH** - Even if Bug #1 is fixed, output will duplicate exponentially, making the terminal unusable.

### Bug #3: Missing Exit Code Tracking

**Location**: `TerminalRepositoryImpl.kt:147-151`

```kotlin
currentBlockId = blockId
currentSession?.write("$command\n")  // ← Command sent, but no exit code captured!
```

**Problem**: Termux PTY doesn't automatically provide exit codes. We need to:
1. Parse `echo $?` after each command, OR
2. Track process termination callbacks, OR
3. Use shell integration to detect exit codes

**Current state**: `getExitCode()` returns from a map that's never populated (except on session death).

**Impact**: **MEDIUM** - Prompt detection will mark commands as SUCCESS, but exit code is always 0 or null, making FAILURE detection impossible.

### Bug #4: Prompt Detection May Fail on Partial Output

**Location**: `SimplePromptDetector.kt:36-40`

```kotlin
val lastLine = output.lines().lastOrNull() ?: return false
return bashPrompt.containsMatchIn(lastLine) ||
       rootPrompt.containsMatchIn(lastLine)
```

**Problem**: If `onTextChanged` is called mid-output (before prompt appears), or if prompt is on a separate chunk, detection may fail or trigger prematurely.

**Impact**: **LOW** - Most commands complete fast enough that prompt arrives atomically, but could cause issues with long-running commands.

## Impact Assessment

**Affected Users**: 100% of users (MVP completely broken)

**Affected Features**:
- Terminal command execution: **Completely broken**
- Command blocks UI: **Non-functional** (spinner never stops)
- All terminal functionality: **Blocked**

**Severity Justification**:
- **Critical Priority**: Blocks all MVP testing and functionality
- **Critical Severity**: Renders the entire terminal feature unusable
- No workaround available (bug is in core execution path)

**Workaround Available**: **NO** - This bug completely blocks terminal functionality. The app builds but is non-functional.

## Regression Test Requirements

1. **Test thread visibility**: Verify output chunks are emitted from Termux callback
2. **Test incremental output**: Verify output updates correctly without duplication
3. **Test prompt detection**: Verify commands transition to SUCCESS when prompt detected
4. **Test multiple commands**: Verify subsequent commands work after first completes

**Test Success Criteria**:
- ✅ Test fails before fix (reproduces infinite spinner)
- ✅ Test passes after fix (output streams correctly)
- ✅ No output duplication
- ✅ Blocks transition to SUCCESS/FAILURE appropriately

## Proposed Solution

### Fix #1: Make currentBlockId Volatile (PRIMARY FIX)

```kotlin
// TerminalRepositoryImpl.kt:51
@Volatile
private var currentBlockId: String? = null
```

**Rationale**: Ensures visibility across threads per Java Memory Model.

### Fix #2: Track Incremental Output Instead of Full Transcript

**Option A** (Recommended): Track last transcript position
```kotlin
private var lastTranscriptLength = 0

override fun onTextChanged(session: com.termux.terminal.TerminalSession) {
    val text = screen.transcriptText
    val newText = text.substring(lastTranscriptLength)
    lastTranscriptLength = text.length

    if (newText.isNotEmpty()) {
        val chunk = OutputChunk(blockId = blockId, data = newText, ...)
        _outputFlow.tryEmit(chunk)
    }
}
```

**Option B**: Track per-block output separately (more complex, better isolation)

### Fix #3: Implement Exit Code Tracking

**Option A** (Quick fix): Parse `echo $?` after command:
```kotlin
currentSession?.write("$command; echo \"__EXIT__:\$?__\"\n")
// Then parse "__EXIT__:N__" from output
```

**Option B** (Better): Use shell integration for automatic exit code reporting

### Fix #4: Improve Prompt Detection

- Add timeout-based fallback (if no prompt in 10s, mark as complete)
- Support partial output scenarios
- Add more robust prompt patterns

**Changes Required**:
- File 1: `TerminalRepositoryImpl.kt` - Add @Volatile, track incremental output, add exit code parsing
- File 2: `CommandBlockViewModel.kt` - Remove output appending (not needed with incremental chunks)
- File 3: `SimplePromptDetector.kt` - Add timeout and partial output handling

**Risks**:
- Incremental output tracking adds state management complexity
- Exit code parsing might interfere with output display
- Prompt detection changes might affect edge cases

**Alternative Approaches**:
- Use a different Termux callback (e.g., onBell for command completion) - Rejected: less reliable
- Buffer output and flush periodically - Rejected: adds latency
- Use separate thread for output processing - Rejected: over-engineering

## Tech Stack Compliance

**Tech Stack File**: /home/marty/code-projects/convocli/tech-stack.md
**Validation Status**: Compliant

**Technologies Used**:
- Kotlin coroutines with @Volatile (standard JVM concurrency)
- Termux TerminalSession API (already integrated)
- Flow-based streaming (existing pattern)

All fixes use existing tech stack components.

## Metadata

**Workflow**: Bugfix (regression-test-first)
**Created By**: SpecSwarm Bugfix Workflow
**Smart Integration**: Basic mode (sequential execution)
**Related Features**: Feature 007 (Terminal Output Integration)
