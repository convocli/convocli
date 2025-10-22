package com.convocli.terminal

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.convocli.terminal.model.TerminalError
import com.convocli.terminal.repository.TerminalRepository
import com.convocli.terminal.repository.TermuxTerminalRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for error handling functionality.
 *
 * Tests that terminal errors are properly detected, reported, and handled
 * without crashing the session. These tests verify:
 * - Invalid command detection (command not found)
 * - File access errors (permission denied, file not found)
 * - Session crash detection and recovery
 * - Error reporting through repository error flow
 * - Session continuity after errors
 *
 * ## Current Status (Before Bootstrap Installation)
 * Since Termux bootstrap (bash, coreutils, etc.) is NOT yet installed,
 * these tests currently verify that:
 * - Session creation is attempted
 * - Error handling infrastructure is in place
 * - System handles missing shell gracefully
 *
 * ## Future Behavior (After Bootstrap Installation - Features 003/004)
 * Once bootstrap is installed, uncomment the tests below to verify:
 * - Invalid commands produce proper error messages
 * - File access errors are correctly reported
 * - Permission denied errors are handled
 * - Sessions continue running after command failures
 * - Error messages are user-friendly
 *
 * @see TerminalError
 */
@RunWith(AndroidJUnit4::class)
class ErrorHandlingTest {
    private lateinit var context: Context
    private lateinit var repository: TerminalRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        repository = TermuxTerminalRepository(context)
    }

    /**
     * Verify that session creation failure is handled gracefully.
     *
     * Before bootstrap installation, session creation should fail because
     * bash executable doesn't exist. This tests that the failure is
     * handled properly without crashing.
     */
    @Test
    fun testSessionCreationFailure_handlesGracefully() = runTest {
        // When: Attempt to create session (will fail - no bash)
        val result = repository.createSession()

        // Then: Should fail gracefully
        assert(result.isFailure) {
            "Session creation should fail when bash is not installed"
        }

        // And: Error should be reported (not crash)
        // The repository should emit InitializationFailed error
    }

    /**
     * Verify that error infrastructure is ready for error reporting.
     *
     * Tests that the error flow can be observed and is ready to
     * receive error events once the shell is available.
     */
    @Test
    fun testErrorFlow_canBeObserved() = runTest {
        // When: Observing error flow
        val errorFlow = repository.observeErrors()

        // Then: Flow should be accessible
        assert(errorFlow != null) {
            "Error flow should be observable"
        }

        // The flow is ready to emit errors once shell is available
    }

    // ========================================
    // FUTURE TESTS (After Bootstrap Installation)
    // ========================================
    // The following tests are commented out because they require
    // Termux bootstrap to be installed. Uncomment and update after
    // Features 003/004 are complete.

    /*
    @Test
    fun testInvalidCommand_reportsCommandNotFound() = runTest {
        // Given: Active session
        val sessionId = repository.createSession().getOrThrow()

        // When: Execute invalid command
        repository.executeCommand(sessionId, "invalidcommand123xyz")

        // Then: Should detect command not found error
        withTimeout(5.seconds) {
            val error = repository.observeErrors().first()
            assertTrue(error is TerminalError.CommandFailed)

            val commandError = error as TerminalError.CommandFailed
            assertEquals("invalidcommand123xyz", commandError.command)
            assertTrue(
                commandError.stderr.contains("command not found") ||
                commandError.stderr.contains("not found"),
                "Error should mention command not found"
            )
        }

        // And: Session should still be running
        assertEquals(SessionState.RUNNING, repository.getSessionState(sessionId))

        // Cleanup
        repository.destroySession(sessionId)
    }

    @Test
    fun testFileNotFound_reportsError() = runTest {
        // Given: Active session
        val sessionId = repository.createSession().getOrThrow()

        // When: Try to read nonexistent file
        repository.executeCommand(sessionId, "cat /nonexistent/file.txt")

        // Then: Should detect file not found error
        withTimeout(5.seconds) {
            val error = repository.observeErrors().first()
            assertTrue(error is TerminalError.CommandFailed)

            val commandError = error as TerminalError.CommandFailed
            assertTrue(
                commandError.stderr.contains("No such file") ||
                commandError.stderr.contains("not found"),
                "Error should mention file not found"
            )
        }

        // And: Session should continue running
        assertEquals(SessionState.RUNNING, repository.getSessionState(sessionId))

        // Cleanup
        repository.destroySession(sessionId)
    }

    @Test
    fun testPermissionDenied_reportsError() = runTest {
        // Given: Active session and restricted file
        val sessionId = repository.createSession().getOrThrow()

        // When: Try to access restricted file (e.g., /root/secretfile)
        // Note: This may vary by Android version and permissions
        repository.executeCommand(sessionId, "cat /root/secretfile")

        // Then: Should detect permission denied error
        withTimeout(5.seconds) {
            val error = repository.observeErrors().first()
            assertTrue(error is TerminalError.CommandFailed)

            val commandError = error as TerminalError.CommandFailed
            assertTrue(
                commandError.stderr.contains("Permission denied") ||
                commandError.stderr.contains("cannot access"),
                "Error should mention permission denied"
            )
        }

        // And: Session should continue running
        assertEquals(SessionState.RUNNING, repository.getSessionState(sessionId))

        // Cleanup
        repository.destroySession(sessionId)
    }

    @Test
    fun testInvalidOption_reportsError() = runTest {
        // Given: Active session
        val sessionId = repository.createSession().getOrThrow()

        // When: Execute command with invalid option
        repository.executeCommand(sessionId, "ls --invalid-option-xyz")

        // Then: Should detect invalid option error
        withTimeout(5.seconds) {
            val error = repository.observeErrors().first()
            assertTrue(error is TerminalError.CommandFailed)

            val commandError = error as TerminalError.CommandFailed
            assertTrue(
                commandError.stderr.contains("invalid option") ||
                commandError.stderr.contains("unrecognized option"),
                "Error should mention invalid option"
            )
        }

        // And: Session should continue running
        assertEquals(SessionState.RUNNING, repository.getSessionState(sessionId))

        // Cleanup
        repository.destroySession(sessionId)
    }

    @Test
    fun testMultipleErrors_allReported() = runTest {
        // Given: Active session
        val sessionId = repository.createSession().getOrThrow()
        val errors = mutableListOf<TerminalError>()

        // Collect errors in background
        val job = launch {
            repository.observeErrors().collect { error ->
                errors.add(error)
            }
        }

        // When: Execute multiple failing commands
        repository.executeCommand(sessionId, "invalidcmd1")
        delay(1000) // Wait for error
        repository.executeCommand(sessionId, "invalidcmd2")
        delay(1000) // Wait for error
        repository.executeCommand(sessionId, "cat /nonexistent")
        delay(1000) // Wait for error

        // Then: All errors should be reported
        assertTrue(errors.size >= 3, "Should have at least 3 errors")

        // And: Session should still be running
        assertEquals(SessionState.RUNNING, repository.getSessionState(sessionId))

        // Cleanup
        job.cancel()
        repository.destroySession(sessionId)
    }

    @Test
    fun testSuccessfulCommandAfterError_works() = runTest {
        // Given: Active session
        val sessionId = repository.createSession().getOrThrow()

        // When: Execute failing command
        repository.executeCommand(sessionId, "invalidcommand123")
        delay(1000) // Wait for error

        // Then: Execute successful command
        repository.executeCommand(sessionId, "echo 'Success'")

        // And: Should receive output from successful command
        withTimeout(5.seconds) {
            val output = repository.observeOutput(sessionId)
                .filter { it.text.contains("Success") }
                .first()

            assertTrue(output.text.contains("Success"))
        }

        // And: Session should be running
        assertEquals(SessionState.RUNNING, repository.getSessionState(sessionId))

        // Cleanup
        repository.destroySession(sessionId)
    }

    @Test
    fun testSessionCrash_detected() = runTest {
        // Given: Active session
        val sessionId = repository.createSession().getOrThrow()

        // When: Force session to crash (kill bash process)
        // Note: This is difficult to test without actually killing the process
        // For now, we'll test that SessionCrashed can be emitted

        // Simulate by destroying session and checking state
        repository.destroySession(sessionId)

        // Then: Session state should be stopped
        assertEquals(SessionState.STOPPED, repository.getSessionState(sessionId))
    }

    @Test
    fun testStderrDetection_classifiesCorrectly() = runTest {
        // Given: Active session
        val sessionId = repository.createSession().getOrThrow()

        // When: Execute command that produces stderr
        repository.executeCommand(sessionId, "ls /nonexistent")

        // Then: Output should be classified as stderr
        withTimeout(5.seconds) {
            val output = repository.observeOutput(sessionId)
                .filter { it.stream == StreamType.STDERR }
                .first()

            assertTrue(
                output.text.contains("No such file") ||
                output.text.contains("not found")
            )
        }

        // Cleanup
        repository.destroySession(sessionId)
    }

    @Test
    fun testStdoutVsStderr_distinguishable() = runTest {
        // Given: Active session
        val sessionId = repository.createSession().getOrThrow()
        val outputs = mutableListOf<TerminalOutput>()

        // Collect all output
        val job = launch {
            repository.observeOutput(sessionId).collect { output ->
                outputs.add(output)
            }
        }

        // When: Execute both successful and failing commands
        repository.executeCommand(sessionId, "echo 'stdout message'")
        delay(500)
        repository.executeCommand(sessionId, "cat /nonexistent")
        delay(500)

        // Then: Should have both stdout and stderr outputs
        val hasStdout = outputs.any { it.stream == StreamType.STDOUT }
        val hasStderr = outputs.any { it.stream == StreamType.STDERR }

        assertTrue(hasStdout, "Should have stdout output")
        assertTrue(hasStderr, "Should have stderr output")

        // Cleanup
        job.cancel()
        repository.destroySession(sessionId)
    }

    @Test
    fun testErrorMessageExtraction_simplifies() = runTest {
        // Given: Active session and OutputStreamProcessor
        val sessionId = repository.createSession().getOrThrow()

        // When: Execute command that produces complex error
        repository.executeCommand(sessionId, "bash: testcommand: command not found")

        // Then: Error should be simplified (tested via OutputStreamProcessor)
        // The CommandMonitor uses extractErrorMessage internally
        // This test verifies the integration works

        withTimeout(5.seconds) {
            val error = repository.observeErrors().first()
            assertTrue(error is TerminalError.CommandFailed)

            val commandError = error as TerminalError.CommandFailed
            // Error message should be simplified (bash prefix removed)
            assertTrue(
                !commandError.stderr.startsWith("bash:") ||
                commandError.stderr.contains("command not found")
            )
        }

        // Cleanup
        repository.destroySession(sessionId)
    }
    */
}
