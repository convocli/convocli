package com.convocli.data.model

import java.util.UUID

/**
 * Represents an active terminal emulator session that executes commands.
 *
 * This is a transient model (not persisted) that wraps the Termux TerminalSession.
 * Single session for Sprint 02; multiple sessions planned for future sprints.
 */
data class TerminalSession(
    /** Unique session identifier */
    val sessionId: String = UUID.randomUUID().toString(),

    /** Current working directory */
    val currentDirectory: String = "/data/data/com.convocli/files/home",

    /** Environment variables */
    val environment: Map<String, String> = emptyMap(),

    /** Session process state */
    val processState: ProcessState = ProcessState.ACTIVE,

    /** Unix timestamp (ms) when created */
    val creationTime: Long = System.currentTimeMillis()
)

/**
 * Represents terminal process lifecycle state.
 */
enum class ProcessState {
    /** Process is running and accepting commands */
    ACTIVE,

    /** Process has terminated or crashed */
    INACTIVE
}
