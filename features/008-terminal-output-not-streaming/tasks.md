# Tasks: Bug 008 - Terminal Output Not Streaming

**Workflow**: Bugfix (Regression-Test-First)
**Status**: Active
**Created**: 2025-10-23
**Priority**: Critical

---

## Execution Strategy

**Mode**: Sequential (basic mode)
**Smart Integration**: None detected (install SpecSwarm/SpecTest for enhanced capabilities)

---

## Phase 1: Regression Test Creation

### T001: Write Regression Test - Output Streaming
**Description**: Implement Test 1 from regression-test.md (verify output streams to UI)
**File**: `app/src/androidTest/kotlin/com/convocli/terminal/TerminalOutputStreamingTest.kt`
**Test Name**: `testTerminalOutputStreamsToUI()`
**Validation**: Test code follows regression-test.md specification
**Parallel**: No (foundational)

**Implementation**:
- Create AndroidTest file
- Set up ComposeTestRule
- Initialize ViewModel with test dependencies
- Execute `echo "Test Output"` command
- Assert output appears and block completes

**Expected Result**: Test compiles successfully

---

### T002: Verify Test Fails Before Fix
**Description**: Run regression test and confirm it fails (proves infinite spinner bug exists)
**Command**: `./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.convocli.terminal.TerminalOutputStreamingTest#testTerminalOutputStreamsToUI`
**Expected**: ❌ Test FAILS with timeout error (proves bug reproduction)
**Validation**: Test failure message indicates "timeout waiting for output" or similar
**Parallel**: No (depends on T001)

**Failure Indicators**:
- Timeout after 3 seconds
- outputReceived remains false
- finalStatus remains null or EXECUTING
- Block never transitions to SUCCESS

**If test passes**: ❌ Invalid test - doesn't reproduce bug. Review test implementation.

---

## Phase 2: Bug Fix Implementation

### T003: Fix #1 - Add @Volatile to currentBlockId
**Description**: Make currentBlockId volatile for thread visibility
**File**: `app/src/main/kotlin/com/convocli/terminal/impl/TerminalRepositoryImpl.kt`
**Changes**:
```kotlin
// Line 51: Change from
private var currentBlockId: String? = null

// To
@Volatile
private var currentBlockId: String? = null
```
**Rationale**: Ensures writes from executeCommand (Dispatchers.IO) are visible to reads in onTextChanged (Termux callback thread)
**Tech Stack Validation**: ✅ Compliant (standard Kotlin/JVM concurrency)
**Parallel**: No (core fix)

---

### T004: Fix #2 - Implement Incremental Output Tracking
**Description**: Track transcript position to emit only new output (not entire history)
**File**: `app/src/main/kotlin/com/convocli/terminal/impl/TerminalRepositoryImpl.kt`
**Changes**:
```kotlin
// Add field after line 51:
@Volatile
private var lastTranscriptLength = 0

// Modify onTextChanged (line 221-240) to:
override fun onTextChanged(session: com.termux.terminal.TerminalSession) {
    try {
        val screen = session.emulator.screen
        val text = screen.transcriptText

        // Only emit new text since last update
        val newText = if (lastTranscriptLength < text.length) {
            text.substring(lastTranscriptLength)
        } else {
            ""
        }

        lastTranscriptLength = text.length

        // Only emit if we have new content and a valid block ID
        val blockId = currentBlockId
        if (blockId != null && newText.isNotEmpty()) {
            val chunk = OutputChunk(
                blockId = blockId,
                data = newText,
                streamType = StreamType.STDOUT,
                timestamp = System.currentTimeMillis()
            )
            _outputFlow.tryEmit(chunk)
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error reading terminal output", e)
    }
}
```
**Rationale**: Prevents output duplication by tracking what we've already emitted
**Tech Stack Validation**: ✅ Compliant
**Parallel**: No (depends on T003)

---

### T005: Fix #3 - Update ViewModel to Not Append Full Transcript
**Description**: Change processOutputChunk to replace (not append) since chunks are now incremental
**File**: `app/src/main/kotlin/com/convocli/ui/viewmodels/CommandBlockViewModel.kt`
**Changes**:
```kotlin
// Line 113: Change from
val newOutput = block.output + chunk.data

// To
val newOutput = if (chunk.data.startsWith(block.output)) {
    // Chunk contains full output - use it directly
    chunk.data
} else {
    // Chunk contains new output - append it
    block.output + chunk.data
}
```
**Rationale**: With incremental chunks, we append. But handle both cases for backwards compatibility.
**Tech Stack Validation**: ✅ Compliant
**Parallel**: No (depends on T004)

