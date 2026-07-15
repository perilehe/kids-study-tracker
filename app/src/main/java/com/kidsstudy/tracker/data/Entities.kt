package com.kidsstudy.tracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 图标类型枚举
 */
enum class IconType {
    EMOJI,           // 使用emoji
    PRESET,          // 使用预设图标
    CUSTOM_IMAGE,    // 使用自定义图片
    DRAWN            // 使用手绘图片
}

/**
 * 头像边框样式
 */
enum class AvatarStyle {
    NONE,        // 无边框
    STAR,        // 星星边框
    HEART,       // 爱心边框
    CLOUD,       // 云朵边框
    RAINBOW,     // 彩虹边框
    CIRCLE       // 圆形边框
}

/**
 * 任务频率
 */
enum class Frequency {
    DAILY,       // 每天
    WEEKLY_3,    // 每周3次
    WEEKLY_5,    // 每周5次
    CUSTOM       // 自定义
}

/**
 * 笔记模式
 */
enum class NoteMode {
    FREE_TEXT,      // 自由输入
    GUIDED,         // 引导式
    SIMPLE,         // 简单模式（表情+选词）
    VOICE,          // 语音输入
    NONE            // 跳过不写
}

/**
 * 任务实体
 */
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val duration: Int,           // 目标时长（分钟）
    val frequency: Frequency = Frequency.DAILY,
    val points: Int,             // 完成可获得积分
    val weekDays: String? = null, // 自定义频率时的周几（逗号分隔）

    // 图标相关字段
    val iconType: IconType = IconType.EMOJI,
    val iconEmoji: String? = null,
    val iconImageUri: String? = null,
    val iconColor: String? = null,

    val isActive: Boolean = true,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 打卡记录实体
 */
@Entity(tableName = "check_ins")
data class CheckIn(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long,              // 关联的任务ID
    val date: String,              // 打卡日期 yyyy-MM-dd
    val startTime: Long? = null,   // 开始时间戳
    val duration: Int? = null,     // 实际时长（分钟）
    val points: Int,               // 获得的积分

    // 打卡笔记字段
    val note: String? = null,      // 自由输入的笔记
    val mood: Int? = null,         // 心情/评分（1-5星）
    val answers: String? = null,   // 引导问题的回答（JSON格式）
    val noteMode: NoteMode = NoteMode.FREE_TEXT,

    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 奖励卡片实体
 */
@Entity(tableName = "reward_cards")
data class RewardCard(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val cost: Int,                 // 需要积分

    // 图标相关字段
    val iconType: IconType = IconType.EMOJI,
    val iconEmoji: String? = null,
    val iconImageUri: String? = null,
    val iconColor: String? = null,

    val stock: Int? = null,        // 库存数量，null表示无限
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 奖励兑换记录实体
 */
@Entity(tableName = "reward_history")
data class RewardHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val rewardCardId: Long,        // 奖励卡片ID
    val rewardName: String,        // 奖励名称（冗余存储）
    val pointsSpent: Int,          // 花费积分
    val isUsed: Boolean = false,   // 是否已使用
    val redeemedAt: Long = System.currentTimeMillis(),
    val usedAt: Long? = null,      // 使用时间
    val expiresAt: Long? = null    // 过期时间
)

/**
 * 用户实体
 */
@Entity(tableName = "user")
data class User(
    @PrimaryKey
    val id: Long = 1,
    val name: String = "小朋友",

    // 头像相关字段
    val avatarType: IconType = IconType.EMOJI,
    val avatarEmoji: String? = "🦁",
    val avatarImageUri: String? = null,
    val avatarBorderColor: String? = "#FFD23F",
    val avatarBorderStyle: AvatarStyle = AvatarStyle.RAINBOW,

    val totalPoints: Int = 0,
    val streakDays: Int = 0,
    val level: Int = 1,
    val lastCheckInDate: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 引导问题实体
 */
@Entity(tableName = "task_questions")
data class TaskQuestion(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long,              // 关联的任务ID
    val question: String,          // 问题内容
    val placeholder: String? = null,
    val sortOrder: Int = 0
)

/**
 * 成就实体
 */
@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey
    val id: String,                // 成就唯一标识
    val name: String,              // 成就名称
    val description: String,       // 成就描述
    val iconEmoji: String,         // 成就图标
    val requirement: Int,          // 达成条件
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null
)
