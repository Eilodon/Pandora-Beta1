package com.pandora.feature.gamification

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Engine for managing achievement unlocks and progress
 */
@Singleton
class AchievementEngine @Inject constructor() {

    /**
     * Check if any achievements should be unlocked based on user activity
     */
    suspend fun checkAchievementUnlocks(
        profile: UserProfile,
        metricType: MetricType,
        value: Float
    ): List<Achievement> {
        val newAchievements = mutableListOf<Achievement>()
        
        // Get all available achievements
        val availableAchievements = getAllAvailableAchievements()
        
        for (achievement in availableAchievements) {
            // Skip if already completed
            if (profile.achievements.any { it.id == achievement.id && it.isCompleted }) {
                continue
            }
            
            // Check if achievement should be unlocked
            if (shouldUnlockAchievement(achievement, profile, metricType, value)) {
                val unlockedAchievement = achievement.copy(
                    isCompleted = true,
                    completedAt = System.currentTimeMillis(),
                    progress = 1.0f
                )
                newAchievements.add(unlockedAchievement)
                Log.d("AchievementEngine", "Unlocked achievement: ${achievement.name}")
            } else {
                // Update progress even if not completed
                val updatedProgress = calculateAchievementProgress(achievement, profile, metricType, value)
                if (updatedProgress > achievement.progress) {
                    val updatedAchievement = achievement.copy(progress = updatedProgress)
                    // Note: In a real implementation, you'd update the achievement in storage
                    Log.d("AchievementEngine", "Updated progress for achievement: ${achievement.name} = $updatedProgress")
                }
            }
        }
        
        return newAchievements
    }

