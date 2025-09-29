package com.pandora.feature.b2b

import android.content.Context
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class B2BManagerTest {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var storage: B2BStorage

    @Mock
    private lateinit var analyticsManager: B2BAnalyticsManager

    @Mock
    private lateinit var teamManager: TeamManager

    @Mock
    private lateinit var shortcutManager: TeamShortcutManager

    private lateinit var b2bManager: B2BManager

    @Before
    fun setUp() {
        b2bManager = B2BManager(
            context = context,
            storage = storage,
            analyticsManager = analyticsManager,
            teamManager = teamManager,
            shortcutManager = shortcutManager
        )
    }

    @Test
    fun `createOrganization should return success when organization is created`() = runTest {
        // Given
        val name = "Test Organization"
        val domain = "test.com"
        val industry = "Technology"
        val size = OrganizationSize.MEDIUM
        val plan = B2BPlan.PROFESSIONAL

        // When
        val result = b2bManager.createOrganization(name, domain, industry, size, plan)

        // Then
        assert(result.isSuccess)
        val organization = result.getOrThrow()
        assert(organization.name == name)
        assert(organization.domain == domain)
        assert(organization.industry == industry)
        assert(organization.size == size)
        assert(organization.plan == plan)
    }

    @Test
    fun `createTeam should return success when team is created`() = runTest {
        // Given
        val organizationId = "org-123"
        val name = "Development Team"
        val description = "Software development team"
        val department = "Engineering"

        // When
        val result = b2bManager.createTeam(organizationId, name, description, department)

        // Then
        assert(result.isSuccess)
        val team = result.getOrThrow()
        assert(team.name == name)
        assert(team.description == description)
        assert(team.department == department)
        assert(team.organizationId == organizationId)
    }

    @Test
    fun `addUser should return success when user is added`() = runTest {
        // Given
        val organizationId = "org-123"
        val teamId = "team-123"
        val email = "user@test.com"
        val name = "John Doe"
        val role = UserRole.MEMBER

        // When
        val result = b2bManager.addUser(organizationId, teamId, email, name, role)

        // Then
        assert(result.isSuccess)
        val user = result.getOrThrow()
        assert(user.email == email)
        assert(user.name == name)
        assert(user.role == role)
        assert(user.organizationId == organizationId)
        assert(user.teamId == teamId)
    }

    @Test
    fun `createTeamShortcut should return success when shortcut is created`() = runTest {
        // Given
        val organizationId = "org-123"
        val teamId = "team-123"
        val createdBy = "user-123"
        val name = "Test Shortcut"
        val trigger = "test"
        val action = ShortcutAction(
            type = ActionType.OPEN_URL,
            target = "https://example.com"
        )
        val category = ShortcutCategory.PRODUCTIVITY
        val description = "Test shortcut description"

        // When
        val result = b2bManager.createTeamShortcut(
            organizationId, teamId, createdBy, name, trigger, action, category, description
        )

        // Then
        assert(result.isSuccess)
        val shortcut = result.getOrThrow()
        assert(shortcut.name == name)
        assert(shortcut.trigger == trigger)
        assert(shortcut.action == action)
        assert(shortcut.category == category)
        assert(shortcut.description == description)
        assert(shortcut.organizationId == organizationId)
        assert(shortcut.teamId == teamId)
        assert(shortcut.createdBy == createdBy)
    }

    @Test
    fun `useTeamShortcut should update usage count`() = runTest {
        // Given
        val shortcutId = "shortcut-123"
        val shortcut = TeamShortcut(
            id = shortcutId,
            organizationId = "org-123",
            teamId = "team-123",
            createdBy = "user-123",
            name = "Test Shortcut",
            trigger = "test",
            action = ShortcutAction(ActionType.OPEN_URL, "https://example.com"),
            category = ShortcutCategory.PRODUCTIVITY,
            usageCount = 5
        )
        
        `when`(storage.getTeamShortcut(shortcutId)).thenReturn(shortcut)

        // When
        b2bManager.useTeamShortcut(shortcutId)

        // Then
        verify(storage).saveTeamShortcut(any())
    }

    @Test
    fun `getTeamShortcuts should return shortcuts for team`() = runTest {
        // Given
        val teamId = "team-123"
        val shortcuts = listOf(
            TeamShortcut(
                id = "shortcut-1",
                organizationId = "org-123",
                teamId = teamId,
                createdBy = "user-123",
                name = "Shortcut 1",
                trigger = "test1",
                action = ShortcutAction(ActionType.OPEN_URL, "https://example1.com"),
                category = ShortcutCategory.PRODUCTIVITY
            ),
            TeamShortcut(
                id = "shortcut-2",
                organizationId = "org-123",
                teamId = teamId,
                createdBy = "user-123",
                name = "Shortcut 2",
                trigger = "test2",
                action = ShortcutAction(ActionType.OPEN_URL, "https://example2.com"),
                category = ShortcutCategory.COMMUNICATION
            )
        )
        
        `when`(storage.getTeamShortcuts(teamId)).thenReturn(shortcuts)

        // When
        val result = b2bManager.getTeamShortcuts(teamId)

        // Then
        assert(result.size == 2)
        assert(result[0].name == "Shortcut 1")
        assert(result[1].name == "Shortcut 2")
    }

    @Test
    fun `exportData should return success when data is exported`() = runTest {
        // Given
        val organizationId = "org-123"
        val exportType = ExportType.FULL_DATA
        val exportData = mapOf(
            "users" to listOf<B2BUser>(),
            "teams" to listOf<Team>(),
            "shortcuts" to listOf<TeamShortcut>(),
            "analytics" to listOf<B2BAnalytics>(),
            "notifications" to listOf<B2BNotification>()
        )
        
        `when`(storage.exportData(organizationId, exportType)).thenReturn(exportData)

        // When
        val result = b2bManager.exportData(organizationId, exportType)

        // Then
        assert(result.isSuccess)
        val export = result.getOrThrow()
        assert(export.organizationId == organizationId)
        assert(export.exportType == exportType)
        assert(export.data == exportData)
    }

    @Test
    fun `addNotification should add notification to list`() = runTest {
        // Given
        val notification = B2BNotification(
            organizationId = "org-123",
            type = NotificationType.SYSTEM_UPDATE,
            title = "Test Notification",
            message = "This is a test notification",
            priority = NotificationPriority.MEDIUM
        )

        // When
        b2bManager.addNotification(notification)

        // Then
        verify(storage).saveNotification(notification)
    }

    @Test
    fun `markNotificationAsRead should update notification`() = runTest {
        // Given
        val notificationId = "notification-123"
        val notification = B2BNotification(
            id = notificationId,
            organizationId = "org-123",
            type = NotificationType.SYSTEM_UPDATE,
            title = "Test Notification",
            message = "This is a test notification",
            priority = NotificationPriority.MEDIUM,
            isRead = false
        )
        
        `when`(storage.loadNotifications("org-123")).thenReturn(listOf(notification))

        // When
        b2bManager.markNotificationAsRead(notificationId)

        // Then
        verify(storage).updateNotification(any())
    }
}
