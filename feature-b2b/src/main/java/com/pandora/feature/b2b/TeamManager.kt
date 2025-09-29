package com.pandora.feature.b2b

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages team operations and team-related functionality
 */
@Singleton
class TeamManager @Inject constructor(
    private val storage: B2BStorage,
    private val analyticsManager: B2BAnalyticsManager
) {
    private val teamScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

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
            
            Log.d("TeamManager", "Team created: $name")
            Result.success(team)
        } catch (e: Exception) {
            Log.e("TeamManager", "Failed to create team", e)
            Result.failure(e)
        }
    }

    /**
     * Update team settings
     */
    suspend fun updateTeamSettings(
        teamId: String,
        settings: TeamSettings
    ): Result<Team> {
        return try {
            val teams = storage.loadTeams("")
            val team = teams.find { it.id == teamId }
            
            if (team != null) {
                val updatedTeam = team.copy(
                    settings = settings,
                    updatedAt = System.currentTimeMillis()
                )
                
                storage.saveTeam(updatedTeam)
                
                analyticsManager.trackEvent(
                    organizationId = team.organizationId,
                    eventType = ActivityType.SETTINGS_CHANGED,
                    description = "Team settings updated: ${team.name}"
                )
                
                Log.d("TeamManager", "Team settings updated: ${team.name}")
                Result.success(updatedTeam)
            } else {
                Result.failure(Exception("Team not found"))
            }
        } catch (e: Exception) {
            Log.e("TeamManager", "Failed to update team settings", e)
            Result.failure(e)
        }
    }

    /**
     * Add user to team
     */
    suspend fun addUserToTeam(
        userId: String,
        teamId: String
    ): Result<B2BUser> {
        return try {
            val users = storage.loadUsers("")
            val user = users.find { it.id == userId }
            
            if (user != null) {
                val updatedUser = user.copy(
                    teamId = teamId,
                    updatedAt = System.currentTimeMillis()
                )
                
                storage.saveUser(updatedUser)
                
                analyticsManager.trackEvent(
                    organizationId = user.organizationId,
                    eventType = ActivityType.TEAM_JOINED,
                    description = "User added to team: ${user.name}"
                )
                
                Log.d("TeamManager", "User added to team: ${user.name}")
                Result.success(updatedUser)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Log.e("TeamManager", "Failed to add user to team", e)
            Result.failure(e)
        }
    }

    /**
     * Remove user from team
     */
    suspend fun removeUserFromTeam(
        userId: String,
        teamId: String
    ): Result<B2BUser> {
        return try {
            val users = storage.loadUsers("")
            val user = users.find { it.id == userId && it.teamId == teamId }
            
            if (user != null) {
                val updatedUser = user.copy(
                    teamId = null,
                    updatedAt = System.currentTimeMillis()
                )
                
                storage.saveUser(updatedUser)
                
                analyticsManager.trackEvent(
                    organizationId = user.organizationId,
                    eventType = ActivityType.SETTINGS_CHANGED,
                    description = "User removed from team: ${user.name}"
                )
                
                Log.d("TeamManager", "User removed from team: ${user.name}")
                Result.success(updatedUser)
            } else {
                Result.failure(Exception("User not found in team"))
            }
        } catch (e: Exception) {
            Log.e("TeamManager", "Failed to remove user from team", e)
            Result.failure(e)
        }
    }

    /**
     * Get team members
     */
    suspend fun getTeamMembers(teamId: String): List<B2BUser> {
        return try {
            val users = storage.loadUsers("")
            users.filter { it.teamId == teamId }
        } catch (e: Exception) {
            Log.e("TeamManager", "Failed to get team members", e)
            emptyList()
        }
    }

    /**
     * Get team by ID
     */
    suspend fun getTeam(teamId: String): Team? {
        return try {
            val teams = storage.loadTeams("")
            teams.find { it.id == teamId }
        } catch (e: Exception) {
            Log.e("TeamManager", "Failed to get team", e)
            null
        }
    }

    /**
     * Get teams for organization
     */
    suspend fun getTeams(organizationId: String): List<Team> {
        return try {
            storage.loadTeams(organizationId)
        } catch (e: Exception) {
            Log.e("TeamManager", "Failed to get teams", e)
            emptyList()
        }
    }

    /**
     * Delete team
     */
    suspend fun deleteTeam(teamId: String): Result<Unit> {
        return try {
            val teams = storage.loadTeams("")
            val team = teams.find { it.id == teamId }
            
            if (team != null) {
                val updatedTeams = teams.filter { it.id != teamId }
                storage.saveTeams(updatedTeams)
                
                analyticsManager.trackEvent(
                    organizationId = team.organizationId,
                    eventType = ActivityType.SETTINGS_CHANGED,
                    description = "Team deleted: ${team.name}"
                )
                
                Log.d("TeamManager", "Team deleted: ${team.name}")
                Result.success(Unit)
            } else {
                Result.failure(Exception("Team not found"))
            }
        } catch (e: Exception) {
            Log.e("TeamManager", "Failed to delete team", e)
            Result.failure(e)
        }
    }
}
