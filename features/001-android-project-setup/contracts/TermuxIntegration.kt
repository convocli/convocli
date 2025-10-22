/**
 * Termux Integration Contracts
 *
 * Feature: 001-android-project-setup
 * Purpose: Defines the contract between ConvoCLI UI layer and Termux terminal backend
 * Status: Design specification (not executable code)
 *
 * This file serves as a contract specification for the Termux integration layer.
 * It will be implemented in the actual codebase during feature development.
 */

package com.convocli.terminal.contracts

import kotlinx.coroutines.flow.Flow

// ============================================================================
// TERMINAL SESSION CONTRACTS
// ============================================================================

/**
 * Contract for terminal session management.
 *
 * Implementations: TermuxRepository
 */
interface TerminalSessionContract {

    /**
     * Creates a new terminal session with default bash shell.
     *
     * @param workingDirectory Initial working directory
     * @return Flow emitting session state changes
     */
    fun createSession(
        workingDirectory: String = DEFAULT_HOME_DIR
    ): Flow<TerminalSessionState>

    /**
     * Executes a command in the active terminal session.
     *
     * @param command Command text to execute
     * @return Result containing command output or error
     */
    suspend fun executeCommand(command: String): Result<CommandOutput>

    /**
     * Observes terminal output in real-time.
     *
     * @return Flow emitting terminal output as it's generated
     */
    fun observeOutput(): Flow<String>

    /**
     * Sends raw input to the terminal (for interactive programs).
     *
     * @param input Raw input string
     */
    suspend fun sendInput(input: String)

    /**
     * Terminates the active terminal session.
     */
    suspend fun terminateSession()

    /**
     * Gets the current working directory of the terminal session.
     *
     * @return Current working directory path
     */
    suspend fun getCurrentWorkingDirectory(): String

    companion object {
        const val DEFAULT_HOME_DIR = "/data/data/com.convocli/files/home"
        const val DEFAULT_SHELL = "/data/data/com.convocli/files/usr/bin/bash"
    }
}

/**
 * Represents the state of a terminal session.
 */
sealed class TerminalSessionState {
    object Initializing : TerminalSessionState()
    data class Ready(val sessionId: String) : TerminalSessionState()
    data class Error(val message: String, val cause: Throwable? = null) : TerminalSessionState()
    object Terminated : TerminalSessionState()
}

/**
 * Represents the output from a command execution.
 */
data class CommandOutput(
    val stdout: String,
    val stderr: String = "",
    val exitCode: Int,
    val duration: Long  // milliseconds
) {
    val isSuccess: Boolean get() = exitCode == 0
    val combinedOutput: String get() = if (stderr.isEmpty()) stdout else "$stdout\n$stderr"
}

// ============================================================================
// TERMUX SESSION CLIENT CALLBACK
// ============================================================================

/**
 * Callback interface for Termux session events.
 *
 * This mirrors the Termux TerminalSessionClient interface with Kotlin-friendly naming.
 */
interface TermuxSessionCallback {

    /**
     * Called when terminal text changes (new output).
     *
     * @param text New text added to terminal
     */
    fun onTextChanged(text: String)

    /**
     * Called when terminal title changes.
     *
     * @param title New terminal title
     */
    fun onTitleChanged(title: String)

    /**
     * Called when terminal session finishes.
     *
     * @param exitCode Process exit code
     */
    fun onSessionFinished(exitCode: Int)

    /**
     * Called when clipboard content should be pasted.
     *
     * @param text Clipboard text
     */
    fun onClipboardText(text: String)

    /**
     * Called when terminal bell rings.
     */
    fun onBell()

    /**
     * Called when terminal colors change.
     */
    fun onColorsChanged()
}

// ============================================================================
// COMMAND HISTORY CONTRACT
// ============================================================================

/**
 * Contract for managing command history.
 *
 * Implementations: CommandHistoryRepository
 */
interface CommandHistoryContract {

    /**
     * Saves a command to history.
     *
     * @param command Command text
     * @param workingDirectory Directory where command was executed
     * @return Command ID
     */
    suspend fun saveCommand(
        command: String,
        workingDirectory: String
    ): Long

    /**
     * Updates command with output and exit code.
     *
     * @param commandId Command ID
     * @param output Command output
     * @param exitCode Exit code
     */
    suspend fun updateCommandOutput(
        commandId: Long,
        output: String,
        exitCode: Int
    )

