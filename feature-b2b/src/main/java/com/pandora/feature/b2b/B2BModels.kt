package com.pandora.feature.b2b

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * Organization/Company entity
 */
@Entity(tableName = "organizations")
data class Organization(
    @PrimaryKey
    val id: String,
    val name: String,
    val domain: String,
    val industry: String,
    val size: OrganizationSize,
    val plan: B2BPlan,
    val settings: OrganizationSettings,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class OrganizationSize {
    STARTUP,      // 1-10 employees
    SMALL,        // 11-50 employees
    MEDIUM,       // 51-200 employees
    LARGE,        // 201-1000 employees
    ENTERPRISE    // 1000+ employees
}

enum class B2BPlan {
    FREE,         // Basic features
    PROFESSIONAL, // Advanced features
    BUSINESS,     // Team features
    ENTERPRISE    // Full features + custom
}

data class OrganizationSettings(
    val allowTeamShortcuts: Boolean = true,
    val allowAnalytics: Boolean = true,
    val allowCustomBranding: Boolean = false,
    val allowAPI: Boolean = false,
    val maxUsers: Int = 10,
    val dataRetentionDays: Int = 365,
    val securityLevel: SecurityLevel = SecurityLevel.STANDARD
)

enum class SecurityLevel {
    BASIC,        // Standard security
    STANDARD,     // Enhanced security
    HIGH,         // Enterprise security
    CUSTOM        // Custom security rules
}

/**
 * Team entity
 */
@Entity(tableName = "teams")
data class Team(
    @PrimaryKey
    val id: String,
    val organizationId: String,
    val name: String,
    val description: String? = null,
    val department: String? = null,
    val managerId: String? = null,
    val settings: TeamSettings,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class TeamSettings(
    val allowShortcuts: Boolean = true,
    val allowAnalytics: Boolean = true,
    val allowCustomization: Boolean = true,
    val maxShortcuts: Int = 50,
    val requireApproval: Boolean = false
)

/**
 * User entity for B2B
 */
@Entity(tableName = "b2b_users")
data class B2BUser(
    @PrimaryKey
    val id: String,
    val organizationId: String,
    val teamId: String? = null,
    val email: String,
    val name: String,
    val role: UserRole,
    val permissions: UserPermissions,
    val isActive: Boolean = true,
    val lastLoginAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class UserRole {
    ADMIN,        // Full access
    MANAGER,      // Team management
    MEMBER,       // Standard user
    VIEWER        // Read-only access
}

data class UserPermissions(
    val canCreateShortcuts: Boolean = true,
    val canViewAnalytics: Boolean = true,
    val canManageTeam: Boolean = false,
    val canAccessAPI: Boolean = false,
    val canExportData: Boolean = false,
    val canManageSettings: Boolean = false
)

/**
 * Team Shortcut entity
 */
@Entity(tableName = "team_shortcuts")
data class TeamShortcut(
    @PrimaryKey
    val id: String,
    val organizationId: String,
    val teamId: String,
    val createdBy: String,
    val name: String,
    val description: String? = null,
    val trigger: String, // Text trigger
    val action: ShortcutAction,
    val category: ShortcutCategory,
    val isPublic: Boolean = true,
    val usageCount: Int = 0,
    val rating: Float = 0f,
    val tags: List<String> = emptyList(),
    val isApproved: Boolean = false,
    val approvedBy: String? = null,
    val approvedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class ShortcutAction(
    val type: ActionType,
    val target: String, // URL, app package, etc.
    val parameters: Map<String, String> = emptyMap(),
    val confirmation: Boolean = false
)

enum class ActionType {
    OPEN_URL,
    OPEN_APP,
    SEND_EMAIL,
    CREATE_CALENDAR_EVENT,
    SEND_MESSAGE,
    CUSTOM_SCRIPT
}

enum class ShortcutCategory {
    COMMUNICATION,
    PRODUCTIVITY,
    DEVELOPMENT,
    MARKETING,
    SALES,
    SUPPORT,
    CUSTOM
}

/**
 * Analytics data
 */
@Entity(tableName = "b2b_analytics")
data class B2BAnalytics(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val organizationId: String,
    val teamId: String? = null,
    val userId: String? = null,
    val metricType: AnalyticsMetricType,
    val value: Float,
    val metadata: Map<String, String> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)

enum class AnalyticsMetricType {
    SHORTCUT_USAGE,
    USER_ACTIVITY,
    TEAM_COLLABORATION,
    PRODUCTIVITY_SCORE,
    ERROR_RATE,
    RESPONSE_TIME,
    CUSTOM_METRIC
}

/**
 * Dashboard data
 */
data class B2BDashboard(
    val organizationId: String,
    val totalUsers: Int,
    val activeUsers: Int,
    val totalShortcuts: Int,
    val shortcutsUsed: Int,
    val productivityScore: Float,
    val topShortcuts: List<ShortcutUsage>,
    val teamStats: List<TeamStats>,
    val recentActivity: List<ActivityEvent>,
    val generatedAt: Long = System.currentTimeMillis()
)

data class ShortcutUsage(
    val shortcutId: String,
    val name: String,
    val usageCount: Int,
    val lastUsed: Long,
    val rating: Float
)

data class TeamStats(
    val teamId: String,
    val name: String,
    val memberCount: Int,
    val shortcutsCreated: Int,
    val productivityScore: Float,
    val activityLevel: ActivityLevel
)

enum class ActivityLevel {
    LOW,      // 0-30%
    MEDIUM,   // 31-70%
    HIGH,     // 71-100%
    VERY_HIGH // 100%+
}

data class ActivityEvent(
    val id: String = UUID.randomUUID().toString(),
    val type: ActivityType,
    val userId: String,
    val userName: String,
    val description: String,
    val metadata: Map<String, String> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)

enum class ActivityType {
    USER_LOGIN,
    SHORTCUT_CREATED,
    SHORTCUT_USED,
    TEAM_JOINED,
    SETTINGS_CHANGED,
    DATA_EXPORTED,
    ERROR_OCCURRED
}

/**
 * API Key for external integrations
 */
@Entity(tableName = "api_keys")
data class APIKey(
    @PrimaryKey
    val id: String,
    val organizationId: String,
    val name: String,
    val key: String,
    val permissions: List<String>,
    val isActive: Boolean = true,
    val lastUsed: Long? = null,
    val expiresAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Export/Import data
 */
data class B2BDataExport(
    val organizationId: String,
    val exportType: ExportType,
    val data: Map<String, Any>,
    val generatedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000) // 7 days
)

enum class ExportType {
    USERS,
    SHORTCUTS,
    ANALYTICS,
    FULL_DATA
}

/**
 * Notification for B2B
 */
data class B2BNotification(
    val id: String = UUID.randomUUID().toString(),
    val organizationId: String,
    val userId: String? = null, // null for organization-wide
    val teamId: String? = null,
    val type: NotificationType,
    val title: String,
    val message: String,
    val priority: NotificationPriority,
    val isRead: Boolean = false,
    val actionUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class NotificationType {
    SYSTEM_UPDATE,
    TEAM_INVITATION,
    SHORTCUT_APPROVAL,
    ANALYTICS_REPORT,
    SECURITY_ALERT,
    BILLING_REMINDER
}

enum class NotificationPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}
