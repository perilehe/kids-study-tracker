package com.kidsstudy.tracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kidsstudy.tracker.data.CheckIn
import com.kidsstudy.tracker.data.Task
import com.kidsstudy.tracker.data.User
import com.kidsstudy.tracker.ui.components.*
import com.kidsstudy.tracker.ui.theme.*
import com.kidsstudy.tracker.viewmodel.MainViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: MainViewModel
) {
    val recentCheckIns by viewModel.recentCheckIns.collectAsState()
    val tasks by viewModel.allTasks.collectAsState()
    val user by viewModel.currentUser.collectAsState()

    // 按日期分组
    val checkInsByDate = recentCheckIns.groupBy { it.date }
    val sortedDates = checkInsByDate.keys.sortedDescending()

    Scaffold { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 用户信息卡片
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3E0)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AvatarComponent(user = user, size = 60)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "${user?.name ?: "小朋友"}的学习之旅",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row {
                                PointsDisplay(points = user?.totalPoints ?: 0)
                                Spacer(modifier = Modifier.width(16.dp))
                                StreakDisplay(days = user?.streakDays ?: 0)
                            }
                        }
                    }
                }
            }

            // 统计卡片
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "总打卡",
                        value = "${recentCheckIns.size}",
                        unit = "次",
                        emoji = "✅",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "总天数",
                        value = "${sortedDates.size}",
                        unit = "天",
                        emoji = "📅",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "任务数",
                        value = "${tasks.size}",
                        unit = "个",
                        emoji = "📋",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 学习日记标题
            item {
                Text(
                    text = "📖 学习日记",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // 空状态
            if (recentCheckIns.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE3F2FD)
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
                                text = "还没有打卡记录",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "完成第一个任务后，你的学习日记就会出现在这里",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextHint
                            )
                        }
                    }
                }
            }

            // 按日期分组显示
            sortedDates.forEach { date ->
                val dayCheckIns = checkInsByDate[date] ?: emptyList()
                val displayDate = formatDisplayDate(date)

                item {
                    Text(
                        text = "📅 $displayDate",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(dayCheckIns, key = { it.id }) { checkIn ->
                    val task = tasks.find { it.id == checkIn.taskId }
                    CheckInCard(checkIn = checkIn, task = task)
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    unit: String,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = VibrantOrange
                )
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun CheckInCard(
    checkIn: CheckIn,
    task: Task?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (task != null) {
                TaskIconBox(task = task, size = 48)
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task?.name ?: "未知任务",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                if (checkIn.duration != null) {
                    Text(
                        text = "${checkIn.duration}分钟",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                if (!checkIn.note.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF9E6)
                        )
                    ) {
                        Text(
                            text = "📝 ${checkIn.note}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }

            // 评分
            if (checkIn.mood != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "⭐".repeat(checkIn.mood),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

private fun formatDisplayDate(dateStr: String): String {
    return try {
        val date = LocalDate.parse(dateStr)
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        when (date) {
            today -> "今天"
            yesterday -> "昨天"
            else -> date.format(DateTimeFormatter.ofPattern("M月d日 EEEE"))
        }
    } catch (e: Exception) {
        dateStr
    }
}
