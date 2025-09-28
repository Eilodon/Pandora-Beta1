package com.pandora.core.ai.hybrid

/**
 * Data class for manager status
 */
data class ManagerStatus(
    val isInitialized: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
