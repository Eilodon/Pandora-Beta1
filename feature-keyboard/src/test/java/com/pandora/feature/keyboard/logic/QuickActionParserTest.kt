package com.pandora.feature.keyboard.logic

import android.content.Context
import com.pandora.core.cac.db.CACDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.junit.Assert.*

@RunWith(RobolectricTestRunner::class)
class QuickActionParserTest {

    @Mock
    private lateinit var mockCacDao: CACDao

    private lateinit var parser: QuickActionParser
    private lateinit var context: Context

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        context = RuntimeEnvironment.getApplication()
        parser = QuickActionParser(context, mockCacDao)
    }

    @Test
    fun `parseText should detect calendar action`() = runTest {
        // Given
        val text = "Họp team lúc 3pm"

        // When
        val suggestions = parser.parseText(text).first()

        // Then
        assertTrue(suggestions.isNotEmpty())
        val calendarSuggestion = suggestions.find { it.actionType == QuickActionType.CALENDAR }
        assertNotNull(calendarSuggestion)
        assertTrue(calendarSuggestion?.confidence ?: 0f > 0.5f)
    }

    @Test
    fun `parseText should detect remind action`() = runTest {
        // Given
        val text = "Remind me call Nam 7pm"

        // When
        val suggestions = parser.parseText(text).first()

        // Then
        assertTrue(suggestions.isNotEmpty())
        val remindSuggestion = suggestions.find { it.actionType == QuickActionType.REMIND }
        assertNotNull(remindSuggestion)
        assertTrue(remindSuggestion?.confidence ?: 0f > 0.5f)
    }

    @Test
    fun `parseText should detect send action`() = runTest {
        // Given
        val text = "Gửi tin nhắn cho Nam"

        // When
        val suggestions = parser.parseText(text).first()

        // Then
        assertTrue(suggestions.isNotEmpty())
        val sendSuggestion = suggestions.find { it.actionType == QuickActionType.SEND }
        assertNotNull(sendSuggestion)
        assertTrue(sendSuggestion?.confidence ?: 0f > 0.5f)
    }

    @Test
    fun `parseText should detect math action`() = runTest {
        // Given
        val text = "Tính 15% của 200"

        // When
        val suggestions = parser.parseText(text).first()

        // Then
        assertTrue(suggestions.isNotEmpty())
        val mathSuggestion = suggestions.find { it.actionType == QuickActionType.MATH }
        assertNotNull(mathSuggestion)
        assertTrue(mathSuggestion?.confidence ?: 0f > 0.5f)
    }

    @Test
    fun `parseText should detect search action`() = runTest {
        // Given
        val text = "Tìm kiếm nhà hàng gần đây"

        // When
        val suggestions = parser.parseText(text).first()

        // Then
        assertTrue(suggestions.isNotEmpty())
        val searchSuggestion = suggestions.find { it.actionType == QuickActionType.SEARCH }
        assertNotNull(searchSuggestion)
        assertTrue(searchSuggestion?.confidence ?: 0f > 0.5f)
    }

    @Test
    fun `createRequest should extract parameters correctly`() = runTest {
        // Given
        val text = "Họp team lúc 3pm tại văn phòng"
        val actionType = QuickActionType.CALENDAR

        // When
        val request = parser.createRequest(text, actionType)

        // Then
        assertEquals(text, request.text)
        assertEquals(actionType, request.actionType)
        assertTrue(request.parameters.containsKey("time"))
        assertTrue(request.parameters.containsKey("location"))
        assertTrue(request.parameters.containsKey("title"))
    }

    @Test
    fun `createRequest should have high confidence for clear triggers`() = runTest {
        // Given
        val text = "Họp team lúc 3pm"
        val actionType = QuickActionType.CALENDAR

        // When
        val request = parser.createRequest(text, actionType)

        // Then
        assertTrue(request.confidence > 0.5f)
    }

    @Test
    fun `parseText should return empty list for unrelated text`() = runTest {
        // Given
        val text = "Hello world"

        // When
        val suggestions = parser.parseText(text).first()

        // Then
        assertTrue(suggestions.isEmpty())
    }

    @Test
    fun `parseText should handle multiple action types`() = runTest {
        // Given
        val text = "Họp team và gửi tin nhắn cho Nam"

        // When
        val suggestions = parser.parseText(text).first()

        // Then
        assertTrue(suggestions.size >= 2)
        val calendarSuggestion = suggestions.find { it.actionType == QuickActionType.CALENDAR }
        val sendSuggestion = suggestions.find { it.actionType == QuickActionType.SEND }
        assertNotNull(calendarSuggestion)
        assertNotNull(sendSuggestion)
    }
}
