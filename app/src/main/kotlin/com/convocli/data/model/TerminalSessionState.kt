package com.convocli.data.model

/**
 * Represents terminal session creation result.
 *
 * Used by TerminalRepository.createSession() to communicate session state.
 */
sealed class TerminalSessionState {
    /** Session created successfully and ready for commands */
    data class Ready(val session: TerminalSession) : TerminalSessionState()

    /** Session creation failed with error */
    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : TerminalSessionState()

    /** Session is inactive or terminated */
    object Inactive : TerminalSessionState()
}
