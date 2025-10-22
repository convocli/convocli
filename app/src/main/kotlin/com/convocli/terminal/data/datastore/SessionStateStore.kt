package com.convocli.terminal.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Serializable representation of terminal session state.
 *
 * This class captures all the necessary information to restore a terminal
 * session after app restart or configuration change.
 *
 * ## Persistence Strategy
 * Session state is saved to DataStore (Android's modern preference storage)
 * and restored when the app starts or when needed.
 *
 * ## What Is Persisted
 * - Session ID (for correlation with restored session)
 * - Current working directory
 * - Environment variables
 * - Shell path
 * - Session creation timestamp
 *
 * ## What Is NOT Persisted
 * - Terminal output/history (too large, should use Room for this)
 * - Running processes (can't be restored)
 * - PTY state (must recreate PTY)
 *
 * @property sessionId Unique identifier for this session
 * @property workingDirectory Current working directory path
 * @property environment Map of environment variable names to values
 * @property shellPath Path to the shell executable (bash)
 * @property createdAt Timestamp when session was originally created
 */
@Serializable
data class PersistedSessionState(
    val sessionId: String,
    val workingDirectory: String,
    val environment: Map<String, String>,
    val shellPath: String,
    val createdAt: Long,
)

/**
 * DataStore-based persistence for terminal session state.
 *
 * Provides methods to save and load session state using Android DataStore.
 * Session state is stored as JSON in preferences for simplicity.
 *
 * ## Usage
 * ```kotlin
 * val store = SessionStateStore(context)
 *
 * // Save session state
 * store.saveSessionState(
 *     PersistedSessionState(
 *         sessionId = "session-123",
 *         workingDirectory = "/data/data/com.convocli/files/home",
 *         environment = mapOf("HOME" to "/home", "PATH" to "/usr/bin"),
 *         shellPath = "/usr/bin/bash",
 *         createdAt = System.currentTimeMillis()
 *     )
 * )
 *
 * // Load session state
 * store.sessionState.collect { state ->
 *     if (state != null) {
 *         restoreSession(state)
 *     }
 * }
 * ```
 *
 * ## Lifecycle
 * - Save: When session is created or important state changes (cd, etc.)
 * - Load: On app startup in ViewModel/Repository
 * - Clear: When session is explicitly destroyed by user
 *
 * @property context Application context for DataStore access
 */
class SessionStateStore(private val context: Context) {
    /**
     * Companion object defining DataStore instance.
     *
     * Creates a singleton DataStore named "session_state" scoped to the Context.
     */
    companion object {
        private val Context.sessionStateDataStore: DataStore<Preferences> by preferencesDataStore(
            name = "session_state",
        )

        private val SESSION_STATE_KEY = stringPreferencesKey("terminal_session_state")
    }

    /**
     * JSON serializer for session state.
     *
     * Configured to be lenient and pretty-print for debugging.
     */
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true // For backwards compatibility if model changes
    }

    /**
     * Flow of the current session state.
     *
     * Emits the saved session state whenever it changes, or null if no
     * session state is saved.
     *
     * ## Usage
     * ```kotlin
     * store.sessionState.collect { state ->
     *     when (state) {
     *         null -> createNewSession()
     *         else -> restoreSession(state)
     *     }
     * }
     * ```
     *
     * @return Flow emitting PersistedSessionState or null
     */
    val sessionState: Flow<PersistedSessionState?> = context.sessionStateDataStore.data
        .map { preferences ->
            val stateJson = preferences[SESSION_STATE_KEY]
            if (stateJson != null) {
                try {
                    json.decodeFromString<PersistedSessionState>(stateJson)
                } catch (e: Exception) {
                    // Deserialization failed - return null
                    // This can happen if the model changes incompatibly
                    null
                }
            } else {
                null
            }
        }

    /**
     * Saves session state to DataStore.
     *
     * Serializes the session state to JSON and persists it. This operation
     * is asynchronous and happens on a background thread.
     *
     * ## When to Call
     * - After creating a new session
     * - After executing `cd` command (working directory changed)
     * - After modifying environment variables
     *
     * ## Error Handling
     * Serialization errors are caught and logged, but don't throw.
     * The previous state (if any) remains unchanged if save fails.
     *
     * @param state The session state to save
     */
    suspend fun saveSessionState(state: PersistedSessionState) {
        try {
            val stateJson = json.encodeToString(state)
            context.sessionStateDataStore.edit { preferences ->
                preferences[SESSION_STATE_KEY] = stateJson
            }
        } catch (e: Exception) {
            // Log error but don't throw - persistence is best-effort
            android.util.Log.e("SessionStateStore", "Failed to save session state", e)
        }
    }

    /**
     * Clears the saved session state.
     *
     * Removes the persisted session state from DataStore. Call this when
     * the user explicitly closes/destroys the session.
     *
     * ## When to Call
     * - When user explicitly closes terminal
     * - When starting a fresh session (after clearing)
     * - On logout or app data clear
     */
    suspend fun clearSessionState() {
        context.sessionStateDataStore.edit { preferences ->
            preferences.remove(SESSION_STATE_KEY)
        }
    }

    /**
     * Updates only the working directory in the saved state.
     *
     * Convenience method to update just the working directory without
     * having to load and save the entire state.
     *
     * ## Usage
     * ```kotlin
     * // After cd command
     * store.updateWorkingDirectory("/new/path")
     * ```
     *
     * @param newDirectory The new working directory path
     */
    suspend fun updateWorkingDirectory(newDirectory: String) {
        context.sessionStateDataStore.edit { preferences ->
            val stateJson = preferences[SESSION_STATE_KEY]
            if (stateJson != null) {
                try {
                    val currentState = json.decodeFromString<PersistedSessionState>(stateJson)
                    val updatedState = currentState.copy(workingDirectory = newDirectory)
                    preferences[SESSION_STATE_KEY] = json.encodeToString(updatedState)
                } catch (e: Exception) {
                    android.util.Log.e("SessionStateStore", "Failed to update working directory", e)
                }
            }
        }
    }
}
