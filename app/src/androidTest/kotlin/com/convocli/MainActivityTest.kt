package com.convocli

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.convocli.ui.theme.ConvoCLITheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test for MainActivity Compose UI.
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun greeting_isDisplayed() {
        // Given
        composeTestRule.setContent {
            ConvoCLITheme {
                Greeting()
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Hello ConvoCLI")
            .assertIsDisplayed()
    }
}
