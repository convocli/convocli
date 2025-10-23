package com.convocli.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.convocli.data.model.CommandBlock
import com.convocli.data.model.CommandStatus
import com.convocli.ui.theme.ConvoCLITheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests for CommandBlockCard composable.
 *
 * Tests action button interactions, expansion state, and visual states.
 * Uses Robolectric for Compose UI testing without instrumentation.
 */
@RunWith(RobolectricTestRunner::class)
class CommandBlockCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createTestBlock(
        id: String = "test-id",
        command: String = "ls -la",
        output: String = "total 24\ndrwxr-xr-x  3 user user 4096 Jan 1 00:00 .",
        status: CommandStatus = CommandStatus.SUCCESS,
        isExpanded: Boolean = true
    ) = CommandBlock(
        id = id,
        command = command,
        output = output,
        status = status,
        timestamp = System.currentTimeMillis(),
        executionDuration = 150,
        exitCode = 0,
        workingDirectory = "/home/user",
        isExpanded = isExpanded
    )

    @Test
    fun commandBlockCard_displaysCommand() {
        val block = createTestBlock(command = "echo 'Hello World'")

        composeTestRule.setContent {
            ConvoCLITheme {
                CommandBlockCard(
                    block = block,
                    onCopyCommand = {},
                    onCopyOutput = {},
                    onRerun = {},
                    onCancel = {},
                    onToggleExpansion = {}
                )
            }
        }

        composeTestRule.onNodeWithText("echo 'Hello World'")
            .assertIsDisplayed()
    }

    @Test
    fun commandBlockCard_displaysOutput() {
        val block = createTestBlock(output = "Hello World\nLine 2")

        composeTestRule.setContent {
            ConvoCLITheme {
                CommandBlockCard(
                    block = block,
                    onCopyCommand = {},
                    onCopyOutput = {},
                    onRerun = {},
                    onCancel = {},
                    onToggleExpansion = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Hello World\nLine 2", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun copyCommandButton_invokesCallback() {
        val block = createTestBlock()
        var copyCommandCalled = false

        composeTestRule.setContent {
            ConvoCLITheme {
                CommandBlockCard(
                    block = block,
                    onCopyCommand = { copyCommandCalled = true },
                    onCopyOutput = {},
                    onRerun = {},
                    onCancel = {},
                    onToggleExpansion = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Copy command")
            .performClick()

        assert(copyCommandCalled) { "onCopyCommand should be invoked" }
    }

    @Test
    fun copyOutputButton_invokesCallback() {
        val block = createTestBlock()
        var copyOutputCalled = false

        composeTestRule.setContent {
            ConvoCLITheme {
                CommandBlockCard(
                    block = block,
                    onCopyCommand = {},
                    onCopyOutput = { copyOutputCalled = true },
                    onRerun = {},
                    onCancel = {},
                    onToggleExpansion = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Copy output")
            .performClick()

        assert(copyOutputCalled) { "onCopyOutput should be invoked" }
    }

    @Test
    fun rerunButton_invokesCallback() {
        val block = createTestBlock()
        var rerunCalled = false

        composeTestRule.setContent {
            ConvoCLITheme {
                CommandBlockCard(
                    block = block,
                    onCopyCommand = {},
                    onCopyOutput = {},
                    onRerun = { rerunCalled = true },
                    onCancel = {},
                    onToggleExpansion = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Re-run command")
            .performClick()

        assert(rerunCalled) { "onRerun should be invoked" }
    }

    @Test
    fun cancelButton_invokesCallback_whenExecuting() {
        val block = createTestBlock(status = CommandStatus.EXECUTING)
        var cancelCalled = false

        composeTestRule.setContent {
            ConvoCLITheme {
                CommandBlockCard(
                    block = block,
                    onCopyCommand = {},
                    onCopyOutput = {},
                    onRerun = {},
                    onCancel = { cancelCalled = true },
                    onToggleExpansion = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Cancel command")
            .performClick()

        assert(cancelCalled) { "onCancel should be invoked" }
    }

    @Test
    fun cancelButton_notVisible_whenCompleted() {
        val block = createTestBlock(status = CommandStatus.SUCCESS)

        composeTestRule.setContent {
            ConvoCLITheme {
                CommandBlockCard(
                    block = block,
                    onCopyCommand = {},
                    onCopyOutput = {},
                    onRerun = {},
                    onCancel = {},
                    onToggleExpansion = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Cancel command")
            .assertDoesNotExist()
    }

    @Test
    fun expansionButton_invokesCallback_whenOutputLong() {
        // Create output with >20 lines to trigger auto-collapse
        val longOutput = (1..25).joinToString("\n") { "Line $it" }
        val block = createTestBlock(output = longOutput, isExpanded = false)
        var toggleCalled = false

        composeTestRule.setContent {
            ConvoCLITheme {
                CommandBlockCard(
                    block = block,
                    onCopyCommand = {},
                    onCopyOutput = {},
                    onRerun = {},
                    onCancel = {},
                    onToggleExpansion = { toggleCalled = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Show more (25 lines)", substring = true)
            .performClick()

        assert(toggleCalled) { "onToggleExpansion should be invoked" }
    }

    @Test
    fun collapsedOutput_showsFirstLines() {
        val longOutput = (1..25).joinToString("\n") { "Line $it" }
        val block = createTestBlock(output = longOutput, isExpanded = false)

        composeTestRule.setContent {
            ConvoCLITheme {
                CommandBlockCard(
                    block = block,
                    onCopyCommand = {},
                    onCopyOutput = {},
                    onRerun = {},
                    onCancel = {},
                    onToggleExpansion = {}
                )
            }
        }

        // Should show first lines
        composeTestRule.onNodeWithText("Line 1", substring = true)
            .assertIsDisplayed()

        // Should NOT show all lines
        composeTestRule.onNodeWithText("Line 25", substring = true)
            .assertDoesNotExist()
    }

    @Test
    fun expandedOutput_showsAllLines() {
        val longOutput = (1..25).joinToString("\n") { "Line $it" }
        val block = createTestBlock(output = longOutput, isExpanded = true)

        composeTestRule.setContent {
            ConvoCLITheme {
                CommandBlockCard(
                    block = block,
                    onCopyCommand = {},
                    onCopyOutput = {},
                    onRerun = {},
                    onCancel = {},
                    onToggleExpansion = {}
                )
            }
        }

        // Should show all lines
        composeTestRule.onNodeWithText("Line 1", substring = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Line 25", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun statusIndicator_showsExecutingState() {
        val block = createTestBlock(status = CommandStatus.EXECUTING)

        composeTestRule.setContent {
            ConvoCLITheme {
                CommandBlockCard(
                    block = block,
                    onCopyCommand = {},
                    onCopyOutput = {},
                    onRerun = {},
                    onCancel = {},
                    onToggleExpansion = {}
                )
            }
        }

        // Check for progress indicator
        composeTestRule.onNodeWithContentDescription("Executing")
            .assertExists()
    }

    @Test
    fun statusIndicator_showsSuccessState() {
        val block = createTestBlock(status = CommandStatus.SUCCESS)

        composeTestRule.setContent {
            ConvoCLITheme {
                CommandBlockCard(
                    block = block,
                    onCopyCommand = {},
                    onCopyOutput = {},
                    onRerun = {},
                    onCancel = {},
                    onToggleExpansion = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Success")
            .assertExists()
    }

    @Test
    fun statusIndicator_showsFailureState() {
        val block = createTestBlock(
            status = CommandStatus.FAILURE,
            exitCode = 1
        )

        composeTestRule.setContent {
            ConvoCLITheme {
                CommandBlockCard(
                    block = block,
                    onCopyCommand = {},
                    onCopyOutput = {},
                    onRerun = {},
                    onCancel = {},
                    onToggleExpansion = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Failed")
            .assertExists()
    }

    @Test
    fun emptyOutput_doesNotShowOutputSection() {
        val block = createTestBlock(output = "")

        composeTestRule.setContent {
            ConvoCLITheme {
                CommandBlockCard(
                    block = block,
                    onCopyCommand = {},
                    onCopyOutput = {},
                    onRerun = {},
                    onCancel = {},
                    onToggleExpansion = {}
                )
            }
        }

        // Command should be visible
        composeTestRule.onNodeWithText("ls -la")
            .assertIsDisplayed()

        // Output section should not be rendered (no output divider)
        composeTestRule.onNodeWithText("")
            .assertDoesNotExist()
    }

    @Test
    fun workingDirectory_isDisplayed() {
        val block = createTestBlock(workingDirectory = "/home/user/projects")

        composeTestRule.setContent {
            ConvoCLITheme {
                CommandBlockCard(
                    block = block,
                    onCopyCommand = {},
                    onCopyOutput = {},
                    onRerun = {},
                    onCancel = {},
                    onToggleExpansion = {}
                )
            }
        }

        composeTestRule.onNodeWithText("/home/user/projects", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun timestamp_isDisplayed() {
        val block = createTestBlock()

        composeTestRule.setContent {
            ConvoCLITheme {
                CommandBlockCard(
                    block = block,
                    onCopyCommand = {},
                    onCopyOutput = {},
                    onRerun = {},
                    onCancel = {},
                    onToggleExpansion = {}
                )
            }
        }

        // Should show relative time (e.g., "just now")
        composeTestRule.onNodeWithText("just now", substring = true, ignoreCase = true)
            .assertExists()
    }

    @Test
    fun duration_isDisplayed_whenCompleted() {
        val block = createTestBlock(
            status = CommandStatus.SUCCESS,
            executionDuration = 1500
        )

        composeTestRule.setContent {
            ConvoCLITheme {
                CommandBlockCard(
                    block = block,
                    onCopyCommand = {},
                    onCopyOutput = {},
                    onRerun = {},
                    onCancel = {},
                    onToggleExpansion = {}
                )
            }
        }

        // Should show duration
        composeTestRule.onNodeWithText("1.5s", substring = true)
            .assertExists()
    }
}
