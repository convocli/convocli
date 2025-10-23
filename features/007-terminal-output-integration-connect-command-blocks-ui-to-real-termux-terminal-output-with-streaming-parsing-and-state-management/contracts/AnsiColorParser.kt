package com.convocli.terminal.contracts

import androidx.compose.ui.text.AnnotatedString

/**
 * Contract for parsing ANSI escape sequences and converting to Compose-renderable text.
 *
 * Supports standard ANSI color codes (16-color palette) and basic text styles.
 * Extended 256-color and true-color sequences are out of scope for Sprint 02.
 *
 * Implementation must handle:
 * - Foreground colors (30-37, 90-97)
 * - Background colors (40-47, 100-107)
 * - Text styles (bold, underline)
 * - Invalid/unsupported sequences (stripped gracefully)
 * - Nested and overlapping styles
 */
interface AnsiColorParser {

    /**
     * Parses raw terminal output with ANSI codes into styled text for Compose UI.
     *
     * ANSI escape sequences are parsed and converted to Compose SpanStyle annotations.
     * Unsupported or invalid codes are stripped cleanly.
     *
     * Supported codes:
     * - 0: Reset all styles
     * - 1: Bold
     * - 4: Underline
     * - 30-37: Foreground colors (black, red, green, yellow, blue, magenta, cyan, white)
     * - 40-47: Background colors
     * - 90-97: Bright foreground colors
     * - 100-107: Bright background colors
     *
     * Example:
     * ```
     * Input:  "\033[31mRed Text\033[0m Normal"
     * Output: AnnotatedString with red color span for "Red Text"
     * ```
     *
     * @param rawText Raw terminal output with ANSI escape sequences
     * @return AnnotatedString with styling applied, ANSI codes removed
     */
    fun parseAnsiString(rawText: String): AnnotatedString

    /**
     * Strips all ANSI escape sequences from text, leaving plain text.
     *
     * Used for:
     * - Clipboard copy operations (plain text)
     * - Text search operations
     * - Length calculations
     *
     * @param rawText Raw terminal output with ANSI escape sequences
     * @return Plain text with all ANSI codes removed
     */
    fun stripAnsiCodes(rawText: String): String

    /**
     * Checks if a string contains ANSI escape sequences.
     *
     * Useful for optimization: skip parsing if no ANSI codes present.
     *
     * @param text Text to check
     * @return True if ANSI codes detected, false otherwise
     */
    fun containsAnsiCodes(text: String): Boolean
}
