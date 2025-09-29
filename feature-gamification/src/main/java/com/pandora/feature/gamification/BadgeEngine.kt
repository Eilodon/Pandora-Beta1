package com.pandora.feature.gamification

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Engine for managing badge unlocks and progress
 */
@Singleton
class BadgeEngine @Inject constructor() {

    /**
     * Check if any badges should be unlocked based on user activity
     */
    suspend fun checkBadgeUnlocks(
        profile: UserProfile,
        metricType: MetricType,
        value: Float
    ): List<Badge> {
        val newBadges = mutableListOf<Badge>()
        
        // Get all available badges
        val availableBadges = getAllAvailableBadges()
        
        for (badge in availableBadges) {
            // Skip if already unlocked
            if (profile.badges.any { it.id == badge.id && it.isUnlocked }) {
                continue
            }
            
            // Check if badge should be unlocked
            if (shouldUnlockBadge(badge, profile, metricType, value)) {
                val unlockedBadge = badge.copy(
                    isUnlocked = true,
                    unlockedAt = System.currentTimeMillis(),
                    progress = 1.0f
                )
                newBadges.add(unlockedBadge)
                Log.d("BadgeEngine", "Unlocked badge: ${badge.name}")
            } else {
                // Update progress even if not unlocked
                val updatedProgress = calculateBadgeProgress(badge, profile, metricType, value)
                if (updatedProgress > badge.progress) {
                    val updatedBadge = badge.copy(progress = updatedProgress)
                    // Note: In a real implementation, you'd update the badge in storage
                    Log.d("BadgeEngine", "Updated progress for badge: ${badge.name} = $updatedProgress")
                }
            }
        }
        
        return newBadges
    }

    /**
     * Check if a specific badge should be unlocked
     */
    private fun shouldUnlockBadge(
        badge: Badge,
        profile: UserProfile,
        metricType: MetricType,
        value: Float
    ): Boolean {
        return badge.requirements.all { requirement ->
            when (requirement.type) {
                RequirementType.TYPING_SPEED_WPM -> {
                    metricType == MetricType.TYPING_SPEED && value >= requirement.target
                }
                RequirementType.ACCURACY_PERCENTAGE -> {
                    metricType == MetricType.ACCURACY && value >= requirement.target
                }
                RequirementType.QUICK_ACTIONS_USED -> {
                    metricType == MetricType.QUICK_ACTIONS && profile.stats.quickActionsUsed >= requirement.target
                }
                RequirementType.LEARNING_SESSIONS -> {
                    profile.stats.learningSessions >= requirement.target
                }
                RequirementType.CONSECUTIVE_DAYS -> {
                    profile.stats.consecutiveDays >= requirement.target
                }
                RequirementType.TOTAL_WORDS_TYPED -> {
                    profile.stats.totalWordsTyped >= requirement.target
                }
                RequirementType.TOTAL_TIME_SPENT -> {
                    profile.stats.totalTimeSpent >= requirement.target
                }
                RequirementType.CUSTOM_METRIC -> {
                    // Custom logic for specific badges
                    checkCustomBadgeRequirement(badge, profile, metricType, value)
                }
            }
        }
    }

    /**
     * Calculate progress for a badge
     */
    private fun calculateBadgeProgress(
        badge: Badge,
        profile: UserProfile,
        metricType: MetricType,
        value: Float
    ): Float {
        var totalProgress = 0f
        var totalWeight = 0f
        
        for (requirement in badge.requirements) {
            val weight = 1f / badge.requirements.size
            val progress = when (requirement.type) {
                RequirementType.TYPING_SPEED_WPM -> {
                    if (metricType == MetricType.TYPING_SPEED) {
                        (value / requirement.target).coerceAtMost(1f)
                    } else {
                        (profile.stats.averageWpm / requirement.target).coerceAtMost(1f)
                    }
                }
                RequirementType.ACCURACY_PERCENTAGE -> {
                    if (metricType == MetricType.ACCURACY) {
                        (value / requirement.target).coerceAtMost(1f)
                    } else {
                        (profile.stats.averageAccuracy / requirement.target).coerceAtMost(1f)
                    }
                }
                RequirementType.QUICK_ACTIONS_USED -> {
                    (profile.stats.quickActionsUsed.toFloat() / requirement.target).coerceAtMost(1f)
                }
                RequirementType.LEARNING_SESSIONS -> {
                    (profile.stats.learningSessions.toFloat() / requirement.target).coerceAtMost(1f)
                }
                RequirementType.CONSECUTIVE_DAYS -> {
                    (profile.stats.consecutiveDays.toFloat() / requirement.target).coerceAtMost(1f)
                }
                RequirementType.TOTAL_WORDS_TYPED -> {
                    (profile.stats.totalWordsTyped.toFloat() / requirement.target).coerceAtMost(1f)
                }
                RequirementType.TOTAL_TIME_SPENT -> {
                    (profile.stats.totalTimeSpent.toFloat() / requirement.target).coerceAtMost(1f)
                }
                RequirementType.CUSTOM_METRIC -> {
                    calculateCustomBadgeProgress(badge, profile, metricType, value)
                }
            }
            
            totalProgress += progress * weight
            totalWeight += weight
        }
        
        return if (totalWeight > 0) totalProgress / totalWeight else 0f
    }

