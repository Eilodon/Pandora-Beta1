package com.pandora.feature.b2b.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for B2B Features dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object B2BModule {
    // All dependencies are provided by @Inject constructors
    // No additional @Provides needed
}
