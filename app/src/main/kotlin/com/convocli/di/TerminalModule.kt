package com.convocli.di

import android.content.Context
import com.convocli.terminal.repository.TerminalRepository
import com.convocli.terminal.repository.TermuxTerminalRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt dependency injection module for terminal-related dependencies.
 *
 * This module provides the terminal repository as a singleton, ensuring
 * that terminal sessions are shared across the application and survive
 * configuration changes.
 *
 * ## Scope
 * Installed in `SingletonComponent`, making all provided dependencies
 * application-scoped singletons.
 *
 * ## Dependencies Provided
 * - `TerminalRepository`: Interface for terminal operations
 *   - Implemented by: `TermuxTerminalRepository`
 *   - Lifecycle: Application singleton
 *   - Thread-safe: Yes
 *
 * ## Usage in ViewModels
 * ```kotlin
 * @HiltViewModel
 * class TerminalViewModel @Inject constructor(
 *     private val terminalRepository: TerminalRepository
 * ) : ViewModel() {
 *     // TerminalRepository automatically injected
 * }
 * ```
 *
 * ## Testing
 * In tests, you can provide a fake implementation:
 * ```kotlin
 * @TestInstallIn(
 *     components = [SingletonComponent::class],
 *     replaces = [TerminalModule::class]
 * )
 * @Module
 * object FakeTerminalModule {
 *     @Provides
 *     @Singleton
 *     fun provideTerminalRepository(): TerminalRepository = FakeTerminalRepository()
 * }
 * ```
 */
@Module
@InstallIn(SingletonComponent::class)
object TerminalModule {
    /**
     * Provides the singleton instance of TerminalRepository.
     *
     * The repository is implemented by `TermuxTerminalRepository`, which
     * integrates with the Termux terminal-emulator library to provide
     * native terminal functionality.
     *
     * ## Singleton Lifecycle
     * - Created once when first requested
     * - Lives for the entire application lifetime
     * - Destroyed when application process is killed
     *
     * ## Thread Safety
     * The repository implementation is thread-safe. Multiple ViewModels
     * can safely access it concurrently.
     *
     * @param context Application context for accessing app private directories
     * @return Singleton TerminalRepository instance
     */
    @Provides
    @Singleton
    fun provideTerminalRepository(
        @ApplicationContext context: Context,
    ): TerminalRepository = TermuxTerminalRepository(context)
}
