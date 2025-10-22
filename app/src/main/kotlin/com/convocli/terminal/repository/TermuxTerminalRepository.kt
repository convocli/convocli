package com.convocli.terminal.repository

import android.content.Context
import com.convocli.terminal.model.SessionState
import com.convocli.terminal.model.TerminalError
import com.convocli.terminal.model.TerminalOutput
import com.convocli.terminal.model.TerminalSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import java.util.UUID

/**
 * Termux-based implementation of TerminalRepository.
 *
 * This class integrates with the Termux terminal-emulator library to provide
 * native Linux terminal functionality on Android. It manages terminal sessions,
 * PTY (pseudo-terminal) operations, and command execution.
 *
 * ## Current Status
 * **STUB IMPLEMENTATION** - This is a minimal stub that implements the interface
 * but does NOT yet integrate with Termux. Real Termux integration will be added
 * in Phase 3 (T013-T018).
 *
 * ## Stub Behavior
 * - `createSession()`: Returns a fake session ID (UUID)
 * - `executeCommand()`: No-op (silently succeeds)
 * - `observeOutput()`: Empty Flow (no output emitted)
 * - `observeErrors()`: Empty SharedFlow (no errors emitted)
 * - `destroySession()`: Removes session from tracking
 * - `getSessionState()`: Returns RUNNING if session exists, null otherwise
 *
 * ## Implementation Plan (Phase 3)
 * The following will be implemented in subsequent tasks:
 * - T013: Initialize Termux TerminalSession (replace fake session creation)
 * - T014: Implement PTY output streaming (real output Flow)
 * - T015: Integrate OutputStreamProcessor (connect to repository)
 * - T016: Implement command execution (write to PTY stdin)
 * - T017: Create TerminalViewModel (consume this repository)
 * - T018: Write integration tests (verify end-to-end functionality)
 *
 * ## Architecture
 * ```
 * TerminalViewModel
 *        ↓
 * TermuxTerminalRepository (this class)
 *        ↓
 * Termux TerminalSession (not yet integrated)
 *        ↓
 * Native PTY (JNI to C code) (not yet integrated)
 * ```
 *
 * @property context Application context for accessing app private directories
 */
