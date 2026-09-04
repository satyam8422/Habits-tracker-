package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Habit
import com.example.data.model.HabitCompletion
import com.example.data.model.UserProfile
import com.example.util.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Habit::class, HabitCompletion::class, UserProfile::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitCompletionDao(): HabitCompletionDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habit_routine_database"
                )
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val habitDao = database.habitDao()
            val completionDao = database.habitCompletionDao()
            val userDao = database.userDao()

            // Routine habits from user prompt
            val defaultHabits = listOf(
                Habit(
                    id = 1,
                    title = "Wake up",
                    emoji = "⏰",
                    time = "06:00 AM",
                    category = "Morning",
                    frequencyType = "DAILY",
                    reminderEnabled = true,
                    reminderTime = "06:00 AM",
                    colorHex = 0xFFF59E0B
                ),
                Habit(
                    id = 2,
                    title = "College",
                    emoji = "🎒",
                    time = "07:00 AM",
                    category = "College",
                    frequencyType = "WEEKDAYS",
                    reminderEnabled = true,
                    reminderTime = "06:45 AM",
                    colorHex = 0xFF3B82F6
                ),
                Habit(
                    id = 3,
                    title = "Study",
                    emoji = "📚",
                    time = "05:00 PM",
                    category = "Study",
                    frequencyType = "DAILY",
                    reminderEnabled = true,
                    reminderTime = "04:55 PM",
                    colorHex = 0xFF8B5CF6
                ),
                Habit(
                    id = 4,
                    title = "Gym",
                    emoji = "🏋️",
                    time = "06:30 PM",
                    category = "Fitness",
                    frequencyType = "DAILY",
                    reminderEnabled = true,
                    reminderTime = "06:15 PM",
                    colorHex = 0xFFEF4444
                ),
                Habit(
                    id = 5,
                    title = "Reading",
                    emoji = "📖",
                    time = "09:00 PM",
                    category = "Evening",
                    frequencyType = "DAILY",
                    reminderEnabled = true,
                    reminderTime = "08:50 PM",
                    colorHex = 0xFF10B981
                ),
                Habit(
                    id = 6,
                    title = "Sleep",
                    emoji = "😴",
                    time = "10:00 PM",
                    category = "Night",
                    frequencyType = "DAILY",
                    reminderEnabled = true,
                    reminderTime = "09:45 PM",
                    colorHex = 0xFF6366F1
                ),
                Habit(
                    id = 7,
                    title = "Drink Water",
                    emoji = "💧",
                    time = "08:00 AM",
                    category = "Health",
                    frequencyType = "DAILY",
                    reminderEnabled = true,
                    reminderTime = "08:00 AM",
                    colorHex = 0xFF06B6D4
                )
            )

            habitDao.insertHabits(defaultHabits)

            // Seed prior days completions to provide realistic streak & weekly charts
            val completions = mutableListOf<HabitCompletion>()
            val today = DateUtils.getTodayDateString()

            // Yesterday (-1) - 6 habits completed
            val day1 = DateUtils.getDateOffsetDays(-1)
            listOf(1L, 2L, 3L, 4L, 5L, 7L).forEach { habitId ->
                completions.add(HabitCompletion(habitId = habitId, date = day1))
            }

            // -2 days - 5 habits completed
            val day2 = DateUtils.getDateOffsetDays(-2)
            listOf(1L, 2L, 3L, 4L, 6L).forEach { habitId ->
                completions.add(HabitCompletion(habitId = habitId, date = day2))
            }

            // -3 days - 6 habits completed
            val day3 = DateUtils.getDateOffsetDays(-3)
            listOf(1L, 2L, 4L, 5L, 6L, 7L).forEach { habitId ->
                completions.add(HabitCompletion(habitId = habitId, date = day3))
            }

            // -4 days - 4 habits completed
            val day4 = DateUtils.getDateOffsetDays(-4)
            listOf(1L, 3L, 5L, 7L).forEach { habitId ->
                completions.add(HabitCompletion(habitId = habitId, date = day4))
            }

            // -5 days - 5 habits completed
            val day5 = DateUtils.getDateOffsetDays(-5)
            listOf(1L, 2L, 4L, 5L, 7L).forEach { habitId ->
                completions.add(HabitCompletion(habitId = habitId, date = day5))
            }

            // Today: 3 habits completed so far
            listOf(1L, 2L, 7L).forEach { habitId ->
                completions.add(HabitCompletion(habitId = habitId, date = today))
            }

            completionDao.insertCompletions(completions)

            // Initial User profile with points & level
            userDao.saveUserProfile(
                UserProfile(
                    id = 1,
                    name = "Satyam Jaiswar",
                    xp = 420,
                    level = 3,
                    dailyGoalCount = 5,
                    weeklyGoalPercent = 80,
                    isDarkMode = null,
                    notificationsEnabled = true,
                    bestStreak = 12
                )
            )
        }
    }
}
