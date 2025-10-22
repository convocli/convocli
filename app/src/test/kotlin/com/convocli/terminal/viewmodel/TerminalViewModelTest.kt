package com.convocli.terminal.viewmodel

import app.cash.turbine.test
import com.convocli.terminal.model.SessionState
import com.convocli.terminal.model.StreamType
import com.convocli.terminal.model.TerminalError
import com.convocli.terminal.repository.FakeTerminalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for TerminalViewModel.
 *
 * Uses FakeTerminalRepository to test ViewModel logic without
 * requiring actual Termux integration.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TerminalViewModelTest {
    private lateinit var repository: FakeTerminalRepository
    private lateinit var viewModel: TerminalViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeTerminalRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `createSession success sets isSessionReady to true`() = runTest {
        // Given: Repository will succeed
        repository.sessionCreationResult = Result.success("test-session-id")

        // When: ViewModel is created (triggers session creation)
        viewModel = TerminalViewModel(repository)
        advanceUntilIdle()

        // Then: Session should be ready
        assertTrue(viewModel.isSessionReady.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `createSession failure sets error and isSessionReady to false`() = runTest {
        // Given: Repository will fail
        repository.sessionCreationResult = Result.failure(
            Exception("Shell not found"),
        )

        // When: ViewModel is created
        viewModel = TerminalViewModel(repository)
        advanceUntilIdle()

        // Then: Session should not be ready and error should be set
        assertFalse(viewModel.isSessionReady.value)
        assertNotNull(viewModel.error.value)
        assertTrue(viewModel.error.value is TerminalError.InitializationFailed)
    }

    @Test
    fun `executeCommand sends command to repository`() = runTest {
        // Given: Successful session
        repository.sessionCreationResult = Result.success("test-session-id")
        viewModel = TerminalViewModel(repository)
        advanceUntilIdle()

        // When: Execute a command
        viewModel.executeCommand("ls -la")
        advanceUntilIdle()

        // Then: Command should be sent to repository
        assertEquals(1, repository.executedCommands.size)
        assertEquals("test-session-id", repository.executedCommands[0].first)
        assertEquals("ls -la", repository.executedCommands[0].second)
    }

    @Test
    fun `executeCommand blank command is ignored`() = runTest {
        // Given: Successful session
        repository.sessionCreationResult = Result.success("test-session-id")
        viewModel = TerminalViewModel(repository)
        advanceUntilIdle()

        // When: Execute blank command
        viewModel.executeCommand("   ")
        advanceUntilIdle()

        // Then: Command should NOT be sent to repository
        assertEquals(0, repository.executedCommands.size)
    }

    @Test
    fun `executeCommand when session not ready sets error`() = runTest {
        // Given: Failed session
        repository.sessionCreationResult = Result.failure(Exception("Failed"))
        viewModel = TerminalViewModel(repository)
        advanceUntilIdle()

        // Clear the initialization error
        viewModel.dismissError()

        // When: Try to execute command
        viewModel.executeCommand("ls")
        advanceUntilIdle()

        // Then: Should emit error, command not executed
        assertNotNull(viewModel.error.value)
        assertTrue(viewModel.error.value is TerminalError.IOError)
        assertEquals(0, repository.executedCommands.size)
    }

    @Test
    fun `output flow collects terminal output`() = runTest {
        // Given: Successful session
        repository.sessionCreationResult = Result.success("test-session-id")
        viewModel = TerminalViewModel(repository)
        advanceUntilIdle()

        // When: Repository emits output
        repository.emitOutput("Hello from terminal")
        advanceUntilIdle()

        // Then: ViewModel should expose the output
        assertEquals("Hello from terminal", viewModel.output.value)
    }

    @Test
    fun `error flow collects terminal errors`() = runTest {
        // Given: Successful session
        repository.sessionCreationResult = Result.success("test-session-id")
        viewModel = TerminalViewModel(repository)
        advanceUntilIdle()

        // Clear initialization success
        viewModel.dismissError()

        // When: Repository emits error
        val testError = TerminalError.IOError("Test error")
        repository.emitError(testError)
        advanceUntilIdle()

        // Then: ViewModel should expose the error
        assertEquals(testError, viewModel.error.value)
    }

    @Test
    fun `restartSession destroys old session and creates new one`() = runTest {
        // Given: Successful initial session
        repository.sessionCreationResult = Result.success("session-1")
        viewModel = TerminalViewModel(repository)
        advanceUntilIdle()

        // Emit some output
        repository.emitOutput("Initial output")
        advanceUntilIdle()

        // When: Restart session
        repository.sessionCreationResult = Result.success("session-2")
        viewModel.restartSession()
        advanceUntilIdle()

        // Then: Old session destroyed, new session created, output cleared
        assertEquals(1, repository.destroyedSessions.size)
        assertEquals("session-1", repository.destroyedSessions[0])
        assertEquals("", viewModel.output.value)
        assertTrue(viewModel.isSessionReady.value)
    }

    @Test
    fun `clearOutput clears the output buffer`() = runTest {
        // Given: Session with output
        repository.sessionCreationResult = Result.success("test-session-id")
        viewModel = TerminalViewModel(repository)
        advanceUntilIdle()

        repository.emitOutput("Some output")
        advanceUntilIdle()

        // When: Clear output
        viewModel.clearOutput()

        // Then: Output should be empty
        assertEquals("", viewModel.output.value)
    }

    @Test
    fun `dismissError clears error state`() = runTest {
        // Given: ViewModel with error
        repository.sessionCreationResult = Result.failure(Exception("Test"))
        viewModel = TerminalViewModel(repository)
        advanceUntilIdle()

        // Verify error exists
        assertNotNull(viewModel.error.value)

        // When: Dismiss error
        viewModel.dismissError()

        // Then: Error should be null
        assertNull(viewModel.error.value)
    }

    @Test
    fun `getSessionState returns current session state`() = runTest {
        // Given: Successful session
        repository.sessionCreationResult = Result.success("test-session-id")
        viewModel = TerminalViewModel(repository)
        advanceUntilIdle()

        // When: Get session state
        val state = viewModel.getSessionState()

        // Then: Should return RUNNING
        assertEquals(SessionState.RUNNING, state)
    }

    @Test
    fun `isExecuting is true during command execution`() = runTest {
        // Given: Successful session
        repository.sessionCreationResult = Result.success("test-session-id")
        viewModel = TerminalViewModel(repository)
        advanceUntilIdle()

        // When: Execute command
        viewModel.isExecuting.test {
            // Initial state should be false
            assertEquals(false, awaitItem())

            viewModel.executeCommand("sleep 5")

            // Should become true during execution
            assertEquals(true, awaitItem())

            advanceUntilIdle()

            // Should return to false after execution
            assertEquals(false, awaitItem())
        }
    }

    @Test
    fun `SessionCrashed error sets isSessionReady to false`() = runTest {
        // Given: Successful initial session
        repository.sessionCreationResult = Result.success("test-session-id")
        viewModel = TerminalViewModel(repository)
        advanceUntilIdle()

        // Verify session is ready
        assertTrue(viewModel.isSessionReady.value)

        // When: Session crashes
        repository.emitError(TerminalError.SessionCrashed("PTY broken"))
        advanceUntilIdle()

        // Then: Session should no longer be ready
        assertFalse(viewModel.isSessionReady.value)
        assertNotNull(viewModel.error.value)
    }
}
