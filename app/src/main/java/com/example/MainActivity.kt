package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AddEditHabitDialog
import com.example.ui.navigation.Screen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.HabitsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.theme.HabitTrackerTheme
import com.example.ui.viewmodel.HabitViewModel
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.createNotificationChannel(this)

        setContent {
            val viewModel: HabitViewModel = viewModel()
            val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

            val isDarkTheme = when (userProfile.isDarkMode) {
                true -> true
                false -> false
                null -> isSystemInDarkTheme()
            }

            HabitTrackerTheme(darkTheme = isDarkTheme) {
                HabitApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun HabitApp(viewModel: HabitViewModel) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    val snackbarHostState = remember { SnackbarHostState() }

    val todayDate by viewModel.todayDate.collectAsStateWithLifecycle()
    val selectedCalendarDate by viewModel.selectedCalendarDate.collectAsStateWithLifecycle()
    val categoryFilter by viewModel.categoryFilter.collectAsStateWithLifecycle()
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val completions by viewModel.completions.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val todayHabitsWithStatus by viewModel.todayHabitsWithStatus.collectAsStateWithLifecycle()
    val selectedDateHabitsWithStatus by viewModel.selectedDateHabitsWithStatus.collectAsStateWithLifecycle()
    val streakStats by viewModel.streakStats.collectAsStateWithLifecycle()
    val weeklyProgress by viewModel.weeklyProgress.collectAsStateWithLifecycle()
    val badges by viewModel.badges.collectAsStateWithLifecycle()

    val isAddEditDialogOpen by viewModel.isAddEditDialogOpen.collectAsStateWithLifecycle()
    val editingHabit by viewModel.editingHabit.collectAsStateWithLifecycle()

    // Listen for toast/feedback events
    LaunchedEffect(Unit) {
        viewModel.toastEvent.collectLatest { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                Screen.bottomNavItems.forEach { screen ->
                    val isSelected = currentScreen.route == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.title
                            )
                        },
                        label = { Text(screen.title) },
                        modifier = Modifier.testTag("nav_item_${screen.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Crossfade(
            targetState = currentScreen,
            animationSpec = tween(durationMillis = 250),
            modifier = Modifier.padding(innerPadding),
            label = "screen_crossfade"
        ) { screen ->
            when (screen) {
                Screen.Home -> HomeScreen(
                    todayDateStr = todayDate,
                    habitsWithStatus = todayHabitsWithStatus,
                    userProfile = userProfile,
                    streakStats = streakStats,
                    weeklyProgress = weeklyProgress,
                    selectedCategory = categoryFilter,
                    onSelectCategory = { viewModel.setCategoryFilter(it) },
                    onToggleHabitDone = { habitId -> viewModel.toggleHabit(habitId) },
                    onEditHabit = { habit -> viewModel.openEditHabitDialog(habit) },
                    onDeleteHabit = { habitId -> viewModel.deleteHabit(habitId) },
                    onSnoozeHabit = { habit -> viewModel.snoozeHabit(habit) },
                    onTestReminder = { habit -> viewModel.triggerTestReminder(context, habit) },
                    onAddNewHabit = { viewModel.openAddHabitDialog() }
                )

                Screen.Habits -> HabitsScreen(
                    habitsWithStatus = todayHabitsWithStatus,
                    selectedCategory = categoryFilter,
                    onSelectCategory = { viewModel.setCategoryFilter(it) },
                    onAddNewHabit = { viewModel.openAddHabitDialog() },
                    onToggleHabitDone = { habitId -> viewModel.toggleHabit(habitId) },
                    onEditHabit = { habit -> viewModel.openEditHabitDialog(habit) },
                    onDeleteHabit = { habitId -> viewModel.deleteHabit(habitId) },
                    onSnoozeHabit = { habit -> viewModel.snoozeHabit(habit) },
                    onTestReminder = { habit -> viewModel.triggerTestReminder(context, habit) }
                )

                Screen.Calendar -> CalendarScreen(
                    selectedDate = selectedCalendarDate,
                    todayDate = todayDate,
                    completions = completions,
                    habitsWithStatusForSelectedDate = selectedDateHabitsWithStatus,
                    totalActiveHabits = habits.size,
                    onSelectDate = { viewModel.setSelectedCalendarDate(it) },
                    onToggleHabitDone = { habitId, date -> viewModel.toggleHabit(habitId, date) },
                    onEditHabit = { habit -> viewModel.openEditHabitDialog(habit) },
                    onDeleteHabit = { habitId -> viewModel.deleteHabit(habitId) },
                    onSnoozeHabit = { habit -> viewModel.snoozeHabit(habit) },
                    onTestReminder = { habit -> viewModel.triggerTestReminder(context, habit) }
                )

                Screen.Statistics -> StatisticsScreen(
                    habits = habits,
                    completions = completions,
                    streakStats = streakStats,
                    weeklyProgress = weeklyProgress,
                    userProfile = userProfile,
                    todayDateStr = todayDate
                )

                Screen.Profile -> ProfileScreen(
                    userProfile = userProfile,
                    badges = badges,
                    onSetDarkMode = { viewModel.setDarkMode(it) },
                    onSetNotifications = { viewModel.setNotificationsEnabled(it) },
                    onExportData = { onResult -> viewModel.exportData(onResult) }
                )
            }
        }

        // Add / Edit Habit Dialog
        if (isAddEditDialogOpen) {
            AddEditHabitDialog(
                habitToEdit = editingHabit,
                onDismiss = { viewModel.closeAddEditDialog() },
                onSave = { habit -> viewModel.saveHabit(habit) }
            )
        }
    }
}
