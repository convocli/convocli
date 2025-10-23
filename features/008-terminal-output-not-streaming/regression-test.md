# Regression Test: Bug 008 - Terminal Output Not Streaming

**Purpose**: Prove bug exists (output not streaming), validate fix, prevent future regressions

**Test Type**: Integration Test (UI + ViewModel + Repository + Termux)
**Created**: 2025-10-23

---

## Test Objective

Write tests that:
1. ✅ **Fail before fix** (proves infinite spinner bug exists)
2. ✅ **Pass after fix** (proves output streaming works)
3. ✅ **Prevent regression** (catches if thread visibility or duplication bugs return)

---

## Test Specification

### Test 1: Terminal Output Streams to UI

**Purpose**: Verify that command output actually reaches the UI layer

#### Test Setup

- Create test TerminalRepository implementation
- Create test CommandBlockManager implementation
- Initialize CommandBlockViewModel with test dependencies
- Set up test coroutine dispatcher
- Create mock Termux session (or use real one in instrumented test)

#### Test Execution

1. Execute simple command: `echo "Test Output"`
2. Wait for output to arrive (with timeout)
3. Verify block transitions from PENDING → EXECUTING → SUCCESS
4. Verify output contains "Test Output"

#### Test Assertions

- ✅ Block status transitions correctly (not stuck in EXECUTING)
- ✅ Output appears in block.output within reasonable time (< 2 seconds)
- ✅ Final status is SUCCESS (not stuck loading)
- ✅ Prompt is detected

#### Test Teardown

- Clean up terminal session
- Clear database
- Cancel coroutines

---

### Test 2: Output Chunks Are Emitted from Repository

**Purpose**: Verify thread visibility fix - chunks are emitted from Termux callback

#### Test Setup

- Create TerminalRepositoryImpl with real Termux session
- Collect output chunks in test
- Set up command block ID

#### Test Execution

1. Call `repository.executeCommand("echo Hello", blockId)`
2. Collect outputFlow emissions with timeout
3. Verify at least one OutputChunk is emitted
4. Verify OutputChunk.blockId matches the command's blockId

#### Test Assertions

- ✅ At least one OutputChunk emitted (proves thread visibility works)
- ✅ OutputChunk.blockId is correct (not null)
- ✅ OutputChunk.data is non-empty
- ✅ Chunks emitted within 2 seconds of command execution

---

### Test 3: Output Does Not Duplicate

**Purpose**: Verify incremental output tracking - no exponential duplication

#### Test Setup

- Execute command with multi-line output
- Track all output chunks received
- Calculate total output length

#### Test Execution

1. Execute command: `echo "Line 1"; echo "Line 2"; echo "Line 3"`
2. Collect all output chunks
3. Concatenate chunk data
4. Count occurrences of "Line 1", "Line 2", "Line 3"

#### Test Assertions

- ✅ Each line appears exactly ONCE (not duplicated)
- ✅ Total output length is reasonable (< 500 bytes for this command)
- ✅ No repeated transcript sections
- ✅ Block.output matches expected output (no duplication)

---

### Test 4: Multiple Commands Work Sequentially

**Purpose**: Verify subsequent commands don't break after first command

#### Test Setup

- Execute multiple commands in sequence
- Verify each completes before next starts

#### Test Execution

1. Execute: `echo "Command 1"`
2. Wait for completion
3. Execute: `echo "Command 2"`
4. Wait for completion
5. Execute: `echo "Command 3"`
6. Wait for completion

#### Test Assertions

- ✅ All three commands complete successfully
- ✅ Each block has correct output (no cross-contamination)
- ✅ All blocks transition to SUCCESS
- ✅ No blocks stuck in EXECUTING

---

## Test Implementation

### Test File Location

**Instrumented Tests** (require device/emulator):
- File: `app/src/androidTest/kotlin/com/convocli/terminal/TerminalOutputStreamingTest.kt`
- Reason: Requires real Termux session and PTY

**Unit Tests** (can use mocks):
- File: `app/src/test/kotlin/com/convocli/ui/viewmodels/CommandBlockViewModelTest.kt`
- Reason: Can mock repository for basic flow testing

### Test Validation Criteria

**Before Fix**:
- ❌ Test 1 MUST fail (timeout waiting for output - infinite spinner reproduced)
- ❌ Test 2 MUST fail (no chunks emitted - thread visibility bug reproduced)
- ❌ Test 3 MUST fail (output duplicates - transcript bug reproduced)

