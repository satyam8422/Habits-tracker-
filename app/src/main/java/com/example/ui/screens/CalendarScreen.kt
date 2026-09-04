package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Habit
import com.example.data.model.HabitCompletion
import com.example.ui.components.HabitItemCard
import com.example.ui.components.MonthlyCalendarView
import com.example.ui.viewmodel.HabitWithStatus
import com.example.util.DateUtils

@Composable
fun CalendarScreen(
    selectedDate: String,
    todayDate: String,
    completions: List<HabitCompletion>,
    habitsWithStatusForSelectedDate: List<HabitWithStatus>,
    totalActiveHabits: Int,
    onSelectDate: (String) -> Unit,
    onToggleHabitDone: (Long, String) -> Unit,
    onEditHabit: (Habit) -> Unit,
    onDeleteHabit: (Long) -> Unit,
    onSnoozeHabit: (Habit) -> Unit,
    onTestReminder: (Habit) -> Unit,
    modifier: Modifier = Modifier
) {
    val completedOnSelectedDate = habitsWithStatusForSelectedDate.count { it.isCompleted }
    val totalOnSelectedDate = habitsWithStatusForSelectedDate.size

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("calendar_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Calendar & History",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "View past consistency and day-by-day habit logs",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Monthly Interactive Calendar Grid
        item {
            MonthlyCalendarView(
                selectedDate = selectedDate,
                todayDate = todayDate,
                completions = completions,
                totalActiveHabits = totalActiveHabits,
                onSelectDate = onSelectDate
            )
        }

        // Section Title for Selected Date
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = DateUtils.formatDateForDisplay(selectedDate),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$completedOnSelectedDate/$totalOnSelectedDate completed",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Habits for that day
        items(habitsWithStatusForSelectedDate, key = { it.habit.id }) { item ->
            HabitItemCard(
                habitWithStatus = item,
                onToggleDone = { onToggleHabitDone(item.habit.id, selectedDate) },
                onEdit = { onEditHabit(item.habit) },
                onDelete = { onDeleteHabit(item.habit.id) },
                onSnooze = { onSnoozeHabit(item.habit) },
                onTestReminder = { onTestReminder(item.habit) }
            )
        }
    }
}
