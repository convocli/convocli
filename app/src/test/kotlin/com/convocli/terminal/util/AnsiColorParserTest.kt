package com.convocli.terminal.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for AnsiColorParserImpl.
 */
class AnsiColorParserTest {

    private lateinit var parser: AnsiColorParser

    @Before
    fun setup() {
        parser = AnsiColorParserImpl(isDarkTheme = false)
    }

    @Test
    fun `stripAnsiCodes removes all escape sequences`() {
        val input = "\u001B[1;31mBold Red\u001B[0m Normal"
        val result = parser.stripAnsiCodes(input)
        assertEquals("Bold Red Normal", result)
    }

    @Test
    fun `stripAnsiCodes handles text without codes`() {
        val input = "Plain text"
        val result = parser.stripAnsiCodes(input)
        assertEquals("Plain text", result)
    }

    @Test
    fun `parse handles plain text without codes`() {
        val input = "Plain text"
        val result = parser.parse(input)
        assertEquals("Plain text", result.text)
        assertTrue(result.spanStyles.isEmpty())
    }

    @Test
    fun `parse handles red color code`() {
        val input = "\u001B[31mRed text\u001B[0m"
        val result = parser.parse(input)
        assertEquals("Red text", result.text)
    }

    @Test
    fun `parse handles bold formatting`() {
        val input = "\u001B[1mBold text\u001B[0m"
        val result = parser.parse(input)
        assertEquals("Bold text", result.text)
       val hasBold = result.spanStyles.any { it.item.fontWeight == FontWeight.Bold }
        assertTrue(hasBold)
    }

    @Test
    fun `parse handles reset code`() {
        val input = "\u001B[31mRed\u001B[0mNormal"
        val result = parser.parse(input)
        assertEquals("RedNormal", result.text)
    }

    @Test
    fun `mapColorCode returns correct light theme colors`() {
        val parser = AnsiColorParserImpl(isDarkTheme = false)

        // Basic colors
        assertEquals(Color(0xFF000000), parser.mapColorCode(0, false))  // Black
        assertEquals(Color(0xFFB3261E), parser.mapColorCode(1, false))  // Red
        assertEquals(Color(0xFF006E1C), parser.mapColorCode(2, false))  // Green

        // Bright colors
        assertEquals(Color(0xFFD32F2F), parser.mapColorCode(9, false))  // Bright Red
    }

    @Test
    fun `mapColorCode returns Color Unspecified for invalid codes`() {
        assertEquals(Color.Unspecified, parser.mapColorCode(99, false))
        assertEquals(Color.Unspecified, parser.mapColorCode(-1, false))
    }
}
