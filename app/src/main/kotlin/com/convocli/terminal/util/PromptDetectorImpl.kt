package com.convocli.terminal.util

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default implementation of PromptDetector for recognizing shell prompts.
 *
 * Supports common Termux/bash prompt formats based on research findings:
 * - Simple prompts: `$ `, `# `, `> `
 * - User@host prompts: `user@host:/path$ `
 * - Path prompts: `~/dir$ `, `/full/path$ `
 *
 * Patterns cover 95%+ of default Termux configurations.
 *
 * Thread-safe: All methods are stateless and can be called concurrently.
 */
@Singleton
class PromptDetectorImpl @Inject constructor() : PromptDetector {

    /**
     * Default prompt patterns based on research (R0.1).
     *
     * Pattern priority:
     * 1. Simple prompts (most common)
     * 2. User@host prompts (Termux default with PS1 customization)
     * 3. Path prompts (common in custom PS1)
     */
    private val defaultPatterns = listOf(
        // Pattern 1: Simple prompts - `$ `, `# `, `> `
        Regex("""^\s*[$#>]\s+$"""),

        // Pattern 2: User@host prompts - `user@host:/path$ ` or `user@host:~$ `
        Regex("""^\s*\w+@[\w-]+:[~/].*[$#]\s+$"""),

        // Pattern 3: Path prompts - `~/dir$ ` or `/full/path$ `
        Regex("""^\s*[~/].*[$#]\s+$""")
    )

    override fun detectsPrompt(output: String): Boolean {
        if (output.isBlank()) return false

        // Get the last line of output (prompts appear at the end)
        val lastLine = output.lines().lastOrNull() ?: return false

        // Check if last line matches any prompt pattern
        return defaultPatterns.any { pattern ->
            pattern.containsMatchIn(lastLine)
        }
    }

    override fun getPromptPatterns(): List<Regex> {
        return defaultPatterns
    }

    override fun stripPrompt(output: String): String {
        if (output.isBlank()) return output

        val lines = output.lines().toMutableList()

        // If last line is a prompt, remove it
        if (lines.isNotEmpty() && detectsPrompt(output)) {
            lines.removeAt(lines.lastIndex)  // Compatible with API 26+
        }

        return lines.joinToString("\n")
    }
}
