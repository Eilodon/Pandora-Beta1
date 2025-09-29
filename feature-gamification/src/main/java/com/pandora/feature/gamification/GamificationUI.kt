package com.pandora.feature.gamification

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.GlobalScope

/**
 * Main gamification dashboard UI
 */
@Composable
fun GamificationDashboard(
    gamificationManager: GamificationManager,
    modifier: Modifier = Modifier
) {
    val userProfile by gamificationManager.userProfile.collectAsState()
    val dailyChallenges by gamificationManager.dailyChallenges.collectAsState()
    val leaderboard by gamificationManager.leaderboard.collectAsState()
    val notifications by gamificationManager.notifications.collectAsState()
    val isInitialized by gamificationManager.isInitialized.collectAsState()

    if (!isInitialized) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Header
        userProfile?.let { profile ->
            item {
                UserProfileHeader(profile = profile)
            }
        }

        // Notifications
        if (notifications.isNotEmpty()) {
            item {
                NotificationsSection(
                    notifications = notifications,
                    onDismiss = { gamificationManager.dismissNotification(it) }
                )
            }
        }

        // Daily Challenges
        item {
            DailyChallengesSection(
                challenges = dailyChallenges,
                onCompleteChallenge = { 
                    GlobalScope.launch {
                        gamificationManager.completeChallenge(it)
                    }
                }
            )
        }

        // Badges Section
        userProfile?.let { profile ->
            item {
                BadgesSection(badges = profile.badges)
            }
        }

        // Achievements Section
        userProfile?.let { profile ->
            item {
                AchievementsSection(achievements = profile.achievements)
            }
        }

        // Leaderboard Section
        item {
            LeaderboardSection(leaderboard = leaderboard)
        }
    }
}

/**
 * User profile header with level, points, and stats
 */
@Composable
fun UserProfileHeader(
    profile: UserProfile,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Level and Points
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Level ${profile.level}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${profile.totalPoints} points",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                
                // Experience Progress
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "${profile.experience}/${profile.experienceToNextLevel} XP",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    LinearProgressIndicator(
                        progress = profile.experience.toFloat() / profile.experienceToNextLevel,
                        modifier = Modifier
                            .width(100.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Default.Speed,
                    label = "Speed",
                    value = "${profile.stats.averageWpm.toInt()} WPM"
                )
                StatItem(
                    icon = Icons.Default.CheckCircle,
                    label = "Accuracy",
                    value = "${profile.stats.averageAccuracy.toInt()}%"
                )
                StatItem(
                    icon = Icons.Default.LocalFireDepartment,
                    label = "Streak",
                    value = "${profile.stats.consecutiveDays} days"
                )
            }
        }
    }
}

@Composable
fun StatItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

/**
 * Notifications section
 */
@Composable
fun NotificationsSection(
    notifications: List<GamificationNotification>,
    onDismiss: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Notifications",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(notifications) { notification ->
                NotificationCard(
                    notification = notification,
                    onDismiss = { onDismiss(notification.id) }
                )
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: GamificationNotification,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (notification.type) {
                NotificationType.BADGE_EARNED -> MaterialTheme.colorScheme.secondaryContainer
                NotificationType.ACHIEVEMENT_COMPLETED -> MaterialTheme.colorScheme.tertiaryContainer
                NotificationType.LEVEL_UP -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (notification.type) {
                    NotificationType.BADGE_EARNED -> Icons.Default.Star
                    NotificationType.ACHIEVEMENT_COMPLETED -> Icons.Default.EmojiEvents
                    NotificationType.LEVEL_UP -> Icons.Default.TrendingUp
                    else -> Icons.Default.Notifications
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Dismiss")
            }
        }
    }
}

/**
 * Daily challenges section
 */
@Composable
fun DailyChallengesSection(
    challenges: List<DailyChallenge>,
    onCompleteChallenge: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Daily Challenges",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(challenges) { challenge ->
                ChallengeCard(
                    challenge = challenge,
                    onComplete = { onCompleteChallenge(challenge.id) }
                )
            }
        }
    }
}

@Composable
fun ChallengeCard(
    challenge: DailyChallenge,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = challenge.current.toFloat() / challenge.target
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000)
    )

    Card(
        modifier = modifier
            .width(200.dp)
            .clickable { if (!challenge.isCompleted) onComplete() },
        colors = CardDefaults.cardColors(
            containerColor = if (challenge.isCompleted) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = challenge.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = challenge.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress bar
            LinearProgressIndicator(
                progress = animatedProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${challenge.current}/${challenge.target}",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            if (challenge.isCompleted) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Completed!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * Badges section
 */
@Composable
fun BadgesSection(
    badges: List<Badge>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Badges",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(badges) { badge ->
                BadgeCard(badge = badge)
            }
        }
    }
}

@Composable
fun BadgeCard(
    badge: Badge,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    
    Card(
        modifier = modifier
            .width(120.dp)
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            },
        colors = CardDefaults.cardColors(
            containerColor = if (badge.isUnlocked) {
                getRarityColor(badge.rarity).copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Badge icon placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (badge.isUnlocked) {
                            getRarityColor(badge.rarity)
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = badge.name,
                    tint = if (badge.isUnlocked) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = badge.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            if (badge.isUnlocked) {
                Text(
                    text = "${badge.points} pts",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    text = "${(badge.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

/**
 * Achievements section
 */
@Composable
fun AchievementsSection(
    achievements: List<Achievement>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Achievements",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(achievements) { achievement ->
                AchievementCard(achievement = achievement)
            }
        }
    }
}

@Composable
fun AchievementCard(
    achievement: Achievement,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.isCompleted) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Achievement icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (achievement.isCompleted) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = achievement.name,
                    tint = if (achievement.isCompleted) {
                        MaterialTheme.colorScheme.onTertiary
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                if (!achievement.isCompleted) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = achievement.progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                    )
                }
            }
            
            if (achievement.isCompleted) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Completed",
                    tint = MaterialTheme.colorScheme.tertiary
                )
            } else {
                Text(
                    text = "${(achievement.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

/**
 * Leaderboard section
 */
@Composable
fun LeaderboardSection(
    leaderboard: List<LeaderboardEntry>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Leaderboard",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(leaderboard) { entry ->
                LeaderboardEntryCard(entry = entry)
            }
        }
    }
}

@Composable
fun LeaderboardEntryCard(
    entry: LeaderboardEntry,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.isCurrentUser) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank
            Text(
                text = "#${entry.rank}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Avatar placeholder
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = entry.username.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.username,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${entry.score} points",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            // Badge
            entry.badge?.let { badge ->
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(getRarityColor(badge.rarity)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = badge.name,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Get color for badge rarity
 */
fun getRarityColor(rarity: BadgeRarity): Color {
    return when (rarity) {
        BadgeRarity.COMMON -> Color(0xFF4CAF50) // Green
        BadgeRarity.UNCOMMON -> Color(0xFF2196F3) // Blue
        BadgeRarity.RARE -> Color(0xFF9C27B0) // Purple
        BadgeRarity.EPIC -> Color(0xFFFF9800) // Orange
        BadgeRarity.LEGENDARY -> Color(0xFFFFD700) // Gold
    }
}
