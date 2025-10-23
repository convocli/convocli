package com.convocli.di

import com.convocli.terminal.service.CommandBlockManager
import com.convocli.terminal.service.CommandBlockManagerImpl
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
 * Hilt module for Command Blocks UI feature.
 *
 * Provides:
 * - CommandBlockManager (singleton service)
 * - PromptDetector (utility for prompt detection)
 * - AnsiColorParser (utility for ANSI code parsing)
 *
 * All implementations are singletons for consistent state and performance.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class CommandBlockModule {

    @Binds
    @Singleton
    abstract fun bindCommandBlockManager(
        impl: CommandBlockManagerImpl
    ): CommandBlockManager

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
