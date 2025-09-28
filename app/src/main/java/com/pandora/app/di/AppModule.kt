package com.pandora.app.di

import android.content.Context
import com.pandora.app.bluetooth.BLEManager
import com.pandora.app.nfc.NFCManager
import com.pandora.app.permissions.PermissionManager
import com.pandora.app.performance.MemoryOptimizer
import com.pandora.app.performance.CPUOptimizer
import com.pandora.app.performance.PerformanceMonitor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Module for App-level dependencies
 * Provides BLE, NFC, and Permission managers
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideBLEManager(
        @ApplicationContext context: Context
    ): BLEManager {
        return BLEManager(context)
    }

    @Provides
    @Singleton
    fun provideNFCManager(
        @ApplicationContext context: Context
    ): NFCManager {
        return NFCManager(context)
    }

    @Provides
    @Singleton
    fun providePermissionManager(
        @ApplicationContext context: Context
    ): PermissionManager {
        return PermissionManager(context)
    }

    @Provides
    @Singleton
    fun provideMemoryOptimizer(
        @ApplicationContext context: Context
    ): MemoryOptimizer {
        return MemoryOptimizer(context)
    }

    @Provides
    @Singleton
    fun provideCPUOptimizer(
        @ApplicationContext context: Context
    ): CPUOptimizer {
        return CPUOptimizer(context)
    }

    @Provides
    @Singleton
    fun providePerformanceMonitor(
        @ApplicationContext context: Context
    ): PerformanceMonitor {
        return PerformanceMonitor(context)
    }
}
