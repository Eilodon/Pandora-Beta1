package com.pandora.feature.gamification

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles storage for gamification data using DataStore
 */
@Singleton
class GamificationStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val Context.dataStore by preferencesDataStore(name = "gamification")
    private val gson = Gson()

    companion object {
        private val USER_PROFILE_KEY = stringPreferencesKey("user_profile")
        private val DAILY_CHALLENGES_KEY = stringPreferencesKey("daily_challenges")
        private val LEADERBOARD_KEY = stringPreferencesKey("leaderboard")
    }

    /**
     * Load user profile
     */
    suspend fun loadUserProfile(): UserProfile {
        return try {
            val profileJson = context.dataStore.data.map { preferences ->
                preferences[USER_PROFILE_KEY] ?: ""
            }.first()
            
            if (profileJson.isEmpty()) {
                UserProfile() // Return default profile
            } else {
                gson.fromJson(profileJson, UserProfile::class.java)
            }
        } catch (e: Exception) {
            UserProfile() // Return default profile on error
        }
    }

    /**
     * Save user profile
     */
    suspend fun saveUserProfile(profile: UserProfile) {
        try {
            context.dataStore.edit { preferences ->
                val profileJson = gson.toJson(profile)
                preferences[USER_PROFILE_KEY] = profileJson
            }
        } catch (e: Exception) {
            // Handle error silently or log it
        }
    }

    /**
     * Load daily challenges
     */
    suspend fun loadDailyChallenges(): List<DailyChallenge> {
        return try {
            val challengesJson = context.dataStore.data.map { preferences ->
                preferences[DAILY_CHALLENGES_KEY] ?: ""
            }.first()
            
            if (challengesJson.isEmpty()) {
                generateDefaultDailyChallenges()
            } else {
                val type = object : TypeToken<List<DailyChallenge>>() {}.type
                gson.fromJson(challengesJson, type)
            }
        } catch (e: Exception) {
            generateDefaultDailyChallenges()
        }
    }

    /**
     * Save daily challenges
     */
    suspend fun saveDailyChallenges(challenges: List<DailyChallenge>) {
        try {
            context.dataStore.edit { preferences ->
                val challengesJson = gson.toJson(challenges)
                preferences[DAILY_CHALLENGES_KEY] = challengesJson
            }
        } catch (e: Exception) {
            // Handle error silently or log it
        }
    }

    /**
     * Load leaderboard
     */
    suspend fun loadLeaderboard(category: BadgeCategory? = null): List<LeaderboardEntry> {
        return try {
            val leaderboardJson = context.dataStore.data.map { preferences ->
                preferences[LEADERBOARD_KEY] ?: ""
            }.first()
            
            if (leaderboardJson.isEmpty()) {
                generateDefaultLeaderboard()
            } else {
                val type = object : TypeToken<List<LeaderboardEntry>>() {}.type
                val allEntries = gson.fromJson<List<LeaderboardEntry>>(leaderboardJson, type)
                if (category != null) {
                    allEntries.filter { it.badge?.category == category }
                } else {
                    allEntries
                }
            }
        } catch (e: Exception) {
            generateDefaultLeaderboard()
        }
    }

    /**
     * Save leaderboard
     */
    suspend fun saveLeaderboard(entries: List<LeaderboardEntry>) {
        try {
            context.dataStore.edit { preferences ->
                val leaderboardJson = gson.toJson(entries)
                preferences[LEADERBOARD_KEY] = leaderboardJson
            }
        } catch (e: Exception) {
            // Handle error silently or log it
        }
    }

    /**
     * Save progress entry
     */
    suspend fun saveProgressEntry(entry: ProgressEntry) {
        // In a real implementation, this would save to Room database
        // For now, we'll just log it
        android.util.Log.d("GamificationStorage", "Progress entry saved: ${entry.metricType} = ${entry.value}")
    }

    /**
     * Load progress entries for analytics
     */
    suspend fun loadProgressEntries(
        userId: String,
        metricType: MetricType? = null,
        startTime: Long? = null,
        endTime: Long? = null
    ): List<ProgressEntry> {
        // In a real implementation, this would query Room database
        // For now, return empty list
        return emptyList()
    }

    /**
     * Clear all gamification data
     */
    suspend fun clearAllData() {
        try {
            context.dataStore.edit { preferences ->
                preferences.clear()
            }
        } catch (e: Exception) {
            // Handle error silently or log it
        }
    }

    // Helper functions
    private fun generateDefaultDailyChallenges(): List<DailyChallenge> {
        val now = System.currentTimeMillis()
        val tomorrow = now + (24 * 60 * 60 * 1000) // 24 hours from now
        
        return listOf(
            DailyChallenge(
                id = "daily_speed_1",
                title = "Speed Demon",
                description = "Type at 60 WPM for 5 minutes",
                type = ChallengeType.TYPING_SPEED,
                target = 60,
                reward = ChallengeReward(points = 50, experience = 25),
                expiresAt = tomorrow,
                difficulty = ChallengeDifficulty.MEDIUM
            ),
            DailyChallenge(
                id = "daily_accuracy_1",
                title = "Precision Master",
                description = "Maintain 95% accuracy for 100 words",
                type = ChallengeType.ACCURACY,
                target = 95,
                reward = ChallengeReward(points = 75, experience = 35),
                expiresAt = tomorrow,
                difficulty = ChallengeDifficulty.HARD
            ),
            DailyChallenge(
                id = "daily_quick_actions_1",
                title = "Quick Action Hero",
                description = "Use 10 quick actions today",
                type = ChallengeType.QUICK_ACTIONS,
                target = 10,
                reward = ChallengeReward(points = 100, experience = 50),
                expiresAt = tomorrow,
                difficulty = ChallengeDifficulty.EASY
            )
        )
    }

    private fun generateDefaultLeaderboard(): List<LeaderboardEntry> {
        return listOf(
            LeaderboardEntry(
                userId = "user1",
                username = "SpeedMaster",
                score = 1500,
                rank = 1,
                badge = Badge(
                    id = "speed_master",
                    name = "Speed Master",
                    description = "Fastest typer",
                    iconRes = "ic_speed",
                    category = BadgeCategory.TYPING_SPEED,
                    rarity = BadgeRarity.LEGENDARY,
                    requirements = emptyList(),
                    points = 100
                )
            ),
            LeaderboardEntry(
                userId = "user2",
                username = "AccuracyKing",
                score = 1200,
                rank = 2,
                badge = Badge(
                    id = "accuracy_king",
                    name = "Accuracy King",
                    description = "Most accurate typer",
                    iconRes = "ic_accuracy",
                    category = BadgeCategory.ACCURACY,
                    rarity = BadgeRarity.EPIC,
                    requirements = emptyList(),
                    points = 80
                )
            ),
            LeaderboardEntry(
                userId = "user3",
                username = "QuickActionPro",
                score = 900,
                rank = 3,
                badge = Badge(
                    id = "quick_action_pro",
                    name = "Quick Action Pro",
                    description = "Master of quick actions",
                    iconRes = "ic_quick_action",
                    category = BadgeCategory.QUICK_ACTIONS,
                    rarity = BadgeRarity.RARE,
                    requirements = emptyList(),
                    points = 60
                )
            )
        )
    }
}
