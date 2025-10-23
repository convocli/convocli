package com.convocli.terminal.contracts

import com.convocli.data.model.TerminalSession
import kotlinx.coroutines.flow.StateFlow

/**
 * Contract for tracking and exposing the current working directory.
 *
 * The working directory is:
 * - Extracted from terminal session environment (PWD variable)
 * - Updated when `cd` commands are executed
 * - Displayed in command block headers
 * - Persisted with each command block
 */
interface WorkingDirectoryTracker {

    /**
     * Current working directory as an absolute path.
     *
     * Initial value: /data/data/com.convocli/files/home
     * Updates when cd commands are detected or environment changes.
     *
     * @return StateFlow emitting current directory path
     */
    val currentDirectory: StateFlow<String>

    /**
     * Updates the working directory from terminal session environment.
     *
     * Queries the PWD environment variable from the terminal session
     * and updates currentDirectory state.
     *
     * @param session The active terminal session
     */
    suspend fun updateFromEnvironment(session: TerminalSession)

    /**
     * Handles a cd command and updates working directory accordingly.
     *
     * Parses the cd command to extract target directory and resolves:
     * - Absolute paths: /tmp → /tmp
     * - Relative paths: ../foo → resolves from current
     * - Home directory: ~ → /data/data/com.convocli/files/home
     * - cd with no args → home directory
     *
     * @param command The command string (e.g., "cd /tmp", "cd ..", "cd")
     */
    suspend fun handleCdCommand(command: String)

    /**
     * Validates if a directory path exists and is accessible.
     *
     * @param path The directory path to validate
     * @return True if directory exists and is readable
     */
    suspend fun isValidDirectory(path: String): Boolean
}
