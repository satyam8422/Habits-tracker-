package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val emoji: String = "⚡",
    val time: String = "08:00 AM",
    val category: String = "Routine", // Morning, Work, Study, Fitness, Evening, Routine
    val frequencyType: String = "DAILY", // DAILY, WEEKDAYS, WEEKENDS, CUSTOM
    val customDays: String = "1,2,3,4,5,6,7", // 1 = Mon ... 7 = Sun
    val reminderEnabled: Boolean = true,
    val reminderTime: String = "08:00 AM",
    val colorHex: Long = 0xFF4F46E5,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

@Entity(
    tableName = "habit_completions",
    primaryKeys = ["habitId", "date"]
)
data class HabitCompletion(
    val habitId: Long,
    val date: String, // Format: "yyyy-MM-dd"
    val completedAt: Long = System.currentTimeMillis(),
    val note: String = ""
)

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
    val id: Int = 1,
    val name: String = "Habit Achiever",
    val xp: Int = 240,
    val level: Int = 2,
    val dailyGoalCount: Int = 5,
    val weeklyGoalPercent: Int = 80,
    val isDarkMode: Boolean? = null, // null = follow system
    val notificationsEnabled: Boolean = true,
    val bestStreak: Int = 7
)
