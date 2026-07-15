package com.kidsstudy.tracker.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsstudy.tracker.data.NoteMode
import com.kidsstudy.tracker.data.Task
import com.kidsstudy.tracker.ui.components.TaskIconBox
import com.kidsstudy.tracker.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInNoteScreen(
    task: Task,
    points: Int,
    onComplete: (note: String?, mood: Int?, answers: String?, noteMode: NoteMode) -> Unit,
    onSkip: () -> Unit,
    onBack: () -> Unit
) {
    var note by remember { mutableStateOf("") }
    var mood by remember { mutableStateOf(3) }
    var showSuccess by remember { mutableStateOf(false) }

    if (showSuccess) {
        // 完成动画
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(1500)
            onComplete(note.ifBlank { null }, mood, null, NoteMode.FREE_TEXT)
        }
    }

    if (showSuccess) {
        // 成功动画
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFF9E6)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎉",
                    fontSize = 80.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "太棒了！",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = VibrantOrange
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "+$points 积分",
                    style = MaterialTheme.typography.headlineMedium,
                    color = SunnyYellow
                )
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("打卡笔记") },
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 完成提示
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8F5E9)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "✨ 完成啦！",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TaskIconBox(task = task, size = 48)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = task.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${task.duration}分钟 · +$points⭐",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 笔记输入
            Text(
                text = "📝 写一句今日心得吧～",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = note,
                onValueChange = { if (it.length <= 100) note = it },
                placeholder = { Text("今天学了什么？有什么收获？") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VibrantOrange,
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                )
            )

            Text(
                text = "${note.length}/100",
                style = MaterialTheme.typography.bodySmall,
                color = TextHint,
                modifier = Modifier.align(Alignment.End)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 心情评分
            Text(
                text = "😊 感觉怎么样？",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                (1..5).forEach { star ->
                    Text(
                        text = if (star <= mood) "⭐" else "☆",
                        fontSize = 36.sp,
                        modifier = Modifier
                            .clickable { mood = star }
                            .padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 完成按钮
            Button(
                onClick = { showSuccess = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VibrantOrange
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "✅ 完成打卡",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 跳过按钮
            TextButton(
                onClick = onSkip,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = TextSecondary
                )
            ) {
                Text("跳过")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
