package com.pandora.core.ui.designkit.icons

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Pandora Icons
 * Centralized icon definitions for the design system
 * 
 * Note: In production, these would be actual vector icons
 * For now, using placeholder ImageVector builders
 */
object PandoraIcons {
    // Search & Navigation
    val Search: ImageVector by lazy { createVectorIcon("Search") }
    val Close: ImageVector by lazy { createVectorIcon("Close") }
    val ChevronDown: ImageVector by lazy { createVectorIcon("ExpandMore") }
    
    // Actions
    val Insert: ImageVector by lazy { createVectorIcon("Add") }
    val Replace: ImageVector by lazy { createVectorIcon("Replace") }
    val Copy: ImageVector by lazy { createVectorIcon("Copy") }
    
    // Status & Feedback
    val Bulb: ImageVector by lazy { createVectorIcon("Lightbulb") }
    val Check: ImageVector by lazy { createVectorIcon("Check") }
    val Warning: ImageVector by lazy { createVectorIcon("Warning") }
    val Error: ImageVector by lazy { createVectorIcon("Error") }
    val ThumbUp: ImageVector by lazy { createVectorIcon("ThumbUp") }
    val ThumbDown: ImageVector by lazy { createVectorIcon("ThumbDown") }
    val Star: ImageVector by lazy { createVectorIcon("Star") }
    
    // Security
    val Lock: ImageVector by lazy { createVectorIcon("Lock") }
}

// Placeholder for vector icon creation
private fun createVectorIcon(name: String) = ImageVector.Builder(
    defaultWidth = 24.dp, 
    defaultHeight = 24.dp, 
    viewportWidth = 24f, 
    viewportHeight = 24f
).build()
