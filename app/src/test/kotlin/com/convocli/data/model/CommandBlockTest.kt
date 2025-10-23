package com.convocli.data.model

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for CommandBlock data class and helper methods.
 */
class CommandBlockTest {

    @Test
    fun `data class properties are correctly initialized`() {
        val block = CommandBlock(
            id = "test-id-123",
            command = "ls -la",
            output = "file1\nfile2\nfile3",
            status = CommandStatus.SUCCESS,
            timestamp = 1729615200000L,
            executionDuration = 1500L,
            exitCode = 0,
            workingDirectory = "/home/user",
            isExpanded = false
        )

        assertEquals("test-id-123", block.id)
        assertEquals("ls -la", block.command)
        assertEquals("file1\nfile2\nfile3", block.output)
        assertEquals(CommandStatus.SUCCESS, block.status)
        assertEquals(1729615200000L, block.timestamp)
        assertEquals(1500L, block.executionDuration)
        assertEquals(0, block.exitCode)
        assertEquals("/home/user", block.workingDirectory)
        assertFalse(block.isExpanded)
    }

    @Test
    fun `default value for isExpanded is true`() {
        val block = CommandBlock(
            id = "test-id",
            command = "pwd",
            output = "/home/user",
            status = CommandStatus.SUCCESS,
            timestamp = System.currentTimeMillis(),
            workingDirectory = "/home/user"
        )

        assertTrue(block.isExpanded)
    }

    @Test
    fun `kotlinx serialization encodes and decodes correctly`() {
        val original = CommandBlock(
            id = "serialize-test",
            command = "echo 'hello'",
            output = "hello",
            status = CommandStatus.SUCCESS,
            timestamp = 1729615200000L,
            executionDuration = 500L,
            exitCode = 0,
            workingDirectory = "/data/data/com.convocli/files/home",
            isExpanded = true
        )

        val json = Json.encodeToString(original)
        val decoded = Json.decodeFromString<CommandBlock>(json)

        assertEquals(original, decoded)
    }

    @Test
    fun `formattedTimestamp returns just now for recent timestamp`() {
        val now = System.currentTimeMillis()
        val block = CommandBlock(
            id = "test",
            command = "pwd",
            output = "",
            status = CommandStatus.SUCCESS,
            timestamp = now - 2000, // 2 seconds ago
            workingDirectory = "/home"
        )

        assertEquals("just now", block.formattedTimestamp())
    }

    @Test
    fun `formattedTimestamp returns seconds ago`() {
        val now = System.currentTimeMillis()
        val block = CommandBlock(
            id = "test",
            command = "pwd",
            output = "",
            status = CommandStatus.SUCCESS,
            timestamp = now - 30000, // 30 seconds ago
            workingDirectory = "/home"
        )

        assertEquals("30s ago", block.formattedTimestamp())
    }

    @Test
    fun `formattedTimestamp returns minutes ago`() {
        val now = System.currentTimeMillis()
        val block = CommandBlock(
            id = "test",
            command = "pwd",
            output = "",
            status = CommandStatus.SUCCESS,
            timestamp = now - 300000, // 5 minutes ago
            workingDirectory = "/home"
        )

        assertEquals("5m ago", block.formattedTimestamp())
    }

    @Test
    fun `formattedTimestamp returns hours ago`() {
        val now = System.currentTimeMillis()
        val block = CommandBlock(
            id = "test",
            command = "pwd",
            output = "",
            status = CommandStatus.SUCCESS,
            timestamp = now - 7200000, // 2 hours ago
            workingDirectory = "/home"
        )

        assertEquals("2h ago", block.formattedTimestamp())
    }

    @Test
    fun `formattedDuration returns null when duration is null`() {
        val block = CommandBlock(
            id = "test",
            command = "pwd",
            output = "",
            status = CommandStatus.EXECUTING,
            timestamp = System.currentTimeMillis(),
            executionDuration = null,
            workingDirectory = "/home"
        )

        assertNull(block.formattedDuration())
    }

    @Test
    fun `formattedDuration returns milliseconds for very short duration`() {
        val block = CommandBlock(
            id = "test",
            command = "pwd",
            output = "",
            status = CommandStatus.SUCCESS,
            timestamp = System.currentTimeMillis(),
            executionDuration = 500L,
            workingDirectory = "/home"
        )

        assertEquals("500ms", block.formattedDuration())
    }

