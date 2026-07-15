package com.kidsstudy.tracker.repository

import com.kidsstudy.tracker.data.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TaskRepository(private val taskDao: TaskDao) {

    fun getAllActiveTasks(): Flow<List<Task>> = taskDao.getAllActiveTasks()
    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()

    suspend fun getTaskById(taskId: Long): Task? = taskDao.getTaskById(taskId)

    suspend fun insertTask(task: Task): Long {
        val maxOrder = taskDao.getMaxSortOrder() ?: 0
        return taskDao.insertTask(task.copy(sortOrder = maxOrder + 1))
    }

    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    suspend fun deactivateTask(taskId: Long) = taskDao.deactivateTask(taskId)
}

class CheckInRepository(
    private val checkInDao: CheckInDao,
    private val userDao: UserDao
) {

    fun getCheckInsByTask(taskId: Long): Flow<List<CheckIn>> = checkInDao.getCheckInsByTask(taskId)

    suspend fun getCheckInsByDate(date: String): List<CheckIn> = checkInDao.getCheckInsByDate(date)

    fun getCheckInsBetween(startDate: String, endDate: String): Flow<List<CheckIn>> =
        checkInDao.getCheckInsBetween(startDate, endDate)

    fun getRecentCheckIns(): Flow<List<CheckIn>> = checkInDao.getRecentCheckIns()

    fun getAllCheckInDates(): Flow<List<String>> = checkInDao.getAllCheckInDates()

    suspend fun insertCheckIn(checkIn: CheckIn, task: Task): Long {
        val checkInId = checkInDao.insertCheckIn(checkIn)

        // 更新用户积分和连续天数
        userDao.addPoints(checkIn.points)

        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val user = userDao.getUserSync()

        if (user != null) {
            val lastDate = user.lastCheckInDate
            if (lastDate == null) {
                userDao.updateStreak(1, today)
            } else {
                val lastLocalDate = LocalDate.parse(lastDate)
                val todayLocalDate = LocalDate.parse(today)
                val daysBetween = java.time.temporal.ChronoUnit.DAYS.between(lastLocalDate, todayLocalDate)

                if (daysBetween == 1L) {
                    // 连续打卡
                    userDao.updateStreak(user.streakDays + 1, today)
                } else if (daysBetween > 1L) {
                    // 断签，重新开始
                    userDao.updateStreak(1, today)
                }
            }
        }

        return checkInId
    }

    suspend fun getPointsByDate(date: String): Int = checkInDao.getPointsByDate(date) ?: 0

    suspend fun getTotalPoints(): Int = checkInDao.getTotalPoints() ?: 0

    suspend fun getTotalCheckInDays(): Int = checkInDao.getTotalCheckInDays()
}

class RewardRepository(
    private val rewardCardDao: RewardCardDao,
    private val rewardHistoryDao: RewardHistoryDao,
    private val userDao: UserDao
) {

    fun getAllActiveRewardCards(): Flow<List<RewardCard>> = rewardCardDao.getAllActiveRewardCards()

    suspend fun getRewardCardById(cardId: Long): RewardCard? = rewardCardDao.getRewardCardById(cardId)

    suspend fun insertRewardCard(card: RewardCard): Long = rewardCardDao.insertRewardCard(card)

    suspend fun updateRewardCard(card: RewardCard) = rewardCardDao.updateRewardCard(card)

    suspend fun deleteRewardCard(card: RewardCard) = rewardCardDao.deleteRewardCard(card)

    fun getAllRewardHistory(): Flow<List<RewardHistory>> = rewardHistoryDao.getAllRewardHistory()

    fun getUnusedRewards(): Flow<List<RewardHistory>> = rewardHistoryDao.getUnusedRewards()

    fun getUsedRewards(): Flow<List<RewardHistory>> = rewardHistoryDao.getUsedRewards()

    suspend fun redeemReward(card: RewardCard): Result<Long> {
        val user = userDao.getUserSync() ?: return Result.failure(Exception("用户不存在"))

        if (user.totalPoints < card.cost) {
            return Result.failure(Exception("积分不足"))
        }

        // 检查库存
        if (card.stock != null && card.stock <= 0) {
            return Result.failure(Exception("库存不足"))
        }

        // 扣除积分
        userDao.subtractPoints(card.cost)

        // 创建兑换记录
        val history = RewardHistory(
            rewardCardId = card.id,
            rewardName = card.name,
            pointsSpent = card.cost,
            expiresAt = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000 // 30天有效期
        )
        val historyId = rewardHistoryDao.insertRewardHistory(history)

        // 更新库存
        if (card.stock != null) {
            rewardCardDao.updateRewardCard(card.copy(stock = card.stock - 1))
        }

        return Result.success(historyId)
    }

    suspend fun markRewardAsUsed(historyId: Long) {
        rewardHistoryDao.markAsUsed(historyId)
    }
}

class UserRepository(private val userDao: UserDao) {

    fun getUser(): Flow<User?> = userDao.getUser()

    suspend fun getUserSync(): User? = userDao.getUserSync()

    suspend fun insertUser(user: User) = userDao.insertUser(user)

    suspend fun updateUser(user: User) = userDao.updateUser(user)

    suspend fun updateStreak(days: Int, date: String) = userDao.updateStreak(days, date)

    suspend fun updateLevel(level: Int) = userDao.updateLevel(level)
}
