package com.convocli.terminal.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString

/**
 * Parses ANSI escape sequences in terminal output and converts to styled text.
 *
 * Supports:
 * - Basic 16 colors (ANSI codes 30-37 and 90-97)
 * - Text formatting: bold, italic, underline
 * - Reset codes
 *
 * Maps ANSI colors to Material 3 theme colors for consistent styling.
 *
 * Implementation should be stateless and thread-safe.
 */
interface AnsiColorParser {

    /**
     * Parses ANSI-encoded text and returns styled annotation string for Compose.
     *
     * Converts ANSI escape sequences to Compose's AnnotatedString with appropriate
     * SpanStyle annotations for colors and formatting.
     *
     * Example:
     * ```
     * Input:  "\u001B[31mError\u001B[0m: file not found"
     * Output: AnnotatedString with "Error" in red, rest in default color
     * ```
     *
     * @param ansiText Raw terminal output with ANSI escape codes
     * @return AnnotatedString with appropriate styling applied
     */
    fun parse(ansiText: String): AnnotatedString

    /**
     * Strips all ANSI escape sequences from text.
     *
     * Useful for copying plain text to clipboard without formatting codes.
     *
     * Example:
     * ```
     * Input:  "\u001B[1;31mBold Red\u001B[0m"
     * Output: "Bold Red"
     * ```
     *
     * @param ansiText Text containing ANSI codes
     * @return Plain text with all escape sequences removed
     */
    fun stripAnsiCodes(ansiText: String): String

    /**
     * Maps ANSI color code to Material 3 theme color.
     *
     * Converts standard ANSI color codes (0-15) to appropriate Material 3 colors,
     * with different mappings for light and dark themes.
     *
     * @param ansiCode ANSI color code (0-7 for basic, 8-15 for bright)
     * @param isDark Whether dark theme is active
     * @return Color from Material 3 palette, or Color.Unspecified for invalid codes
     */
    fun mapColorCode(ansiCode: Int, isDark: Boolean): Color
}
