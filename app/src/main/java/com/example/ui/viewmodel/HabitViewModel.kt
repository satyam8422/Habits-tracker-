package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Habit
import com.example.data.model.HabitCompletion
import com.example.data.model.UserProfile
import com.example.data.repository.HabitRepository
import com.example.util.DateUtils
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HabitWithStatus(
    val habit: Habit,
    val isCompleted: Boolean
)

data class DayProgress(
    val dayShort: String,
    val dateStr: String,
    val completedCount: Int,
    val totalCount: Int,
    val percentage: Float
)

data class Badge(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val isUnlocked: Boolean,
    val requiredProgress: Int,
    val currentProgress: Int
)

class HabitViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = HabitRepository(
        database.habitDao(),
        database.habitCompletionDao(),
        database.userDao()
    )

    private val _todayDate = MutableStateFlow(DateUtils.getTodayDateString())
    val todayDate: StateFlow<String> = _todayDate.asStateFlow()

    private val _selectedCalendarDate = MutableStateFlow(DateUtils.getTodayDateString())
    val selectedCalendarDate: StateFlow<String> = _selectedCalendarDate.asStateFlow()

    private val _categoryFilter = MutableStateFlow("All")
    val categoryFilter: StateFlow<String> = _categoryFilter.asStateFlow()

    // Dialog & UI transient states
    private val _editingHabit = MutableStateFlow<Habit?>(null)
    val editingHabit: StateFlow<Habit?> = _editingHabit.asStateFlow()

    private val _isAddEditDialogOpen = MutableStateFlow(false)
    val isAddEditDialogOpen: StateFlow<Boolean> = _isAddEditDialogOpen.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    val habits: StateFlow<List<Habit>> = repository.activeHabits.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val completions: StateFlow<List<HabitCompletion>> = repository.allCompletions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val userProfile: StateFlow<UserProfile> = repository.userProfile.combine(
        MutableStateFlow(UserProfile())
    ) { profile, default ->
        profile ?: default
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserProfile()
    )

    init {
        // Ensure initial database population if empty
        viewModelScope.launch {
            val habitList = database.habitDao().getHabitsOnce()
            if (habitList.isEmpty()) {
                AppDatabase.populateInitialData(database)
            }
        }
    }

    // Today's habits combined with today's completions
    val todayHabitsWithStatus: StateFlow<List<HabitWithStatus>> = combine(
        habits,
        completions,
        _todayDate
    ) { habitList, compList, today ->
        val todayCompIds = compList.filter { it.date == today }.map { it.habitId }.toSet()
        habitList.map { habit ->
            HabitWithStatus(
                habit = habit,
                isCompleted = todayCompIds.contains(habit.id)
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Completions for selected calendar date
    val selectedDateHabitsWithStatus: StateFlow<List<HabitWithStatus>> = combine(
        habits,
        completions,
        _selectedCalendarDate
    ) { habitList, compList, date ->
        val dateCompIds = compList.filter { it.date == date }.map { it.habitId }.toSet()
        habitList.map { habit ->
            HabitWithStatus(
                habit = habit,
                isCompleted = dateCompIds.contains(habit.id)
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Current Streak calculation
    val streakStats: StateFlow<Pair<Int, Int>> = combine(
        completions,
        userProfile
    ) { compList, profile ->
        calculateStreaks(compList, profile.bestStreak)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Pair(5, 12) // Current streak, best streak fallback
    )

    // 7 Days Weekly Progress
    val weeklyProgress: StateFlow<List<DayProgress>> = combine(
        habits,
        completions
    ) { habitList, compList ->
        val days = DateUtils.getPastDaysList(7)
        val totalActive = habitList.size.coerceAtLeast(1)
        days.map { dayStr ->
            val count = compList.count { it.date == dayStr }
            DayProgress(
                dayShort = DateUtils.getDayOfWeekShort(dayStr),
                dateStr = dayStr,
                completedCount = count,
                totalCount = totalActive,
                percentage = (count.toFloat() / totalActive.toFloat()).coerceIn(0f, 1f)
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Badges calculation
    val badges: StateFlow<List<Badge>> = combine(
        completions,
        streakStats,
        habits
    ) { compList, streaks, habitList ->
        val currentStreak = streaks.first
        val totalCompleted = compList.size

        val wakeUpCompletions = compList.count { comp ->
            habitList.find { it.id == comp.habitId }?.title?.contains("Wake", ignoreCase = true) == true
        }
        val gymCompletions = compList.count { comp ->
            habitList.find { it.id == comp.habitId }?.title?.contains("Gym", ignoreCase = true) == true
        }
        val studyCompletions = compList.count { comp ->
            habitList.find { it.id == comp.habitId }?.title?.contains("Study", ignoreCase = true) == true
        }

        listOf(
            Badge(
                id = "spark_3",
                title = "3-Day Spark",
                description = "Maintain a 3-day routine streak",
                iconEmoji = "🔥",
                isUnlocked = currentStreak >= 3,
                requiredProgress = 3,
                currentProgress = currentStreak.coerceAtMost(3)
            ),
            Badge(
                id = "flame_7",
                title = "7-Day Flame",
                description = "Complete 7 days consecutively",
                iconEmoji = "⚡",
                isUnlocked = currentStreak >= 7,
                requiredProgress = 7,
                currentProgress = currentStreak.coerceAtMost(7)
            ),
            Badge(
                id = "early_bird",
                title = "Early Bird",
                description = "Wake up on time 5 times",
                iconEmoji = "🌅",
                isUnlocked = wakeUpCompletions >= 5,
                requiredProgress = 5,
                currentProgress = wakeUpCompletions.coerceAtMost(5)
            ),
            Badge(
                id = "fitness_beast",
                title = "Iron Beast",
                description = "Hit the gym 3 times",
                iconEmoji = "🏋️",
                isUnlocked = gymCompletions >= 3,
                requiredProgress = 3,
                currentProgress = gymCompletions.coerceAtMost(3)
            ),
            Badge(
                id = "scholar",
                title = "Scholar",
                description = "Complete study sessions 5 times",
                iconEmoji = "📚",
                isUnlocked = studyCompletions >= 5,
                requiredProgress = 5,
                currentProgress = studyCompletions.coerceAtMost(5)
            ),
            Badge(
                id = "century_club",
                title = "Century Club",
                description = "Complete 50 total habit checkpoints",
                iconEmoji = "🏅",
                isUnlocked = totalCompleted >= 50,
                requiredProgress = 50,
                currentProgress = totalCompleted.coerceAtMost(50)
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun toggleHabit(habitId: Long, date: String = _todayDate.value) {
        viewModelScope.launch {
            val isCurrentlyCompleted = completions.value.any { it.habitId == habitId && it.date == date }
            repository.toggleHabitCompletion(habitId, date, isCurrentlyCompleted)

            // Reward XP on completion
            if (!isCurrentlyCompleted) {
                val currentProfile = userProfile.value
                val newXp = currentProfile.xp + 15
                val newLevel = (newXp / 150) + 1
                repository.updateUserProfile(
                    currentProfile.copy(xp = newXp, level = newLevel)
                )
                _toastEvent.emit("+15 XP earned! Keep going 🔥")
            }
        }
    }

    fun openAddHabitDialog() {
        _editingHabit.value = null
        _isAddEditDialogOpen.value = true
    }

    fun openEditHabitDialog(habit: Habit) {
        _editingHabit.value = habit
        _isAddEditDialogOpen.value = true
    }

    fun closeAddEditDialog() {
        _isAddEditDialogOpen.value = false
        _editingHabit.value = null
    }

    fun saveHabit(habit: Habit) {
        viewModelScope.launch {
            if (habit.id == 0L) {
                repository.insertHabit(habit)
                _toastEvent.emit("New habit '${habit.title}' added!")
            } else {
                repository.updateHabit(habit)
                _toastEvent.emit("Habit '${habit.title}' updated!")
            }
            closeAddEditDialog()
        }
    }

    fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            repository.deleteHabit(habitId)
            _toastEvent.emit("Habit removed.")
        }
    }

    fun snoozeHabit(habit: Habit) {
        viewModelScope.launch {
            _toastEvent.emit("⏰ Snoozed '${habit.title}' for 10 minutes.")
        }
    }

    fun triggerTestReminder(context: Context, habit: Habit) {
        NotificationHelper.showHabitNotification(
            context = context,
            habitId = habit.id,
            title = habit.title,
            time = habit.reminderTime,
            emoji = habit.emoji
        )
        viewModelScope.launch {
            _toastEvent.emit("🔔 Notification sent for ${habit.title}")
        }
    }

    fun setSelectedCalendarDate(date: String) {
        _selectedCalendarDate.value = date
    }

    fun setCategoryFilter(category: String) {
        _categoryFilter.value = category
    }

    fun setDarkMode(isDark: Boolean?) {
        viewModelScope.launch {
            repository.updateUserProfile(userProfile.value.copy(isDarkMode = isDark))
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateUserProfile(userProfile.value.copy(notificationsEnabled = enabled))
        }
    }

    fun exportData(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportDataAsJson(habits.value, completions.value)
            onResult(json)
        }
    }

    private fun calculateStreaks(compList: List<HabitCompletion>, recordedBest: Int): Pair<Int, Int> {
        val completedDates = compList.map { it.date }.toSet()
        if (completedDates.isEmpty()) return Pair(0, recordedBest)

        var currentStreak = 0
        var checkOffset = 0
        val todayStr = DateUtils.getTodayDateString()

        // If today has completion, count from today; else if yesterday has completion, streak is still active!
        if (completedDates.contains(todayStr)) {
            currentStreak++
            checkOffset = -1
        } else {
            val yesterdayStr = DateUtils.getDateOffsetDays(-1)
            if (completedDates.contains(yesterdayStr)) {
                checkOffset = -1
            } else {
                return Pair(0, recordedBest)
            }
        }

        while (true) {
            val checkDate = DateUtils.getDateOffsetDays(checkOffset)
            if (completedDates.contains(checkDate)) {
                currentStreak++
                checkOffset--
            } else {
                break
            }
        }

        val bestStreak = maxOf(currentStreak, recordedBest)
        return Pair(currentStreak, bestStreak)
    }
}