    /**
     * Observes all commands in history (most recent first).
     *
     * @return Flow of command list
     */
    fun observeHistory(): Flow<List<Command>>

    /**
     * Searches command history.
     *
     * @param query Search query
     * @return List of matching commands
     */
    suspend fun search(query: String): List<Command>

    /**
     * Gets recent commands.
     *
     * @param limit Number of commands to retrieve
     * @return List of recent commands
     */
    suspend fun getRecent(limit: Int = 100): List<Command>

    /**
     * Clears all command history.
     */
    suspend fun clearHistory()

    /**
     * Deletes commands older than the specified timestamp.
     *
     * @param timestamp Unix timestamp (milliseconds)
     * @return Number of commands deleted
     */
    suspend fun deleteOlderThan(timestamp: Long): Int
}

/**
 * Data class representing a persisted command.
 * This is the contract version (actual Room entity may differ slightly).
 */
data class Command(
    val id: Long,
    val commandText: String,
    val output: String?,
    val exitCode: Int?,
    val executedAt: Long,
    val workingDirectory: String,
    val sessionId: String?
)

// ============================================================================
// COMMAND BLOCK PARSER CONTRACT
// ============================================================================

/**
 * Contract for parsing terminal output into command blocks.
 *
 * Implementations: CommandBlockParser
 */
interface CommandBlockParserContract {

    /**
     * Detects if terminal output indicates command completion (prompt detected).
     *
     * @param output Terminal output text
     * @return true if prompt detected (command finished)
     */
    fun detectCommandEnd(output: String): Boolean

    /**
     * Extracts command from terminal output.
     *
     * @param output Terminal output containing command
     * @return Extracted command text or null
     */
    fun extractCommand(output: String): String?

    /**
     * Parses terminal output to separate command from output.
     *
     * @param terminalOutput Full terminal output
     * @return Parsed command block data
     */
    fun parseCommandBlock(terminalOutput: String): CommandBlockData
}

/**
 * Represents parsed command block data.
 */
data class CommandBlockData(
    val command: String,
    val output: String,
    val hasPrompt: Boolean
)

// ============================================================================
// TERMINAL OUTPUT PROCESSOR CONTRACT
// ============================================================================

/**
 * Contract for processing and formatting terminal output.
 *
 * Implementations: TerminalOutputProcessor
 */
interface TerminalOutputProcessorContract {

    /**
     * Strips ANSI escape codes from output.
     *
     * @param output Raw terminal output with ANSI codes
     * @return Plain text output
     */
    fun stripAnsiCodes(output: String): String

    /**
     * Preserves ANSI codes but converts to Compose-renderable format.
     *
     * @param output Raw terminal output with ANSI codes
     * @return Formatted output for Compose UI
     */
    fun formatForCompose(output: String): FormattedOutput

    /**
     * Truncates output if it exceeds maximum length.
     *
     * @param output Output text
     * @param maxLength Maximum length in characters
     * @return Truncated output with indicator
     */
    fun truncateOutput(output: String, maxLength: Int = MAX_OUTPUT_LENGTH): String

    companion object {
        const val MAX_OUTPUT_LENGTH = 1_000_000  // 1MB
    }
}

/**
 * Represents formatted terminal output for UI rendering.
 */
data class FormattedOutput(
    val text: String,
    val spans: List<TextSpan>  // Color/style spans for Compose AnnotatedString
)

/**
 * Represents a styled text span.
 */
data class TextSpan(
    val start: Int,
    val end: Int,
    val style: TextStyle
)

/**
 * Terminal text styling.
 */
sealed class TextStyle {
    data class Color(val foreground: Int?, val background: Int?) : TextStyle()
    object Bold : TextStyle()
    object Italic : TextStyle()
    object Underline : TextStyle()
}

// ============================================================================
// SETTINGS CONTRACT
// ============================================================================

/**
 * Contract for application settings management.
 *
 * Implementations: SettingsRepository
 */
interface SettingsContract {

    /**
     * Observes dark mode setting.
     *
     * @return Flow emitting dark mode enabled state
     */
    fun observeDarkMode(): Flow<Boolean>

    /**
     * Sets dark mode preference.
     *
     * @param enabled true to enable dark mode
     */
    suspend fun setDarkMode(enabled: Boolean)

