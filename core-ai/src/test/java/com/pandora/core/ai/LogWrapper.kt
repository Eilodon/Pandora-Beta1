package com.pandora.core.ai

import android.util.Log

/**
 * Wrapper for Android Log to enable mocking in tests
 */
object LogWrapper {
    
    fun d(tag: String, msg: String): Int {
        return Log.d(tag, msg)
    }
    
    fun e(tag: String, msg: String): Int {
        return Log.e(tag, msg)
    }
    
    fun e(tag: String, msg: String, tr: Throwable): Int {
        return Log.e(tag, msg, tr)
    }
    
    fun i(tag: String, msg: String): Int {
        return Log.i(tag, msg)
    }
    
    fun w(tag: String, msg: String): Int {
        return Log.w(tag, msg)
    }
    
    fun v(tag: String, msg: String): Int {
        return Log.v(tag, msg)
    }
}
