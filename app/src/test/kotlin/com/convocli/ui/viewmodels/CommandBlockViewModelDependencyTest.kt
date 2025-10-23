package com.convocli.ui.viewmodels

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.convocli.terminal.repository.TerminalRepository
import com.convocli.terminal.service.CommandBlockManager
import com.convocli.terminal.util.AnsiColorParser
import io.mockk.mockk
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertNotNull

/**
 * Regression test for Bug 005: Hilt ViewModel Injection Violation
 *
 * This test ensures that CommandBlockViewModel follows proper Hilt patterns:
 * - ViewModels should inject repositories, not other ViewModels
 * - Prevents re-introduction of ViewModel-to-ViewModel injection
 *
 * ## Background
 * Bug 005 occurred when CommandBlockViewModel injected TerminalViewModel directly.
 * This violates Hilt's ViewModel lifecycle management, as ViewModels created with
 * @HiltViewModel should only be obtained via ViewModelProvider, not injected.
 *
 * ## What This Test Validates
 * This test validates that CommandBlockViewModel can be instantiated with:
 * - Context ✓
 * - CommandBlockManager ✓
 * - TerminalRepository ✓ (CORRECT - repository injection)
 * - AnsiColorParser ✓
 *
 * And does NOT accept:
 * - TerminalViewModel ❌ (VIOLATION - ViewModel injection)
 *
 * ## How It Works
 * The test uses a compile-time check - it attempts to create a reference to
 * the CommandBlockViewModel constructor with the expected signature. If the
 * signature changes to include TerminalViewModel, this test will fail to compile.
 *
 * ## Related Tasks
 * - Bug: features/005-hilt-viewmodel-injection-bug/bugfix.md
 * - Fix: T002 (Remove ViewModel-to-ViewModel Injection)
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class CommandBlockViewModelDependencyTest {

    /**
     * Compile-time test: Verify CommandBlockViewModel constructor signature.
     *
     * This test validates the constructor dependencies at compile-time.
     * If someone tries to add TerminalViewModel back to the constructor,
     * this test will fail to compile.
     *
     * ## Expected Constructor Signature
     * ```kotlin
     * CommandBlockViewModel(
     *     context: Context,
     *     commandBlockManager: CommandBlockManager,
     *     terminalRepository: TerminalRepository,  // ✓ Repository injection (correct)
     *     ansiColorParser: AnsiColorParser
     * )
     * ```
     *
     * ## Forbidden Constructor Signature (would cause compile error)
     * ```kotlin
     * CommandBlockViewModel(
     *     ...,
     *     terminalViewModel: TerminalViewModel,  // ❌ ViewModel injection (violation)
     *     ...
     * )
     * ```
     */
    @Test
    fun `CommandBlockViewModel should have correct constructor signature without TerminalViewModel`() {
        // This is a compile-time test. The act of referencing the constructor
        // with this signature validates that it exists and matches exactly.

        // Get mock dependencies
        val context: Context = ApplicationProvider.getApplicationContext()
        val commandBlockManager: CommandBlockManager = mockk(relaxed = true)
        val terminalRepository: TerminalRepository = mockk(relaxed = true)
        val ansiColorParser: AnsiColorParser = mockk(relaxed = true)

        // Attempt to create ViewModel with correct dependencies
        // This will COMPILE if signature is correct
        // This will FAIL TO COMPILE if:
        // - TerminalViewModel is added as parameter
        // - TerminalRepository is removed
        // - Any other signature change occurs
        val viewModel = CommandBlockViewModel(
            context = context,
            commandBlockManager = commandBlockManager,
            terminalRepository = terminalRepository,
            ansiColorParser = ansiColorParser
        )

        // Verify ViewModel was created successfully
        assertNotNull(viewModel, "CommandBlockViewModel should be instantiable with correct dependencies")
    }

    /**
     * Additional documentation test.
     *
     * This test exists to document the INCORRECT pattern that caused Bug 005.
     * It will NOT compile, which is the desired behavior.
     *
     * ```kotlin
     * // THIS SHOULD NOT COMPILE (and it doesn't):
     * val viewModel = CommandBlockViewModel(
     *     context = context,
     *     commandBlockManager = commandBlockManager,
     *     terminalViewModel = terminalViewModel,  // ❌ Compile error!
     *     ansiColorParser = ansiColorParser
     * )
     * ```
     */
    @Test
    fun `documentation of incorrect pattern for reference`() {
        // This test documents what NOT to do.
        // If someone tries to add TerminalViewModel parameter, the code won't compile.
        // This is intentional and desired.
        assertNotNull(CommandBlockViewModel::class, "Test for documentation purposes")
    }
}
