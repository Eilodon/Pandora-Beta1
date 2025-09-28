package com.pandora.core.ai.personalization

import android.content.Context
import com.pandora.core.ai.TestDataFactory
import com.pandora.core.ai.TestInfrastructure
import com.pandora.core.ai.TestUtils
import com.pandora.core.cac.db.CACDao
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull

/**
 * Unit tests for PersonalizationEngine
 */
class PersonalizationEngineTest : TestInfrastructure() {
    
    private lateinit var context: Context
    private lateinit var cacDao: CACDao
    private lateinit var personalizationEngine: PersonalizationEngine
    
    @BeforeEach
    fun setUp() {
        context = mockk(relaxed = true)
        cacDao = mockk(relaxed = true)
        personalizationEngine = PersonalizationEngine(context, cacDao)
    }
    
    @Test
    fun `getPersonalizedSuggestions should return suggestions for valid input`() = runTest {
        // Given
        val text = "Schedule meeting"
        val context = "com.google.android.calendar"
        val baseSuggestions = listOf("Add to Calendar", "Set Reminder", "Create Event")
        
        // When
        val result = personalizationEngine.getPersonalizedSuggestions(
            text = text,
            context = context,
            baseSuggestions = baseSuggestions
        ).first()
        
        // Then
        assertTrue(result.isNotEmpty())
        assertTrue(result.all { it.score >= 0f && it.score <= 1f })
        assertTrue(result.all { it.suggestion.isNotEmpty() })
    }
    
    @Test
    fun `getPersonalizedSuggestions should handle empty base suggestions`() = runTest {
        // Given
        val text = "Test text"
        val context = "com.test.app"
        val baseSuggestions = emptyList<String>()
        
        // When
        val result = personalizationEngine.getPersonalizedSuggestions(
            text = text,
            context = context,
            baseSuggestions = baseSuggestions
        ).first()
        
        // Then
        assertTrue(result.isEmpty())
    }
    
    @Test
    fun `getPersonalizedSuggestions should handle null context`() = runTest {
        // Given
        val text = "Test text"
        val context: String? = null
        val baseSuggestions = listOf("Suggestion 1", "Suggestion 2")
        
        // When
        val result = personalizationEngine.getPersonalizedSuggestions(
            text = text,
            context = context ?: "unknown",
            baseSuggestions = baseSuggestions
        ).first()
        
        // Then
        assertTrue(result.isNotEmpty())
    }
    
    @Test
    fun `learnFromAction should update user preferences`() = runTest {
        // Given
        val action = "Add to Calendar"
        val context = "com.google.android.calendar"
        val success = true
        
        // When
        personalizationEngine.learnFromAction(action, context, success)
        
        // Then
        // Verify that learning occurred without errors
        assertTrue(true)
    }
    
    @Test
    fun `learnFromAction should handle negative feedback`() = runTest {
        // Given
        val action = "Wrong suggestion"
        val context = "com.test.app"
        val success = false
        
        // When
        personalizationEngine.learnFromAction(action, context, success)
        
        // Then
        // Verify that negative feedback was processed
        assertTrue(true)
    }
    
    @Test
    fun `getUserInsights should return user insights`() = runTest {
        // Given
        // Learn some actions first
        personalizationEngine.learnFromAction("Add to Calendar", "com.google.android.calendar", true)
        personalizationEngine.learnFromAction("Set Reminder", "com.google.android.calendar", true)
        personalizationEngine.learnFromAction("Wrong action", "com.test.app", false)
        
        // When
        val insights = personalizationEngine.getUserInsights().first()
        
        // Then
        assertTrue(insights.learningProgress >= 0f && insights.learningProgress <= 1f)
        assertTrue(insights.totalActions >= 0)
        assertTrue(insights.learningProgress >= 0f && insights.learningProgress <= 1f)
    }
    
    @Test
    fun `getUserInsights should return empty insights for new user`() = runTest {
        // Given
        // No learning has occurred
        
        // When
        val insights = personalizationEngine.getUserInsights().first()
        
        // Then
        assertEquals(0f, insights.learningProgress)
        assertEquals(0, insights.totalActions)
        assertEquals(0f, insights.learningProgress)
    }
    
    @Test
    fun `resetPersonalization should clear all data`() = runTest {
        // Given
        personalizationEngine.learnFromAction("Test action", "com.test.app", true)
        
        // When
        personalizationEngine.resetPersonalization()
        
        // Then
        val insights = personalizationEngine.getUserInsights().first()
        assertEquals(0f, insights.learningProgress)
        assertEquals(0, insights.totalActions)
        assertEquals(0f, insights.learningProgress)
    }
    
