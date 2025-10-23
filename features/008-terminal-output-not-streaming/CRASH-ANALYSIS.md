# App Crash Analysis - Bug 008 Extended

**Date**: 2025-10-23
**Status**: Critical - App crashing on startup
**Branch**: sprint-02

## Executive Summary

The app is crashing on startup due to **blocking I/O operations on the main thread** during ViewModel initialization. The terminal session creation attempt (which fails due to missing bootstrap) is causing a 3+ second freeze on the main thread, leading to the InputDispatcher timeout and channel breakage.

---

## Error Sequence Analysis

### 1. Input Channel Broken (Critical Error)
```
16:34:59.017 InputDispatcher E  channel 'cd7e87b com.convocli/com.convocli.MainActivity'
                                ~ Channel is unrecoverably broken and will be disposed!
PROCESS ENDED (17836)
```

**What This Means**: The Android system killed the app because the main thread was unresponsive for too long (>5 seconds). The InputDispatcher couldn't communicate with the activity, so it marked the channel as "unrecoverably broken" and terminated the process.

### 2. Process Restart & Immediate Freeze
```
PROCESS STARTED (17973)
16:36:36.311 HWUI I  Davey! duration=3416ms
16:36:36.336 Choreographer I  Skipped 195 frames!
```

**What This Means**: After restarting, the app immediately froze again for 3.4 seconds, skipping 195 frames (should be 60fps = ~3.25 seconds of freeze). The main thread is completely blocked.

### 3. Multiple Frame Skips
```
Skipped 195 frames! (3.25 seconds)
Skipped 107 frames! (1.78 seconds)
Skipped 43 frames! (0.72 seconds)
```

**What This Means**: The app is repeatedly freezing the UI thread, causing janky/frozen user experience.

---

## Root Cause Deep Dive

### Stack Trace Analysis

```kotlin
at com.convocli.ui.viewmodels.CommandBlockViewModel.<init>(CommandBlockViewModel.kt:53)
at com.convocli.DaggerConvoCLIApplication_HiltComponents_SingletonC$ViewModelCImpl$SwitchingProvider.get(...)
at androidx.lifecycle.viewmodel.ViewModelProviderImpl.getViewModel$lifecycle_viewmodel_release(...)
at com.convocli.ui.screens.CommandBlocksScreenKt.CommandBlocksScreen(CommandBlocksScreen.kt:127)
at androidx.compose.runtime.ComposerImpl.doCompose-aFTiNEg(Composer.kt:3843)
```

**The Problem Flow**:
1. Compose starts rendering `CommandBlocksScreen`
2. Compose creates `CommandBlockViewModel` via Hilt
3. ViewModel's `init` block **immediately** starts:
   ```kotlin
   init {
       viewModelScope.launch {  // LINE 53
           terminalRepository.createSession().collect { ... }
       }
   }
   ```
4. `createSession()` is a `Flow` that does file I/O **synchronously**:
   ```kotlin
   override fun createSession(): Flow<TerminalSessionState> = flow {
       // CHECK FILE EXISTS - BLOCKING I/O ON MAIN THREAD!
       val shellFile = java.io.File(shellPath)  // LINE 99
       if (!shellFile.exists()) {  // LINE 100 - BLOCKING FILE SYSTEM CHECK!
           throw IllegalStateException(...)  // LINE 105
       }
   }
   ```

### The Critical Mistake

**The `flow { }` builder does NOT automatically run on a background thread!**

When you write:
```kotlin
flow {
    val file = java.io.File(path)
    if (!file.exists()) { ... }  // ← THIS RUNS ON THE COLLECTOR'S DISPATCHER!
    emit(...)
}
```

...and then collect it from the main thread:
```kotlin
viewModelScope.launch {  // viewModelScope = Dispatchers.Main.immediate by default!
    terminalRepository.createSession().collect { ... }
}
```

**Result**: The file I/O runs on the main thread, freezing the UI!

---

## Why This Causes a Crash (Not Just Lag)

### Android ANR (Application Not Responding) Rules

1. **5-second rule**: If the main thread is blocked for >5 seconds, Android kills the app
2. **Input event timeout**: If the app doesn't respond to input events within 5 seconds, InputDispatcher marks the channel as broken
3. **Compose initialization**: ViewModel creation happens during composition, which is on the main thread

### The Blocking Chain

```
Main Thread (UI Thread)
  ↓
Compose starts rendering
  ↓
Compose creates ViewModel (synchronous!)
  ↓
ViewModel init block launches coroutine
  ↓
Coroutine collects from createSession() Flow
  ↓
Flow does file I/O (java.io.File.exists())
  ↓
File system check on main thread (BLOCKING!)
  ↓
Main thread frozen for 3+ seconds
  ↓
InputDispatcher timeout
  ↓
CRASH: "Channel is unrecoverably broken"
```

---

## Additional Contributing Factors

### 1. SurfaceSyncGroup Timeouts
```
16:36:35.419 SurfaceSyncGroup E  Failed to receive transaction ready in 1000ms
```
This indicates the rendering pipeline is also blocked waiting for the frozen main thread.

### 2. Multiple Davey Violations
- **First Davey**: 3416ms freeze
- **Second Davey**: 2144ms freeze
- **Third Davey**: 2012ms freeze
- **Fourth Davey**: 833ms freeze

This suggests the app is repeatedly trying to initialize and failing, each time blocking the main thread.

### 3. Profile Installer Running
```
16:36:39.246 ProfileInstaller D  Installing profile for com.convocli
```
This runs AFTER the crashes, indicating the app never fully initialized properly.

