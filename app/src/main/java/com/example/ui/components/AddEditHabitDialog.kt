package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Habit

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditHabitDialog(
    habitToEdit: Habit?,
    onDismiss: () -> Unit,
    onSave: (Habit) -> Unit
) {
    val isEdit = habitToEdit != null

    var title by remember { mutableStateOf(habitToEdit?.title ?: "") }
    var selectedEmoji by remember { mutableStateOf(habitToEdit?.emoji ?: "⏰") }
    var selectedTime by remember { mutableStateOf(habitToEdit?.time ?: "06:00 AM") }
    var selectedCategory by remember { mutableStateOf(habitToEdit?.category ?: "Morning") }
    var selectedFrequency by remember { mutableStateOf(habitToEdit?.frequencyType ?: "DAILY") }
    var reminderEnabled by remember { mutableStateOf(habitToEdit?.reminderEnabled ?: true) }
    var reminderTime by remember { mutableStateOf(habitToEdit?.reminderTime ?: selectedTime) }
    var selectedColorHex by remember { mutableLongStateOf(habitToEdit?.colorHex ?: 0xFF4F46E5) }

    val emojis = listOf("⏰", "🎒", "📚", "🏋️", "📖", "😴", "💧", "🧘", "🏃", "🥗", "💊", "✍️")
    val timePresets = listOf("06:00 AM", "07:00 AM", "08:00 AM", "05:00 PM", "06:30 PM", "09:00 PM", "10:00 PM")
    val categories = listOf("Morning", "College", "Study", "Fitness", "Evening", "Night", "Health", "Routine")
    val frequencies = listOf("DAILY", "WEEKDAYS", "WEEKENDS")
    val colorPresets = listOf(
        0xFF4F46E5, // Indigo
        0xFF10B981, // Emerald
        0xFFF59E0B, // Amber
        0xFFEF4444, // Red
        0xFF8B5CF6, // Purple
        0xFF06B6D4, // Cyan
        0xFFEC4899  // Pink
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEdit) "Edit Habit" else "New Habit",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Title Field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Habit Title") },
                    placeholder = { Text("e.g. Wake up, Gym, Study...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("habit_title_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Emoji Picker
                Text(
                    text = "Icon / Emoji",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    emojis.forEach { emoji ->
                        val isSelected = emoji == selectedEmoji
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedEmoji = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 20.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Time Presets
                Text(
                    text = "Routine Time",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    timePresets.forEach { timeStr ->
                        val isSelected = timeStr == selectedTime
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedTime = timeStr
                                reminderTime = timeStr
                            },
                            label = { Text(timeStr, fontSize = 11.sp) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Category
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = cat == selectedCategory
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Frequency
                Text(
                    text = "Repeat Frequency",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    frequencies.forEach { freq ->
                        val isSelected = freq == selectedFrequency
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFrequency = freq },
                            label = {
                                Text(
                                    when (freq) {
                                        "DAILY" -> "Daily"
                                        "WEEKDAYS" -> "Mon - Fri"
                                        "WEEKENDS" -> "Weekends"
                                        else -> freq
                                    },
                                    fontSize = 11.sp
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Reminder Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Reminder",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reminder Alarm",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                    }
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { reminderEnabled = it }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Color tint
                Text(
                    text = "Color Theme",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    colorPresets.forEach { colorVal ->
                        val isSelected = colorVal == selectedColorHex
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(colorVal))
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorHex = colorVal }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val habit = Habit(
                            id = habitToEdit?.id ?: 0L,
                            title = title.trim(),
                            emoji = selectedEmoji,
                            time = selectedTime,
                            category = selectedCategory,
                            frequencyType = selectedFrequency,
                            reminderEnabled = reminderEnabled,
                            reminderTime = reminderTime,
                            colorHex = selectedColorHex,
                            createdAt = habitToEdit?.createdAt ?: System.currentTimeMillis(),
                            isActive = true
                        )
                        onSave(habit)
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("save_habit_dialog_btn")
            ) {
                Text(if (isEdit) "Save Changes" else "Add Habit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
