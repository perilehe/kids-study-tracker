package com.kidsstudy.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kidsstudy.tracker.data.Task
import com.kidsstudy.tracker.navigation.Screen
import com.kidsstudy.tracker.ui.screens.*
import com.kidsstudy.tracker.ui.theme.KidsStudyTheme
import com.kidsstudy.tracker.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KidsStudyTheme {
                val viewModel: MainViewModel = viewModel()
                val navController = rememberNavController()
                var currentTask by remember { mutableStateOf<Task?>(null) }

                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route
                ) {
                    composable(Screen.Home.route) {
                        MainScaffold(
                            viewModel = viewModel,
                            navController = navController
                        )
                    }

                    composable(Screen.AddTask.route) {
                        AddTaskScreen(
                            onSave = { name, desc, duration, freq, points, weekDays, iconType, emoji, uri, color ->
                                viewModel.addTask(
                                    name = name,
                                    description = desc,
                                    duration = duration,
                                    frequency = freq,
                                    points = points,
                                    weekDays = weekDays,
                                    iconType = iconType,
                                    iconEmoji = emoji,
                                    iconImageUri = uri,
                                    iconColor = color
                                )
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = Screen.CheckInNote.route,
                        arguments = listOf(navArgument("taskId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val taskId = backStackEntry.arguments?.getLong("taskId") ?: return@composable
                        val task = viewModel.activeTasks.value.find { it.id == taskId }
                        if (task != null) {
                            CheckInNoteScreen(
                                task = task,
                                points = task.points,
                                onComplete = { note, mood, answers, mode ->
                                    viewModel.checkInTask(
                                        taskId = taskId,
                                        duration = task.duration,
                                        note = note,
                                        mood = mood,
                                        answers = answers,
                                        noteMode = mode
                                    )
                                    navController.popBackStack()
                                },
                                onSkip = {
                                    viewModel.checkInTask(
                                        taskId = taskId,
                                        duration = task.duration,
                                        note = null,
                                        mood = null,
                                        answers = null,
                                        noteMode = com.kidsstudy.tracker.data.NoteMode.NONE
                                    )
                                    navController.popBackStack()
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }

                    composable(
                        route = Screen.ScratchCard.route,
                        arguments = listOf(navArgument("rewardId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val rewardId = backStackEntry.arguments?.getLong("rewardId") ?: return@composable
                        val reward = viewModel.rewardCards.value.find { it.id == rewardId }
                        if (reward != null) {
                            ScratchCardScreen(
                                reward = reward,
                                onScratchComplete = { },
                                onClose = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
