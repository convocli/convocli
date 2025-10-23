package com.convocli.terminal.service

import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Unit tests for WorkingDirectoryTracker.
 */
class WorkingDirectoryTrackerTest {
    private lateinit var tracker: WorkingDirectoryTracker
    private val homeDirectory = "/data/data/com.convocli/files/home"

    @Before
    fun setup() {
        tracker = WorkingDirectoryTracker(initialDirectory = homeDirectory)
    }

    @Test
    fun `initial directory is set correctly`() = runTest {
        assertEquals(homeDirectory, tracker.currentDirectory.value)
    }

    @Test
    fun `cd with no argument goes to home`() = runTest {
        // Given: Currently in a different directory
        tracker.onCommand("cd /storage", homeDirectory)

        // When: Run cd with no argument
        tracker.onCommand("cd", homeDirectory)

        // Then: Should be in home directory
        assertEquals(homeDirectory, tracker.currentDirectory.value)
    }

    @Test
    fun `cd tilde goes to home`() = runTest {
        // Given: Currently in a different directory
        tracker.onCommand("cd /storage", homeDirectory)

        // When: Run cd ~
        tracker.onCommand("cd ~", homeDirectory)

        // Then: Should be in home directory
        assertEquals(homeDirectory, tracker.currentDirectory.value)
    }

    @Test
    fun `cd absolute path changes directory`() = runTest {
        // When: Change to absolute path
        tracker.onCommand("cd /storage/emulated/0", homeDirectory)

        // Then: Directory should be updated
        assertEquals("/storage/emulated/0", tracker.currentDirectory.value)
    }

    @Test
    fun `cd relative path appends to current directory`() = runTest {
        // Given: In home directory
        assertEquals(homeDirectory, tracker.currentDirectory.value)

        // When: Change to relative path
        tracker.onCommand("cd subdir", homeDirectory)

        // Then: Should append to current directory
        assertEquals("$homeDirectory/subdir", tracker.currentDirectory.value)
    }

    @Test
    fun `cd parent directory moves up one level`() = runTest {
        // Given: In a subdirectory
        tracker.onCommand("cd /data/data/com.convocli/files/home/subdir", homeDirectory)

        // When: Go to parent directory
        tracker.onCommand("cd ..", homeDirectory)

        // Then: Should move up one level
        assertEquals("/data/data/com.convocli/files/home", tracker.currentDirectory.value)
    }

    @Test
    fun `cd multiple parent directories moves up correctly`() = runTest {
        // Given: In a nested subdirectory
        tracker.onCommand("cd /data/data/com.convocli/files/home/a/b/c", homeDirectory)

        // When: Go up two levels
        tracker.onCommand("cd ../..", homeDirectory)

        // Then: Should be two levels up
        assertEquals("/data/data/com.convocli/files/home/a", tracker.currentDirectory.value)
    }

    @Test
    fun `cd with tilde slash goes to home subdirectory`() = runTest {
        // When: cd ~/Documents
        tracker.onCommand("cd ~/Documents", homeDirectory)

        // Then: Should be in home/Documents
        assertEquals("$homeDirectory/Documents", tracker.currentDirectory.value)
    }

    @Test
    fun `cd current directory dot does not change directory`() = runTest {
        // Given: In home directory
        val initial = tracker.currentDirectory.value

        // When: cd .
        tracker.onCommand("cd .", homeDirectory)

        // Then: Should stay in same directory
        assertEquals(initial, tracker.currentDirectory.value)
    }

    @Test
    fun `cd minus goes to previous directory`() = runTest {
        // Given: Navigate to a directory
        tracker.onCommand("cd /storage", homeDirectory)
        assertEquals("/storage", tracker.currentDirectory.value)

        // Then navigate somewhere else
        tracker.onCommand("cd /sdcard", homeDirectory)
        assertEquals("/sdcard", tracker.currentDirectory.value)

        // When: cd -
        tracker.onCommand("cd -", homeDirectory)

        // Then: Should be back in previous directory
        assertEquals("/storage", tracker.currentDirectory.value)
    }

    @Test
    fun `non-cd commands do not change directory`() = runTest {
        // Given: In home directory
        val initial = tracker.currentDirectory.value

        // When: Execute non-cd commands
        tracker.onCommand("ls -la", homeDirectory)
        tracker.onCommand("echo hello", homeDirectory)
        tracker.onCommand("cat file.txt", homeDirectory)

        // Then: Directory should not change
        assertEquals(initial, tracker.currentDirectory.value)
    }

    @Test
    fun `cd command with extra whitespace is handled correctly`() = runTest {
        // When: cd with extra spaces
        tracker.onCommand("cd    /storage", homeDirectory)

        // Then: Should still work
        assertEquals("/storage", tracker.currentDirectory.value)
    }

    @Test
    fun `reset restores directory to specified path`() = runTest {
        // Given: Navigate to a different directory
        tracker.onCommand("cd /storage", homeDirectory)
        assertEquals("/storage", tracker.currentDirectory.value)

        // When: Reset to home
        tracker.reset(homeDirectory)

        // Then: Should be back in home
        assertEquals(homeDirectory, tracker.currentDirectory.value)
    }

    @Test
    fun `complex navigation scenario works correctly`() = runTest {
        // Start at home
        assertEquals(homeDirectory, tracker.currentDirectory.value)

        // cd to absolute path
        tracker.onCommand("cd /storage/emulated/0", homeDirectory)
        assertEquals("/storage/emulated/0", tracker.currentDirectory.value)

        // cd to relative subdirectory
        tracker.onCommand("cd Documents", homeDirectory)
        assertEquals("/storage/emulated/0/Documents", tracker.currentDirectory.value)

        // cd to parent
        tracker.onCommand("cd ..", homeDirectory)
        assertEquals("/storage/emulated/0", tracker.currentDirectory.value)

        // cd to home
        tracker.onCommand("cd ~", homeDirectory)
        assertEquals(homeDirectory, tracker.currentDirectory.value)

        // cd to home subdirectory
        tracker.onCommand("cd ~/Downloads", homeDirectory)
        assertEquals("$homeDirectory/Downloads", tracker.currentDirectory.value)
    }
}
