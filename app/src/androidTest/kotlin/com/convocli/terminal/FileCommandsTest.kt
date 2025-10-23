package com.convocli.terminal

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.convocli.terminal.repository.TerminalRepository
import com.convocli.terminal.repository.TermuxTerminalRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Integration tests for file viewing commands.
 *
 * Tests `cat`, `less`, pipes, and file redirection commands with the
 * real Termux terminal integration.
 *
 * ## Current Status (Before Bootstrap Installation)
 * Since Termux bootstrap (bash, coreutils, etc.) is NOT yet installed,
 * these tests currently verify that:
 * - Session creation is attempted
 * - Commands can be sent (even though shell doesn't exist)
 * - System handles missing shell gracefully
 *
 * ## Future Behavior (After Bootstrap Installation - Features 003/004)
 * Once bootstrap is installed, uncomment the tests below to verify:
 * - `cat` displays file contents correctly
 * - Large files are handled without crashes
 * - Pipe commands work (`cat file | grep pattern`)
 * - Output redirection works (`echo text > file`)
 * - File contents match expected output
 *
 * ## Test Setup
 * Tests create temporary files in the app's private directory for testing.
 * Files are cleaned up after each test.
 */
@RunWith(AndroidJUnit4::class)
class FileCommandsTest {
    private lateinit var context: Context
    private lateinit var repository: TerminalRepository
    private lateinit var homeDir: File

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        repository = TermuxTerminalRepository(context)

        // Home directory where test files will be created
        homeDir = File(context.filesDir, "home")
        homeDir.mkdirs()
    }

    /**
     * Verify that test file can be created in home directory.
     *
     * This is a prerequisite for the file viewing tests.
     */
    @Test
    fun testFileCreation_canCreateTestFile() = runTest {
        // When: Create a test file
        val testFile = File(homeDir, "test.txt")
        testFile.writeText("line1\nline2\nline3\n")

        // Then: File should exist
        assert(testFile.exists()) { "Test file should exist" }
        assert(testFile.canRead()) { "Test file should be readable" }

        // Verify content
        val content = testFile.readText()
        assert(content.contains("line1")) { "File should contain line1" }
        assert(content.contains("line2")) { "File should contain line2" }
        assert(content.contains("line3")) { "File should contain line3" }

        // Cleanup
        testFile.delete()
    }

    /**
     * Verify that session can be created (will fail gracefully without bootstrap).
     *
     * This ensures the infrastructure is in place for file commands,
     * even though they won't work until bootstrap is installed.
     */
    @Test
    fun testFileCommands_sessionCreation_infrastructureReady() = runTest {
        // When: Attempt to create session (will fail - no bash)
        val result = repository.createSession()

        // Then: Should fail gracefully (bash not installed)
        assert(result.isFailure) {
            "Session creation should fail when bash is not installed"
        }

        // But the repository infrastructure should be ready
        // (no crashes, proper error handling)
    }

    /**
     * Verify that large file can be created for testing.
     *
     * This prepares for large file handling tests once bootstrap is installed.
     */
    @Test
    fun testFileCreation_canCreateLargeFile() = runTest {
        // When: Create a large test file (1000+ lines)
        val largeFile = File(homeDir, "large.txt")
        val lines = (1..1000).map { "Line number $it" }
        largeFile.writeText(lines.joinToString("\n"))

        // Then: File should exist and have correct size
        assert(largeFile.exists()) { "Large file should exist" }
        assert(largeFile.length() > 10000) { "File should be reasonably large" }

        val content = largeFile.readText()
        assert(content.contains("Line number 1")) { "Should contain first line" }
        assert(content.contains("Line number 500")) { "Should contain middle line" }
        assert(content.contains("Line number 1000")) { "Should contain last line" }

        // Cleanup
        largeFile.delete()
    }

    // ========================================
    // FUTURE TESTS (After Bootstrap Installation)
    // ========================================
    // The following tests are commented out because they require
    // Termux bootstrap to be installed. Uncomment and update after
    // Features 003/004 are complete.

    /*
    @Test
    fun testCatCommand_smallFile_displaysContent() = runTest {
        // Given: Session and test file
        val sessionId = repository.createSession().getOrThrow()
        val testFile = File(homeDir, "test.txt")
        testFile.writeText("line1\nline2\nline3\n")

        // When: Execute cat command
        repository.executeCommand(sessionId, "cat test.txt")

        // Then: Output should contain file contents
        withTimeout(5.seconds) {
            val output = repository.observeOutput(sessionId).first()
            assertTrue(output.text.contains("line1"))
            assertTrue(output.text.contains("line2"))
            assertTrue(output.text.contains("line3"))
        }

        // Cleanup
        testFile.delete()
        repository.destroySession(sessionId)
    }

    @Test
    fun testCatCommand_largeFile_handlesWithoutCrash() = runTest {
        // Given: Session and large file (1000+ lines)
        val sessionId = repository.createSession().getOrThrow()
        val largeFile = File(homeDir, "large.txt")
        val lines = (1..1000).map { "Line number $it" }
        largeFile.writeText(lines.joinToString("\n"))

        // When: Execute cat on large file
        repository.executeCommand(sessionId, "cat large.txt")

        // Then: Should handle large output without crashing
        withTimeout(10.seconds) {
            val output = repository.observeOutput(sessionId)
                .take(10) // Just collect some output, not all
                .toList()

            // Verify we got some output
            assertTrue(output.isNotEmpty())

            // Should not crash the session
            assertEquals(SessionState.RUNNING, repository.getSessionState(sessionId))
        }

        // Cleanup
        largeFile.delete()
        repository.destroySession(sessionId)
    }

    @Test
    fun testPipeCommand_catGrepPattern_filtersCorrectly() = runTest {
        // Given: Session and test file
        val sessionId = repository.createSession().getOrThrow()
        val testFile = File(homeDir, "test.txt")
        testFile.writeText("line1\nline2\nline3\nother\n")

        // When: Execute cat with grep pipe
        repository.executeCommand(sessionId, "cat test.txt | grep line2")

        // Then: Output should only contain line2
        withTimeout(5.seconds) {
            val output = repository.observeOutput(sessionId).first()
            assertTrue(output.text.contains("line2"))
            assertFalse(output.text.contains("line1"))
            assertFalse(output.text.contains("line3"))
        }

        // Cleanup
        testFile.delete()
        repository.destroySession(sessionId)
    }

    @Test
    fun testRedirection_echoToFile_createsFile() = runTest {
        // Given: Session
        val sessionId = repository.createSession().getOrThrow()
        val outputFile = File(homeDir, "output.txt")

        // Ensure file doesn't exist before test
        outputFile.delete()

        // When: Execute echo with redirection
        repository.executeCommand(sessionId, "echo 'Hello World' > output.txt")

        // Wait for command to complete
        delay(1000)

        // Then: File should be created with correct content
        assertTrue(outputFile.exists())
        val content = outputFile.readText()
        assertTrue(content.contains("Hello World"))

        // Cleanup
        outputFile.delete()
        repository.destroySession(sessionId)
    }

    @Test
    fun testLessCommand_paginatesOutput() = runTest {
        // Given: Session and large file
        val sessionId = repository.createSession().getOrThrow()
        val largeFile = File(homeDir, "large.txt")
        val lines = (1..100).map { "Line $it" }
        largeFile.writeText(lines.joinToString("\n"))

        // When: Execute less command
        repository.executeCommand(sessionId, "less large.txt")

        // Then: Should show paginated output
        // (This test would need interaction simulation to test pagination properly)
        // For now, just verify it doesn't crash
        withTimeout(5.seconds) {
            val output = repository.observeOutput(sessionId).first()
            assertNotNull(output)
        }

        // Cleanup
        largeFile.delete()
        repository.destroySession(sessionId)
    }

    @Test
    fun testHeadCommand_showsFirstLines() = runTest {
        // Given: Session and test file
        val sessionId = repository.createSession().getOrThrow()
        val testFile = File(homeDir, "test.txt")
        val lines = (1..20).map { "Line $it" }
        testFile.writeText(lines.joinToString("\n"))

        // When: Execute head command (default 10 lines)
        repository.executeCommand(sessionId, "head test.txt")

        // Then: Should show first 10 lines
        withTimeout(5.seconds) {
            val output = repository.observeOutput(sessionId).first()
            assertTrue(output.text.contains("Line 1"))
            assertTrue(output.text.contains("Line 10"))
            assertFalse(output.text.contains("Line 15"))
        }

        // Cleanup
        testFile.delete()
        repository.destroySession(sessionId)
    }

    @Test
    fun testTailCommand_showsLastLines() = runTest {
        // Given: Session and test file
        val sessionId = repository.createSession().getOrThrow()
        val testFile = File(homeDir, "test.txt")
        val lines = (1..20).map { "Line $it" }
        testFile.writeText(lines.joinToString("\n"))

        // When: Execute tail command (default 10 lines)
        repository.executeCommand(sessionId, "tail test.txt")

        // Then: Should show last 10 lines
        withTimeout(5.seconds) {
            val output = repository.observeOutput(sessionId).first()
            assertFalse(output.text.contains("Line 5"))
            assertTrue(output.text.contains("Line 11"))
            assertTrue(output.text.contains("Line 20"))
        }

        // Cleanup
        testFile.delete()
        repository.destroySession(sessionId)
    }

    @Test
    fun testWcCommand_countsLines() = runTest {
        // Given: Session and test file
        val sessionId = repository.createSession().getOrThrow()
        val testFile = File(homeDir, "test.txt")
        val lines = (1..42).map { "Line $it" }
        testFile.writeText(lines.joinToString("\n"))

        // When: Execute wc -l command
        repository.executeCommand(sessionId, "wc -l test.txt")

        // Then: Should output line count
        withTimeout(5.seconds) {
            val output = repository.observeOutput(sessionId).first()
            assertTrue(output.text.contains("42"))
            assertTrue(output.text.contains("test.txt"))
        }

        // Cleanup
        testFile.delete()
        repository.destroySession(sessionId)
    }

    @Test
    fun testCatNonexistentFile_showsError() = runTest {
        // Given: Session (no file created)
        val sessionId = repository.createSession().getOrThrow()

        // When: Try to cat a file that doesn't exist
        repository.executeCommand(sessionId, "cat nonexistent.txt")

        // Then: Should show error message
        withTimeout(5.seconds) {
            val output = repository.observeOutput(sessionId).first()
            // Error should mention file not found or no such file
            assertTrue(
                output.text.contains("No such file") ||
                output.text.contains("not found") ||
                output.text.contains("cannot open"),
                "Should show file not found error"
            )
        }

        // Cleanup
        repository.destroySession(sessionId)
    }

    @Test
    fun testMultiplePipes_chainedCommands() = runTest {
        // Given: Session and test file
        val sessionId = repository.createSession().getOrThrow()
        val testFile = File(homeDir, "test.txt")
        testFile.writeText("apple\nbanana\ncherry\napricot\nblueberry\n")

        // When: Execute command with multiple pipes
        repository.executeCommand(sessionId, "cat test.txt | grep 'a' | wc -l")

        // Then: Should count lines containing 'a' (apple, banana, apricot = 3)
        withTimeout(5.seconds) {
            val output = repository.observeOutput(sessionId).first()
            assertTrue(output.text.contains("3"))
        }

        // Cleanup
        testFile.delete()
        repository.destroySession(sessionId)
    }
    */
}
