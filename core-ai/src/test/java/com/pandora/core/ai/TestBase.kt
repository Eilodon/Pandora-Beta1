package com.pandora.core.ai

import android.util.Log
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.jupiter.api.BeforeEach

/**
 * Base class for AI module tests with Android Log mocking
 */
abstract class TestBase {
    
    @BeforeEach
    fun setUpLogging() {
        // Mock Android Log methods with specific overloads
        mockkStatic(Log::class)
        every { Log.d(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>()) } returns 0
        every { Log.i(any<String>(), any<String>()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0
        every { Log.v(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>(), any<Throwable>()) } returns 0
        every { Log.w(any<String>(), any<Throwable>()) } returns 0
        every { Log.d(any<String>(), any<String>(), any<Throwable>()) } returns 0
        every { Log.i(any<String>(), any<String>(), any<Throwable>()) } returns 0
        every { Log.v(any<String>(), any<String>(), any<Throwable>()) } returns 0
    }
}