**After Fix**:
- ✅ Test 1 MUST pass (output arrives, block completes)
- ✅ Test 2 MUST pass (chunks emitted correctly)
- ✅ Test 3 MUST pass (no duplication)
- ✅ Test 4 MUST pass (multiple commands work)
- ✅ All existing tests still pass (no regressions)

---

## Test Code Example

```kotlin
@RunWith(AndroidJUnit4::class)
class TerminalOutputStreamingTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var repository: TerminalRepository
    private lateinit var viewModel: CommandBlockViewModel

    @Before
    fun setup() {
        // Initialize dependencies
        repository = TerminalRepositoryImpl(ApplicationProvider.getApplicationContext())
        // ... setup
    }

    @Test
    fun testTerminalOutputStreamsToUI() = runTest {
        // Arrange
        val command = "echo 'Test Output'"
        var finalStatus: CommandStatus? = null
        var outputReceived = false

        // Act
        viewModel.executeCommand(command)

        // Observe block updates
        val job = launch {
            viewModel.uiState
                .map { it.blocks.firstOrNull() }
                .filterNotNull()
                .collect { block ->
                    if (block.output.contains("Test Output")) {
                        outputReceived = true
                    }
                    if (block.status == CommandStatus.SUCCESS) {
                        finalStatus = CommandStatus.SUCCESS
                    }
                }
        }

        // Wait for completion (with timeout)
        advanceTimeBy(3000)

        // Assert
        assertTrue("Output should be received", outputReceived)
        assertEquals("Block should complete", CommandStatus.SUCCESS, finalStatus)

        job.cancel()
    }

    @Test
    fun testOutputChunksEmittedFromRepository() = runTest {
        // Arrange
        val blockId = UUID.randomUUID().toString()
        val chunks = mutableListOf<OutputChunk>()

        // Collect chunks
        val job = launch {
            repository.observeOutput().collect { chunk ->
                if (chunk.blockId == blockId) {
                    chunks.add(chunk)
                }
            }
        }

        // Act
        repository.executeCommand("echo Hello", blockId)

        // Wait for chunks
        advanceTimeBy(2000)

        // Assert
        assertTrue("At least one chunk should be emitted", chunks.isNotEmpty())
        assertEquals("Chunk blockId should match", blockId, chunks.first().blockId)
        assertFalse("Chunk data should not be empty", chunks.first().data.isEmpty())

        job.cancel()
    }

    @Test
    fun testOutputDoesNotDuplicate() = runTest {
        // Arrange
        val command = "echo 'Line 1'; echo 'Line 2'; echo 'Line 3'"
        var finalOutput = ""

        // Act
        viewModel.executeCommand(command)

        // Observe final output
        val job = launch {
            viewModel.uiState
                .map { it.blocks.firstOrNull()?.output ?: "" }
                .collect { output ->
                    if (output.isNotEmpty()) {
                        finalOutput = output
                    }
                }
        }

        advanceTimeBy(3000)

        // Assert - each line should appear exactly once
        val line1Count = finalOutput.split("Line 1").size - 1
        val line2Count = finalOutput.split("Line 2").size - 1
        val line3Count = finalOutput.split("Line 3").size - 1

        assertEquals("Line 1 should appear exactly once", 1, line1Count)
        assertEquals("Line 2 should appear exactly once", 1, line2Count)
        assertEquals("Line 3 should appear exactly once", 1, line3Count)

        job.cancel()
    }
}
```

---

## Edge Cases to Test

1. **Long-running commands**: `sleep 5` - verify output doesn't block
2. **Commands with errors**: `ls /nonexistent` - verify FAILURE status
3. **Interactive commands**: `read -p "Input: "` - verify doesn't hang
4. **Binary output**: `cat /dev/urandom | head -c 100` - verify binary handling
5. **Rapid commands**: Execute 10 commands quickly - verify no race conditions
6. **Session restart**: Kill and recreate session - verify recovery

---

## Performance Criteria

- **Output latency**: < 100ms from PTY to UI
- **Memory usage**: No leaks from output duplication
- **Thread safety**: No race conditions or deadlocks
- **UI responsiveness**: No ANR from blocking operations

---

## Metadata

**Workflow**: Bugfix (regression-test-first)
**Created By**: SpecSwarm Bugfix Workflow
**Test Strategy**: Integration tests (primary), Unit tests (supplementary)
**Estimated Test Time**: 10-15 seconds per test