---

### T006: Verify Tests Pass After Fixes
**Description**: Run regression test and confirm it passes (proves bug fixed)
**Command**: `./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.convocli.terminal.TerminalOutputStreamingTest#testTerminalOutputStreamsToUI`
**Expected**: ✅ Test PASSES (proves output streaming works)
**Validation**: Test success proves infinite spinner bug is fixed
**Parallel**: No (depends on T003-T005)

**Success Indicators**:
- Test completes within 2-3 seconds
- outputReceived is true
- finalStatus is SUCCESS
- No timeouts or errors

**If test fails**: Review fixes, check logs, debug with breakpoints

---

## Phase 3: Regression Validation

### T007: Run Full Test Suite
**Description**: Verify no new regressions introduced by fixes
**Command**: `./gradlew test connectedAndroidTest`
**Expected**: ✅ All tests pass (existing + new regression test)
**Validation**: 100% test pass rate
**Parallel**: No (final validation)

**If any tests fail**:
- Review failure logs
- Check if fix broke existing functionality
- May need to adjust fix approach

---

### T008: Manual Smoke Test
**Description**: Manually test terminal functionality
**Steps**:
1. Build and install: `./gradlew installDebug`
2. Launch app
3. Execute: `echo "Hello World"`
4. Verify: Output appears, block completes with SUCCESS
5. Execute: `pwd`
6. Verify: Working directory appears
7. Execute: `ls`
8. Verify: File list appears (may be empty)

**Expected**: All commands execute correctly, no infinite spinners

**If smoke test fails**:
- Check logcat for errors
- Verify thread visibility with logging
- Check for null pointer exceptions

---

## Phase 4: Additional Regression Tests (Optional but Recommended)

### T009: Add Test #2 - Output Chunks Emitted
**Description**: Implement Test 2 from regression-test.md (verify chunks emitted from repository)
**File**: `app/src/androidTest/kotlin/com/convocli/terminal/TerminalOutputStreamingTest.kt`
**Test Name**: `testOutputChunksEmittedFromRepository()`
**Status**: Optional (validates thread visibility fix more directly)

---

### T010: Add Test #3 - No Output Duplication
**Description**: Implement Test 3 from regression-test.md (verify no exponential duplication)
**File**: `app/src/androidTest/kotlin/com/convocli/terminal/TerminalOutputStreamingTest.kt`
**Test Name**: `testOutputDoesNotDuplicate()`
**Status**: Optional (prevents regression of duplication bug)

---

### T011: Add Test #4 - Multiple Commands
**Description**: Implement Test 4 from regression-test.md (verify sequential commands work)
**File**: `app/src/androidTest/kotlin/com/convocli/terminal/TerminalOutputStreamingTest.kt`
**Test Name**: `testMultipleCommandsWorkSequentially()`
**Status**: Optional (validates overall flow)

---

## Summary

**Total Tasks**: 8 core tasks (3 optional)
**Estimated Time**: 2-3 hours
**Parallel Opportunities**: None (regression-test-first is inherently sequential)

**Success Criteria**:
- ✅ Regression test created (T001)
- ✅ Test failed before fix (T002) - proved bug reproduction
- ✅ Fixes implemented (T003-T005)
- ✅ Test passed after fix (T006) - proved bug fixed
- ✅ No new regressions (T007)
- ✅ Manual testing confirms fix (T008)

**Optional Enhancements**:
- Additional regression tests (T009-T011)
- Performance profiling
- Exit code tracking (Bug #3 - future work)
- Prompt detection improvements (Bug #4 - future work)

---

## Metadata

**Workflow**: Bugfix (regression-test-first)
**Created By**: SpecSwarm Bugfix Workflow v1.0.0
**Related Feature**: Feature 007 (Terminal Output Integration)
**Bugs Fixed**:
- Primary: Thread visibility causing infinite spinner
- Secondary: Output duplication
**Bugs Deferred**: Exit code tracking, prompt detection edge cases
