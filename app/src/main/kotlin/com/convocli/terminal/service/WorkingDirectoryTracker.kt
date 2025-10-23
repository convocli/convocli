package com.convocli.terminal.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

/**
 * Tracks the current working directory for terminal sessions.
 *
 * This class monitors `cd` commands and maintains the current working directory
 * state for UI display purposes. Note that the actual working directory of the
 * shell process is managed by bash itself - this tracker is purely for showing
 * the current directory in the UI.
 *
 * ## Functionality
 * - Tracks `cd` command executions
 * - Resolves relative and absolute paths
 * - Handles special cases (`cd ~`, `cd ..`, `cd -`)
 * - Exposes current directory via StateFlow
 *
 * ## Path Resolution
 * - Absolute paths: `/storage/emulated/0` → Used as-is
 * - Relative paths: `cd subdir` → Appends to current directory
 * - Parent directory: `cd ..` → Moves up one level
 * - Home directory: `cd ~` or `cd` → Goes to $HOME
 * - Previous directory: `cd -` → Returns to previous directory (not yet implemented)
 *
 * ## Usage Example
 * ```kotlin
 * val tracker = WorkingDirectoryTracker(initialDirectory = "/data/data/com.convocli/files/home")
 *
 * tracker.onCommand("cd /storage")  // Changes to /storage
 * println(tracker.currentDirectory.value)  // "/storage"
 *
 * tracker.onCommand("cd subdir")  // Changes to /storage/subdir
 * println(tracker.currentDirectory.value)  // "/storage/subdir"
 * ```
 *
 * @property initialDirectory The starting directory (typically $HOME)
 */
class WorkingDirectoryTracker(
    initialDirectory: String,
) {
    /**
     * The current working directory.
     *
     * Updated whenever a `cd` command is detected.
     * Exposed as StateFlow for reactive UI updates.
     */
    private val _currentDirectory = MutableStateFlow(initialDirectory)
    val currentDirectory: StateFlow<String> = _currentDirectory.asStateFlow()

    /**
     * The previous working directory (for `cd -`).
     *
     * Not yet implemented, but reserved for future use.
     */
    private var previousDirectory: String = initialDirectory

    /**
     * Processes a command and updates the current directory if it's a `cd` command.
     *
     * This method should be called for every command executed in the terminal.
     * It will detect `cd` commands and update the tracked directory accordingly.
     *
     * ## Supported Formats
     * - `cd` - Changes to $HOME
     * - `cd ~` - Changes to $HOME
     * - `cd /absolute/path` - Changes to absolute path
     * - `cd relative/path` - Changes relative to current directory
     * - `cd ..` - Moves up one directory level
     * - `cd ../..` - Moves up two directory levels
     *
     * ## Not Supported (Yet)
     * - `cd -` - Return to previous directory
     * - Environment variable expansion in paths
     *
     * @param command The command to process
     * @param homeDirectory The user's home directory (for `cd` and `cd ~`)
     */
    fun onCommand(command: String, homeDirectory: String) {
        val trimmedCommand = command.trim()

        // Check if it's a cd command
        if (!trimmedCommand.startsWith("cd")) {
            return
        }

        // Extract the path argument
        val parts = trimmedCommand.split(Regex("\\s+"), limit = 2)
        if (parts.size == 1) {
            // Just "cd" with no argument → go to home
            changeDirectory(homeDirectory)
            return
        }

        val path = parts[1].trim()

        // Handle special cases
        when {
            path.isEmpty() -> {
                // "cd " with whitespace → go to home
                changeDirectory(homeDirectory)
            }
            path == "~" -> {
                // "cd ~" → go to home
                changeDirectory(homeDirectory)
            }
            path.startsWith("~/") -> {
                // "cd ~/subdir" → go to home/subdir
                val relativePath = path.substringAfter("~/")
                changeDirectory(File(homeDirectory, relativePath).absolutePath)
            }
            path == "-" -> {
                // "cd -" → go to previous directory (not yet implemented)
                // TODO: Implement previous directory tracking
                changeDirectory(previousDirectory)
            }
            path.startsWith("/") -> {
                // Absolute path
                changeDirectory(path)
            }
            else -> {
                // Relative path
                val newPath = resolvePath(_currentDirectory.value, path)
                changeDirectory(newPath)
            }
        }
    }

    /**
     * Changes the current directory to the specified path.
     *
     * Updates both current and previous directory.
     *
     * @param newDirectory The new directory path
     */
    private fun changeDirectory(newDirectory: String) {
        previousDirectory = _currentDirectory.value
        _currentDirectory.value = newDirectory
    }

    /**
     * Resolves a relative path against the current directory.
     *
     * Handles `.` (current directory) and `..` (parent directory) correctly.
     *
     * ## Examples
     * - `resolvePath("/home", "subdir")` → `/home/subdir`
     * - `resolvePath("/home/user", "..")` → `/home`
     * - `resolvePath("/home/user", "../other")` → `/home/other`
     * - `resolvePath("/home", ".")` → `/home`
     *
     * @param currentPath The current working directory
     * @param relativePath The relative path to resolve
     * @return The resolved absolute path
     */
    private fun resolvePath(currentPath: String, relativePath: String): String {
        val current = File(currentPath)
        val target = File(current, relativePath)

        // Normalize the path (resolves .. and .)
        return try {
            target.canonicalPath
        } catch (e: Exception) {
            // If canonicalPath fails, use absolutePath as fallback
            target.absolutePath
        }
    }

    /**
     * Resets the tracker to the initial directory.
     *
     * Useful for session restart or testing.
     *
     * @param directory The directory to reset to (defaults to initial directory)
     */
    fun reset(directory: String? = null) {
        val resetDir = directory ?: _currentDirectory.value
        _currentDirectory.value = resetDir
        previousDirectory = resetDir
    }
}
