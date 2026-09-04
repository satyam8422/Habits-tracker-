package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.HabitCompletionDao
import com.example.data.local.HabitDao
import com.example.data.local.UserDao
import com.example.data.model.Habit
import com.example.data.model.HabitCompletion
import com.example.data.model.UserProfile
import com.example.util.DateUtils
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class HabitRepository(
    private val habitDao: HabitDao,
    private val completionDao: HabitCompletionDao,
    private val userDao: UserDao
) {
    val activeHabits: Flow<List<Habit>> = habitDao.getAllActiveHabits()
    val allHabits: Flow<List<Habit>> = habitDao.getAllHabits()
    val allCompletions: Flow<List<HabitCompletion>> = completionDao.getAllCompletions()
    val userProfile: Flow<UserProfile?> = userDao.getUserProfile()

    fun getCompletionsForDate(date: String): Flow<List<HabitCompletion>> {
        return completionDao.getCompletionsForDate(date)
    }

    fun getCompletionsBetween(startDate: String, endDate: String): Flow<List<HabitCompletion>> {
        return completionDao.getCompletionsBetweenDates(startDate, endDate)
    }

    suspend fun toggleHabitCompletion(habitId: Long, date: String, isCurrentlyCompleted: Boolean) {
        if (isCurrentlyCompleted) {
            completionDao.deleteCompletion(habitId, date)
        } else {
            completionDao.insertCompletion(
                HabitCompletion(habitId = habitId, date = date)
            )
        }
    }

    suspend fun insertHabit(habit: Habit): Long {
        return habitDao.insertHabit(habit)
    }

    suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(habit)
    }

    suspend fun deleteHabit(habitId: Long) {
        completionDao.deleteCompletionsForHabit(habitId)
        habitDao.deleteHabitById(habitId)
    }

    suspend fun updateUserProfile(profile: UserProfile) {
        userDao.saveUserProfile(profile)
    }

    suspend fun addXp(amount: Int) {
        // Will be called when habits are completed
    }

    suspend fun exportDataAsJson(habits: List<Habit>, completions: List<HabitCompletion>): String {
        val root = JSONObject()
        val habitsArray = JSONArray()
        for (h in habits) {
            val hObj = JSONObject()
            hObj.put("id", h.id)
            hObj.put("title", h.title)
            hObj.put("emoji", h.emoji)
            hObj.put("time", h.time)
            hObj.put("category", h.category)
            hObj.put("frequencyType", h.frequencyType)
            hObj.put("reminderEnabled", h.reminderEnabled)
            hObj.put("reminderTime", h.reminderTime)
            hObj.put("colorHex", h.colorHex)
            habitsArray.put(hObj)
        }
        root.put("habits", habitsArray)

        val compArray = JSONArray()
        for (c in completions) {
            val cObj = JSONObject()
            cObj.put("habitId", c.habitId)
            cObj.put("date", c.date)
            compArray.put(cObj)
        }
        root.put("completions", compArray)
        root.put("exportDate", DateUtils.getTodayDateString())
        return root.toString(2)
    }
}
