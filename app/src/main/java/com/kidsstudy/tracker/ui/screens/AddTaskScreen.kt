package com.kidsstudy.tracker.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsstudy.tracker.data.Frequency
import com.kidsstudy.tracker.data.IconType
import com.kidsstudy.tracker.data.Task
import com.kidsstudy.tracker.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTaskScreen(
    task: Task? = null,
    onSave: (
        name: String,
        description: String?,
        duration: Int,
        frequency: Frequency,
        points: Int,
        weekDays: String?,
        iconType: IconType,
        iconEmoji: String?,
        iconImageUri: String?,
        iconColor: String?
    ) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf(task?.name ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var duration by remember { mutableStateOf(task?.duration?.toString() ?: "30") }
    var points by remember { mutableStateOf(task?.points?.toString() ?: "10") }
    var frequency by remember { mutableStateOf(task?.frequency ?: Frequency.DAILY) }
    var selectedEmoji by remember { mutableStateOf(task?.iconEmoji ?: "📖") }
    var selectedColorIndex by remember { mutableStateOf(0) }

    // 预设emoji
    val emojis = listOf(
        "📖", "📚", "📝", "✏️", "📐", "🔬", "🧮",
        "🎹", "🎸", "🎨", "🎭", "🏀", "⚽", "🏃",
        "💻", "🎮", "🧩", "🎯", "🎪", "🎢", "🚀",
        "🦁", "🐯", "🦊", "🐼", "🦄", "🐉", "🦅"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (task == null) "新建任务" else "编辑任务") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 图标选择
            Text(
                text = "选择图标",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // 当前选中预览
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(CardColors[selectedColorIndex]),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = selectedEmoji, fontSize = 32.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Emoji网格
                    Text(text = "表情图标", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        emojis.forEach { emoji ->
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (emoji == selectedEmoji) VibrantOrange else Color(0xFFF5F5F5)
                                    )
                                    .clickable { selectedEmoji = emoji }
                                    .then(
                                        if (emoji == selectedEmoji) Modifier.border(
                                            2.dp,
                                            VibrantOrange,
                                            RoundedCornerShape(8.dp)
                                        ) else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 20.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 颜色选择
                    Text(text = "背景颜色", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CardColors.forEachIndexed { index, color ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable { selectedColorIndex = index }
                                    .then(
                                        if (index == selectedColorIndex) Modifier.border(
                                            3.dp,
                                            VibrantOrange,
                                            CircleShape
                                        ) else Modifier
                                    )
                            )
                        }
                    }
                }
            }

            // 任务名称
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("任务名称") },
                placeholder = { Text("例如：阅读、练琴、运动...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // 描述
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("描述（可选）") },
                placeholder = { Text("任务的详细描述...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // 时长
            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it.filter { c -> c.isDigit() } },
                label = { Text("目标时长（分钟）") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // 积分
            OutlinedTextField(
                value = points,
                onValueChange = { points = it.filter { c -> c.isDigit() } },
                label = { Text("完成积分") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // 频率
            Text(
                text = "频率",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Frequency.values().forEach { freq ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { frequency = freq }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = frequency == freq,
                                onClick = { frequency = freq },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = VibrantOrange
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (freq) {
                                    Frequency.DAILY -> "每天"
                                    Frequency.WEEKLY_3 -> "每周3次"
                                    Frequency.WEEKLY_5 -> "每周5次"
                                    Frequency.CUSTOM -> "自定义"
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 保存按钮
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            name = name,
                            description = description.ifBlank { null },
                            duration = duration.toIntOrNull() ?: 30,
                            frequency = frequency,
                            points = points.toIntOrNull() ?: 10,
                            weekDays = null,
                            iconType = IconType.EMOJI,
                            iconEmoji = selectedEmoji,
                            iconImageUri = null,
                            iconColor = null
                        )
                        onBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VibrantOrange
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "保存",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
