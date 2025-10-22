# Quickstart: Terminal Integration

**Feature 002**: Termux Integration - Terminal Emulator Core for Android Command Execution

This guide provides a quick introduction to using the terminal integration in ConvoCLI.

---

## Table of Contents

1. [Setup](#setup)
2. [Basic Usage](#basic-usage)
3. [Advanced Features](#advanced-features)
4. [Testing](#testing)
5. [Troubleshooting](#troubleshooting)
6. [API Reference](#api-reference)

---

## Setup

### Prerequisites

- Android API 26+ (Android 8.0 Oreo or higher)
- Kotlin 1.9.20+
- Android Studio Hedgehog (2023.1.1) or newer

### Dependencies

The terminal integration requires:

```kotlin
// build.gradle.kts (app module)
dependencies {
    // Termux terminal emulator
    implementation("com.github.termux.termux-app:terminal-emulator:v0.118.3")

    // Hilt for dependency injection
    implementation("com.google.dagger:hilt-android:2.48")
    ksp("com.google.dagger:hilt-compiler:2.48")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // DataStore for session persistence
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}
```

---

## Basic Usage

### 1. Inject Terminal Repository

```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val terminalRepository: TerminalRepository
) : ViewModel() {
    // ViewModel implementation
}
```

### 2. Create a Terminal Session

```kotlin
viewModelScope.launch {
    terminalRepository.createSession()
        .onSuccess { sessionId ->
            // Session created successfully
            println("Session ID: $sessionId")
        }
        .onFailure { exception ->
            // Handle error
            println("Failed to create session: ${exception.message}")
        }
}
```

### 3. Execute Commands

```kotlin
suspend fun executeCommand(sessionId: String, command: String) {
    terminalRepository.executeCommand(sessionId, command)
}

// Example:
executeCommand(sessionId, "ls -la")
executeCommand(sessionId, "cd /home")
executeCommand(sessionId, "pwd")
```

### 4. Observe Terminal Output

```kotlin
viewModelScope.launch {
    terminalRepository.observeOutput(sessionId)
        .collect { output ->
            when (output.stream) {
                StreamType.STDOUT -> {
                    println("Output: ${output.text}")
                }
                StreamType.STDERR -> {
                    println("Error: ${output.text}")
                }
            }
        }
}
```

### 5. Handle Errors

```kotlin
viewModelScope.launch {
    terminalRepository.observeErrors()
        .collect { error ->
            when (error) {
                is TerminalError.InitializationFailed -> {
                    // Handle session creation failure
                    showError("Failed to initialize: ${error.reason}")
                }
                is TerminalError.CommandFailed -> {
                    // Handle command failure
                    showError("Command failed: ${error.stderr}")
                }
                is TerminalError.SessionCrashed -> {
                    // Handle session crash
                    showError("Session crashed: ${error.reason}")
                    offerRestart()
                }
                is TerminalError.IOError -> {
                    // Handle I/O error
                    showError("I/O error: ${error.message}")
                }
            }
        }
}
```

### 6. Clean Up Session

```kotlin
override fun onCleared() {
    super.onCleared()
    viewModelScope.launch {
        terminalRepository.destroySession(sessionId)
    }
}
```

---

## Advanced Features

### Session Persistence & Restoration

Sessions automatically persist across app restarts:

```kotlin
// On app start, check for saved session
viewModelScope.launch {
    val savedState = terminalRepository.getSavedSessionState().first()

    val sessionId = if (savedState != null) {
        // Restore previous session
        terminalRepository.restoreSession(savedState).getOrThrow()
    } else {
        // Create new session
        terminalRepository.createSession().getOrThrow()
    }
}
```

### Working Directory Tracking

Track current working directory:

```kotlin
viewModelScope.launch {
    terminalRepository.observeWorkingDirectory(sessionId)
        .collect { directory ->
            println("Current directory: $directory")
            updateUI(directory)
        }
}
```

### Session State Monitoring

Monitor session state:

```kotlin
val state = terminalRepository.getSessionState(sessionId)
when (state) {
    SessionState.RUNNING -> println("Session is active")
    SessionState.STOPPED -> println("Session has stopped")
    SessionState.ERROR -> println("Session encountered an error")
    null -> println("Session not found")
}
```

---

## Testing

### Unit Tests

Test your ViewModel with `FakeTerminalRepository`:

```kotlin
@Test
fun testCommandExecution() = runTest {
    // Given: Fake repository
    val fakeRepository = FakeTerminalRepository()
    fakeRepository.sessionCreationResult = Result.success("test-session")

    val viewModel = MyViewModel(fakeRepository)
    advanceUntilIdle()

    // When: Execute command
    viewModel.executeCommand("ls")
    advanceUntilIdle()

    // Then: Command should be recorded
    assertEquals(1, fakeRepository.executedCommands.size)
    assertEquals("ls", fakeRepository.executedCommands[0].second)
}
```

### Integration Tests

Test with real Termux integration:

```kotlin
@RunWith(AndroidJUnit4::class)
class TerminalIntegrationTest {
    private lateinit var repository: TerminalRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        repository = TermuxTerminalRepository(context)
    }

    @Test
    fun testSessionCreation() = runTest {
        val result = repository.createSession()

        // Note: Will fail until Termux bootstrap is installed
        // See Features 003/004 for bootstrap installation
        assert(result.isFailure) {
            "Session creation should fail when bash is not installed"
        }
    }
}
```

---

## Troubleshooting

### Session Creation Fails

**Problem**: `createSession()` returns failure

**Causes**:
- Termux bootstrap not installed (bash executable missing)
- Insufficient permissions
- Out of memory

**Solution**:
1. Verify bootstrap installation (Features 003/004)
2. Check logcat for detailed error messages
3. Ensure app has necessary permissions

### Commands Don't Execute

**Problem**: Commands sent but no output received

**Causes**:
- Session not in RUNNING state
- Shell process died
- PTY connection broken

**Solution**:
1. Check session state: `getSessionState(sessionId)`
2. Monitor errors: `observeErrors()`
3. Restart session if crashed

### Output Not Appearing

**Problem**: Terminal output not emitted to Flow

**Causes**:
- Not collecting output Flow
- Collection cancelled
- Backpressure issues

**Solution**:
1. Ensure Flow collection is active
2. Use proper coroutine scope
3. Handle backpressure with buffer operators

### Session Doesn't Persist

**Problem**: Session lost after app restart

**Causes**:
- DataStore not initialized
- Session state not saved
- Permissions issue

**Solution**:
1. Verify `SessionStateStore` is initialized
2. Check DataStore writes are completing
3. Enable debug logging for persistence

---

## API Reference

### Core Interfaces

- **[TerminalRepository](../../app/src/main/kotlin/com/convocli/terminal/repository/TerminalRepository.kt)**: Main terminal operations interface
- **[TerminalViewModel](../../app/src/main/kotlin/com/convocli/terminal/viewmodel/TerminalViewModel.kt)**: ViewModel for UI integration

### Data Models

- **[TerminalSession](../../app/src/main/kotlin/com/convocli/terminal/model/TerminalSession.kt)**: Session metadata
- **[TerminalOutput](../../app/src/main/kotlin/com/convocli/terminal/model/TerminalOutput.kt)**: Output events
- **[TerminalError](../../app/src/main/kotlin/com/convocli/terminal/model/TerminalError.kt)**: Error types
- **[SessionState](../../app/src/main/kotlin/com/convocli/terminal/model/SessionState.kt)**: Session states

### Services

- **[OutputStreamProcessor](../../app/src/main/kotlin/com/convocli/terminal/service/OutputStreamProcessor.kt)**: Stderr detection
- **[CommandMonitor](../../app/src/main/kotlin/com/convocli/terminal/service/CommandMonitor.kt)**: Command failure tracking
- **[WorkingDirectoryTracker](../../app/src/main/kotlin/com/convocli/terminal/service/WorkingDirectoryTracker.kt)**: Directory tracking
- **[SessionStateStore](../../app/src/main/kotlin/com/convocli/terminal/data/datastore/SessionStateStore.kt)**: Session persistence

### Full Documentation

- [Feature Specification](./spec.md)
- [Implementation Plan](./plan.md)
- [Task Breakdown](./tasks.md)
- [Data Model Documentation](./data-model.md)
- [Testing Guide](../../docs/TESTING_GUIDE.md)

---

## Next Steps

1. **Install Termux Bootstrap** (Features 003/004)
   - Required for actual command execution
   - Provides bash, coreutils, and other Linux utilities

2. **Implement Command Blocks UI** (Feature 005+)
   - Chat-like terminal interface
   - Warp 2.0-inspired design

3. **Add ConvoSync** (Future features)
   - Cross-device session synchronization
   - Cloud backup and restore

---

## Questions or Issues?

- Review the [spec.md](./spec.md) for detailed requirements
- Check [TESTING_GUIDE.md](../../docs/TESTING_GUIDE.md) for testing strategies
- See [COMPLETION_SUMMARY.md](./COMPLETION_SUMMARY.md) for implementation details
