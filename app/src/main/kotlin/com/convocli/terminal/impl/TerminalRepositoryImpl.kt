package com.convocli.terminal.impl

import android.content.Context
import android.util.Log
import com.convocli.data.model.OutputChunk
import com.convocli.data.model.ProcessState
import com.convocli.data.model.StreamType
import com.convocli.data.model.TerminalSession
import com.convocli.data.model.TerminalSessionState
import com.convocli.terminal.TerminalRepository
import com.termux.terminal.TerminalSessionClient
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of TerminalRepository that wraps Termux TerminalSession.
 *
 * This is a foundational implementation for Sprint 02. Full Termux integration
 * will be completed in Phase 3 (US1 implementation).
 *
 * Current implementation provides:
 * - Session lifecycle management
 * - Command execution infrastructure
 * - Output streaming foundation
 * - Working directory tracking
 *
 * TODO: Complete Termux integration in T017
 */
@Singleton
class TerminalRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : TerminalRepository {

    companion object {
        private const val TAG = "TerminalRepository"
    }

    // Session state
    private var currentSession: com.termux.terminal.TerminalSession? = null

    @Volatile
    private var currentBlockId: String? = null

    @Volatile
    private var lastTranscriptLength = 0

    // Reactive streams
    // Buffer chunks in case collector isn't ready yet (prevents race condition)
    private val _outputFlow = MutableSharedFlow<OutputChunk>(
        replay = 0,
        extraBufferCapacity = 64,  // Buffer up to 64 chunks
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val _workingDirectory = MutableStateFlow("/data/data/com.convocli/files/home")
    private val _isSessionActive = MutableStateFlow(false)

    // Exit code tracking
    private val commandExitCodes = mutableMapOf<String, Int>()

    override val workingDirectory: StateFlow<String> = _workingDirectory.asStateFlow()
    override val isSessionActive: StateFlow<Boolean> = _isSessionActive.asStateFlow()

    override fun createSession(): Flow<TerminalSessionState> = flow {
        try {
            Log.d(TAG, "Creating terminal session...")

            val sessionId = UUID.randomUUID().toString()
            val filesDir = context.filesDir.absolutePath

            // Prepare environment variables
            val env = getDefaultEnvironment()
            val envArray = env.map { "${it.key}=${it.value}" }.toTypedArray()

            // Ensure home directory exists
            val homeDir = java.io.File("$filesDir/home")
            if (!homeDir.exists()) {
                homeDir.mkdirs()
                Log.d(TAG, "Created home directory: ${homeDir.absolutePath}")
            }

            // Prepare shell arguments
            val shellPath = "$filesDir/usr/bin/bash"
            val workingDir = homeDir.absolutePath
            val args = arrayOf("bash") // First arg is process name

            // Check if bash exists (critical for terminal functionality)
            val shellFile = java.io.File(shellPath)
            if (!shellFile.exists()) {
                val errorMsg = "Bash shell not found at $shellPath - Bootstrap not installed! " +
                    "Terminal will not function until bootstrap is installed."
                Log.e(TAG, errorMsg)
                Log.e(TAG, "Install bootstrap via Feature 003 or bootstrap installation UI")
                throw IllegalStateException(errorMsg)
            }

            Log.d(TAG, "Creating Termux session: shell=$shellPath, cwd=$workingDir")
            Log.d(TAG, "Shell exists: ${shellFile.exists()}, executable: ${shellFile.canExecute()}")

            // Create Termux TerminalSession with callback client
            currentSession = com.termux.terminal.TerminalSession(
                shellPath,
                workingDir,
                args,
                envArray,
                10000, // transcript rows (history buffer)
                createSessionClient(sessionId)
            )

            // Initialize terminal size (standard 80x24)
            currentSession?.updateSize(80, 24, 10, 20)

            // Create our data model
            val session = TerminalSession(
                sessionId = sessionId,
                currentDirectory = workingDir,
                environment = env,
                processState = ProcessState.ACTIVE,
                creationTime = System.currentTimeMillis()
            )

            _workingDirectory.value = workingDir
            _isSessionActive.value = true

            Log.d(TAG, "Terminal session created successfully: $sessionId")
            emit(TerminalSessionState.Ready(session))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create terminal session", e)
            _isSessionActive.value = false
            emit(TerminalSessionState.Error("Failed to create session: ${e.message}", e))
        }
    }.flowOn(Dispatchers.IO)  // CRITICAL: Run file I/O operations on background thread

    override suspend fun destroySession() {
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Destroying terminal session...")

                currentSession?.finishIfRunning()
                currentSession = null
                currentBlockId = null
                commandExitCodes.clear()

                _isSessionActive.value = false
                Log.d(TAG, "Terminal session destroyed")
            } catch (e: Exception) {
                Log.e(TAG, "Error destroying session", e)
            }
        }
    }

    override suspend fun executeCommand(command: String, blockId: String) {
        require(command.isNotEmpty()) { "Command cannot be empty" }
        require(command.length <= 4096) { "Command too long (max 4096 chars)" }

        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Executing command: $command (block: $blockId)")

                // Defensive check: Ensure session is active before attempting write
                if (!_isSessionActive.value) {
                    throw IllegalStateException(
                        "Terminal session not ready. Please wait for initialization to complete."
                    )
                }

                currentBlockId = blockId

                // Write command to PTY stdin with newline
                currentSession?.write("$command\n")
                    ?: throw IllegalStateException("No active terminal session")

                Log.d(TAG, "Command sent to terminal")
            } catch (e: Exception) {
                Log.e(TAG, "Error executing command", e)
                throw e
            }
        }
    }

    override suspend fun cancelCommand(blockId: String) {
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Canceling command for block: $blockId")

                // Send Ctrl+C (SIGINT) to interrupt running command
                // ASCII 3 (ETX - End of Text) sends SIGINT signal
                currentSession?.write("\u0003")
                    ?: Log.w(TAG, "No active session to cancel")

                // Mark as canceled
                commandExitCodes[blockId] = -1

                Log.d(TAG, "SIGINT sent to terminal")
            } catch (e: Exception) {
                Log.e(TAG, "Error canceling command", e)
            }
        }
    }

    override fun observeOutput(): Flow<OutputChunk> = _outputFlow.asSharedFlow()

    override suspend fun getExitCode(blockId: String): Int? {
        return commandExitCodes[blockId]
    }

    /**
     * Internal method to emit output chunks.
     * Will be called by Termux session callback in Phase 3.
     */
    internal suspend fun emitOutput(chunk: OutputChunk) {
        _outputFlow.emit(chunk)
    }

    /**
     * Internal method to update working directory.
     * Will be called when cd commands are detected.
     */
    internal fun updateWorkingDirectory(directory: String) {
        _workingDirectory.value = directory
        Log.d(TAG, "Working directory updated: $directory")
    }

    /**
     * Internal method to store exit code.
     * Will be called when command completes.
     */
    internal fun storeExitCode(blockId: String, exitCode: Int) {
        commandExitCodes[blockId] = exitCode
        Log.d(TAG, "Exit code stored for block $blockId: $exitCode")
    }

    /**
     * Creates a TerminalSessionClient callback for Termux session.
     * Handles terminal events and converts them to our Flow-based API.
     */
    private fun createSessionClient(@Suppress("UNUSED_PARAMETER") sessionId: String): TerminalSessionClient {
        return object : TerminalSessionClient {
            override fun onTextChanged(session: com.termux.terminal.TerminalSession) {
                // Terminal screen updated - emit output
                try {
                    val screen = session.emulator.screen
                    val text = screen.transcriptText

                    // Calculate new content since last update (incremental output)
                    val newText = if (lastTranscriptLength < text.length) {
                        text.substring(lastTranscriptLength)
                    } else {
                        "" // No new content
                    }

                    // Update tracker for next change
                    lastTranscriptLength = text.length

                    // Only emit if we have new content and a valid block ID
                    val blockId = currentBlockId
                    if (blockId != null && newText.isNotEmpty()) {
                        // Create output chunk with only new content
                        val chunk = OutputChunk(
                            blockId = blockId,
                            data = newText,
                            streamType = StreamType.STDOUT,
                            timestamp = System.currentTimeMillis()
                        )

                        // Emit to output flow (check for success)
                        val emitted = _outputFlow.tryEmit(chunk)
                        if (!emitted) {
                            Log.w(TAG, "Failed to emit chunk - buffer full or no collectors! blockId=${chunk.blockId}")
                        }
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Error reading terminal output", e)
                }
            }

            override fun onTitleChanged(session: com.termux.terminal.TerminalSession) {
                // Terminal title changed - not currently used
            }

            override fun onSessionFinished(session: com.termux.terminal.TerminalSession) {
                // Shell process exited
                _isSessionActive.value = false
                Log.w(TAG, "Terminal session finished (exit code: ${session.exitStatus})")

                // Store exit code for current command
                val blockId = currentBlockId
                if (blockId != null) {
                    commandExitCodes[blockId] = session.exitStatus
                }
            }

            override fun onCopyTextToClipboard(session: com.termux.terminal.TerminalSession, text: String) {
                // Clipboard copy - not implemented yet
            }

            override fun onPasteTextFromClipboard(session: com.termux.terminal.TerminalSession) {
                // Clipboard paste - not implemented yet
            }

            override fun onBell(session: com.termux.terminal.TerminalSession) {
                // Terminal bell - not implemented yet
            }

            override fun onColorsChanged(session: com.termux.terminal.TerminalSession) {
                // Color scheme changed - not implemented yet
            }

            override fun onTerminalCursorStateChange(state: Boolean) {
                // Cursor visibility changed - not used
            }

            override fun getTerminalCursorStyle(): Int? = null

            override fun logError(tag: String, message: String) {
                Log.e(tag, message)
            }

            override fun logWarn(tag: String, message: String) {
                Log.w(tag, message)
            }

            override fun logInfo(tag: String, message: String) {
                Log.i(tag, message)
            }

            override fun logDebug(tag: String, message: String) {
                Log.d(tag, message)
            }

            override fun logVerbose(tag: String, message: String) {
                Log.v(tag, message)
            }

            override fun logStackTraceWithMessage(tag: String, message: String, e: Exception) {
                Log.e(tag, message, e)
            }

            override fun logStackTrace(tag: String, e: Exception) {
                Log.e(tag, "Exception occurred", e)
            }
        }
    }

    /**
     * Gets the default environment variables for a terminal session.
     * These are standard Unix/Linux environment variables.
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
            "LANG" to "en_US.UTF-8"
        )
    }
}
