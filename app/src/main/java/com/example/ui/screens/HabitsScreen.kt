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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
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
import com.example.ui.components.HabitItemCard
import com.example.ui.viewmodel.HabitWithStatus

@Composable
fun HabitsScreen(
    habitsWithStatus: List<HabitWithStatus>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    onAddNewHabit: () -> Unit,
    onToggleHabitDone: (Long) -> Unit,
    onEditHabit: (Habit) -> Unit,
    onDeleteHabit: (Long) -> Unit,
    onSnoozeHabit: (Habit) -> Unit,
    onTestReminder: (Habit) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf("All", "Morning", "College", "Study", "Fitness", "Evening", "Night", "Health")

    val filteredHabits = if (selectedCategory == "All") {
        habitsWithStatus
    } else {
        habitsWithStatus.filter { it.habit.category.equals(selectedCategory, ignoreCase = true) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("habits_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Habit Management",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Configure your daily routines, reminders, and timing",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Add Habit Action Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Add a New Habit",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Choose routine, category, frequency & alarms",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    Button(
                        onClick = onAddNewHabit,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("create_habit_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.size(4.dp))
                        Text("Add")
                    }
                }
            }
        }

        // Categories Row
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = cat == selectedCategory,
                        onClick = { onSelectCategory(cat) },
                        label = { Text(cat, fontSize = 12.sp) }
                    )
                }
            }
        }

        // List of Habits
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
