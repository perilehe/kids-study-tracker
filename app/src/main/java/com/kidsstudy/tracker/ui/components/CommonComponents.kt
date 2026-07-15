package com.kidsstudy.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsstudy.tracker.data.IconType
import com.kidsstudy.tracker.data.Task
import com.kidsstudy.tracker.data.User
import com.kidsstudy.tracker.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 头像组件
 */
@Composable
fun AvatarComponent(
    user: User?,
    size: Int = 80,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val borderColors = when (user?.avatarBorderStyle) {
        AvatarStyle.RAINBOW -> listOf(VibrantOrange, SunnyYellow, MintGreen, FreshBlue, DreamPurple)
        AvatarStyle.STAR -> listOf(SunnyYellow, VibrantOrange)
        AvatarStyle.HEART -> listOf(PassionRed, Color(0xFFFF9999))
        AvatarStyle.CLOUD -> listOf(FreshBlue, Color(0xFFB3E5FC))
        else -> listOf(Color(0xFFE0E0E0))
    }

    Box(
        modifier = modifier
            .size(size.dp)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .background(
                brush = Brush.linearGradient(borderColors),
                shape = CircleShape
            )
            .padding(3.dp)
            .background(
                color = Color(0xFFE0E0E0),
                shape = CircleShape
            )
            .padding(2.dp)
            .background(
                color = user?.avatarBorderColor?.let { Color(android.graphics.Color.parseColor(it)) }
                    ?: SunnyYellow,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        when (user?.avatarType) {
            IconType.CUSTOM_IMAGE -> {
                // TODO: 加载自定义图片
                Text(
                    text = user.avatarEmoji ?: "👤",
                    fontSize = (size * 0.5).sp
                )
            }
            else -> {
                Text(
                    text = user?.avatarEmoji ?: "👤",
                    fontSize = (size * 0.5).sp
                )
            }
        }
    }
}

/**
 * 任务图标组件
 */
@Composable
fun TaskIconBox(
    task: Task,
    size: Int = 48,
    modifier: Modifier = Modifier
) {
    val bgColor = task.iconColor?.let {
        try {
            Color(android.graphics.Color.parseColor(it))
        } catch (e: Exception) {
            CardColors[task.id.toInt() % CardColors.size]
        }
    } ?: CardColors[task.id.toInt() % CardColors.size]

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        when (task.iconType) {
            IconType.CUSTOM_IMAGE -> {
                // TODO: 加载自定义图片
                Text(
                    text = task.iconEmoji ?: "📋",
                    fontSize = (size * 0.5).sp
                )
            }
            else -> {
                Text(
                    text = task.iconEmoji ?: "📋",
                    fontSize = (size * 0.5).sp
                )
            }
        }
    }
}

/**
 * 任务卡片组件
 */
@Composable
fun TaskCard(
    task: Task,
    isCompleted: Boolean,
    streakDays: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardColor = if (isCompleted) {
        SuccessGreen.copy(alpha = 0.15f)
    } else {
        CardBackground
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCompleted) 0.dp else 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TaskIconBox(task = task, size = 56)

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${task.duration}分钟 · ${task.points}⭐",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                if (streakDays > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🔥 连续$streakDays天",
                        style = MaterialTheme.typography.bodySmall,
                        color = VibrantOrange
                    )
                }
            }

            if (isCompleted) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SuccessGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(2.dp, VibrantOrange, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "○",
                        color = VibrantOrange,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

/**
 * 进度条组件
 */
@Composable
fun GradientProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFEEEEEE))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(6.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(VibrantOrange, SunnyYellow, MintGreen)
                    )
                )
        )
    }
}

/**
 * 积分显示组件
 */
@Composable
fun PointsDisplay(
    points: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "⭐",
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$points",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = SunnyYellow
        )
        Text(
            text = " 积分",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

/**
 * 连续打卡显示组件
 */
@Composable
fun StreakDisplay(
    days: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "🔥",
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "连续 $days 天",
            style = MaterialTheme.typography.titleMedium,
            color = VibrantOrange,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * 欢迎卡片组件
 */
@Composable
fun WelcomeCard(
    user: User?,
    modifier: Modifier = Modifier
) {
    val currentHour = java.time.LocalTime.now().hour
    val greeting = when {
        currentHour < 12 -> "早上好"
        currentHour < 18 -> "下午好"
        else -> "晚上好"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFE4E8)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    text = "$greeting！",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${user?.name ?: "小朋友"} · Lv.${user?.level ?: 1}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}
