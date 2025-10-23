# Bug 008 Testing & Verification Plan

**Status**: Critical fixes applied, awaiting verification
**Date**: 2025-10-23

---

## Fixes Applied

### Critical Fixes (Completed)

1. **✅ SharedFlow Buffer Added** (`TerminalRepositoryImpl.kt:60-64`)
   - Added `extraBufferCapacity = 64` to prevent chunk dropping
   - Prevents race condition between output emission and collector setup
   - Uses `BufferOverflow.DROP_OLDEST` strategy

2. **✅ Bootstrap Existence Check** (`TerminalRepositoryImpl.kt:97-105`)
   - Checks if bash shell exists before creating session
   - Throws clear error: "Bootstrap not installed!"
   - Prevents silent failure when shell is missing

3. **✅ Emit Success Validation** (`TerminalRepositoryImpl.kt:259-262`)
   - Checks `tryEmit()` return value
   - Logs warning if chunks are dropped
   - Enables debugging of flow collection issues

4. **✅ Exception Handling Improved** (`CommandBlockViewModel.kt:157-180`)
   - Updates block to FAILURE status on exception
   - Provides user-friendly error messages
   - Prevents blocks from staying in EXECUTING forever

### Previous Fixes (From Initial Bug 008)

5. **✅ Thread Visibility** - Added `@Volatile` to `currentBlockId` and `lastTranscriptLength`
6. **✅ Incremental Output** - Implemented transcript position tracking

---

## Testing Strategy

### Phase 1: Bootstrap Verification (Priority 1)

**Purpose**: Confirm if bootstrap is installed (90% likely cause)

#### Step 1: Check Bash Existence

```bash
# Connect to device
adb shell

# Check if bash exists
ls -la /data/data/com.convocli/files/usr/bin/bash

# Expected results:
# ✅ File exists: -rwxr-xr-x ... /data/data/.../bash
# ❌ File not found: "No such file or directory"
```

**If bash exists**: Bootstrap is installed → proceed to Phase 2
**If bash missing**: Bootstrap NOT installed → proceed to Bootstrap Installation

#### Step 2: Check Logcat for Bootstrap Errors

```bash
# Clear logcat
adb logcat -c

# Build and install app
./gradlew installDebug

# Monitor logcat in real-time
adb logcat | grep -E "TerminalRepository|Bootstrap|CommandBlockViewModel"

# Launch app and try to execute a command
# Look for these error messages:
# ❌ "Bash shell not found at ... - Bootstrap not installed!"
# ❌ "Failed to create terminal session"
# ✅ "Terminal session created successfully"
```

**Expected Behavior**:
- **If bootstrap missing**: Should see "Bootstrap not installed!" error immediately when session creates
- **If bootstrap exists**: Should see "Session created successfully"

---

### Phase 2: Output Streaming Verification (Priority 2)

**Purpose**: Verify SharedFlow buffer and output streaming work correctly

**Prerequisites**: Bootstrap must be installed

#### Step 1: Monitor Output Chunks

```bash
# Monitor logcat with focus on output flow
adb logcat | grep -E "onTextChanged|OutputChunk|tryEmit|Failed to emit"

# In app, execute: echo "Hello World"

# Expected log output:
# ✅ "onTextChanged called! transcript length: X"
# ✅ "Chunk emission: true, blockId=abc123, data=Hello World"
# ❌ "Failed to emit chunk - buffer full or no collectors!" (shouldn't see this)
```

**Success Criteria**:
- `onTextChanged` is called
- `tryEmit()` returns true (chunk emitted successfully)
- No "Failed to emit chunk" warnings

**Failure Indicators**:
- `onTextChanged` never called → Shell not running
- `tryEmit()` returns false → Collector not ready (race condition)

#### Step 2: Test Command Execution

```bash
# Build and install
./gradlew installDebug

# Launch app
# Execute these commands in sequence:
1. echo "Hello World"
2. pwd
3. date
4. ls -la

# For each command, verify:
# ✅ Output appears in UI within 1-2 seconds
# ✅ Spinner stops (block completes)
# ✅ Status shows SUCCESS (green checkmark)
# ✅ No infinite spinner
```

**Success Criteria**:
- All commands execute and complete
- Output appears correctly
- No blocks stuck in EXECUTING
- No infinite spinners

**Failure Scenarios**:
- Spinner runs forever → Check logcat for root cause
- No output appears → Check "Failed to emit chunk" logs
- Error message shown → Read error message for diagnosis

---

### Phase 3: Edge Case Testing (Priority 3)

**Purpose**: Verify fixes handle edge cases

#### Test 1: Rapid Commands

```bash
# Execute 5 commands rapidly:
echo "1"
echo "2"
echo "3"
echo "4"
echo "5"

# Verify:
# ✅ All 5 blocks complete
# ✅ No output cross-contamination
# ✅ No chunks dropped (check logcat)
```

#### Test 2: Long Output

```bash
# Execute command with long output:
ls -laR /system

# Verify:
# ✅ All output captured
# ✅ No buffer overflow warnings
# ✅ UI remains responsive
```

#### Test 3: Multi-line Output

```bash
# Execute:
echo "Line 1"; echo "Line 2"; echo "Line 3"

# Verify:
# ✅ All three lines appear exactly once
# ✅ No duplication
# ✅ Correct line breaks preserved
```

---

## Bootstrap Installation

**If bootstrap is NOT installed**, you need to install it before terminal will work.

### Option 1: Feature 003 Integration (Recommended)

Feature 003 (Bootstrap Installation) exists but is not yet integrated into the UI flow. You can:

1. Implement Feature 003 UI trigger
2. Auto-install bootstrap on first launch
3. Show installation progress