---

## The Real Bug

**It's not the exception - it's the blocking I/O during ViewModel construction!**

Even if bootstrap were installed and the exception didn't occur, the file system checks would still cause UI freezes. The exception just makes it worse by forcing the Flow to throw during composition.

---

## Solution Strategy

### Option 1: Lazy Terminal Session Initialization (RECOMMENDED)

**Problem**: We're creating the terminal session eagerly in `init`, even though the user hasn't tried to run a command yet.

**Solution**: Only create the session when actually needed.

```kotlin
@HiltViewModel
class CommandBlockViewModel @Inject constructor(...) : ViewModel() {

    private var isSessionInitialized = false

    // Remove init block - don't create session eagerly!

    fun executeCommand(command: String) {
        viewModelScope.launch {
            // Lazy initialization on first command
            if (!isSessionInitialized) {
                initializeSession()
            }

            if (uiState.value.isSessionReady) {
                // Execute command...
            }
        }
    }

    private suspend fun initializeSession() {
        withContext(Dispatchers.IO) {  // FORCE background thread
            terminalRepository.createSession()
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isSessionReady = false,
                            error = "Failed to create terminal session: ${e.message}"
                        )
                    }
                }
                .collect { sessionState ->
                    when (sessionState) {
                        is TerminalSessionState.Ready -> {
                            isSessionInitialized = true
                            _uiState.update { it.copy(isSessionReady = true) }
                        }
                        // ...
                    }
                }
        }
    }
}
```

### Option 2: Move File I/O to Background Dispatcher in Repository

**Problem**: `createSession()` Flow does blocking I/O without specifying a dispatcher.

**Solution**: Force the Flow to run on Dispatchers.IO.

```kotlin
override fun createSession(): Flow<TerminalSessionState> = flow {
    try {
        Log.d(TAG, "Creating terminal session...")

        // ... setup code ...

        // Check if bash exists (CRITICAL: This must run on background thread!)
        val shellFile = java.io.File(shellPath)
        if (!shellFile.exists()) {
            throw IllegalStateException("Bash shell not found...")
        }

        // ... rest of creation ...

    } catch (e: Exception) {
        Log.e(TAG, "Failed to create terminal session", e)
        emit(TerminalSessionState.Error("Failed to create session: ${e.message}", e))
    }
}.flowOn(Dispatchers.IO)  // ← FORCE this Flow to run on background thread!
```

### Option 3: Use LaunchedEffect in Composable Instead of ViewModel Init

**Problem**: ViewModel init runs synchronously during composition.

**Solution**: Move session creation to a `LaunchedEffect` in the Composable.

```kotlin
@Composable
fun CommandBlocksScreen(
    viewModel: CommandBlockViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Initialize session asynchronously, not in ViewModel init
    LaunchedEffect(Unit) {
        viewModel.initializeSession()
    }

    // ... rest of UI ...
}
```

---

## Recommended Fix (Hybrid Approach)

**Combine Option 1 + Option 2 for maximum safety:**

1. **Fix the Repository** - Add `.flowOn(Dispatchers.IO)` to createSession()
2. **Fix the ViewModel** - Remove eager initialization from init block
3. **Add lazy initialization** - Only create session when needed
4. **Add proper error boundaries** - Use try/catch and .catch() operators

---

## Priority Assessment

**CRITICAL - P0**: This is a crash on app startup. The app is completely unusable until this is fixed.

**Impact**:
- App crashes immediately on launch
- User cannot use the app at all
- No workaround available (even with bootstrap installed, the file I/O blocking would still cause freezes)

**Affected Users**: 100% of users

---

## Testing Strategy

### Before Fix
```bash
./gradlew installDebug
# Launch app
# Expected: Crash with "Channel is unrecoverably broken"
# Observed: ✅ Confirmed crash
```

### After Fix
```bash
./gradlew installDebug
# Launch app
# Expected:
# - App starts immediately (<500ms)
# - No frame drops on startup
# - Error message displays in input field
# - No crashes
```

### Performance Verification
```bash
adb shell am start -W com.convocli/.MainActivity
# Should show TotalTime < 500ms

adb logcat | grep -E "Davey|Skipped.*frames"
# Should show no Davey violations on startup
```

---

## Implementation Plan

**Phase 1: Emergency Fix (Today)**
1. Add `.flowOn(Dispatchers.IO)` to `createSession()` in TerminalRepositoryImpl.kt
2. Test app startup - should eliminate crash

**Phase 2: Proper Architecture (Next)**
1. Remove eager session initialization from ViewModel init
2. Add lazy initialization on first command execution
3. Add proper error boundaries with .catch() operators

**Phase 3: Optimization (Future)**
1. Add session pooling/caching
2. Optimize file system checks
3. Add startup performance monitoring

---

## Code Locations

**Files to Modify**:
1. `app/src/main/kotlin/com/convocli/terminal/impl/TerminalRepositoryImpl.kt:75-143`
   - Add `.flowOn(Dispatchers.IO)` to createSession()

2. `app/src/main/kotlin/com/convocli/ui/viewmodels/CommandBlockViewModel.kt:51-84`
   - Remove eager initialization from init block
   - Add lazy initialization method

---

## References

- Android ANR Documentation: https://developer.android.com/topic/performance/vitals/anr
- Kotlin Flow Threading: https://kotlinlang.org/docs/flow.html#flow-context
- Compose Side Effects: https://developer.android.com/jetpack/compose/side-effects
- ViewModel Best Practices: https://developer.android.com/topic/libraries/architecture/viewmodel
