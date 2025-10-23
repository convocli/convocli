# Testing Guide: Termux Integration

**Feature**: 002 - Termux Integration - Terminal Emulator Core
**Phase**: Phase 3 - User Story 1: Execute Basic Shell Commands
**Task**: T018 - Integration Test for Basic Command Execution
**Status**: Complete

---

## Test Structure

### 1. Unit Tests (`app/src/test/`)

**Location**: `app/src/test/kotlin/com/convocli/terminal/`

#### FakeTerminalRepository.kt
A test double for `TerminalRepository` that allows testing ViewModel logic without real Termux integration.

**Features**:
- Controllable session creation results
- Tracks executed commands
- Tracks destroyed sessions
- Can emit output and errors on demand
- Fully reset-able for test isolation

**Usage Example**:
```kotlin
@Test
fun testExample() = runTest {
    val fakeRepo = FakeTerminalRepository()
    fakeRepo.sessionCreationResult = Result.success("test-id")

    val viewModel = TerminalViewModel(fakeRepo)
    advanceUntilIdle()

    assertTrue(viewModel.isSessionReady.value)
}
```

#### TerminalViewModelTest.kt
Comprehensive unit tests for `TerminalViewModel` using `FakeTerminalRepository`.

**Test Coverage** (14 tests):
- ✅ Session creation success
- ✅ Session creation failure
- ✅ Command execution
- ✅ Blank command handling
- ✅ Command execution when session not ready
- ✅ Output flow collection
- ✅ Error flow collection
- ✅ Session restart
- ✅ Clear output
- ✅ Dismiss error
- ✅ Get session state
- ✅ isExecuting state management
- ✅ SessionCrashed error handling

**Running Unit Tests**:
```bash
./gradlew testDebugUnitTest
```

Or in Android Studio:
- Right-click `TerminalViewModelTest.kt` → Run

### 2. Integration Tests (`app/src/androidTest/`)

**Location**: `app/src/androidTest/kotlin/com/convocli/terminal/`

#### TerminalIntegrationTest.kt
Tests real `TermuxTerminalRepository` with actual Termux library integration.

**Current Test Coverage** (Before Bootstrap Installation):
- ✅ Session creation fails gracefully when bash not installed
- ✅ Get session state for non-existent session
- ✅ Destroy non-existent session doesn't crash
- ✅ Error flow is accessible
- ✅ Multiple session creation attempts
- ✅ Output flow is accessible

**Expected Behavior**:
- All tests verify graceful failure when Termux bootstrap is NOT installed
- Tests document the error handling behavior
- Future tests (commented out) show expected behavior after bootstrap installation

**Running Integration Tests**:
```bash
./gradlew connectedAndroidTest
```

Or in Android Studio:
- Right-click `TerminalIntegrationTest.kt` → Run
- Requires: Android device or emulator

---

## Test Dependencies

All required test dependencies are already configured in `app/build.gradle.kts`:

```kotlin
dependencies {
    // Unit Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.20")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("app.cash.turbine:turbine:1.0.0")

    // Instrumented Testing
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
```

**Key Testing Libraries**:
- `kotlinx-coroutines-test` - Test utilities for coroutines and Flows
- `turbine` - Flow testing library (makes testing StateFlow/SharedFlow easy)
- `androidx.test.ext:junit` - AndroidX testing framework
- `kotlin-test` - Kotlin testing assertions

---

## Current Test Status

### ✅ What Works
- Unit tests verify ViewModel logic with fake repository
- Integration tests verify graceful failure when bootstrap not installed
- Error handling is properly tested
- Flow collection is tested with turbine

