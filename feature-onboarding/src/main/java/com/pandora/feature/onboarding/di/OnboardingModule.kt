package com.pandora.feature.onboarding.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module for Onboarding System.
 * Các lớp `OnboardingStorage`, `OnboardingAnalytics`, `OnboardingManager` đã dùng @Inject constructor,
 * nên KHÔNG cần @Provides tự ràng buộc, tránh tạo dependency cycle.
 */
@Module
@InstallIn(SingletonComponent::class)
object OnboardingModule
