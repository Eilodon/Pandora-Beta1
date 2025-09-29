package com.pandora.feature.b2b

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
 * Handles storage for B2B data using DataStore
 */
@Singleton
class B2BStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val Context.dataStore by preferencesDataStore(name = "b2b")
    private val gson = Gson()

    companion object {
        private val CURRENT_ORGANIZATION_KEY = stringPreferencesKey("current_organization")
        private val CURRENT_USER_KEY = stringPreferencesKey("current_user")
        private val ORGANIZATIONS_KEY = stringPreferencesKey("organizations")
        private val TEAMS_KEY = stringPreferencesKey("teams")
        private val USERS_KEY = stringPreferencesKey("users")
        private val SHORTCUTS_KEY = stringPreferencesKey("shortcuts")
        private val ANALYTICS_KEY = stringPreferencesKey("analytics")
        private val NOTIFICATIONS_KEY = stringPreferencesKey("notifications")
        private val API_KEYS_KEY = stringPreferencesKey("api_keys")
    }

    /**
     * Organization management
     */
    suspend fun loadCurrentOrganization(): Organization? {
        return try {
            val orgJson = context.dataStore.data.map { preferences ->
                preferences[CURRENT_ORGANIZATION_KEY] ?: ""
            }.first()

            if (orgJson.isEmpty()) {
                null
            } else {
                gson.fromJson(orgJson, Organization::class.java)
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveOrganization(organization: Organization) {
        try {
            context.dataStore.edit { preferences ->
                val orgJson = gson.toJson(organization)
                preferences[CURRENT_ORGANIZATION_KEY] = orgJson
            }
        } catch (e: Exception) {
            // Handle error silently or log it
        }
    }

    suspend fun loadOrganizations(): List<Organization> {
        return try {
            val orgsJson = context.dataStore.data.map { preferences ->
                preferences[ORGANIZATIONS_KEY] ?: ""
            }.first()

            if (orgsJson.isEmpty()) {
                emptyList()
            } else {
                val type = object : TypeToken<List<Organization>>() {}.type
                gson.fromJson(orgsJson, type)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveOrganizations(organizations: List<Organization>) {
        try {
            context.dataStore.edit { preferences ->
                val orgsJson = gson.toJson(organizations)
                preferences[ORGANIZATIONS_KEY] = orgsJson
            }
        } catch (e: Exception) {
            // Handle error silently or log it
        }
    }

    /**
     * User management
     */
    suspend fun loadCurrentUser(): B2BUser? {
        return try {
            val userJson = context.dataStore.data.map { preferences ->
                preferences[CURRENT_USER_KEY] ?: ""
            }.first()

            if (userJson.isEmpty()) {
                null
            } else {
                gson.fromJson(userJson, B2BUser::class.java)
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveUser(user: B2BUser) {
        try {
            context.dataStore.edit { preferences ->
                val userJson = gson.toJson(user)
                preferences[CURRENT_USER_KEY] = userJson
            }
        } catch (e: Exception) {
            // Handle error silently or log it
        }
    }

    suspend fun loadUsers(organizationId: String): List<B2BUser> {
        return try {
            val usersJson = context.dataStore.data.map { preferences ->
                preferences[USERS_KEY] ?: ""
            }.first()

            if (usersJson.isEmpty()) {
                emptyList()
            } else {
                val type = object : TypeToken<List<B2BUser>>() {}.type
                val allUsers = gson.fromJson<List<B2BUser>>(usersJson, type)
                allUsers.filter { it.organizationId == organizationId }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveUsers(users: List<B2BUser>) {
        try {
            context.dataStore.edit { preferences ->
                val usersJson = gson.toJson(users)
                preferences[USERS_KEY] = usersJson
            }
        } catch (e: Exception) {
            // Handle error silently or log it
        }
    }

    /**
     * Team management
     */
    suspend fun loadTeams(organizationId: String): List<Team> {
        return try {
            val teamsJson = context.dataStore.data.map { preferences ->
                preferences[TEAMS_KEY] ?: ""
            }.first()

            if (teamsJson.isEmpty()) {
                emptyList()
            } else {
                val type = object : TypeToken<List<Team>>() {}.type
                val allTeams = gson.fromJson<List<Team>>(teamsJson, type)
                allTeams.filter { it.organizationId == organizationId }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveTeam(team: Team) {
        try {
            val currentTeams = loadTeams(team.organizationId).toMutableList()
            val index = currentTeams.indexOfFirst { it.id == team.id }
            
            if (index != -1) {
                currentTeams[index] = team
            } else {
                currentTeams.add(team)
            }
            
            saveTeams(currentTeams)
        } catch (e: Exception) {
            // Handle error silently or log it
        }
    }

    suspend fun saveTeams(teams: List<Team>) {
        try {
            context.dataStore.edit { preferences ->
                val teamsJson = gson.toJson(teams)
                preferences[TEAMS_KEY] = teamsJson
            }
        } catch (e: Exception) {
            // Handle error silently or log it
        }
    }

    /**
     * Team shortcuts management
     */
    suspend fun loadTeamShortcuts(teamId: String): List<TeamShortcut> {
        return try {
            val shortcutsJson = context.dataStore.data.map { preferences ->
                preferences[SHORTCUTS_KEY] ?: ""
            }.first()

            if (shortcutsJson.isEmpty()) {
                emptyList()
            } else {
                val type = object : TypeToken<List<TeamShortcut>>() {}.type
                val allShortcuts = gson.fromJson<List<TeamShortcut>>(shortcutsJson, type)
                allShortcuts.filter { it.teamId == teamId }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveTeamShortcut(shortcut: TeamShortcut) {
        try {
            val currentShortcuts = loadTeamShortcuts(shortcut.teamId).toMutableList()
            val index = currentShortcuts.indexOfFirst { it.id == shortcut.id }
            
            if (index != -1) {
                currentShortcuts[index] = shortcut
            } else {
                currentShortcuts.add(shortcut)
            }
            
            saveTeamShortcuts(currentShortcuts)
        } catch (e: Exception) {
            // Handle error silently or log it
        }
    }

    suspend fun saveTeamShortcuts(shortcuts: List<TeamShortcut>) {
        try {
            context.dataStore.edit { preferences ->
                val shortcutsJson = gson.toJson(shortcuts)
                preferences[SHORTCUTS_KEY] = shortcutsJson
            }
        } catch (e: Exception) {
            // Handle error silently or log it
        }
    }

    suspend fun getTeamShortcut(shortcutId: String): TeamShortcut? {
        return try {
            val shortcutsJson = context.dataStore.data.map { preferences ->
                preferences[SHORTCUTS_KEY] ?: ""
            }.first()

            if (shortcutsJson.isEmpty()) {
                null
            } else {
                val type = object : TypeToken<List<TeamShortcut>>() {}.type
                val allShortcuts = gson.fromJson<List<TeamShortcut>>(shortcutsJson, type)
                allShortcuts.find { it.id == shortcutId }
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Analytics management
     */
    suspend fun loadAnalytics(organizationId: String): List<B2BAnalytics> {
        return try {
            val analyticsJson = context.dataStore.data.map { preferences ->
                preferences[ANALYTICS_KEY] ?: ""
            }.first()

            if (analyticsJson.isEmpty()) {
                emptyList()
            } else {
                val type = object : TypeToken<List<B2BAnalytics>>() {}.type
                val allAnalytics = gson.fromJson<List<B2BAnalytics>>(analyticsJson, type)
                allAnalytics.filter { it.organizationId == organizationId }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveAnalytics(analytics: B2BAnalytics) {
        try {
            val currentAnalytics = loadAnalytics(analytics.organizationId).toMutableList()
            currentAnalytics.add(analytics)
            saveAnalyticsList(currentAnalytics)
        } catch (e: Exception) {
            // Handle error silently or log it
        }
    }

    suspend fun saveAnalyticsList(analytics: List<B2BAnalytics>) {
        try {
            context.dataStore.edit { preferences ->
                val analyticsJson = gson.toJson(analytics)
                preferences[ANALYTICS_KEY] = analyticsJson
            }
        } catch (e: Exception) {
            // Handle error silently or log it
        }
    }

    /**
     * Notifications management
     */
    suspend fun loadNotifications(organizationId: String): List<B2BNotification> {
        return try {
            val notificationsJson = context.dataStore.data.map { preferences ->
                preferences[NOTIFICATIONS_KEY] ?: ""
            }.first()

            if (notificationsJson.isEmpty()) {
                emptyList()
            } else {
                val type = object : TypeToken<List<B2BNotification>>() {}.type
                val allNotifications = gson.fromJson<List<B2BNotification>>(notificationsJson, type)
                allNotifications.filter { it.organizationId == organizationId }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveNotification(notification: B2BNotification) {
        try {
            val currentNotifications = loadNotifications(notification.organizationId).toMutableList()
            currentNotifications.add(notification)
            saveNotifications(currentNotifications)
        } catch (e: Exception) {
            // Handle error silently or log it
        }
    }

    suspend fun updateNotification(notification: B2BNotification) {
        try {
            val currentNotifications = loadNotifications(notification.organizationId).toMutableList()
            val index = currentNotifications.indexOfFirst { it.id == notification.id }
            
            if (index != -1) {
                currentNotifications[index] = notification
                saveNotifications(currentNotifications)
            }
        } catch (e: Exception) {
            // Handle error silently or log it
        }
    }

    suspend fun saveNotifications(notifications: List<B2BNotification>) {
        try {
            context.dataStore.edit { preferences ->
                val notificationsJson = gson.toJson(notifications)
                preferences[NOTIFICATIONS_KEY] = notificationsJson
            }
        } catch (e: Exception) {
            // Handle error silently or log it
        }
    }

    /**
     * Data export
     */
    suspend fun exportData(organizationId: String, exportType: ExportType): Map<String, Any> {
        return try {
            when (exportType) {
                ExportType.USERS -> {
                    val users = loadUsers(organizationId)
                    mapOf("users" to users)
                }
                ExportType.SHORTCUTS -> {
                    val teams = loadTeams(organizationId)
                    val shortcuts = teams.flatMap { loadTeamShortcuts(it.id) }
                    mapOf("shortcuts" to shortcuts)
                }
                ExportType.ANALYTICS -> {
                    val analytics = loadAnalytics(organizationId)
                    mapOf("analytics" to analytics)
                }
                ExportType.FULL_DATA -> {
                    val users = loadUsers(organizationId)
                    val teams = loadTeams(organizationId)
                    val shortcuts = teams.flatMap { loadTeamShortcuts(it.id) }
                    val analytics = loadAnalytics(organizationId)
                    val notifications = loadNotifications(organizationId)
                    
                    mapOf(
                        "users" to users,
                        "teams" to teams,
                        "shortcuts" to shortcuts,
                        "analytics" to analytics,
                        "notifications" to notifications
                    )
                }
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * Clear all B2B data
     */
    suspend fun clearAllData() {
        context.dataStore.edit { it.clear() }
    }
}