    /**
     * Observes default shell path.
     *
     * @return Flow emitting shell path
     */
    fun observeDefaultShell(): Flow<String>

    /**
     * Sets default shell.
     *
     * @param shellPath Path to shell executable
     */
    suspend fun setDefaultShell(shellPath: String)

    /**
     * Observes terminal font size.
     *
     * @return Flow emitting font size in SP
     */
    fun observeFontSize(): Flow<Int>

    /**
     * Sets terminal font size.
     *
     * @param size Font size in SP
     */
    suspend fun setFontSize(size: Int)

    /**
     * Observes maximum history size.
     *
     * @return Flow emitting max history size
     */
    fun observeMaxHistorySize(): Flow<Int>

    /**
     * Sets maximum history size.
     *
     * @param size Maximum number of commands to keep
     */
    suspend fun setMaxHistorySize(size: Int)
}

// ============================================================================
// TERMUX PACKAGE MANAGER CONTRACT (Future)
// ============================================================================

/**
 * Contract for Termux package management.
 *
 * Note: Out of scope for Feature 001, defined for future reference.
 */
interface TermuxPackageManagerContract {

    /**
     * Installs a package via apt.
     *
     * @param packageName Package name
     * @return Flow emitting installation progress
     */
    fun installPackage(packageName: String): Flow<InstallProgress>

    /**
     * Uninstalls a package.
     *
     * @param packageName Package name
     */
    suspend fun uninstallPackage(packageName: String): Result<Unit>

    /**
     * Updates package lists (apt update).
     *
     * @return Flow emitting update progress
     */
    fun updatePackageLists(): Flow<UpdateProgress>

    /**
     * Lists installed packages.
     *
     * @return List of installed packages
     */
    suspend fun listInstalledPackages(): List<Package>
}

data class Package(
    val name: String,
    val version: String,
    val description: String
)

sealed class InstallProgress {
    object Downloading : InstallProgress()
    data class Installing(val percent: Int) : InstallProgress()
    object Success : InstallProgress()
    data class Failure(val error: String) : InstallProgress()
}

sealed class UpdateProgress {
    object Fetching : UpdateProgress()
    data class Progress(val percent: Int) : UpdateProgress()
    object Success : UpdateProgress()
    data class Failure(val error: String) : UpdateProgress()
}

// ============================================================================
// VALIDATION & ERROR HANDLING
// ============================================================================

/**
 * Terminal-specific exceptions.
 */
sealed class TerminalException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class SessionNotInitialized : TerminalException("Terminal session not initialized")
    class SessionTerminated : TerminalException("Terminal session has been terminated")
    class CommandExecutionFailed(message: String, cause: Throwable? = null) : TerminalException(message, cause)
    class InvalidWorkingDirectory(path: String) : TerminalException("Invalid working directory: $path")
    class PermissionDenied(operation: String) : TerminalException("Permission denied: $operation")
}

/**
 * Input validation utilities.
 */
object TerminalValidator {

    /**
     * Validates command text.
     *
     * @param command Command to validate
     * @return Validation result
     */
    fun validateCommand(command: String): ValidationResult {
        return when {
            command.isBlank() -> ValidationResult.Invalid("Command cannot be blank")
            command.length > MAX_COMMAND_LENGTH -> ValidationResult.Invalid("Command too long (max $MAX_COMMAND_LENGTH chars)")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validates working directory path.
     *
     * @param path Directory path to validate
     * @return Validation result
     */
    fun validateWorkingDirectory(path: String): ValidationResult {
        return when {
            path.isBlank() -> ValidationResult.Invalid("Path cannot be blank")
            !path.startsWith("/") -> ValidationResult.Invalid("Path must be absolute")
            else -> ValidationResult.Valid
        }
    }

    private const val MAX_COMMAND_LENGTH = 4096
}

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val reason: String) : ValidationResult()
}

// ============================================================================
// CONTRACT METADATA
// ============================================================================

/**
 * Contract version for compatibility tracking.
 */
object ContractVersion {
    const val MAJOR = 1
    const val MINOR = 0
    const val PATCH = 0
    const val VERSION = "$MAJOR.$MINOR.$PATCH"
}

/**
 * Contract changelog.
 */
object ContractChangelog {
    const val V1_0_0 = """
        Version 1.0.0 (2025-10-20)
        - Initial contract definition
        - Terminal session management
        - Command history
        - Output processing
        - Settings management
    """
}