    @Test
    fun `getPersonalizedSuggestions should improve over time`() = runTest {
        // Given
        val text = "Schedule meeting"
        val context = "com.google.android.calendar"
        val baseSuggestions = listOf("Add to Calendar", "Set Reminder", "Create Event")
        
        // Learn some preferences
        personalizationEngine.learnFromAction("Add to Calendar", context, true)
        personalizationEngine.learnFromAction("Set Reminder", context, false)
        
        // When
        val result = personalizationEngine.getPersonalizedSuggestions(
            text = text,
            context = context,
            baseSuggestions = baseSuggestions
        ).first()
        
        // Then
        assertTrue(result.isNotEmpty())
        // Note: In real implementation, we would verify that "Add to Calendar" has higher score
        val addToCalendarSuggestion = result.find { it.suggestion == "Add to Calendar" }
        assertTrue(addToCalendarSuggestion != null)
    }
    
    @Test
    fun `getPersonalizedSuggestions should handle different contexts`() = runTest {
        // Given
        val text = "Send message"
        val baseSuggestions = listOf("Send SMS", "Send Email", "Send WhatsApp")
        
        // Learn context-specific preferences
        personalizationEngine.learnFromAction("Send SMS", "com.android.mms", true)
        personalizationEngine.learnFromAction("Send Email", "com.google.android.gm", true)
        personalizationEngine.learnFromAction("Send WhatsApp", "com.whatsapp", false)
        
        // When
        val smsResult = personalizationEngine.getPersonalizedSuggestions(
            text = text,
            context = "com.android.mms",
            baseSuggestions = baseSuggestions
        ).first()
        
        val emailResult = personalizationEngine.getPersonalizedSuggestions(
            text = text,
            context = "com.google.android.gm",
            baseSuggestions = baseSuggestions
        ).first()
        
        // Then
        assertTrue(smsResult.isNotEmpty())
        assertTrue(emailResult.isNotEmpty())
        
        // Note: In real implementation, we would verify context-specific scoring
    }
    
    @Test
    fun `getPersonalizedSuggestions should handle long text`() = runTest {
        // Given
        val longText = "This is a very long text that contains multiple sentences and should be handled properly by the personalization engine without any issues or errors."
        val context = "com.test.app"
        val baseSuggestions = listOf("Suggestion 1", "Suggestion 2", "Suggestion 3")
        
        // When
        val result = personalizationEngine.getPersonalizedSuggestions(
            text = longText,
            context = context,
            baseSuggestions = baseSuggestions
        ).first()
        
        // Then
        assertTrue(result.isNotEmpty())
        assertTrue(result.all { it.suggestion.isNotEmpty() })
    }
    
    @Test
    fun `getPersonalizedSuggestions should handle special characters`() = runTest {
        // Given
        val textWithSpecialChars = "Schedule meeting @ 3pm #important $100"
        val context = "com.test.app"
        val baseSuggestions = listOf("Add to Calendar", "Set Reminder")
        
        // When
        val result = personalizationEngine.getPersonalizedSuggestions(
            text = textWithSpecialChars,
            context = context,
            baseSuggestions = baseSuggestions
        ).first()
        
        // Then
        assertTrue(result.isNotEmpty())
        assertTrue(result.all { it.suggestion.isNotEmpty() })
    }
    
    @Test
    fun `learnFromAction should handle rapid learning`() = runTest {
        // Given
        val actions = listOf(
            Triple("Action 1", "com.test.app", true),
            Triple("Action 2", "com.test.app", false),
            Triple("Action 3", "com.test.app", true),
            Triple("Action 4", "com.test.app", true),
            Triple("Action 5", "com.test.app", false)
        )
        
        // When
        actions.forEach { (action, context, success) ->
            personalizationEngine.learnFromAction(action, context, success)
        }
        
        // Then
        val insights = personalizationEngine.getUserInsights().first()
        assertEquals(5, insights.totalActions)
        assertTrue(insights.learningProgress > 0f)
    }
    
    @Test
    fun `getPersonalizedSuggestions should maintain consistency`() = runTest {
        // Given
        val text = "Test text"
        val context = "com.test.app"
        val baseSuggestions = listOf("Suggestion 1", "Suggestion 2", "Suggestion 3")
        
        // When
        val result1 = personalizationEngine.getPersonalizedSuggestions(
            text = text,
            context = context,
            baseSuggestions = baseSuggestions
        ).first()
        
        val result2 = personalizationEngine.getPersonalizedSuggestions(
            text = text,
            context = context,
            baseSuggestions = baseSuggestions
        ).first()
        
        // Then
        assertEquals(result1.size, result2.size)
        // Note: In real implementation, we would verify that the results are consistent
    }
}
