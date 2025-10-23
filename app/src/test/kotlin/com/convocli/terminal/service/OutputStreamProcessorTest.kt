package com.convocli.terminal.service

import com.convocli.terminal.model.StreamType
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for OutputStreamProcessor.
 *
 * Tests the stderr detection logic to ensure error patterns are correctly
 * identified and classified.
 */
class OutputStreamProcessorTest {
    private lateinit var processor: OutputStreamProcessor

    @Before
    fun setup() {
        processor = OutputStreamProcessor()
    }

    // ========================================
    // STDOUT Detection Tests
    // ========================================

    @Test
    fun detectStreamType_normalOutput_returnsStdout() {
        // Given: Normal command output
        val output = "file1.txt\nfile2.txt\nfile3.txt"

        // When: Detecting stream type
        val result = processor.detectStreamType(output)

        // Then: Should be classified as stdout
        assertEquals(StreamType.STDOUT, result)
    }

    @Test
    fun detectStreamType_emptyString_returnsStdout() {
        // Given: Empty output
        val output = ""

        // When: Detecting stream type
        val result = processor.detectStreamType(output)

        // Then: Should be classified as stdout
        assertEquals(StreamType.STDOUT, result)
    }

    @Test
    fun detectStreamType_whitespaceOnly_returnsStdout() {
        // Given: Whitespace-only output
        val output = "   \n   \n   "

        // When: Detecting stream type
        val result = processor.detectStreamType(output)

        // Then: Should be classified as stdout
        assertEquals(StreamType.STDOUT, result)
    }

    @Test
    fun detectStreamType_successMessage_returnsStdout() {
        // Given: Success message
        val output = "Command executed successfully\nAll tests passed"

        // When: Detecting stream type
        val result = processor.detectStreamType(output)

        // Then: Should be classified as stdout
        assertEquals(StreamType.STDOUT, result)
    }

    // ========================================
    // STDERR Detection Tests - Command Not Found
    // ========================================

    @Test
    fun detectStreamType_commandNotFound_returnsStderr() {
        // Given: Command not found error
        val output = "bash: foo: command not found"

        // When: Detecting stream type
        val result = processor.detectStreamType(output)

        // Then: Should be classified as stderr
        assertEquals(StreamType.STDERR, result)
    }

    @Test
    fun detectStreamType_shCommandNotFound_returnsStderr() {
        // Given: sh command not found error
        val output = "sh: bar: not found"

        // When: Detecting stream type
        val result = processor.detectStreamType(output)

        // Then: Should be classified as stderr
        assertEquals(StreamType.STDERR, result)
    }

    // ========================================
    // STDERR Detection Tests - File Errors
    // ========================================

    @Test
    fun detectStreamType_noSuchFileOrDirectory_returnsStderr() {
        // Given: File not found error
        val output = "ls: /nonexistent: No such file or directory"

        // When: Detecting stream type
        val result = processor.detectStreamType(output)

        // Then: Should be classified as stderr
        assertEquals(StreamType.STDERR, result)
    }

    @Test
    fun detectStreamType_cannotAccess_returnsStderr() {
        // Given: Cannot access error
        val output = "cat: /root/secret: cannot access '/root/secret': Permission denied"

        // When: Detecting stream type
        val result = processor.detectStreamType(output)

        // Then: Should be classified as stderr
        assertEquals(StreamType.STDERR, result)
    }

    @Test
    fun detectStreamType_cannotOpen_returnsStderr() {
        // Given: Cannot open error
        val output = "cat: missing.txt: cannot open 'missing.txt': No such file or directory"

        // When: Detecting stream type
        val result = processor.detectStreamType(output)

        // Then: Should be classified as stderr
        assertEquals(StreamType.STDERR, result)
    }

    @Test
    fun detectStreamType_isADirectory_returnsStderr() {
        // Given: Is a directory error
        val output = "cat: /home: Is a directory"

        // When: Detecting stream type
        val result = processor.detectStreamType(output)

        // Then: Should be classified as stderr
        assertEquals(StreamType.STDERR, result)
    }

    // ========================================
    // STDERR Detection Tests - Permission Errors
    // ========================================

    @Test
    fun detectStreamType_permissionDenied_returnsStderr() {
        // Given: Permission denied error
        val output = "bash: /etc/shadow: Permission denied"

        // When: Detecting stream type
        val result = processor.detectStreamType(output)

        // Then: Should be classified as stderr
        assertEquals(StreamType.STDERR, result)
    }

    // ========================================
    // STDERR Detection Tests - Syntax Errors
    // ========================================

    @Test
    fun detectStreamType_invalidOption_returnsStderr() {
        // Given: Invalid option error
        val output = "ls: invalid option -- 'z'\nTry 'ls --help' for more information."

        // When: Detecting stream type
        val result = processor.detectStreamType(output)

        // Then: Should be classified as stderr
        assertEquals(StreamType.STDERR, result)
    }

