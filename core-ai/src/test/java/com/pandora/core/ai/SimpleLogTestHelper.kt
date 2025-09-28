package com.pandora.core.ai

import android.util.Log
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.ArgumentMatchers

/**
 * Simple helper for mocking Android Log in tests
 */
object SimpleLogTestHelper {
    
    fun mockAndroidLog(): MockedStatic<Log> {
        val mockedLog = Mockito.mockStatic(Log::class.java)
        
        // Mock all Log methods with simple approach
        mockedLog.`when`<Int> { Log.d(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()) }.thenReturn(0)
        mockedLog.`when`<Int> { Log.e(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()) }.thenReturn(0)
        mockedLog.`when`<Int> { Log.e(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.any()) }.thenReturn(0)
        mockedLog.`when`<Int> { Log.i(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()) }.thenReturn(0)
        mockedLog.`when`<Int> { Log.w(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()) }.thenReturn(0)
        mockedLog.`when`<Int> { Log.v(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()) }.thenReturn(0)
        
        return mockedLog
    }
}
