package com.convocli.di

import com.convocli.terminal.util.AnsiColorParser
import com.convocli.terminal.util.AnsiColorParserImpl
import com.convocli.terminal.util.PromptDetector
import com.convocli.terminal.util.PromptDetectorImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for Command Blocks UI utilities.
 *
 * Provides:
 * - PromptDetector (utility for prompt detection)
 * - AnsiColorParser (utility for ANSI code parsing)
 *
 * Note: CommandBlockManager is provided by TerminalModule
 *
 * All implementations are singletons for consistent state and performance.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class CommandBlockModule {

    @Binds
    @Singleton
    abstract fun bindPromptDetector(
        impl: PromptDetectorImpl
    ): PromptDetector

    @Binds
    @Singleton
    abstract fun bindAnsiColorParser(
        impl: AnsiColorParserImpl
    ): AnsiColorParser
}