    @Test
    fun `formattedDuration returns seconds for short duration`() {
        val block = CommandBlock(
            id = "test",
            command = "pwd",
            output = "",
            status = CommandStatus.SUCCESS,
            timestamp = System.currentTimeMillis(),
            executionDuration = 1500L,
            workingDirectory = "/home"
        )

        assertEquals("1.5s", block.formattedDuration())
    }

    @Test
    fun `formattedDuration returns minutes and seconds for long duration`() {
        val block = CommandBlock(
            id = "test",
            command = "npm install",
            output = "installed packages",
            status = CommandStatus.SUCCESS,
            timestamp = System.currentTimeMillis(),
            executionDuration = 135000L, // 2m 15s
            workingDirectory = "/home"
        )

        assertEquals("2m 15s", block.formattedDuration())
    }

    @Test
    fun `isCancelled returns true for SIGINT exit code 130`() {
        val block = CommandBlock(
            id = "test",
            command = "sleep 1000",
            output = "",
            status = CommandStatus.FAILURE,
            timestamp = System.currentTimeMillis(),
            exitCode = 130,
            workingDirectory = "/home"
        )

        assertTrue(block.isCancelled())
    }

    @Test
    fun `isCancelled returns true for SIGTERM exit code 143`() {
        val block = CommandBlock(
            id = "test",
            command = "long-process",
            output = "",
            status = CommandStatus.FAILURE,
            timestamp = System.currentTimeMillis(),
            exitCode = 143,
            workingDirectory = "/home"
        )

        assertTrue(block.isCancelled())
    }

    @Test
    fun `isCancelled returns true for SIGKILL exit code 137`() {
        val block = CommandBlock(
            id = "test",
            command = "stuck-process",
            output = "",
            status = CommandStatus.FAILURE,
            timestamp = System.currentTimeMillis(),
            exitCode = 137,
            workingDirectory = "/home"
        )

        assertTrue(block.isCancelled())
    }

    @Test
    fun `isCancelled returns false for normal exit code 0`() {
        val block = CommandBlock(
            id = "test",
            command = "ls",
            output = "file1",
            status = CommandStatus.SUCCESS,
            timestamp = System.currentTimeMillis(),
            exitCode = 0,
            workingDirectory = "/home"
        )

        assertFalse(block.isCancelled())
    }

    @Test
    fun `isCancelled returns false for error exit code 1`() {
        val block = CommandBlock(
            id = "test",
            command = "invalid-cmd",
            output = "command not found",
            status = CommandStatus.FAILURE,
            timestamp = System.currentTimeMillis(),
            exitCode = 1,
            workingDirectory = "/home"
        )

        assertFalse(block.isCancelled())
    }

    @Test
    fun `lineCount returns correct number of lines`() {
        val block = CommandBlock(
            id = "test",
            command = "ls -la",
            output = "line1\nline2\nline3\nline4\nline5",
            status = CommandStatus.SUCCESS,
            timestamp = System.currentTimeMillis(),
            workingDirectory = "/home"
        )

        assertEquals(5, block.lineCount())
    }

    @Test
    fun `lineCount returns 1 for single line output`() {
        val block = CommandBlock(
            id = "test",
            command = "pwd",
            output = "/home/user",
            status = CommandStatus.SUCCESS,
            timestamp = System.currentTimeMillis(),
            workingDirectory = "/home"
        )

        assertEquals(1, block.lineCount())
    }

    @Test
    fun `lineCount returns 1 for empty output`() {
        val block = CommandBlock(
            id = "test",
            command = "mkdir test",
            output = "",
            status = CommandStatus.SUCCESS,
            timestamp = System.currentTimeMillis(),
            workingDirectory = "/home"
        )

        assertEquals(1, block.lineCount())
    }

    @Test
    fun `CommandStatus enum has all expected values`() {
        assertEquals(4, CommandStatus.values().size)
        assertNotNull(CommandStatus.valueOf("PENDING"))
        assertNotNull(CommandStatus.valueOf("EXECUTING"))
        assertNotNull(CommandStatus.valueOf("SUCCESS"))
        assertNotNull(CommandStatus.valueOf("FAILURE"))
    }
}