### Option 2: Manual Installation (Temporary Workaround)

```bash
# Use Termux to bootstrap the environment
# This is a temporary solution for testing

# 1. Install official Termux app
# 2. Open Termux (this installs bootstrap)
# 3. Check if bootstrap files exist:
ls -la /data/data/com.termux/files/usr/bin/bash

# 4. Copy to ConvoCLI directory (requires root):
adb shell
su
cp -r /data/data/com.termux/files/usr /data/data/com.convocli/files/
chown -R com.convocli:com.convocli /data/data/com.convocli/files/usr

# 5. Test ConvoCLI again
```

**Note**: Manual installation is hacky and not sustainable. Proper solution is to integrate Feature 003.

### Option 3: Create Bootstrap Installation Flow

Create a simple bootstrap installation workflow:

1. Add bootstrap check to ViewModel init
2. If missing, show installation dialog
3. Download bootstrap from Termux mirrors
4. Extract to `/data/data/com.convocli/files/usr`
5. Set proper permissions
6. Create session after installation completes

---

## Diagnostic Checklist

Use this checklist to diagnose the infinite spinner issue:

### ✅ Verification Checklist

- [ ] Bootstrap installed (bash file exists)
- [ ] Session creates successfully (check logcat)
- [ ] `onTextChanged` callback fires (check logcat)
- [ ] Output chunks emitted (check logcat)
- [ ] `tryEmit()` returns true (check logcat)
- [ ] No "Failed to emit chunk" warnings
- [ ] Block output updates in UI
- [ ] Block status changes from EXECUTING to SUCCESS
- [ ] Prompt detection works (check logcat)
- [ ] Exit code retrieved (check logcat)

### ❌ Common Failure Patterns

**Pattern 1: Bootstrap Missing**
- Symptom: Session creates but no output ever appears
- Logcat: "Bootstrap not installed!" error
- Fix: Install bootstrap via Feature 003

**Pattern 2: Collector Not Ready**
- Symptom: First command fails, subsequent commands work
- Logcat: "Failed to emit chunk" warnings
- Fix: Already applied (SharedFlow buffer)

**Pattern 3: Thread Visibility**
- Symptom: Intermittent failures
- Logcat: Chunks emitted but blockId is null
- Fix: Already applied (@Volatile)

**Pattern 4: Output Duplication**
- Symptom: Output repeats exponentially
- Logcat: Transcript length grows rapidly
- Fix: Already applied (incremental tracking)

---

## Expected Results

### If Bootstrap IS Installed

**Expected behavior**:
1. ✅ Commands execute and complete within 1-2 seconds
2. ✅ Output streams to UI smoothly
3. ✅ Blocks transition: PENDING → EXECUTING → SUCCESS
4. ✅ No infinite spinners
5. ✅ No errors in logcat

**If still broken**: Check logcat for "Failed to emit chunk" → collector race condition

### If Bootstrap NOT Installed

**Expected behavior**:
1. ❌ Error message appears: "Terminal not ready: Bash shell not installed."
2. ❌ Block status changes to FAILURE immediately
3. ❌ No infinite spinner (block completes with FAILURE)
4. ✅ Clear error in logcat: "Bootstrap not installed!"

**Action**: Install bootstrap before proceeding

---

## Next Steps

### Path A: Bootstrap Installed (Commands Work)

1. ✅ Bug 008 is fully resolved
2. Mark Bug 008 as complete
3. Continue with Feature 007 Phase 4 tasks
4. Consider adding optional regression tests

### Path B: Bootstrap Installed (Commands Still Fail)

1. Review logcat for "Failed to emit chunk" warnings
2. Check collector timing (may need to delay command execution)
3. Add more logging to trace execution flow
4. May need to adjust SharedFlow buffer size

### Path C: Bootstrap NOT Installed

1. **PRIORITY**: Integrate Feature 003 bootstrap installation
2. Options:
   - Auto-install on first launch
   - Show installation UI with progress
   - Download from Termux mirrors
3. After installation, retry Bug 008 testing

---

## Rollback Plan

If fixes cause new issues, rollback to previous state:

```bash
git checkout HEAD~1 app/src/main/kotlin/com/convocli/terminal/impl/TerminalRepositoryImpl.kt
git checkout HEAD~1 app/src/main/kotlin/com/convocli/ui/viewmodels/CommandBlockViewModel.kt
./gradlew clean build
```

---

## Confidence Levels

**HIGH CONFIDENCE (90%+)**: Bootstrap not installed is the primary cause
- Evidence: No shell = no output = infinite spinner
- Fix: Install bootstrap via Feature 003

**HIGH CONFIDENCE (95%+)**: SharedFlow buffer prevents race condition
- Evidence: Zero buffer = dropped chunks if collector not ready
- Fix: Already applied (extraBufferCapacity=64)

**MEDIUM CONFIDENCE (60%+)**: Session creation race is minor issue
- Evidence: Possible but less likely (session creates fast)
- Fix: Already applied (FAILURE status on exception)

---

## Summary

**Critical fixes applied**:
1. ✅ SharedFlow buffer (prevents chunk dropping)
2. ✅ Bootstrap existence check (clear error message)
3. ✅ Emit validation (debugging capability)
4. ✅ Exception handling (no stuck blocks)

**Most likely outcome**: Bootstrap is not installed, and you'll see the error message immediately when trying to execute a command.

**Action required**: Follow Phase 1 testing to confirm bootstrap status, then either:
- Install bootstrap if missing
- Proceed to Phase 2 testing if installed

**Estimated time to resolution**:
- If bootstrap missing: 30-60 min to integrate Feature 003
- If bootstrap installed: 5-10 min testing should confirm fix works
