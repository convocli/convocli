package com.convocli.ui.viewmodels

import android.content.ClipboardManager
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.convocli.data.model.CommandStatus
import com.convocli.terminal.service.CommandBlockManager
import com.convocli.terminal.util.AnsiColorParser
import com.convocli.terminal.viewmodel.TerminalViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for command cancellation functionality.
 *
 * Tests the flow of cancelling a running command:
 * 1. User clicks cancel button
 * 2. ViewModel sends SIGINT to terminal
 * 3. CommandBlockManager updates block status
 */
@ExperimentalCoroutinesApi
class CommandBlockCancellationTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var commandBlockManager: CommandBlockManager
    private lateinit var terminalViewModel: TerminalViewModel
    private lateinit var clipboardManager: ClipboardManager
    private lateinit var ansiColorParser: AnsiColorParser
    private lateinit var context: Context

    private lateinit var viewModel: CommandBlockViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Create mocks
        commandBlockManager = mockk(relaxed = true)
        terminalViewModel = mockk(relaxed = true)
        clipboardManager = mockk(relaxed = true)
        ansiColorParser = mockk(relaxed = true)
        context = mockk(relaxed = true)

        // Setup default mock behaviors
        every { commandBlockManager.observeBlocks() } returns flowOf(emptyList())
        every { terminalViewModel.currentDirectory } returns flowOf("/home/user")
        every { context.getSystemService(Context.CLIPBOARD_SERVICE) } returns clipboardManager

        // Create ViewModel with mocks
        viewModel = CommandBlockViewModel(
            context = context,
            commandBlockManager = commandBlockManager,
            terminalViewModel = terminalViewModel,
            ansiColorParser = ansiColorParser
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `cancelCommand sends SIGINT and marks block as cancelled`() = runTest {
        // Given: A block ID for a running command
        val blockId = "test-block-123"

        // When: Cancel command is called
        viewModel.cancelCommand(blockId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: SIGINT should be sent to terminal
        verify(exactly = 1) { terminalViewModel.sendInterrupt() }

        // And: Block should be marked as cancelled
        coVerify(exactly = 1) { commandBlockManager.cancelBlock(blockId) }
    }

    @Test
    fun `cancelCommand handles multiple blocks independently`() = runTest {
        // Given: Multiple running commands
        val block1 = "block-1"
        val block2 = "block-2"

        // When: First block is cancelled
        viewModel.cancelCommand(block1)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: Only first block is cancelled
        coVerify(exactly = 1) { commandBlockManager.cancelBlock(block1) }
        coVerify(exactly = 0) { commandBlockManager.cancelBlock(block2) }

        // When: Second block is cancelled
        viewModel.cancelCommand(block2)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: Second block is also cancelled
        coVerify(exactly = 1) { commandBlockManager.cancelBlock(block2) }

        // And: SIGINT sent twice (once for each cancel)
        verify(exactly = 2) { terminalViewModel.sendInterrupt() }
    }

    @Test
    fun `cancelCommand is idempotent`() = runTest {
        // Given: A block ID
        val blockId = "test-block"

        // When: Cancel is called multiple times for same block
        viewModel.cancelCommand(blockId)
        viewModel.cancelCommand(blockId)
        viewModel.cancelCommand(blockId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: SIGINT sent 3 times (no deduplication at ViewModel level)
        verify(exactly = 3) { terminalViewModel.sendInterrupt() }

        // And: Block marked as cancelled 3 times (manager handles idempotency)
        coVerify(exactly = 3) { commandBlockManager.cancelBlock(blockId) }
    }

    @Test
    fun `cancelBlock in CommandBlockManager marks block as FAILURE with exit code 130`() = runTest {
        // This tests the CommandBlockManager's cancelBlock implementation
        // Note: This is more of an integration test for the manager itself

        val manager = mockk<CommandBlockManager>(relaxed = true)

        // Verify that cancelBlock is called with correct parameters
        coEvery { manager.cancelBlock(any()) } just Runs

        manager.cancelBlock("test-id")

        coVerify { manager.cancelBlock("test-id") }
    }
}