class TermuxTerminalRepository(
    private val context: Context,
) : TerminalRepository {
    /**
     * Internal map tracking active sessions by ID.
     *
     * Currently stores fake TerminalSession objects. In Phase 3, this will
     * store actual Termux TerminalSession instances.
     */
    private val sessions = mutableMapOf<String, TerminalSession>()

    /**
     * Shared output flow for all terminal output events.
     *
     * Stub: Currently never emits any events.
     * Phase 3: Will emit TerminalOutput as it's received from PTY.
     */
    private val _output = MutableSharedFlow<TerminalOutput>()

    /**
     * Shared error flow for all terminal error events.
     *
     * Stub: Currently never emits any events.
     * Phase 3: Will emit TerminalError when errors occur.
     */
    private val _errors = MutableSharedFlow<TerminalError>()
    private val errors: SharedFlow<TerminalError> = _errors.asSharedFlow()

    /**
     * Creates a new terminal session (STUB).
     *
     * ## Stub Behavior
     * - Generates a fake UUID session ID
     * - Creates a fake TerminalSession with dummy values
     * - Stores in `sessions` map
     * - Always returns success
     *
     * ## Phase 3 Implementation (T013)
     * Will replace this with real Termux TerminalSession initialization:
     * ```kotlin
     * val termuxSession = TerminalSession(
     *     shellPath = "${context.filesDir}/usr/bin/bash",
     *     workingDir = "${context.filesDir}/home",
     *     args = arrayOf(),
     *     env = getDefaultEnvironment(),
     *     sessionClient = terminalSessionClient
     * )
     * ```
     *
     * @return Result.success with fake session ID
     */
    override suspend fun createSession(): Result<String> {
        val sessionId = UUID.randomUUID().toString()

        // STUB: Create fake session
        val fakeSession = TerminalSession(
            sessionId = sessionId,
            shellPath = "${context.filesDir}/usr/bin/bash", // Fake path
            workingDirectory = "${context.filesDir}/home", // Fake directory
            environment = getDefaultEnvironment(),
            state = SessionState.RUNNING,
            createdAt = System.currentTimeMillis(),
        )

        sessions[sessionId] = fakeSession

        return Result.success(sessionId)
    }

    /**
     * Executes a command in the specified session (STUB).
     *
     * ## Stub Behavior
     * - Validates command is non-empty
     * - Checks session exists
     * - Silently succeeds (does nothing)
     *
     * ## Phase 3 Implementation (T016)
     * Will write command to Termux PTY stdin:
     * ```kotlin
     * termuxSession.write(command + "\n")
     * ```
     *
     * @param sessionId The session to execute in
     * @param command The command to execute
     */
    override suspend fun executeCommand(sessionId: String, command: String) {
        // Validate inputs
        if (command.isBlank()) return
        if (!sessions.containsKey(sessionId)) return

        // STUB: No-op - Phase 3 will implement actual PTY write
    }

    /**
     * Observes output from a specific session (STUB).
     *
     * ## Stub Behavior
     * - Returns Flow that filters by sessionId
     * - Flow never emits any events (no output)
     *
     * ## Phase 3 Implementation (T014-T015)
     * Will emit real TerminalOutput events from PTY:
     * ```kotlin
     * return _output
     *     .filter { output -> /* output is for sessionId */ }
     * ```
     *
     * @param sessionId The session to observe
     * @return Flow of terminal output (currently empty)
     */
    override fun observeOutput(sessionId: String): Flow<TerminalOutput> {
        // STUB: Return empty flow filtered by sessionId
        return _output.filter { false } // Never emits
    }

    /**
     * Observes errors from all sessions (STUB).
     *
     * ## Stub Behavior
     * - Returns SharedFlow that never emits
     *
     * ## Phase 3 Implementation (T024-T027)
     * Will emit real TerminalError events:
     * - InitializationFailed (if createSession fails)
     * - CommandFailed (if command returns non-zero exit code)
     * - SessionCrashed (if PTY breaks or process dies)
     * - IOError (if PTY I/O operations fail)
     *
     * @return SharedFlow of errors (currently never emits)
     */
    override fun observeErrors(): SharedFlow<TerminalError> {
        return errors
    }

    /**
     * Destroys a terminal session (STUB).
     *
     * ## Stub Behavior
     * - Removes session from `sessions` map
     * - No actual cleanup needed (no real PTY)
     *
     * ## Phase 3 Implementation (T032)
     * Will perform full cleanup:
     * 1. Stop output processor
     * 2. Send SIGHUP to shell process
     * 3. Close PTY file descriptors
     * 4. Cancel coroutines
     * 5. Remove from sessions map
     *
     * @param sessionId The session to destroy
     */
    override suspend fun destroySession(sessionId: String) {
        sessions.remove(sessionId)
        // STUB: No actual cleanup needed
    }

    /**
     * Gets the current state of a session (STUB).
     *
     * ## Stub Behavior
     * - Returns RUNNING if session exists in map
     * - Returns null if session doesn't exist
     *
     * ## Phase 3 Implementation (T029)
     * Will return actual session state:
     * - RUNNING: PTY active, shell process alive
     * - STOPPED: Session terminated normally
     * - ERROR: Session crashed
     *
     * @param sessionId The session to check
     * @return SessionState.RUNNING or null
     */
    override fun getSessionState(sessionId: String): SessionState? {
        return sessions[sessionId]?.state
    }

    /**
     * Gets the default environment variables for a terminal session.
     *
     * These are the standard Unix/Linux environment variables expected
     * by shell scripts and command-line programs.
     *
     * ## Environment Variables
     * - **HOME**: User's home directory (app private directory)
     * - **PATH**: Executable search path (app binaries + system binaries)
     * - **SHELL**: Path to the shell executable (bash)
     * - **TMPDIR**: Temporary files directory
     * - **PREFIX**: Termux installation prefix
     * - **TERM**: Terminal type for ANSI escape sequences
     * - **LANG**: Locale for character encoding
     *
     * @return Map of environment variable names to values
     */
    private fun getDefaultEnvironment(): Map<String, String> {
        val filesDir = context.filesDir.absolutePath
        return mapOf(
            "HOME" to "$filesDir/home",
            "PATH" to "$filesDir/usr/bin:/system/bin",
            "SHELL" to "$filesDir/usr/bin/bash",
            "TMPDIR" to "$filesDir/usr/tmp",
            "PREFIX" to "$filesDir/usr",
            "TERM" to "xterm-256color",
            "LANG" to "en_US.UTF-8",
        )
    }
}
