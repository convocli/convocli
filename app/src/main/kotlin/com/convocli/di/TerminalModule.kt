package com.convocli.di

import com.convocli.terminal.CommandBlockManager
import com.convocli.terminal.TerminalOutputProcessor
import com.convocli.terminal.TerminalRepository
import com.convocli.terminal.impl.CommandBlockManagerImpl
import com.convocli.terminal.impl.TerminalOutputProcessorImpl
import com.convocli.terminal.impl.TerminalRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module for terminal-related dependencies.
 *
 * Binds interfaces to their implementations:
 * - TerminalRepository: Session management and command execution
 * - CommandBlockManager: Block lifecycle and persistence
 * - TerminalOutputProcessor: Output processing and buffering
 *
 * All implementations are singletons for consistency across the app.
 */
@Module
@InstallIn(SingletonComponent::class)
interface TerminalModule {

    /**
     * Binds TerminalRepository implementation.
     *
     * Provides terminal session management with Termux integration.
     */
    @Binds
    fun bindTerminalRepository(
        impl: TerminalRepositoryImpl
    ): TerminalRepository

    /**
     * Binds CommandBlockManager implementation.
     *
     * Provides command block lifecycle management with Room persistence.
     */
    @Binds
    fun bindCommandBlockManager(
        impl: CommandBlockManagerImpl
    ): CommandBlockManager

    /**
     * Binds TerminalOutputProcessor implementation.
     *
     * Provides output processing with buffering and binary detection.
     */
    @Binds
    fun bindTerminalOutputProcessor(
        impl: TerminalOutputProcessorImpl
    ): TerminalOutputProcessor
}
