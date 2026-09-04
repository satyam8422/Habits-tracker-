package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Habit
import com.example.data.model.UserProfile
import com.example.ui.components.DailyProgressCard
import com.example.ui.components.HabitItemCard
import com.example.ui.components.WeeklyBarChart
import com.example.ui.viewmodel.DayProgress
import com.example.ui.viewmodel.HabitWithStatus

@Composable
fun HomeScreen(
    todayDateStr: String,
    habitsWithStatus: List<HabitWithStatus>,
    userProfile: UserProfile,
    streakStats: Pair<Int, Int>,
    weeklyProgress: List<DayProgress>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    onToggleHabitDone: (Long) -> Unit,
    onEditHabit: (Habit) -> Unit,
    onDeleteHabit: (Long) -> Unit,
    onSnoozeHabit: (Habit) -> Unit,
    onTestReminder: (Habit) -> Unit,
    onAddNewHabit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalCount = habitsWithStatus.size
    val completedCount = habitsWithStatus.count { it.isCompleted }

    val categories = listOf("All", "Morning", "College", "Study", "Fitness", "Evening", "Night", "Health")

    val filteredHabits = if (selectedCategory == "All") {
        habitsWithStatus
    } else {
        habitsWithStatus.filter { it.habit.category.equals(selectedCategory, ignoreCase = true) }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("home_screen_list"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Dashboard Hero Progress Card
            item {
                DailyProgressCard(
                    todayDateStr = todayDateStr,
                    completedCount = completedCount,
                    totalCount = totalCount,
                    currentStreak = streakStats.first,
                    xp = userProfile.xp,
                    level = userProfile.level
                )
            }

            // 2. Mini Weekly Summary Chart
            item {
                WeeklyBarChart(
                    weeklyProgress = weeklyProgress,
                    todayDateStr = todayDateStr
                )
            }

            // 3. Category Filter Chips
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Schedule",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$completedCount/$totalCount done",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { category ->
                            val isSelected = category == selectedCategory
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectCategory(category) },
                                label = { Text(category, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }

            // 4. Routine Habits List
            if (filteredHabits.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircleOutline,
                            contentDescription = "No habits",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (selectedCategory == "All") "No habits scheduled" else "No $selectedCategory habits",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredHabits, key = { it.habit.id }) { item ->
                    HabitItemCard(
                        habitWithStatus = item,
                        onToggleDone = { onToggleHabitDone(item.habit.id) },
                        onEdit = { onEditHabit(item.habit) },
                        onDelete = { onDeleteHabit(item.habit.id) },
                        onSnooze = { onSnoozeHabit(item.habit) },
                        onTestReminder = { onTestReminder(item.habit) }
                    )
                }
            }
        }

        // Floating Action Button to Add New Habit
        FloatingActionButton(
            onClick = onAddNewHabit,
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
                .testTag("add_habit_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add New Habit")
        }
    }
}
