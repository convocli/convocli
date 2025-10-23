package com.convocli.terminal.util

/**
 * Detects shell prompts in terminal output to determine command boundaries.
 *
 * This interface provides methods for identifying when a command has completed
 * by detecting shell prompt patterns in the terminal output stream.
 *
 * Recognizes common prompt formats used by bash and other shells:
 * - Simple: `$ `, `# `, `> `
 * - User@host: `user@host:~$ `
 * - Path-included: `~/dir$ `, `/full/path$ `
 *
 * Implementation should be thread-safe as it may be called from multiple coroutines.
 */
interface PromptDetector {

    /**
     * Analyzes terminal output to detect if it ends with a shell prompt.
     *
     * This method checks if the last line of the provided output matches
     * any of the recognized shell prompt patterns.
     *
     * @param output Terminal output text to analyze (may contain multiple lines)
     * @return true if output ends with a recognized prompt pattern, false otherwise
     */
    fun detectsPrompt(output: String): Boolean

    /**
     * Returns the list of regex patterns used to match shell prompts.
     *
     * This can be used for debugging, logging, or allowing users to
     * add custom prompt patterns in the future.
     *
     * @return List of regex patterns that match shell prompts
     */
    fun getPromptPatterns(): List<Regex>

    /**
     * Strips the prompt from the end of output if present.
     *
     * This is useful when displaying command output to users, as the
     * trailing prompt should not be shown in the command block output.
     *
     * @param output Terminal output that may end with a prompt
     * @return Output with trailing prompt removed (if present), or original output if no prompt detected
     */
    fun stripPrompt(output: String): String
}
