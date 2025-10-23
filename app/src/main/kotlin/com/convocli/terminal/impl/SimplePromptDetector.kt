package com.convocli.terminal.impl

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Simple prompt detector for basic bash and root prompts.
 *
 * This detector uses regex pattern matching to identify common shell prompts,
 * which indicates command execution has completed.
 *
 * Detected prompt patterns:
 * - `$ ` - Standard bash user prompt
 * - `# ` - Root/superuser prompt
 *
 * This is a simplified implementation for US1 (MVP). Phase 4 will add:
 * - Custom prompt detection (PS1 awareness)
 * - Multi-line prompt support
 * - Zsh/Fish/other shell prompts
 */
@Singleton
class SimplePromptDetector @Inject constructor() {

    private val bashPrompt = Regex("""^\$ """)
    private val rootPrompt = Regex("""^# """)

    /**
     * Detects if output contains a shell prompt.
     *
     * Checks the last line of output for common prompt patterns.
     * A detected prompt indicates the shell is ready for the next command.
     *
     * @param output Terminal output to analyze
     * @return True if a prompt is detected, false otherwise
     */
    fun detectPrompt(output: String): Boolean {
        if (output.isEmpty()) return false

        val lastLine = output.lines().lastOrNull() ?: return false

        return bashPrompt.containsMatchIn(lastLine) ||
               rootPrompt.containsMatchIn(lastLine)
    }

    /**
     * Gets the type of detected prompt.
     *
     * @param output Terminal output to analyze
     * @return "bash" for user prompt, "root" for superuser, null if no prompt
     */
    fun getPromptType(output: String): String? {
        if (output.isEmpty()) return null

        val lastLine = output.lines().lastOrNull() ?: return null

        return when {
            bashPrompt.containsMatchIn(lastLine) -> "bash"
            rootPrompt.containsMatchIn(lastLine) -> "root"
            else -> null
        }
    }
}
