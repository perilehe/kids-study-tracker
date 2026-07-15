package com.kidsstudy.tracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kidsstudy.tracker.data.Task
import com.kidsstudy.tracker.data.User
import com.kidsstudy.tracker.ui.components.*
import com.kidsstudy.tracker.ui.theme.*
import com.kidsstudy.tracker.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onTaskClick: (Task) -> Unit,
    onAddTaskClick: () -> Unit,
    onAvatarClick: () -> Unit
) {
    val tasks by viewModel.activeTasks.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val todayCheckIns by viewModel.todayCheckIns.collectAsState()

    val completedCount = todayCheckIns.size
    val totalCount = tasks.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTaskClick,
                containerColor = VibrantOrange,
                contentColor = kotlinx.compose.ui.graphics.Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加任务")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 欢迎卡片
            item {
                WelcomeCard(
                    user = user,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 积分和连续打卡
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PointsDisplay(points = user?.totalPoints ?: 0)
                    StreakDisplay(days = user?.streakDays ?: 0)
                }
            }

            // 今日进度
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CardBackground
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "今日进度",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "$completedCount/$totalCount",
                                style = MaterialTheme.typography.titleMedium,
                                color = VibrantOrange,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        GradientProgressBar(progress = progress)
                    }
                }
            }

            // 任务列表标题
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "今日任务",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (tasks.isEmpty()) {
                        TextButton(onClick = onAddTaskClick) {
                            Text("添加第一个任务")
                        }
                    }
                }
            }

            // 空状态
            if (tasks.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF3E0)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "📝",
                                style = MaterialTheme.typography.displayLarge
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "还没有任务哦",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "点击下方 + 按钮添加你的第一个学习任务",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextHint
                            )
                        }
                    }
                }
            }

            // 任务卡片列表
            items(tasks, key = { it.id }) { task ->
                val isCompleted = todayCheckIns.any { it.taskId == task.id }
                TaskCard(
                    task = task,
                    isCompleted = isCompleted,
                    streakDays = if (isCompleted) user?.streakDays ?: 0 else 0,
                    onClick = { onTaskClick(task) }
                )
            }

            // 底部间距
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
