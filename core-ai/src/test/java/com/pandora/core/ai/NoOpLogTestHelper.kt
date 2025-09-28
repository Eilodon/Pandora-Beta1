package com.pandora.core.ai

import android.util.Log
import org.mockito.MockedStatic
import org.mockito.Mockito

/**
 * No-op helper for mocking Android Log in tests
 * This approach doesn't actually mock Log but provides a way to handle it
 */
object NoOpLogTestHelper {
    
    fun mockAndroidLog(): MockedStatic<Log> {
        val mockedLog = Mockito.mockStatic(Log::class.java)
        
        // Mock all Log methods with no-op approach
        mockedLog.`when`<Int> { Log.d(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString()) }.thenReturn(0)
        mockedLog.`when`<Int> { Log.e(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString()) }.thenReturn(0)
        mockedLog.`when`<Int> { Log.e(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any()) }.thenReturn(0)
        mockedLog.`when`<Int> { Log.i(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString()) }.thenReturn(0)
        mockedLog.`when`<Int> { Log.w(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString()) }.thenReturn(0)
        mockedLog.`when`<Int> { Log.v(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString()) }.thenReturn(0)
        
        return mockedLog
    }
}
