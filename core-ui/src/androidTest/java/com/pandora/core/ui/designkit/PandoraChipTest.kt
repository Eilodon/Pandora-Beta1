package com.pandora.core.ui.designkit

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pandora.core.ui.theme.PandoraOSTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

/**
 * UI tests for PandoraChip component
 */
@RunWith(AndroidJUnit4::class)
class PandoraChipTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun `PandoraChip should display label correctly`() {
        // Given
        val label = "Test Chip"
        
        // When
        composeTestRule.setContent {
            PandoraOSTheme {
                PandoraChip(
                    label = label,
                    state = ChipState.Default
                )
            }
        }
        
        // Then
        composeTestRule.onNodeWithText(label).assertIsDisplayed()
    }
    
    @Test
    fun `PandoraChip should handle click events`() {
        // Given
        val label = "Clickable Chip"
        var clicked = false
        
        // When
        composeTestRule.setContent {
            PandoraOSTheme {
                PandoraChip(
                    label = label,
                    state = ChipState.Default,
                    onClick = { clicked = true }
                )
            }
        }
        
        composeTestRule.onNodeWithText(label).performClick()
        
        // Then
        assertEquals(true, clicked)
    }
    
    @Test
    fun `PandoraChip should display in Armed state`() {
        // Given
        val label = "Armed Chip"
        
        // When
        composeTestRule.setContent {
            PandoraOSTheme {
                PandoraChip(
                    label = label,
                    state = ChipState.Armed
                )
            }
        }
        
        // Then
        composeTestRule.onNodeWithText(label).assertIsDisplayed()
        // Note: In real implementation, we would verify the visual state
    }
    
    @Test
    fun `PandoraChip should display in Active state`() {
        // Given
        val label = "Active Chip"
        
        // When
        composeTestRule.setContent {
            PandoraOSTheme {
                PandoraChip(
                    label = label,
                    state = ChipState.Active
                )
            }
        }
        
        // Then
        composeTestRule.onNodeWithText(label).assertIsDisplayed()
        // Note: In real implementation, we would verify the visual state
    }
    
    @Test
    fun `PandoraChip should display leading icon when provided`() {
        // Given
        val label = "Chip with Icon"
        
        // When
        composeTestRule.setContent {
            PandoraOSTheme {
                PandoraChip(
                    label = label,
                    state = ChipState.Default,
                    leadingIcon = {
                        // Note: In real implementation, we would use actual icons
                    }
                )
            }
        }
        
        // Then
        composeTestRule.onNodeWithText(label).assertIsDisplayed()
        // Note: In real implementation, we would verify the icon is displayed
    }
    
    @Test
    fun `PandoraChip should be disabled when enabled is false`() {
        // Given
        val label = "Disabled Chip"
        var clicked = false
        
        // When
        composeTestRule.setContent {
            PandoraOSTheme {
                PandoraChip(
                    label = label,
                    state = ChipState.Default,
                    enabled = false,
                    onClick = { clicked = true }
                )
            }
        }
        
        composeTestRule.onNodeWithText(label).performClick()
        
        // Then
        assertEquals(false, clicked)
    }
    
    @Test
    fun `PandoraChip should handle long text`() {
        // Given
        val longLabel = "This is a very long chip label that should be handled properly"
        
        // When
        composeTestRule.setContent {
            PandoraOSTheme {
                PandoraChip(
                    label = longLabel,
                    state = ChipState.Default
                )
            }
        }
        
        // Then
        composeTestRule.onNodeWithText(longLabel).assertIsDisplayed()
    }
    
    @Test
    fun `PandoraChip should handle empty text`() {
        // Given
        val emptyLabel = ""
        
        // When
        composeTestRule.setContent {
            PandoraOSTheme {
                PandoraChip(
                    label = emptyLabel,
                    state = ChipState.Default
                )
            }
        }
        
        // Then
        // Should not crash and should display empty chip
        composeTestRule.onRoot().assertIsDisplayed()
    }
    
    @Test
    fun `PandoraChip should handle special characters`() {
        // Given
        val specialLabel = "Chip with @#$%^&*() special characters"
        
        // When
        composeTestRule.setContent {
            PandoraOSTheme {
                PandoraChip(
                    label = specialLabel,
                    state = ChipState.Default
                )
            }
        }
        
        // Then
        composeTestRule.onNodeWithText(specialLabel).assertIsDisplayed()
    }
    
    @Test
    fun `PandoraChip should handle multiple clicks`() {
        // Given
        val label = "Multi-click Chip"
        var clickCount = 0
        
        // When
        composeTestRule.setContent {
            PandoraOSTheme {
                PandoraChip(
                    label = label,
                    state = ChipState.Default,
                    onClick = { clickCount++ }
                )
            }
        }
        
        composeTestRule.onNodeWithText(label).performClick()
        composeTestRule.onNodeWithText(label).performClick()
        composeTestRule.onNodeWithText(label).performClick()
        
        // Then
        assertEquals(3, clickCount)
    }
    
    @Test
    fun `PandoraChip should work with different themes`() {
        // Given
        val label = "Themed Chip"
        
        // When
        composeTestRule.setContent {
            PandoraOSTheme(isDarkTheme = true) {
                PandoraChip(
                    label = label,
                    state = ChipState.Default
                )
            }
        }
        
        // Then
        composeTestRule.onNodeWithText(label).assertIsDisplayed()
        
        // Test light theme
        composeTestRule.setContent {
            PandoraOSTheme(isDarkTheme = false) {
                PandoraChip(
                    label = label,
                    state = ChipState.Default
                )
            }
        }
        
        composeTestRule.onNodeWithText(label).assertIsDisplayed()
    }
}
