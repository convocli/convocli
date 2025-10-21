package com.convocli.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module for application-level dependencies.
 *
 * Currently minimal; will expand with Termux integration, repositories, etc.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // Reserved for future application-level dependencies
    // Examples: Termux service bindings, network clients, etc.
}