    @Test
    fun detectStreamType_syntaxError_returnsStderr() {
        // Given: Syntax error
        val output = "bash: syntax error near unexpected token `('"

        // When: Detecting stream type
        val result = processor.detectStreamType(output)

        // Then: Should be classified as stderr
        assertEquals(StreamType.STDERR, result)
    }

    @Test
    fun detectStreamType_usage_returnsStderr() {
        // Given: Usage message (typically stderr)
        val output = "usage: grep [OPTIONS] PATTERN [FILE...]"

        // When: Detecting stream type
        val result = processor.detectStreamType(output)

        // Then: Should be classified as stderr
        assertEquals(StreamType.STDERR, result)
    }

    // ========================================
    // STDERR Detection Tests - Generic Error Prefixes
    // ========================================

    @Test
    fun detectStreamType_errorPrefix_returnsStderr() {
        // Given: Generic error prefix
        val output = "error: Something went wrong"

        // When: Detecting stream type
        val result = processor.detectStreamType(output)

        // Then: Should be classified as stderr
        assertEquals(StreamType.STDERR, result)
    }

    @Test
    fun detectStreamType_fatalPrefix_returnsStderr() {
        // Given: Fatal error prefix
        val output = "fatal: could not read from remote repository"

        // When: Detecting stream type
        val result = processor.detectStreamType(output)

        // Then: Should be classified as stderr
        assertEquals(StreamType.STDERR, result)
    }

    @Test
    fun detectStreamType_warningPrefix_returnsStderr() {
        // Given: Warning prefix
        val output = "warning: deprecated feature used"

        // When: Detecting stream type
        val result = processor.detectStreamType(output)

        // Then: Should be classified as stderr
        assertEquals(StreamType.STDERR, result)
    }

    // ========================================
    // STDERR Detection Tests - Git Errors
    // ========================================

    @Test
    fun detectStreamType_gitNotARepository_returnsStderr() {
        // Given: Git not a repository error
        val output = "fatal: not a git repository (or any of the parent directories): .git"

        // When: Detecting stream type
        val result = processor.detectStreamType(output)

        // Then: Should be classified as stderr
        assertEquals(StreamType.STDERR, result)
    }

    // ========================================
    // STDERR Detection Tests - Mixed Content
    // ========================================

    @Test
    fun detectStreamType_mixedStdoutAndStderr_returnsStderr() {
        // Given: Mixed stdout and stderr (error present)
        val output = "Processing file...\nls: /nonexistent: No such file or directory\nDone"

        // When: Detecting stream type
        val result = processor.detectStreamType(output)

        // Then: Should be classified as stderr (error takes precedence)
        assertEquals(StreamType.STDERR, result)
    }

    // ========================================
    // Error Message Extraction Tests
    // ========================================

    @Test
    fun extractErrorMessage_bashPrefix_removesPrefix() {
        // Given: Error with bash prefix
        val stderr = "bash: foo: command not found"

        // When: Extracting error message
        val result = processor.extractErrorMessage(stderr)

        // Then: Should remove bash prefix
        assertEquals("foo: command not found", result)
    }

    @Test
    fun extractErrorMessage_shPrefix_removesPrefix() {
        // Given: Error with sh prefix
        val stderr = "sh: bar: not found"

        // When: Extracting error message
        val result = processor.extractErrorMessage(stderr)

        // Then: Should remove sh prefix
        assertEquals("bar: not found", result)
    }

    @Test
    fun extractErrorMessage_errorPrefix_removesPrefix() {
        // Given: Error with error prefix
        val stderr = "error: failed to open file"

        // When: Extracting error message
        val result = processor.extractErrorMessage(stderr)

        // Then: Should remove error prefix
        assertEquals("failed to open file", result)
    }

    @Test
    fun extractErrorMessage_fatalPrefix_removesPrefix() {
        // Given: Error with fatal prefix
        val stderr = "fatal: not a git repository"

        // When: Extracting error message
        val result = processor.extractErrorMessage(stderr)

        // Then: Should remove fatal prefix
        assertEquals("not a git repository", result)
    }

    @Test
    fun extractErrorMessage_noPrefix_returnsOriginal() {
        // Given: Error without recognized prefix
        val stderr = "No such file or directory"

        // When: Extracting error message
        val result = processor.extractErrorMessage(stderr)

        // Then: Should return original message
        assertEquals("No such file or directory", result)
    }

    @Test
    fun extractErrorMessage_withWhitespace_trims() {
        // Given: Error with leading/trailing whitespace
        val stderr = "  error: something went wrong  "

        // When: Extracting error message
        val result = processor.extractErrorMessage(stderr)

        // Then: Should trim and remove prefix
        assertEquals("something went wrong", result)
    }
}
