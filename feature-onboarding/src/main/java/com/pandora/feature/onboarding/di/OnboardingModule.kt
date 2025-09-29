package com.pandora.feature.onboarding.di

import com.pandora.feature.onboarding.OnboardingAnalytics
import com.pandora.feature.onboarding.OnboardingManager
import com.pandora.feature.onboarding.OnboardingStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for Onboarding System dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object OnboardingModule {

    @Provides
    @Singleton
    fun provideOnboardingStorage(
        onboardingStorage: OnboardingStorage
    ): OnboardingStorage = onboardingStorage

    @Provides
    @Singleton
    fun provideOnboardingAnalytics(
        onboardingAnalytics: OnboardingAnalytics
    ): OnboardingAnalytics = onboardingAnalytics

    @Provides
    @Singleton
    fun provideOnboardingManager(
        onboardingManager: OnboardingManager
    ): OnboardingManager = onboardingManager
}