    /**
     * Check custom badge requirements
     */
    private fun checkCustomBadgeRequirement(
        badge: Badge,
        profile: UserProfile,
        metricType: MetricType,
        value: Float
    ): Boolean {
        return when (badge.id) {
            "first_quick_action" -> {
                metricType == MetricType.QUICK_ACTIONS && value > 0
            }
            "speed_demon" -> {
                metricType == MetricType.TYPING_SPEED && value >= 80
            }
            "accuracy_master" -> {
                metricType == MetricType.ACCURACY && value >= 98
            }
            "learning_champion" -> {
                profile.stats.learningSessions >= 7
            }
            "streak_master" -> {
                profile.stats.consecutiveDays >= 30
            }
            else -> false
        }
    }

    /**
     * Calculate custom badge progress
     */
    private fun calculateCustomBadgeProgress(
        badge: Badge,
        profile: UserProfile,
        metricType: MetricType,
        value: Float
    ): Float {
        return when (badge.id) {
            "first_quick_action" -> {
                if (metricType == MetricType.QUICK_ACTIONS && value > 0) 1f else 0f
            }
            "speed_demon" -> {
                if (metricType == MetricType.TYPING_SPEED) {
                    (value / 80f).coerceAtMost(1f)
                } else {
                    (profile.stats.averageWpm / 80f).coerceAtMost(1f)
                }
            }
            "accuracy_master" -> {
                if (metricType == MetricType.ACCURACY) {
                    (value / 98f).coerceAtMost(1f)
                } else {
                    (profile.stats.averageAccuracy / 98f).coerceAtMost(1f)
                }
            }
            "learning_champion" -> {
                (profile.stats.learningSessions.toFloat() / 7f).coerceAtMost(1f)
            }
            "streak_master" -> {
                (profile.stats.consecutiveDays.toFloat() / 30f).coerceAtMost(1f)
            }
            else -> 0f
        }
    }

    /**
     * Get all available badges
     */
    private fun getAllAvailableBadges(): List<Badge> {
        return listOf(
            // Typing Speed Badges
            Badge(
                id = "first_quick_action",
                name = "Quick Start",
                description = "Use your first quick action",
                iconRes = "ic_quick_action",
                category = BadgeCategory.QUICK_ACTIONS,
                rarity = BadgeRarity.COMMON,
                requirements = listOf(
                    BadgeRequirement(
                        type = RequirementType.QUICK_ACTIONS_USED,
                        target = 1,
                        description = "Use 1 quick action"
                    )
                ),
                points = 10
            ),
            Badge(
                id = "speed_demon",
                name = "Speed Demon",
                description = "Type at 80 WPM",
                iconRes = "ic_speed",
                category = BadgeCategory.TYPING_SPEED,
                rarity = BadgeRarity.RARE,
                requirements = listOf(
                    BadgeRequirement(
                        type = RequirementType.TYPING_SPEED_WPM,
                        target = 80,
                        description = "Type at 80 WPM"
                    )
                ),
                points = 50
            ),
            Badge(
                id = "accuracy_master",
                name = "Accuracy Master",
                description = "Achieve 98% accuracy",
                iconRes = "ic_accuracy",
                category = BadgeCategory.ACCURACY,
                rarity = BadgeRarity.EPIC,
                requirements = listOf(
                    BadgeRequirement(
                        type = RequirementType.ACCURACY_PERCENTAGE,
                        target = 98,
                        description = "Achieve 98% accuracy"
                    )
                ),
                points = 100
            ),
            Badge(
                id = "learning_champion",
                name = "Learning Champion",
                description = "Complete 7 learning sessions",
                iconRes = "ic_learning",
                category = BadgeCategory.LEARNING,
                rarity = BadgeRarity.UNCOMMON,
                requirements = listOf(
                    BadgeRequirement(
                        type = RequirementType.LEARNING_SESSIONS,
                        target = 7,
                        description = "Complete 7 learning sessions"
                    )
                ),
                points = 75
            ),
            Badge(
                id = "streak_master",
                name = "Streak Master",
                description = "Maintain a 30-day streak",
                iconRes = "ic_streak",
                category = BadgeCategory.MILESTONE,
                rarity = BadgeRarity.LEGENDARY,
                requirements = listOf(
                    BadgeRequirement(
                        type = RequirementType.CONSECUTIVE_DAYS,
                        target = 30,
                        description = "Maintain a 30-day streak"
                    )
                ),
                points = 200
            ),
            Badge(
                id = "word_master",
                name = "Word Master",
                description = "Type 10,000 words",
                iconRes = "ic_words",
                category = BadgeCategory.MILESTONE,
                rarity = BadgeRarity.RARE,
                requirements = listOf(
                    BadgeRequirement(
                        type = RequirementType.TOTAL_WORDS_TYPED,
                        target = 10000,
                        description = "Type 10,000 words"
                    )
                ),
                points = 150
            )
        )
    }
}
