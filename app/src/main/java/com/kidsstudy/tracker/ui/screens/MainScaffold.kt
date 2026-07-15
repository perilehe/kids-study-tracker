package com.kidsstudy.tracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kidsstudy.tracker.data.Task
import com.kidsstudy.tracker.navigation.Screen
import com.kidsstudy.tracker.ui.theme.*
import com.kidsstudy.tracker.viewmodel.MainViewModel

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Home : BottomNavItem(Screen.Home.route, Icons.Default.Home, "首页")
    object Stats : BottomNavItem(Screen.Stats.route, Icons.Default.BarChart, "统计")
    object Rewards : BottomNavItem(Screen.Rewards.route, Icons.Default.CardGiftcard, "奖励")
    object Settings : BottomNavItem(Screen.Settings.route, Icons.Default.Settings, "设置")
}

@Composable
fun MainScaffold(
    viewModel: MainViewModel,
    navController: NavController
) {
    val bottomNavItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Stats,
        BottomNavItem.Rewards,
        BottomNavItem.Settings
    )

    var showCheckInNote by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<Task?>(null) }

    // 内部导航
    var currentRoute by remember { mutableStateOf(Screen.Home.route) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            currentRoute = item.route
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = VibrantOrange,
                            selectedTextColor = VibrantOrange,
                            indicatorColor = VibrantOrange.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentRoute) {
                Screen.Home.route -> HomeScreen(
                    viewModel = viewModel,
                    onTaskClick = { task ->
                        if (!viewModel.isTaskCompletedToday(task.id)) {
                            selectedTask = task
                            showCheckInNote = true
                        }
                    },
                    onAddTaskClick = {
                        // TODO: 导航到添加任务
                    },
                    onAvatarClick = {
                        // TODO: 导航到编辑头像
                    }
                )
                Screen.Stats.route -> StatsScreen(viewModel = viewModel)
                Screen.Rewards.route -> RewardsScreen(
                    viewModel = viewModel,
                    onRewardClick = { reward ->
                        viewModel.redeemReward(reward)
                    },
                    onAddRewardClick = {
                        // TODO: 导航到添加奖励
                    },
                    onMyRewardsClick = {
                        // TODO: 导航到我的奖励
                    }
                )
                Screen.Settings.route -> SettingsScreen(
                    viewModel = viewModel,
                    onEditAvatarClick = {
                        // TODO: 导航到编辑头像
                    },
                    onEditTaskClick = { task ->
                        // TODO: 导航到编辑任务
                    },
                    onAddTaskClick = {
                        // TODO: 导航到添加任务
                    },
                    onEditRewardClick = {
                        // TODO: 导航到管理奖励
                    }
                )
            }
        }
    }

    // 打卡笔记弹窗
    if (showCheckInNote && selectedTask != null) {
        CheckInNoteScreen(
            task = selectedTask!!,
            points = selectedTask!!.points,
            onComplete = { note, mood, answers, mode ->
                viewModel.checkInTask(
                    taskId = selectedTask!!.id,
                    duration = selectedTask!!.duration,
                    note = note,
                    mood = mood,
                    answers = answers,
                    noteMode = mode
                )
                showCheckInNote = false
                selectedTask = null
            },
            onSkip = {
                viewModel.checkInTask(
                    taskId = selectedTask!!.id,
                    duration = selectedTask!!.duration,
                    note = null,
                    mood = null,
                    answers = null,
                    noteMode = com.kidsstudy.tracker.data.NoteMode.NONE
                )
                showCheckInNote = false
                selectedTask = null
            },
            onBack = {
                showCheckInNote = false
                selectedTask = null
            }
        )
    }
}
