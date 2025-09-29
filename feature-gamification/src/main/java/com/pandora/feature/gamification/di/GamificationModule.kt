package com.pandora.feature.gamification

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for Gamification System dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object GamificationModule {
    // All dependencies are provided by @Inject constructors
    // No additional @Provides needed
}