    /**
     * Check if a specific achievement should be unlocked
     */
    private fun shouldUnlockAchievement(
        achievement: Achievement,
        profile: UserProfile,
        metricType: MetricType,
        value: Float
    ): Boolean {
        return achievement.requirements.all { requirement ->
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
                    checkCustomAchievementRequirement(achievement, profile, metricType, value)
                }
            }
        }
    }

    /**
     * Calculate progress for an achievement
     */
    private fun calculateAchievementProgress(
        achievement: Achievement,
        profile: UserProfile,
        metricType: MetricType,
        value: Float
    ): Float {
        var totalProgress = 0f
        var totalWeight = 0f
        
        for (requirement in achievement.requirements) {
            val weight = 1f / achievement.requirements.size
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
                    calculateCustomAchievementProgress(achievement, profile, metricType, value)
                }
            }
            
            totalProgress += progress * weight
            totalWeight += weight
        }
        
        return if (totalWeight > 0) totalProgress / totalWeight else 0f
    }

    /**
     * Check custom achievement requirements
     */
    private fun checkCustomAchievementRequirement(
        achievement: Achievement,
        profile: UserProfile,
        metricType: MetricType,
        value: Float
    ): Boolean {
        return when (achievement.id) {
            "first_day" -> {
                profile.stats.consecutiveDays >= 1
            }
            "week_warrior" -> {
                profile.stats.consecutiveDays >= 7
            }
            "month_master" -> {
                profile.stats.consecutiveDays >= 30
            }
            "speed_legend" -> {
                metricType == MetricType.TYPING_SPEED && value >= 100
            }
            "accuracy_god" -> {
                metricType == MetricType.ACCURACY && value >= 99
            }
            "quick_action_legend" -> {
                profile.stats.quickActionsUsed >= 100
            }
            "learning_guru" -> {
                profile.stats.learningSessions >= 30
            }
            "word_tycoon" -> {
                profile.stats.totalWordsTyped >= 100000
            }
            "time_titan" -> {
                profile.stats.totalTimeSpent >= 3600000 // 1 hour in milliseconds
            }
            else -> false
        }
    }

    /**
     * Calculate custom achievement progress
     */
    private fun calculateCustomAchievementProgress(
        achievement: Achievement,
        profile: UserProfile,
        metricType: MetricType,
        value: Float
    ): Float {
        return when (achievement.id) {
            "first_day" -> {
                (profile.stats.consecutiveDays.toFloat() / 1f).coerceAtMost(1f)
            }
            "week_warrior" -> {
                (profile.stats.consecutiveDays.toFloat() / 7f).coerceAtMost(1f)
            }
            "month_master" -> {
                (profile.stats.consecutiveDays.toFloat() / 30f).coerceAtMost(1f)
            }
            "speed_legend" -> {
                if (metricType == MetricType.TYPING_SPEED) {
                    (value / 100f).coerceAtMost(1f)
                } else {
                    (profile.stats.averageWpm / 100f).coerceAtMost(1f)
                }
            }
            "accuracy_god" -> {
                if (metricType == MetricType.ACCURACY) {
                    (value / 99f).coerceAtMost(1f)
                } else {
                    (profile.stats.averageAccuracy / 99f).coerceAtMost(1f)
                }
            }
            "quick_action_legend" -> {
                (profile.stats.quickActionsUsed.toFloat() / 100f).coerceAtMost(1f)
            }
            "learning_guru" -> {
                (profile.stats.learningSessions.toFloat() / 30f).coerceAtMost(1f)
            }
            "word_tycoon" -> {
                (profile.stats.totalWordsTyped.toFloat() / 100000f).coerceAtMost(1f)
            }
            "time_titan" -> {
                (profile.stats.totalTimeSpent.toFloat() / 3600000f).coerceAtMost(1f)
            }
            else -> 0f
        }
    }

    /**
     * Get all available achievements
     */
    private fun getAllAvailableAchievements(): List<Achievement> {
        return listOf(
            // Daily Achievements
            Achievement(
                id = "first_day",
                name = "First Day",
                description = "Complete your first day of typing",
                iconRes = "ic_first_day",
                category = AchievementCategory.DAILY,
                points = 25,
                requirements = listOf(
                    AchievementRequirement(
                        type = RequirementType.CONSECUTIVE_DAYS,
                        target = 1,
                        description = "Complete 1 day"
                    )
                )
            ),
            
            // Weekly Achievements
            Achievement(
                id = "week_warrior",
                name = "Week Warrior",
                description = "Maintain a 7-day streak",
                iconRes = "ic_week_warrior",
                category = AchievementCategory.WEEKLY,
                points = 100,
                requirements = listOf(
                    AchievementRequirement(
                        type = RequirementType.CONSECUTIVE_DAYS,
                        target = 7,
                        description = "Maintain a 7-day streak"
                    )
                )
            ),
            
            // Monthly Achievements
            Achievement(
                id = "month_master",
                name = "Month Master",
                description = "Maintain a 30-day streak",
                iconRes = "ic_month_master",
                category = AchievementCategory.MONTHLY,
                points = 500,
                requirements = listOf(
                    AchievementRequirement(
                        type = RequirementType.CONSECUTIVE_DAYS,
                        target = 30,
                        description = "Maintain a 30-day streak"
                    )
                )
            ),
            
            // Lifetime Achievements
            Achievement(
                id = "speed_legend",
                name = "Speed Legend",
                description = "Type at 100 WPM",
                iconRes = "ic_speed_legend",
                category = AchievementCategory.LIFETIME,
                points = 1000,
                requirements = listOf(
                    AchievementRequirement(
                        type = RequirementType.TYPING_SPEED_WPM,
                        target = 100,
                        description = "Type at 100 WPM"
                    )
                )
            ),
            Achievement(
                id = "accuracy_god",
                name = "Accuracy God",
                description = "Achieve 99% accuracy",
                iconRes = "ic_accuracy_god",
                category = AchievementCategory.LIFETIME,
                points = 1000,
                requirements = listOf(
                    AchievementRequirement(
                        type = RequirementType.ACCURACY_PERCENTAGE,
                        target = 99,
                        description = "Achieve 99% accuracy"
                    )
                )
            ),
            Achievement(
                id = "quick_action_legend",
                name = "Quick Action Legend",
                description = "Use 100 quick actions",
                iconRes = "ic_quick_action_legend",
                category = AchievementCategory.LIFETIME,
                points = 750,
                requirements = listOf(
                    AchievementRequirement(
                        type = RequirementType.QUICK_ACTIONS_USED,
                        target = 100,
                        description = "Use 100 quick actions"
                    )
                )
            ),
            Achievement(
                id = "learning_guru",
                name = "Learning Guru",
                description = "Complete 30 learning sessions",
                iconRes = "ic_learning_guru",
                category = AchievementCategory.LIFETIME,
                points = 800,
                requirements = listOf(
                    AchievementRequirement(
                        type = RequirementType.LEARNING_SESSIONS,
                        target = 30,
                        description = "Complete 30 learning sessions"
                    )
                )
            ),
            Achievement(
                id = "word_tycoon",
                name = "Word Tycoon",
                description = "Type 100,000 words",
                iconRes = "ic_word_tycoon",
                category = AchievementCategory.LIFETIME,
                points = 1500,
                requirements = listOf(
                    AchievementRequirement(
                        type = RequirementType.TOTAL_WORDS_TYPED,
                        target = 100000,
                        description = "Type 100,000 words"
                    )
                )
            ),
            Achievement(
                id = "time_titan",
                name = "Time Titan",
                description = "Spend 1 hour typing",
                iconRes = "ic_time_titan",
                category = AchievementCategory.LIFETIME,
                points = 600,
                requirements = listOf(
                    AchievementRequirement(
                        type = RequirementType.TOTAL_TIME_SPENT,
                        target = 3600000, // 1 hour in milliseconds
                        description = "Spend 1 hour typing"
                    )
                )
            )
        )
    }
}
