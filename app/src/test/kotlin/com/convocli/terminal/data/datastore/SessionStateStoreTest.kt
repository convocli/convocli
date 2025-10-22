package com.convocli.terminal.data.datastore

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Unit tests for SessionStateStore.
 *
 * Tests session state persistence and restoration using Android DataStore.
 *
 * Note: These tests require Android test environment (not pure JVM)
 * because they use DataStore.
 */
@RunWith(AndroidJUnit4::class)
class SessionStateStoreTest {
    private lateinit var context: Context
    private lateinit var store: SessionStateStore

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        store = SessionStateStore(context)
    }

    @Test
    fun saveSessionState_persistsState() = runTest {
        // Given: Session state to save
        val state = PersistedSessionState(
            sessionId = "test-session-123",
            workingDirectory = "/data/data/com.convocli/files/home",
            environment = mapOf(
                "HOME" to "/data/data/com.convocli/files/home",
                "PATH" to "/data/data/com.convocli/files/usr/bin",
                "SHELL" to "/data/data/com.convocli/files/usr/bin/bash",
            ),
            shellPath = "/data/data/com.convocli/files/usr/bin/bash",
            createdAt = System.currentTimeMillis(),
        )

        // When: Saving session state
        store.saveSessionState(state)

        // Then: State should be persisted
        val loaded = store.sessionState.first()
        assertNotNull("Session state should be saved", loaded)
        assertEquals("Session ID should match", state.sessionId, loaded?.sessionId)
        assertEquals("Working directory should match", state.workingDirectory, loaded?.workingDirectory)
        assertEquals("Environment should match", state.environment, loaded?.environment)
        assertEquals("Shell path should match", state.shellPath, loaded?.shellPath)
        assertEquals("Created timestamp should match", state.createdAt, loaded?.createdAt)

        // Cleanup
        store.clearSessionState()
    }

    @Test
    fun sessionState_initiallyNull() = runTest {
        // Given: Fresh store (cleared)
        store.clearSessionState()

        // When: Loading session state
        val loaded = store.sessionState.first()

        // Then: Should be null
        assertNull("Initial session state should be null", loaded)
    }

    @Test
    fun clearSessionState_removesState() = runTest {
        // Given: Saved session state
        val state = PersistedSessionState(
            sessionId = "test-session-456",
            workingDirectory = "/home",
            environment = mapOf("HOME" to "/home"),
            shellPath = "/bin/bash",
            createdAt = System.currentTimeMillis(),
        )
        store.saveSessionState(state)

        // Verify it was saved
        assertNotNull("State should be saved", store.sessionState.first())

        // When: Clearing session state
        store.clearSessionState()

        // Then: State should be null
        val loaded = store.sessionState.first()
        assertNull("Session state should be cleared", loaded)
    }

    @Test
    fun updateWorkingDirectory_updatesOnly() = runTest {
        // Given: Saved session state
        val originalState = PersistedSessionState(
            sessionId = "test-session-789",
            workingDirectory = "/home",
            environment = mapOf(
                "HOME" to "/home",
                "PATH" to "/usr/bin",
            ),
            shellPath = "/bin/bash",
            createdAt = 1234567890L,
        )
        store.saveSessionState(originalState)

        // When: Updating working directory
        val newDirectory = "/home/projects"
        store.updateWorkingDirectory(newDirectory)

        // Then: Only working directory should be updated
        val loaded = store.sessionState.first()
        assertNotNull("State should still exist", loaded)
        assertEquals("Working directory should be updated", newDirectory, loaded?.workingDirectory)
        assertEquals("Session ID should not change", originalState.sessionId, loaded?.sessionId)
        assertEquals("Environment should not change", originalState.environment, loaded?.environment)
        assertEquals("Shell path should not change", originalState.shellPath, loaded?.shellPath)
        assertEquals("Created timestamp should not change", originalState.createdAt, loaded?.createdAt)

        // Cleanup
        store.clearSessionState()
    }

    @Test
    fun saveSessionState_overwritesPrevious() = runTest {
        // Given: Initial session state
        val state1 = PersistedSessionState(
            sessionId = "session-1",
            workingDirectory = "/home",
            environment = mapOf("HOME" to "/home"),
            shellPath = "/bin/bash",
            createdAt = 1000L,
        )
        store.saveSessionState(state1)

        // When: Saving different session state
        val state2 = PersistedSessionState(
            sessionId = "session-2",
            workingDirectory = "/tmp",
            environment = mapOf("HOME" to "/tmp"),
            shellPath = "/bin/sh",
            createdAt = 2000L,
        )
        store.saveSessionState(state2)

        // Then: New state should replace old state
        val loaded = store.sessionState.first()
        assertNotNull("State should exist", loaded)
        assertEquals("Should load latest session ID", state2.sessionId, loaded?.sessionId)
        assertEquals("Should load latest working directory", state2.workingDirectory, loaded?.workingDirectory)
        assertEquals("Should load latest environment", state2.environment, loaded?.environment)

        // Cleanup
        store.clearSessionState()
    }

    @Test
    fun saveSessionState_withComplexEnvironment_preservesAll() = runTest {
        // Given: Session state with many environment variables
        val complexEnv = mapOf(
            "HOME" to "/data/data/com.convocli/files/home",
            "PATH" to "/data/data/com.convocli/files/usr/bin:/system/bin:/system/xbin",
            "SHELL" to "/data/data/com.convocli/files/usr/bin/bash",
            "TMPDIR" to "/data/data/com.convocli/files/usr/tmp",
            "PREFIX" to "/data/data/com.convocli/files/usr",
            "TERM" to "xterm-256color",
            "LANG" to "en_US.UTF-8",
            "USER" to "termux",
            "LOGNAME" to "termux",
        )

        val state = PersistedSessionState(
            sessionId = "complex-session",
            workingDirectory = "/data/data/com.convocli/files/home",
            environment = complexEnv,
            shellPath = "/data/data/com.convocli/files/usr/bin/bash",
            createdAt = System.currentTimeMillis(),
        )

        // When: Saving and loading
        store.saveSessionState(state)
        val loaded = store.sessionState.first()

        // Then: All environment variables should be preserved
        assertNotNull("State should be saved", loaded)
        assertEquals("All env vars should be preserved", complexEnv.size, loaded?.environment?.size)
        complexEnv.forEach { (key, value) ->
            assertEquals("Env var $key should match", value, loaded?.environment?.get(key))
        }

        // Cleanup
        store.clearSessionState()
    }

    @Test
    fun updateWorkingDirectory_whenNoState_doesNotCrash() = runTest {
        // Given: No saved state
        store.clearSessionState()

        // When: Attempting to update working directory
        store.updateWorkingDirectory("/some/path")

        // Then: Should not crash, state remains null
        val loaded = store.sessionState.first()
        assertNull("State should still be null", loaded)
    }
}
