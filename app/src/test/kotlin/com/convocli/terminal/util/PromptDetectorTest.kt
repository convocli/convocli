package com.convocli.terminal.util

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for PromptDetectorImpl.
 *
 * Tests cover:
 * - Various prompt formats (simple, user@host, path-based)
 * - Edge cases (prompts in output, multi-line, empty input)
 * - False positive prevention
 * - Prompt stripping functionality
 */
class PromptDetectorTest {

    private lateinit var detector: PromptDetector

    @Before
    fun setup() {
        detector = PromptDetectorImpl()
    }

    // ========== detectsPrompt() Tests ==========

    @Test
    fun `detects simple dollar prompt`() {
        val output = "$ "
        assertTrue(detector.detectsPrompt(output))
    }

    @Test
    fun `detects simple hash prompt`() {
        val output = "# "
        assertTrue(detector.detectsPrompt(output))
    }

    @Test
    fun `detects simple greater-than prompt`() {
        val output = "> "
        assertTrue(detector.detectsPrompt(output))
    }

    @Test
    fun `detects prompt with leading whitespace`() {
        val output = "  $ "
        assertTrue(detector.detectsPrompt(output))
    }

    @Test
    fun `detects user at host prompt with tilde path`() {
        val output = "user@localhost:~$ "
        assertTrue(detector.detectsPrompt(output))
    }

    @Test
    fun `detects user at host prompt with full path`() {
        val output = "user@host:/data/data/com.convocli/files/home$ "
        assertTrue(detector.detectsPrompt(output))
    }

    @Test
    fun `detects Termux UID prompt`() {
        val output = "u0_a123@localhost:~$ "
        assertTrue(detector.detectsPrompt(output))
    }

    @Test
    fun `detects tilde path prompt`() {
        val output = "~/project$ "
        assertTrue(detector.detectsPrompt(output))
    }

    @Test
    fun `detects full path prompt`() {
        val output = "/home/user/project$ "
        assertTrue(detector.detectsPrompt(output))
    }

    @Test
    fun `detects prompt with hash ending for root`() {
        val output = "root@localhost:/# "
        assertTrue(detector.detectsPrompt(output))
    }

    @Test
    fun `detects prompt at end of multi-line output`() {
        val output = """
            file1
            file2
            file3
            $
        """.trimIndent()

        assertTrue(detector.detectsPrompt(output))
    }

    @Test
    fun `does not detect prompt in middle of output`() {
        val output = """
            $ echo "test"
            test
            more output
        """.trimIndent()

        assertFalse(detector.detectsPrompt(output))
    }

    @Test
    fun `does not detect dollar sign without space`() {
        val output = "$"
        assertFalse(detector.detectsPrompt(output))
    }

    @Test
    fun `does not detect dollar sign in text`() {
        val output = "Total cost: $50"
        assertFalse(detector.detectsPrompt(output))
    }

    @Test
    fun `does not detect prompt-like text in command output`() {
        val output = """
            echo "$ this looks like a prompt"
            $ this looks like a prompt
        """.trimIndent()

        assertFalse(detector.detectsPrompt(output))
    }

    @Test
    fun `does not detect empty output as prompt`() {
        val output = ""
        assertFalse(detector.detectsPrompt(output))
    }

    @Test
    fun `does not detect blank output as prompt`() {
        val output = "   \n\n  "
        assertFalse(detector.detectsPrompt(output))
    }

    @Test
    fun `does not detect output without prompt`() {
        val output = "just some text\nwithout a prompt"
        assertFalse(detector.detectsPrompt(output))
    }

    // ========== getPromptPatterns() Tests ==========

    @Test
    fun `returns non-empty list of patterns`() {
        val patterns = detector.getPromptPatterns()
        assertTrue(patterns.isNotEmpty())
    }

    @Test
    fun `returns at least 3 default patterns`() {
        val patterns = detector.getPromptPatterns()
        assertTrue(patterns.size >= 3)
    }

    // ========== stripPrompt() Tests ==========

    @Test
    fun `stripPrompt removes simple dollar prompt`() {
        val output = "$ "
        val result = detector.stripPrompt(output)
        assertEquals("", result)
    }

    @Test
    fun `stripPrompt removes prompt from multi-line output`() {
        val input = """
            file1
            file2
            $
        """.trimIndent()

        val expected = """
            file1
            file2
        """.trimIndent()

        assertEquals(expected, detector.stripPrompt(input))
    }

    @Test
    fun `stripPrompt removes user at host prompt`() {
        val input = """
            total 24
            drwxr-xr-x 3 user group 4096 Oct 22 10:00 dir1
            user@localhost:~$
        """.trimIndent()

        val expected = """
            total 24
            drwxr-xr-x 3 user group 4096 Oct 22 10:00 dir1
        """.trimIndent()

        assertEquals(expected, detector.stripPrompt(input))
    }

    @Test
    fun `stripPrompt returns original output if no prompt detected`() {
        val output = "file1\nfile2\nfile3"
        val result = detector.stripPrompt(output)
        assertEquals(output, result)
    }

    @Test
    fun `stripPrompt handles empty output`() {
        val output = ""
        val result = detector.stripPrompt(output)
        assertEquals("", result)
    }

    @Test
    fun `stripPrompt does not remove prompt-like text from middle`() {
        val output = """
            $ this is in output
            more text
            final line
        """.trimIndent()

        val result = detector.stripPrompt(output)
        assertEquals(output, result)
    }

    @Test
    fun `stripPrompt preserves output with dollar signs in text`() {
        val output = """
            Price: $100
            Total: $250
        """.trimIndent()

        val result = detector.stripPrompt(output)
        assertEquals(output, result)
    }

    // ========== Edge Case Tests ==========

    @Test
    fun `handles command continuation prompt`() {
        val output = "> "
        assertTrue(detector.detectsPrompt(output))
    }

    @Test
    fun `handles prompt with tabs`() {
        val output = "$\t"  // Dollar with tab instead of space
        // This should NOT match (requires space after $)
        assertFalse(detector.detectsPrompt(output))
    }

    @Test
    fun `handles very long prompt with path`() {
        val output = "user@host:/very/long/path/to/some/deeply/nested/directory/structure$ "
        assertTrue(detector.detectsPrompt(output))
    }

    @Test
    fun `handles prompt with hyphenated hostname`() {
        val output = "user@my-server-01:~$ "
        assertTrue(detector.detectsPrompt(output))
    }
}
