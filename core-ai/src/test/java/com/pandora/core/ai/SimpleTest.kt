package com.pandora.core.ai

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue

/**
 * Simple test to verify test setup is working
 */
class SimpleTest {
    
    @Test
    fun `simple test should pass`() {
        // Given
        val value = 1 + 1
        
        // When
        val result = value == 2
        
        // Then
        assertTrue(result)
    }
}