### ⏳ What's Expected to Fail (Until Bootstrap Installed)
- Session creation with real Termux will fail (bash executable doesn't exist)
- Command execution requires working session (not available yet)
- Output streaming requires working session (not available yet)

### 🔜 Future Tests (After Feature 003/004 - Bootstrap Installation)
The following tests are commented out in `TerminalIntegrationTest.kt`:
- `testSessionCreation_afterBootstrap_succeeds()`
- `testCommandExecution_simpleCommand_producesOutput()`
- `testCommandExecution_multipleCommands_allExecute()`
- `testSessionDestroy_cleanupsProperly()`

**To enable**: Uncomment tests after bootstrap is installed and verified working.

---

## Running Tests

### Run All Unit Tests
```bash
./gradlew test
```

### Run All Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

### Run Specific Test Class
```bash
# Unit test
./gradlew test --tests TerminalViewModelTest

# Instrumented test
./gradlew connectedAndroidTest --tests TerminalIntegrationTest
```

### Run Specific Test Method
```bash
# Unit test
./gradlew test --tests TerminalViewModelTest.createSession_success_sets_isSessionReady_to_true

# Instrumented test
./gradlew connectedAndroidTest --tests TerminalIntegrationTest.testSessionCreation_beforeBootstrap_failsGracefully
```

### In Android Studio
1. **Run all tests in a file**:
   - Right-click test file → "Run 'TerminalViewModelTest'"

2. **Run a single test**:
   - Click green arrow next to `@Test` method
   - Or right-click test method → "Run 'testName()'"

3. **Run with coverage**:
   - Right-click test file → "Run 'TerminalViewModelTest' with Coverage"

---

## Test Reports

After running tests, reports are generated at:

**Unit Tests**:
- `app/build/reports/tests/testDebugUnitTest/index.html`

**Instrumented Tests**:
- `app/build/reports/androidTests/connected/index.html`

**Coverage Reports** (if enabled):
- `app/build/reports/coverage/test/debug/index.html`

---

## Troubleshooting

### Unit Tests Fail with "No such file or directory"
- Ensure you're running from project root
- Check Gradle wrapper exists: `./gradlew --version`

### Integration Tests Fail to Start
- Ensure device/emulator is connected: `adb devices`
- Check test APK installed: `./gradlew :app:assembleDebugAndroidTest`

### "Method X not found" Errors
- Sync Gradle: Android Studio → File → Sync Project with Gradle Files
- Clean build: `./gradlew clean`

### Turbine Flow Tests Timeout
- Increase timeout in test: `withTimeout(5.seconds)`
- Ensure `advanceUntilIdle()` is called after async operations

---

## Next Steps

After Feature 003/004 (Bootstrap Installation) is complete:

1. **Update Integration Tests**:
   - Uncomment future test cases
   - Verify session creation succeeds
   - Test actual command execution
   - Test output streaming

2. **Add More Test Cases**:
   - Long-running commands
   - Interactive programs
   - Error output (stderr)
   - Session state transitions

3. **Performance Tests**:
   - Command execution latency
   - Output streaming throughput
   - Memory usage during long sessions

4. **UI Tests**:
   - Test ViewModel integration with Compose UI
   - Test command input handling
   - Test output display

---

## Test Best Practices

1. **Use `runTest` for coroutine tests**:
   ```kotlin
   @Test
   fun myTest() = runTest {
       // Test code
   }
   ```

2. **Always call `advanceUntilIdle()` after async operations**:
   ```kotlin
   viewModel.executeCommand("ls")
   advanceUntilIdle() // Wait for coroutines to complete
   ```

3. **Use Turbine for Flow testing**:
   ```kotlin
   viewModel.output.test {
       assertEquals("", awaitItem()) // Initial value
       viewModel.executeCommand("echo test")
       assertEquals("test", awaitItem()) // After command
   }
   ```

4. **Reset state between tests**:
   ```kotlin
   @Before
   fun setup() {
       repository = FakeTerminalRepository()
       repository.reset() // Clean slate for each test
   }
   ```

5. **Test error cases, not just happy paths**:
   - Session creation failure
   - Command execution when not ready
   - Session crashes
   - Network/PTY errors

---

**Documentation Status**: ✅ Complete
**Tests Written**: 14 unit tests + 6 integration tests
**Test Coverage**: ViewModel logic, Repository integration, Error handling
**Ready for**: Feature 003/004 (Bootstrap Installation)
