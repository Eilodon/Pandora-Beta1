package com.pandora.core.ai

import android.util.Log
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.kotlin.any

/**
 * Test infrastructure utilities
 */
open class TestInfrastructure {
    
    fun mockAndroidLog(): MockedStatic<Log> {
        val mockedLog = Mockito.mockStatic(Log::class.java)
        mockedLog.`when`<Int> { Log.d(any(), any()) }.thenReturn(0)
        mockedLog.`when`<Int> { Log.e(any(), any()) }.thenReturn(0)
        mockedLog.`when`<Int> { Log.e(any(), any(), any()) }.thenReturn(0)
        mockedLog.`when`<Int> { Log.i(any(), any()) }.thenReturn(0)
        mockedLog.`when`<Int> { Log.w(any(), any<String>()) }.thenReturn(0)
        mockedLog.`when`<Int> { Log.v(any(), any()) }.thenReturn(0)
        return mockedLog
    }
}