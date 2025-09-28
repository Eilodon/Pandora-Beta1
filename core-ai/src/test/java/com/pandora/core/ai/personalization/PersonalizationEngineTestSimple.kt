package com.pandora.core.ai.personalization

import android.content.Context
import com.pandora.core.ai.TestDataFactory
import com.pandora.core.ai.TestUtils
import com.pandora.core.cac.db.CACDao
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Simplified unit tests for PersonalizationEngine without Log dependencies
 */
class PersonalizationEngineTestSimple {
    
    private lateinit var context: Context
    private lateinit var cacDao: CACDao
    private lateinit var personalizationEngine: PersonalizationEngine
    
    @BeforeEach
    fun setUp() {
        context = mockk(relaxed = true)
        cacDao = mockk(relaxed = true)
        
        personalizationEngine = PersonalizationEngine(
            context = context,
            cacDao = cacDao
        )
    }
    
    @Test
    fun `personalizationEngine should be initialized`() {
        assertNotNull(personalizationEngine)
    }
    
    @Test
    fun `getPersonalizedSuggestions should return empty list initially`() = runTest {
        val suggestions = personalizationEngine.getPersonalizedSuggestions("test", "context", emptyList())
        val result = suggestions.first()
        assertTrue(result.isEmpty())
    }
    
    @Test
    fun `learnFromAction should complete without error`() = runTest {
        personalizationEngine.learnFromAction("test_action", "test_context", true)
        // Test passes if no exception is thrown
    }
    
    @Test
    fun `getUserInsights should return empty insights initially`() = runTest {
        val insights = personalizationEngine.getUserInsights()
        val result = insights.first()
        assertNotNull(result)
        assertEquals(0, result.totalActions)
    }
}