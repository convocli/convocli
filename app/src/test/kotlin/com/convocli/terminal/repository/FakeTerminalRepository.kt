package com.convocli.terminal.repository

import com.convocli.terminal.model.SessionState
import com.convocli.terminal.model.StreamType
import com.convocli.terminal.model.TerminalError
import com.convocli.terminal.model.TerminalOutput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Fake implementation of TerminalRepository for testing.
 *
 * This allows testing ViewModel logic without requiring actual Termux integration.
 * Behavior can be controlled by setting properties and calling methods.
 *
 * ## Usage Example
 * ```kotlin
 * val fakeRepo = FakeTerminalRepository()
 * fakeRepo.sessionCreationResult = Result.success("test-session-id")
 * val viewModel = TerminalViewModel(fakeRepo)
 * ```
 */
class FakeTerminalRepository : TerminalRepository {
    /**
     * Controls what createSession() returns.
     * Set to Result.failure() to simulate initialization failure.
     */
    var sessionCreationResult: Result<String> = Result.success(UUID.randomUUID().toString())

    /**
     * Tracks all commands that were executed.
     * Useful for verifying commands were sent to repository.
     */
    val executedCommands = mutableListOf<Pair<String, String>>() // sessionId, command

    /**
     * Tracks all sessions that were destroyed.
     */
    val destroyedSessions = mutableListOf<String>()

    /**
     * Current session states by ID.
     */
    private val sessionStates = mutableMapOf<String, SessionState>()

    /**
     * Output flow for emitting terminal output.
     */
    private val _output = MutableSharedFlow<TerminalOutput>(replay = 0)
    private val output: SharedFlow<TerminalOutput> = _output.asSharedFlow()

    /**
     * Error flow for emitting terminal errors.
     */
    private val _errors = MutableSharedFlow<TerminalError>(replay = 0)
    private val errors: SharedFlow<TerminalError> = _errors.asSharedFlow()

    /**
     * Current working directory flow.
     */
    private val _currentDirectory = MutableStateFlow("/data/data/com.convocli/files/home")
    val currentDirectory: StateFlow<String> = _currentDirectory.asStateFlow()

    override suspend fun createSession(): Result<String> {
        return sessionCreationResult.also { result ->
            result.onSuccess { sessionId ->
                sessionStates[sessionId] = SessionState.RUNNING
            }
        }
    }

    override suspend fun executeCommand(sessionId: String, command: String) {
        executedCommands.add(sessionId to command)
    }

    override fun observeOutput(sessionId: String): Flow<TerminalOutput> {
        return output
    }

    override fun observeErrors(): SharedFlow<TerminalError> {
        return errors
    }

    override suspend fun destroySession(sessionId: String) {
        destroyedSessions.add(sessionId)
        sessionStates[sessionId] = SessionState.STOPPED
    }

    override fun getSessionState(sessionId: String): SessionState? {
        return sessionStates[sessionId]
    }

    override fun observeWorkingDirectory(sessionId: String): Flow<String> {
        return currentDirectory
    }

    /**
     * Emits output to the output Flow.
     * Call this in tests to simulate terminal output.
     */
    suspend fun emitOutput(text: String, stream: StreamType = StreamType.STDOUT) {
        _output.emit(
            TerminalOutput(
                text = text,
                stream = stream,
                timestamp = System.currentTimeMillis(),
            ),
        )
    }

    /**
     * Emits error to the error Flow.
     * Call this in tests to simulate terminal errors.
     */
    suspend fun emitError(error: TerminalError) {
        _errors.emit(error)
    }

    /**
     * Resets the fake repository to initial state.
     */
    fun reset() {
        executedCommands.clear()
        destroyedSessions.clear()
        sessionStates.clear()
        sessionCreationResult = Result.success(UUID.randomUUID().toString())
    }
}
