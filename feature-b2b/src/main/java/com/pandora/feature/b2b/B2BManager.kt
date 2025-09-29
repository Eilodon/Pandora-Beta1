package com.pandora.feature.b2b

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Main coordinator for B2B Features system
 */
@Singleton
class B2BManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storage: B2BStorage,
    private val analyticsManager: B2BAnalyticsManager,
    private val teamManager: TeamManager,
    private val shortcutManager: TeamShortcutManager
) {

    private val managerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _currentOrganization = MutableStateFlow<Organization?>(null)
    val currentOrganization: StateFlow<Organization?> = _currentOrganization.asStateFlow()

    private val _currentUser = MutableStateFlow<B2BUser?>(null)
    val currentUser: StateFlow<B2BUser?> = _currentUser.asStateFlow()

    private val _dashboard = MutableStateFlow<B2BDashboard?>(null)
    val dashboard: StateFlow<B2BDashboard?> = _dashboard.asStateFlow()

    private val _notifications = MutableStateFlow<List<B2BNotification>>(emptyList())
    val notifications: StateFlow<List<B2BNotification>> = _notifications.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        managerScope.launch {
            initialize()
        }
    }

    /**
     * Initialize the B2B system
     */
    suspend fun initialize() {
        _isLoading.value = true
        try {
            val organization = storage.loadCurrentOrganization()
            _currentOrganization.value = organization

            val user = storage.loadCurrentUser()
            _currentUser.value = user

            if (organization != null) {
                val dashboard = analyticsManager.generateDashboard(organization.id)
                _dashboard.value = dashboard

                val notifications = storage.loadNotifications(organization.id)
                _notifications.value = notifications
            }

            Log.d("B2BManager", "B2B system initialized for organization: ${organization?.name}")
        } catch (e: Exception) {
            _error.value = "Failed to initialize B2B system: ${e.message}"
            Log.e("B2BManager", "Error initializing B2B system", e)
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Create a new organization
     */
    suspend fun createOrganization(
        name: String,
        domain: String,
        industry: String,
        size: OrganizationSize,
        plan: B2BPlan
    ): Result<Organization> {
        return try {
            val organization = Organization(
                id = java.util.UUID.randomUUID().toString(),
                name = name,
                domain = domain,
                industry = industry,
                size = size,
                plan = plan,
                settings = OrganizationSettings()
            )
            
            storage.saveOrganization(organization)
            _currentOrganization.value = organization
            
            analyticsManager.trackEvent(
                organizationId = organization.id,
                eventType = ActivityType.SETTINGS_CHANGED,
                description = "Organization created: $name"
            )
            
            Log.d("B2BManager", "Organization created: $name")
            Result.success(organization)
        } catch (e: Exception) {
            Log.e("B2BManager", "Failed to create organization", e)
            Result.failure(e)
        }
    }

    /**
     * Create a new team
     */
    suspend fun createTeam(
        organizationId: String,
        name: String,
        description: String? = null,
        department: String? = null,
        managerId: String? = null
    ): Result<Team> {
        return try {
            val team = Team(
                id = java.util.UUID.randomUUID().toString(),
                organizationId = organizationId,
                name = name,
                description = description,
                department = department,
                managerId = managerId,
                settings = TeamSettings()
            )
            
            storage.saveTeam(team)
            
            analyticsManager.trackEvent(
                organizationId = organizationId,
                eventType = ActivityType.TEAM_JOINED,
                description = "Team created: $name"
            )
            
            Log.d("B2BManager", "Team created: $name")
            Result.success(team)
        } catch (e: Exception) {
            Log.e("B2BManager", "Failed to create team", e)
            Result.failure(e)
        }
    }

    /**
     * Add user to organization
     */
    suspend fun addUser(
        organizationId: String,
        teamId: String? = null,
        email: String,
        name: String,
        role: UserRole
    ): Result<B2BUser> {
        return try {
            val user = B2BUser(
                id = java.util.UUID.randomUUID().toString(),
                organizationId = organizationId,
                teamId = teamId,
                email = email,
                name = name,
                role = role,
                permissions = getDefaultPermissions(role)
            )
            
            storage.saveUser(user)
            
            analyticsManager.trackEvent(
                organizationId = organizationId,
                eventType = ActivityType.USER_LOGIN,
                description = "User added: $name"
            )
            
            Log.d("B2BManager", "User added: $name")
            Result.success(user)
        } catch (e: Exception) {
            Log.e("B2BManager", "Failed to add user", e)
            Result.failure(e)
        }
    }

    /**
     * Create team shortcut
     */
    suspend fun createTeamShortcut(
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
            
            Log.d("B2BManager", "Team shortcut created: $name")
            Result.success(shortcut)
        } catch (e: Exception) {
            Log.e("B2BManager", "Failed to create team shortcut", e)
            Result.failure(e)
        }
    }

    /**
     * Use team shortcut
     */
    suspend fun useTeamShortcut(shortcutId: String) {
        try {
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
                
                Log.d("B2BManager", "Team shortcut used: ${shortcut.name}")
            }
        } catch (e: Exception) {
            Log.e("B2BManager", "Failed to use team shortcut", e)
        }
    }

    /**
     * Get team shortcuts
     */
    suspend fun getTeamShortcuts(teamId: String): List<TeamShortcut> {
        return try {
            storage.loadTeamShortcuts(teamId)
        } catch (e: Exception) {
            Log.e("B2BManager", "Failed to get team shortcuts", e)
            emptyList()
        }
    }

    /**
     * Get organization analytics
     */
    suspend fun getOrganizationAnalytics(organizationId: String): B2BDashboard {
        return try {
            analyticsManager.generateDashboard(organizationId)
        } catch (e: Exception) {
            Log.e("B2BManager", "Failed to get organization analytics", e)
            B2BDashboard(
                organizationId = organizationId,
                totalUsers = 0,
                activeUsers = 0,
                totalShortcuts = 0,
                shortcutsUsed = 0,
                productivityScore = 0f,
                topShortcuts = emptyList(),
                teamStats = emptyList(),
                recentActivity = emptyList()
            )
        }
    }

    /**
     * Export organization data
     */
    suspend fun exportData(organizationId: String, exportType: ExportType): Result<B2BDataExport> {
        return try {
            val data = storage.exportData(organizationId, exportType)
            val export = B2BDataExport(
                organizationId = organizationId,
                exportType = exportType,
                data = data
            )
            
            analyticsManager.trackEvent(
                organizationId = organizationId,
                eventType = ActivityType.DATA_EXPORTED,
                description = "Data exported: $exportType"
            )
            
            Log.d("B2BManager", "Data exported: $exportType")
            Result.success(export)
        } catch (e: Exception) {
            Log.e("B2BManager", "Failed to export data", e)
            Result.failure(e)
        }
    }

    /**
     * Add notification
     */
    fun addNotification(notification: B2BNotification) {
        val notifications = _notifications.value.toMutableList()
        notifications.add(notification)
        _notifications.value = notifications
        managerScope.launch {
            storage.saveNotification(notification)
        }
    }

    /**
     * Mark notification as read
     */
    suspend fun markNotificationAsRead(notificationId: String) {
        val notifications = _notifications.value.toMutableList()
        val index = notifications.indexOfFirst { it.id == notificationId }
        if (index != -1) {
            val notification = notifications[index].copy(isRead = true)
            notifications[index] = notification
            _notifications.value = notifications
            storage.updateNotification(notification)
        }
    }

    /**
     * Get default permissions based on role
     */
    private fun getDefaultPermissions(role: UserRole): UserPermissions {
        return when (role) {
            UserRole.ADMIN -> UserPermissions(
                canCreateShortcuts = true,
                canViewAnalytics = true,
                canManageTeam = true,
                canAccessAPI = true,
                canExportData = true,
                canManageSettings = true
            )
            UserRole.MANAGER -> UserPermissions(
                canCreateShortcuts = true,
                canViewAnalytics = true,
                canManageTeam = true,
                canAccessAPI = false,
                canExportData = true,
                canManageSettings = false
            )
            UserRole.MEMBER -> UserPermissions(
                canCreateShortcuts = true,
                canViewAnalytics = true,
                canManageTeam = false,
                canAccessAPI = false,
                canExportData = false,
                canManageSettings = false
            )
            UserRole.VIEWER -> UserPermissions(
                canCreateShortcuts = false,
                canViewAnalytics = true,
                canManageTeam = false,
                canAccessAPI = false,
                canExportData = false,
                canManageSettings = false
            )
        }
    }
}
