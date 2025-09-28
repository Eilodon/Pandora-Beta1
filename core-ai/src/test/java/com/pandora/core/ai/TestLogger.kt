package com.pandora.core.ai

import android.util.Log

/**
 * Test logger wrapper to avoid Android Log mocking issues
 */
object TestLogger {
    fun d(tag: String, msg: String) {
        // No-op for tests
    }
    
    fun e(tag: String, msg: String) {
        // No-op for tests
    }
    
    fun i(tag: String, msg: String) {
        // No-op for tests
    }
    
    fun w(tag: String, msg: String) {
        // No-op for tests
    }
    
    fun v(tag: String, msg: String) {
        // No-op for tests
    }
    
    fun e(tag: String, msg: String, tr: Throwable) {
        // No-op for tests
    }
    
    fun w(tag: String, tr: Throwable) {
        // No-op for tests
    }
    
    fun d(tag: String, msg: String, tr: Throwable) {
        // No-op for tests
    }
    
    fun i(tag: String, msg: String, tr: Throwable) {
        // No-op for tests
    }
    
    fun v(tag: String, msg: String, tr: Throwable) {
        // No-op for tests
    }
}
