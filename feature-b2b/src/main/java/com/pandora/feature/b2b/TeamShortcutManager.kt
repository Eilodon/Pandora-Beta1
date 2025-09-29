package com.pandora.feature.b2b

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages team shortcuts and shortcut-related functionality
 */
@Singleton
class TeamShortcutManager @Inject constructor(
    private val storage: B2BStorage,
    private val analyticsManager: B2BAnalyticsManager
) {
    private val shortcutScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Create a new team shortcut
     */
    suspend fun createShortcut(
        organizationId: String,
        teamId: String,
        createdBy: String,
        name: String,
        trigger: String,
        action: ShortcutAction,
        category: ShortcutCategory,
        description: String? = null
    ): Result<TeamShortcut> {
        return try {
            val shortcut = TeamShortcut(
                id = java.util.UUID.randomUUID().toString(),
                organizationId = organizationId,
                teamId = teamId,
                createdBy = createdBy,
                name = name,
                description = description,
                trigger = trigger,
                action = action,
                category = category
            )
            
            storage.saveTeamShortcut(shortcut)
            
            analyticsManager.trackEvent(
                organizationId = organizationId,
                eventType = ActivityType.SHORTCUT_CREATED,
                description = "Shortcut created: $name"
            )
            
            Log.d("TeamShortcutManager", "Shortcut created: $name")
            Result.success(shortcut)
        } catch (e: Exception) {
            Log.e("TeamShortcutManager", "Failed to create shortcut", e)
            Result.failure(e)
        }
    }

    /**
     * Update team shortcut
     */
    suspend fun updateShortcut(
        shortcutId: String,
        name: String? = null,
        description: String? = null,
        trigger: String? = null,
        action: ShortcutAction? = null,
        category: ShortcutCategory? = null
    ): Result<TeamShortcut> {
        return try {
            val shortcut = storage.getTeamShortcut(shortcutId)
            
            if (shortcut != null) {
                val updatedShortcut = shortcut.copy(
                    name = name ?: shortcut.name,
                    description = description ?: shortcut.description,
                    trigger = trigger ?: shortcut.trigger,
                    action = action ?: shortcut.action,
                    category = category ?: shortcut.category,
                    updatedAt = System.currentTimeMillis()
                )
                
                storage.saveTeamShortcut(updatedShortcut)
                
                analyticsManager.trackEvent(
                    organizationId = shortcut.organizationId,
                    eventType = ActivityType.SETTINGS_CHANGED,
                    description = "Shortcut updated: ${updatedShortcut.name}"
                )
                
                Log.d("TeamShortcutManager", "Shortcut updated: ${updatedShortcut.name}")
                Result.success(updatedShortcut)
            } else {
                Result.failure(Exception("Shortcut not found"))
            }
        } catch (e: Exception) {
            Log.e("TeamShortcutManager", "Failed to update shortcut", e)
            Result.failure(e)
        }
    }

    /**
     * Use team shortcut
     */
    suspend fun useShortcut(shortcutId: String): Result<Unit> {
        return try {
            val shortcut = storage.getTeamShortcut(shortcutId)
            
            if (shortcut != null) {
                val updatedShortcut = shortcut.copy(
                    usageCount = shortcut.usageCount + 1,
                    updatedAt = System.currentTimeMillis()
                )
                
                storage.saveTeamShortcut(updatedShortcut)
                
                analyticsManager.trackEvent(
                    organizationId = shortcut.organizationId,
                    eventType = ActivityType.SHORTCUT_USED,
                    description = "Shortcut used: ${shortcut.name}"
                )
                
                Log.d("TeamShortcutManager", "Shortcut used: ${shortcut.name}")
                Result.success(Unit)
            } else {
                Result.failure(Exception("Shortcut not found"))
            }
        } catch (e: Exception) {
            Log.e("TeamShortcutManager", "Failed to use shortcut", e)
            Result.failure(e)
        }
    }

    /**
     * Rate team shortcut
     */
    suspend fun rateShortcut(
        shortcutId: String,
        rating: Float
    ): Result<TeamShortcut> {
        return try {
            val shortcut = storage.getTeamShortcut(shortcutId)
            
            if (shortcut != null) {
                val updatedShortcut = shortcut.copy(
                    rating = rating.coerceIn(0f, 5f),
                    updatedAt = System.currentTimeMillis()
                )
                
                storage.saveTeamShortcut(updatedShortcut)
                
                analyticsManager.trackEvent(
                    organizationId = shortcut.organizationId,
                    eventType = ActivityType.SETTINGS_CHANGED,
                    description = "Shortcut rated: ${shortcut.name} ($rating stars)"
                )
                
                Log.d("TeamShortcutManager", "Shortcut rated: ${shortcut.name} ($rating stars)")
                Result.success(updatedShortcut)
            } else {
                Result.failure(Exception("Shortcut not found"))
            }
        } catch (e: Exception) {
            Log.e("TeamShortcutManager", "Failed to rate shortcut", e)
            Result.failure(e)
        }
    }

    /**
     * Approve team shortcut
     */
    suspend fun approveShortcut(
        shortcutId: String,
        approvedBy: String
    ): Result<TeamShortcut> {
        return try {
            val shortcut = storage.getTeamShortcut(shortcutId)
            
            if (shortcut != null) {
                val updatedShortcut = shortcut.copy(
                    isApproved = true,
                    approvedBy = approvedBy,
                    approvedAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                
                storage.saveTeamShortcut(updatedShortcut)
                
                analyticsManager.trackEvent(
                    organizationId = shortcut.organizationId,
                    eventType = ActivityType.SETTINGS_CHANGED,
                    description = "Shortcut approved: ${shortcut.name}"
                )
                
                Log.d("TeamShortcutManager", "Shortcut approved: ${shortcut.name}")
                Result.success(updatedShortcut)
            } else {
                Result.failure(Exception("Shortcut not found"))
            }
        } catch (e: Exception) {
            Log.e("TeamShortcutManager", "Failed to approve shortcut", e)
            Result.failure(e)
        }
    }

    /**
     * Get team shortcuts
     */
    suspend fun getTeamShortcuts(teamId: String): List<TeamShortcut> {
        return try {
            storage.loadTeamShortcuts(teamId)
        } catch (e: Exception) {
            Log.e("TeamShortcutManager", "Failed to get team shortcuts", e)
            emptyList()
        }
    }

    /**
     * Get shortcut by ID
     */
    suspend fun getShortcut(shortcutId: String): TeamShortcut? {
        return try {
            storage.getTeamShortcut(shortcutId)
        } catch (e: Exception) {
            Log.e("TeamShortcutManager", "Failed to get shortcut", e)
            null
        }
    }

    /**
     * Search shortcuts
     */
    suspend fun searchShortcuts(
        teamId: String,
        query: String,
        category: ShortcutCategory? = null
    ): List<TeamShortcut> {
        return try {
            val shortcuts = storage.loadTeamShortcuts(teamId)
            
            shortcuts.filter { shortcut ->
                val matchesQuery = shortcut.name.contains(query, ignoreCase = true) ||
                        shortcut.description?.contains(query, ignoreCase = true) == true ||
                        shortcut.trigger.contains(query, ignoreCase = true)
                
                val matchesCategory = category == null || shortcut.category == category
                
                matchesQuery && matchesCategory
            }
        } catch (e: Exception) {
            Log.e("TeamShortcutManager", "Failed to search shortcuts", e)
            emptyList()
        }
    }

    /**
     * Get popular shortcuts
     */
    suspend fun getPopularShortcuts(teamId: String, limit: Int = 10): List<TeamShortcut> {
        return try {
            val shortcuts = storage.loadTeamShortcuts(teamId)
            shortcuts
                .sortedByDescending { it.usageCount }
                .take(limit)
        } catch (e: Exception) {
            Log.e("TeamShortcutManager", "Failed to get popular shortcuts", e)
            emptyList()
        }
    }

    /**
     * Get shortcuts by category
     */
    suspend fun getShortcutsByCategory(
        teamId: String,
        category: ShortcutCategory
    ): List<TeamShortcut> {
        return try {
            val shortcuts = storage.loadTeamShortcuts(teamId)
            shortcuts.filter { it.category == category }
        } catch (e: Exception) {
            Log.e("TeamShortcutManager", "Failed to get shortcuts by category", e)
            emptyList()
        }
    }

    /**
     * Delete shortcut
     */
    suspend fun deleteShortcut(shortcutId: String): Result<Unit> {
        return try {
            val shortcut = storage.getTeamShortcut(shortcutId)
            
            if (shortcut != null) {
                val shortcuts = storage.loadTeamShortcuts(shortcut.teamId)
                val updatedShortcuts = shortcuts.filter { it.id != shortcutId }
                storage.saveTeamShortcuts(updatedShortcuts)
                
                analyticsManager.trackEvent(
                    organizationId = shortcut.organizationId,
                    eventType = ActivityType.SETTINGS_CHANGED,
                    description = "Shortcut deleted: ${shortcut.name}"
                )
                
                Log.d("TeamShortcutManager", "Shortcut deleted: ${shortcut.name}")
                Result.success(Unit)
            } else {
                Result.failure(Exception("Shortcut not found"))
            }
        } catch (e: Exception) {
            Log.e("TeamShortcutManager", "Failed to delete shortcut", e)
            Result.failure(e)
        }
    }
}
