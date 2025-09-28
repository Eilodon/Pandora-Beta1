package com.pandora.core.ai

import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Test utilities for common test operations
 */
@OptIn(ExperimentalCoroutinesApi::class)
object TestUtils {
    
    fun setupTestDispatcher(): TestDispatcher {
        val testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        return testDispatcher
    }
    
    fun setupStandardTestDispatcher(): TestDispatcher {
        val testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        return testDispatcher
    }
    
    fun cleanupTestDispatcher() {
        Dispatchers.resetMain()
    }
    
    fun createTestByteBuffer(size: Int = 1024): java.nio.ByteBuffer {
        val data = ByteArray(size) { it.toByte() }
        return java.nio.ByteBuffer.wrap(data)
    }
    
    fun waitForCoroutines(timeoutMs: Long = 1000L) {
        // Simple wait for coroutines to complete
        Thread.sleep(timeoutMs)
    }
}
