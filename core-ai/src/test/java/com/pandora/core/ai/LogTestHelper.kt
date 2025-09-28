package com.pandora.core.ai

import android.util.Log
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.kotlin.any

/**
 * Helper for mocking Android Log in tests
 */
object LogTestHelper {
    
    fun mockAndroidLog(): MockedStatic<Log> {
        val mockedLog = Mockito.mockStatic(Log::class.java)
        
        // Mock all Log methods
        mockedLog.`when`<Int> { Log.d(any<String>(), any<String>()) }.thenReturn(0)
        mockedLog.`when`<Int> { Log.e(any<String>(), any<String>()) }.thenReturn(0)
        mockedLog.`when`<Int> { Log.e(any<String>(), any<String>(), any<Throwable>()) }.thenReturn(0)
        mockedLog.`when`<Int> { Log.i(any<String>(), any<String>()) }.thenReturn(0)
        mockedLog.`when`<Int> { Log.w(any<String>(), any<String>()) }.thenReturn(0)
        mockedLog.`when`<Int> { Log.v(any<String>(), any<String>()) }.thenReturn(0)
        
        return mockedLog
    }
    
    fun mockLogWrapper(): LogWrapper {
        return Mockito.mock(LogWrapper::class.java)
    }
}
