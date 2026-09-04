package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.RunningWithErrors
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Habit
import com.example.data.model.HabitCompletion
import com.example.data.model.UserProfile
import com.example.ui.components.WeeklyBarChart
import com.example.ui.viewmodel.DayProgress

@Composable
fun StatisticsScreen(
    habits: List<Habit>,
    completions: List<HabitCompletion>,
    streakStats: Pair<Int, Int>,
    weeklyProgress: List<DayProgress>,
    userProfile: UserProfile,
    todayDateStr: String,
    modifier: Modifier = Modifier
) {
    val currentStreak = streakStats.first
    val bestStreak = streakStats.second

    val distinctCompletedDays = completions.map { it.date }.distinct().size
    val totalCompletions = completions.size

    val past7TotalScheduled = (habits.size * 7).coerceAtLeast(1)
    val past7CompletedCount = weeklyProgress.sumOf { it.completedCount }
    val overallPercentage = ((past7CompletedCount.toFloat() / past7TotalScheduled.toFloat()) * 100).toInt().coerceIn(0, 100)

    val missedHabitsPast7 = (past7TotalScheduled - past7CompletedCount).coerceAtLeast(0)

    val todayCompletedCount = weeklyProgress.find { it.dateStr == todayDateStr }?.completedCount ?: 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("statistics_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Tracking & Analytics",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Deep dive into your habits consistency & performance",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Metrics Grid (2x2)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatMetricCard(
                        title = "Current Streak",
                        value = "$currentStreak Days",
                        icon = Icons.Default.LocalFireDepartment,
                        iconTint = Color(0xFFF97316),
                        bgColor = Color(0xFFFFF7ED),
                        modifier = Modifier.weight(1f)
                    )

                    StatMetricCard(
                        title = "Best Streak",
                        value = "$bestStreak Days",
                        icon = Icons.Default.EmojiEvents,
                        iconTint = Color(0xFFEAB308),
                        bgColor = Color(0xFFFEFCE8),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatMetricCard(
                        title = "Completion %",
                        value = "$overallPercentage%",
                        icon = Icons.Default.Timeline,
                        iconTint = Color(0xFF4F46E5),
                        bgColor = Color(0xFFEEF2FF),
                        modifier = Modifier.weight(1f)
                    )

                    StatMetricCard(
                        title = "Completed Days",
                        value = "$distinctCompletedDays Days",
                        icon = Icons.Default.CheckCircle,
                        iconTint = Color(0xFF10B981),
                        bgColor = Color(0xFFECFDF5),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatMetricCard(
                        title = "Total Checkpoints",
                        value = "$totalCompletions Done",
                        icon = Icons.Default.PieChart,
                        iconTint = Color(0xFF06B6D4),
                        bgColor = Color(0xFFECFEFF),
                        modifier = Modifier.weight(1f)
                    )

                    StatMetricCard(
                        title = "Missed (7 Days)",
                        value = "$missedHabitsPast7 Habits",
                        icon = Icons.Default.RunningWithErrors,
                        iconTint = Color(0xFFEF4444),
                        bgColor = Color(0xFFFEF2F2),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Weekly Activity Chart
        item {
            WeeklyBarChart(
                weeklyProgress = weeklyProgress,
                todayDateStr = todayDateStr
            )
        }

        // Motivation & Goals Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Daily Goal & Weekly Challenge",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Daily Goal item
                    val dailyGoal = userProfile.dailyGoalCount
                    val dailyRatio = (todayCompletedCount.toFloat() / dailyGoal.toFloat()).coerceIn(0f, 1f)
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("🎯 Daily Goal ($dailyGoal habits)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text("$todayCompletedCount/$dailyGoal", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { dailyRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF10B981),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Weekly Challenge
                    val weeklyGoal = userProfile.weeklyGoalPercent
                    val weeklyRatio = (overallPercentage.toFloat() / weeklyGoal.toFloat()).coerceIn(0f, 1f)
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("🏆 Weekly Routine ($weeklyGoal% target)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text("$overallPercentage% / $weeklyGoal%", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { weeklyRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF4F46E5),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
