package com.kidsstudy.tracker.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsstudy.tracker.data.RewardCard
import com.kidsstudy.tracker.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun ScratchCardScreen(
    reward: RewardCard,
    onScratchComplete: () -> Unit,
    onClose: () -> Unit
) {
    var scratchProgress by remember { mutableStateOf(0f) }
    var isComplete by remember { mutableStateOf(false) }
    val paths = remember { mutableStateListOf<Path>() }
    val currentPath = remember { mutableStateOf<Path?>(null) }

    // 刮开效果的路径集合
    val scratchPaths = remember { mutableStateListOf<Pair<Offset, Float>>() }

    LaunchedEffect(scratchProgress) {
        if (scratchProgress > 0.7f && !isComplete) {
            delay(500)
            isComplete = true
            onScratchComplete()
        }
    }

    if (isComplete) {
        // 揭示奖励
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFF9E6),
                            Color(0xFFFFE4E8),
                            Color(0xFFE3F2FD)
                        )
                    )
                )
        ) {
            // 庆祝效果
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🎊🎉🎊",
                    fontSize = 48.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "恭喜获得！",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = VibrantOrange
                )

                Spacer(modifier = Modifier.height(32.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = reward.iconEmoji ?: "🎁",
                            fontSize = 72.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = reward.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        if (reward.description != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = reward.description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "太棒了！继续加油！",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onClose,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VibrantOrange
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "太棒了！",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    } else {
        // 刮刮卡界面
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1A1A1A))
        ) {
            // 关闭按钮
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "关闭",
                    tint = Color.White
                )
            }

            // 积分
            Text(
                text = "⭐ ${reward.cost}积分",
                color = SunnyYellow,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            )

            // 刮刮卡区域
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "用手指滑动刮开涂层～",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // 刮刮卡
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.5f)
                ) {
                    // 底层奖励内容
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFFFFE4E8),
                                            Color(0xFFFFF3E0),
                                            Color(0xFFE8F5E9)
                                        )
                                    )
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = reward.iconEmoji ?: "🎁",
                                fontSize = 64.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = reward.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    // 涂层
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val position = change.position
                                    scratchPaths.add(Pair(position, 30f))

                                    // 计算刮开面积
                                    val totalPixels = size.width * size.height
                                    val scratchedPixels = scratchPaths.size * 30f * 30f * 3.14f
                                    scratchProgress = (scratchedPixels / totalPixels).coerceIn(0f, 1f)
                                }
                            }
                    ) {
                        // 绘制银色涂层
                        drawRect(
                            color = Color(0xFFC0C0C0),
                            size = size
                        )

                        // 绘制闪光效果
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFE0E0E0),
                                    Color(0xFFC0C0C0),
                                    Color(0xFFA0A0A0),
                                    Color(0xFFC0C0C0),
                                    Color(0xFFE0E0E0)
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(size.width, size.height)
                            )
                        )

                        // 绘制提示文字
                        drawContext.canvas.nativeCanvas.apply {
                            // 使用Android原生Canvas绘制文字
                        }

                        // 刮开的区域（使用混合模式）
                        scratchPaths.forEach { (pos, radius) ->
                            drawCircle(
                                color = Color.Transparent,
                                radius = radius,
                                center = pos,
                                blendMode = BlendMode.Clear
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 进度指示
                LinearProgressIndicator(
                    progress = scratchProgress,
                    modifier = Modifier.fillMaxWidth(),
                    color = SunnyYellow,
                    trackColor = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "已刮开 ${(scratchProgress * 100).toInt()}%",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
