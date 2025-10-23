package com.convocli.terminal.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default implementation of AnsiColorParser.
 *
 * Parses ANSI escape sequences and converts to Compose AnnotatedString.
 * Supports basic 16 colors and text formatting (bold, italic, underline).
 *
 * Based on research findings (R0.2):
 * - Covers 95%+ of common terminal output
 * - Handles compound codes (bold + color)
 * - Gracefully ignores unsupported sequences
 *
 * Thread-safe: All methods are stateless.
 *
 * @param isDarkTheme Whether dark theme is currently active (for color mapping)
 */
@Singleton
class AnsiColorParserImpl @Inject constructor() : AnsiColorParser {

    // TODO: Inject actual theme state from MaterialTheme
    private val isDarkTheme: Boolean = false

    /**
     * ANSI escape sequence regex pattern.
     * Matches: ESC[{codes}m where ESC = \u001B
     * Example: \u001B[1;31m (bold red)
     */
    private val ansiPattern = Regex("""\u001B\[([0-9;]+)m""")

    override fun parse(ansiText: String): AnnotatedString {
        val builder = AnnotatedString.Builder()
        var currentIndex = 0
        var currentColor: Color? = null
        var isBold = false
        var isItalic = false
        var isUnderline = false

        // Find all ANSI escape sequences
        ansiPattern.findAll(ansiText).forEach { match ->
            // Append text before this escape code (with current styling)
            val textBefore = ansiText.substring(currentIndex, match.range.first)
            if (textBefore.isNotEmpty()) {
                val style = buildSpanStyle(currentColor, isBold, isItalic, isUnderline)
                if (style != null) {
                    builder.pushStyle(style)
                    builder.append(textBefore)
                    builder.pop()
                } else {
                    builder.append(textBefore)
                }
            }

            // Parse escape codes and update styling state
            val codes = match.groupValues[1].split(";").mapNotNull { it.toIntOrNull() }
            codes.forEach { code ->
                when (code) {
                    0 -> {
                        // Reset all formatting
                        currentColor = null
                        isBold = false
                        isItalic = false
                        isUnderline = false
                    }
                    1 -> isBold = true
                    22 -> isBold = false  // Normal intensity
                    3 -> isItalic = true
                    23 -> isItalic = false  // Not italic
                    4 -> isUnderline = true
                    24 -> isUnderline = false  // Not underlined
                    in 30..37 -> currentColor = mapColorCode(code - 30, isDarkTheme)  // Foreground
                    in 90..97 -> currentColor = mapColorCode(code - 90 + 8, isDarkTheme)  // Bright foreground
                    // Background colors (40-47, 100-107) not supported in MVP
                    else -> {
                        // Unsupported code - ignore gracefully
                    }
                }
            }

            currentIndex = match.range.last + 1
        }

        // Append remaining text after last escape code
        val remainingText = ansiText.substring(currentIndex)
        if (remainingText.isNotEmpty()) {
            val style = buildSpanStyle(currentColor, isBold, isItalic, isUnderline)
            if (style != null) {
                builder.pushStyle(style)
                builder.append(remainingText)
                builder.pop()
            } else {
                builder.append(remainingText)
            }
        }

        return builder.toAnnotatedString()
    }

    override fun stripAnsiCodes(ansiText: String): String {
        return ansiPattern.replace(ansiText, "")
    }

    override fun mapColorCode(ansiCode: Int, isDark: Boolean): Color {
        return when (ansiCode) {
            // Basic colors (0-7)
            0 -> if (isDark) Color(0xFFFFFFFF) else Color(0xFF000000)  // Black/White
            1 -> if (isDark) Color(0xFFF2B8B5) else Color(0xFFB3261E)  // Red
            2 -> if (isDark) Color(0xFF52C760) else Color(0xFF006E1C)  // Green
            3 -> if (isDark) Color(0xFFFFB95A) else Color(0xFF7D5700)  // Yellow
            4 -> if (isDark) Color(0xFF9ECAFF) else Color(0xFF0061A6)  // Blue
            5 -> if (isDark) Color(0xFFFFB1F4) else Color(0xFF8E4585)  // Magenta
            6 -> if (isDark) Color(0xFF4FD8D8) else Color(0xFF006A6A)  // Cyan
            7 -> if (isDark) Color(0xFF000000) else Color(0xFFFFFFFF)  // White/Black

            // Bright colors (8-15)
            8 -> if (isDark) Color(0xFFBBBBBB) else Color(0xFF666666)   // Bright Black (Gray)
            9 -> if (isDark) Color(0xFFFF5252) else Color(0xFFD32F2F)   // Bright Red
            10 -> if (isDark) Color(0xFF69F0AE) else Color(0xFF388E3C)  // Bright Green
            11 -> if (isDark) Color(0xFFFFEB3B) else Color(0xFFFBC02D)  // Bright Yellow
            12 -> if (isDark) Color(0xFF448AFF) else Color(0xFF1976D2)  // Bright Blue
            13 -> if (isDark) Color(0xFFEA80FC) else Color(0xFF7B1FA2)  // Bright Magenta
            14 -> if (isDark) Color(0xFF18FFFF) else Color(0xFF0097A7)  // Bright Cyan
            15 -> if (isDark) Color(0xFFFFFFFF) else Color(0xFFEEEEEE)  // Bright White

            else -> Color.Unspecified  // Invalid code
        }
    }

    /**
     * Builds a SpanStyle from current formatting state.
     * Returns null if no styling is active (to avoid unnecessary span).
     */
    private fun buildSpanStyle(
        color: Color?,
        bold: Boolean,
        italic: Boolean,
        underline: Boolean
    ): SpanStyle? {
        if (color == null && !bold && !italic && !underline) {
            return null  // No styling needed
        }

        return SpanStyle(
            color = color ?: Color.Unspecified,
            fontWeight = if (bold) FontWeight.Bold else null,
            fontStyle = if (italic) FontStyle.Italic else null,
            textDecoration = if (underline) androidx.compose.ui.text.style.TextDecoration.Underline else null
        )
    }
}
