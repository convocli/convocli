package com.convocli.di

import com.convocli.bootstrap.BootstrapDownloader
import com.convocli.bootstrap.BootstrapExtractor
import com.convocli.bootstrap.BootstrapManager
import com.convocli.bootstrap.BootstrapValidator
import com.convocli.bootstrap.impl.BootstrapDownloaderImpl
import com.convocli.bootstrap.impl.BootstrapExtractorImpl
import com.convocli.bootstrap.impl.BootstrapManagerImpl
import com.convocli.bootstrap.impl.BootstrapValidatorImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for bootstrap installation services.
 *
 * Provides singleton instances of bootstrap manager and related services.
 *
 * **Feature**: 003 - Termux Bootstrap Installation
 * **Created**: 2025-10-22
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class BootstrapModule {

    @Binds
    @Singleton
    abstract fun bindBootstrapManager(
        impl: BootstrapManagerImpl
    ): BootstrapManager

    @Binds
    @Singleton
    abstract fun bindBootstrapDownloader(
        impl: BootstrapDownloaderImpl
    ): BootstrapDownloader

    @Binds
    @Singleton
    abstract fun bindBootstrapExtractor(
        impl: BootstrapExtractorImpl
    ): BootstrapExtractor

    @Binds
    @Singleton
    abstract fun bindBootstrapValidator(
        impl: BootstrapValidatorImpl
    ): BootstrapValidator
}
