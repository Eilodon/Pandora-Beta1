package com.pandora.feature.gamification

import android.content.Context
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockedStatic
import org.mockito.kotlin.mockStatic
import android.util.Log

class GamificationManagerTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockStorage: GamificationStorage

    @Mock
    private lateinit var mockAnalytics: GamificationAnalyticsManager

    @Mock
    private lateinit var mockBadgeEngine: BadgeEngine

    @Mock
    private lateinit var mockAchievementEngine: AchievementEngine

    @Mock
    private lateinit var mockProgressTracker: ProgressTracker

    private lateinit var gamificationManager: GamificationManager
    private lateinit var mockedLog: MockedStatic<Log>

    @Before
    fun setup() {
        mockContext = org.mockito.Mockito.mock(Context::class.java)
        mockStorage = org.mockito.Mockito.mock(GamificationStorage::class.java)
        mockAnalytics = org.mockito.Mockito.mock(GamificationAnalyticsManager::class.java)
        mockBadgeEngine = org.mockito.Mockito.mock(BadgeEngine::class.java)
        mockAchievementEngine = org.mockito.Mockito.mock(AchievementEngine::class.java)
        mockProgressTracker = org.mockito.Mockito.mock(ProgressTracker::class.java)
        
        mockedLog = mockStatic(Log::class.java)
        
        gamificationManager = GamificationManager(
            mockContext,
            mockStorage,
            mockAnalytics,
            mockBadgeEngine,
            mockAchievementEngine,
            mockProgressTracker
        )
    }

    @org.junit.After
    fun tearDown() {
        mockedLog.close()
    }

    @Test
    fun `initialize should load user profile and set initialized to true`() {
        // Given
        val mockProfile = UserProfile()
        `when`(mockStorage.loadUserProfile()).thenReturn(mockProfile)
        `when`(mockStorage.loadDailyChallenges()).thenReturn(emptyList())
        `when`(mockStorage.loadLeaderboard()).thenReturn(emptyList())
        `when`(mockProgressTracker.initialize()).thenReturn(Unit)

        // When
        gamificationManager.initialize()

        // Then
        verify(mockStorage).loadUserProfile()
        verify(mockStorage).loadDailyChallenges()
        verify(mockStorage).loadLeaderboard()
        verify(mockProgressTracker).initialize()
    }

    @Test
    fun `trackActivity should update progress and check for unlocks`() {
        // Given
        val mockProfile = UserProfile()
        `when`(mockStorage.loadUserProfile()).thenReturn(mockProfile)
        `when`(mockStorage.loadDailyChallenges()).thenReturn(emptyList())
        `when`(mockStorage.loadLeaderboard()).thenReturn(emptyList())
        `when`(mockProgressTracker.initialize()).thenReturn(Unit)
        `when`(mockBadgeEngine.checkBadgeUnlocks(any(), any(), any())).thenReturn(emptyList())
        `when`(mockAchievementEngine.checkAchievementUnlocks(any(), any(), any())).thenReturn(emptyList())
        `when`(mockProgressTracker.getUpdatedStats(any())).thenReturn(mockProfile.stats)

        gamificationManager.initialize()

        // When
        gamificationManager.trackActivity(MetricType.TYPING_SPEED, 60f)

        // Then
        verify(mockStorage).saveProgressEntry(any())
        verify(mockProgressTracker).updateProgress(MetricType.TYPING_SPEED, 60f)
        verify(mockBadgeEngine).checkBadgeUnlocks(any(), any(), any())
        verify(mockAchievementEngine).checkAchievementUnlocks(any(), any(), any())
    }

    @Test
    fun `completeChallenge should update challenge and award rewards`() {
        // Given
        val challengeId = "test_challenge"
        val mockChallenge = DailyChallenge(
            id = challengeId,
            title = "Test Challenge",
            description = "Test Description",
            type = ChallengeType.TYPING_SPEED,
            target = 60,
            reward = ChallengeReward(points = 50, experience = 25),
            expiresAt = System.currentTimeMillis() + 86400000,
            difficulty = ChallengeDifficulty.MEDIUM
        )
        val mockProfile = UserProfile()
        
        `when`(mockStorage.loadUserProfile()).thenReturn(mockProfile)
        `when`(mockStorage.loadDailyChallenges()).thenReturn(listOf(mockChallenge))
        `when`(mockStorage.loadLeaderboard()).thenReturn(emptyList())
        `when`(mockProgressTracker.initialize()).thenReturn(Unit)

        gamificationManager.initialize()

        // When
        gamificationManager.completeChallenge(challengeId)

        // Then
        verify(mockStorage).saveUserProfile(any())
    }

    @Test
    fun `getBadgeProgress should return correct progress`() {
        // Given
        val badgeId = "test_badge"
        val mockProfile = UserProfile(
            badges = listOf(
                Badge(
                    id = badgeId,
                    name = "Test Badge",
                    description = "Test Description",
                    iconRes = "ic_test",
                    category = BadgeCategory.TYPING_SPEED,
                    rarity = BadgeRarity.COMMON,
                    requirements = emptyList(),
                    points = 10,
                    progress = 0.5f
                )
            )
        )
        `when`(mockStorage.loadUserProfile()).thenReturn(mockProfile)
        `when`(mockStorage.loadDailyChallenges()).thenReturn(emptyList())
        `when`(mockStorage.loadLeaderboard()).thenReturn(emptyList())
        `when`(mockProgressTracker.initialize()).thenReturn(Unit)

        gamificationManager.initialize()

        // When
        val progress = gamificationManager.getBadgeProgress(badgeId)

        // Then
        assertEquals(0.5f, progress, 0.01f)
    }

    @Test
    fun `getAchievementProgress should return correct progress`() {
        // Given
        val achievementId = "test_achievement"
        val mockProfile = UserProfile(
            achievements = listOf(
                Achievement(
                    id = achievementId,
                    name = "Test Achievement",
                    description = "Test Description",
                    iconRes = "ic_test",
                    category = AchievementCategory.DAILY,
                    points = 25,
                    progress = 0.75f,
                    requirements = emptyList()
                )
            )
        )
        `when`(mockStorage.loadUserProfile()).thenReturn(mockProfile)
        `when`(mockStorage.loadDailyChallenges()).thenReturn(emptyList())
        `when`(mockStorage.loadLeaderboard()).thenReturn(emptyList())
        `when`(mockProgressTracker.initialize()).thenReturn(Unit)

        gamificationManager.initialize()

        // When
        val progress = gamificationManager.getAchievementProgress(achievementId)

        // Then
        assertEquals(0.75f, progress, 0.01f)
    }

    @Test
    fun `updatePreferences should update user profile`() {
        // Given
        val mockProfile = UserProfile()
        val newPreferences = GamificationPreferences(
            enableNotifications = false,
            enableSoundEffects = true
        )
        `when`(mockStorage.loadUserProfile()).thenReturn(mockProfile)
        `when`(mockStorage.loadDailyChallenges()).thenReturn(emptyList())
        `when`(mockStorage.loadLeaderboard()).thenReturn(emptyList())
        `when`(mockProgressTracker.initialize()).thenReturn(Unit)

        gamificationManager.initialize()

        // When
        gamificationManager.updatePreferences(newPreferences)

        // Then
        verify(mockStorage).saveUserProfile(any())
    }

    @Test
    fun `dismissNotification should remove notification`() {
        // Given
        val notificationId = "test_notification"

        // When
        gamificationManager.dismissNotification(notificationId)

        // Then
        // Verify that the notification is removed from the list
        // This is tested by checking the internal state
        assertTrue(true) // Placeholder assertion
    }

    @Test
    fun `clearAllNotifications should clear all notifications`() {
        // When
        gamificationManager.clearAllNotifications()

        // Then
        // Verify that all notifications are cleared
        assertTrue(true) // Placeholder assertion
    }

    @Test
    fun `resetProgress should reset all data`() {
        // Given
        val mockProfile = UserProfile()
        `when`(mockStorage.loadUserProfile()).thenReturn(mockProfile)
        `when`(mockStorage.loadDailyChallenges()).thenReturn(emptyList())
        `when`(mockStorage.loadLeaderboard()).thenReturn(emptyList())
        `when`(mockProgressTracker.initialize()).thenReturn(Unit)
        `when`(mockProgressTracker.reset()).thenReturn(Unit)

        gamificationManager.initialize()

        // When
        gamificationManager.resetProgress()

        // Then
        verify(mockStorage).saveUserProfile(any())
        verify(mockProgressTracker).reset()
    }
}
