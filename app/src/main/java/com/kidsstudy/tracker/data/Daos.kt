package com.kidsstudy.tracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE isActive = 1 ORDER BY sortOrder ASC")
    fun getAllActiveTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks ORDER BY sortOrder ASC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): Task?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("UPDATE tasks SET isActive = 0 WHERE id = :taskId")
    suspend fun deactivateTask(taskId: Long)

    @Query("SELECT MAX(sortOrder) FROM tasks")
    suspend fun getMaxSortOrder(): Int?
}

@Dao
interface CheckInDao {
    @Query("SELECT * FROM check_ins WHERE taskId = :taskId ORDER BY date DESC")
    fun getCheckInsByTask(taskId: Long): Flow<List<CheckIn>>

    @Query("SELECT * FROM check_ins WHERE date = :date")
    suspend fun getCheckInsByDate(date: String): List<CheckIn>

    @Query("SELECT * FROM check_ins WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getCheckInsBetween(startDate: String, endDate: String): Flow<List<CheckIn>>

    @Query("SELECT * FROM check_ins ORDER BY date DESC LIMIT 50")
    fun getRecentCheckIns(): Flow<List<CheckIn>>

    @Query("SELECT DISTINCT date FROM check_ins ORDER BY date DESC")
    fun getAllCheckInDates(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM check_ins WHERE date = :date")
    suspend fun getCheckInCountByDate(date: String): Int

    @Query("SELECT COUNT(DISTINCT date) FROM check_ins")
    suspend fun getTotalCheckInDays(): Int

    @Query("SELECT SUM(points) FROM check_ins WHERE date = :date")
    suspend fun getPointsByDate(date: String): Int?

    @Query("SELECT SUM(points) FROM check_ins")
    suspend fun getTotalPoints(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: CheckIn): Long

    @Delete
    suspend fun deleteCheckIn(checkIn: CheckIn)
}

@Dao
interface RewardCardDao {
    @Query("SELECT * FROM reward_cards WHERE isActive = 1 ORDER BY cost ASC")
    fun getAllActiveRewardCards(): Flow<List<RewardCard>>

    @Query("SELECT * FROM reward_cards WHERE id = :cardId")
    suspend fun getRewardCardById(cardId: Long): RewardCard?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRewardCard(card: RewardCard): Long

    @Update
    suspend fun updateRewardCard(card: RewardCard)

    @Delete
    suspend fun deleteRewardCard(card: RewardCard)
}

@Dao
interface RewardHistoryDao {
    @Query("SELECT * FROM reward_history ORDER BY redeemedAt DESC")
    fun getAllRewardHistory(): Flow<List<RewardHistory>>

    @Query("SELECT * FROM reward_history WHERE isUsed = 0 ORDER BY redeemedAt DESC")
    fun getUnusedRewards(): Flow<List<RewardHistory>>

    @Query("SELECT * FROM reward_history WHERE isUsed = 1 ORDER BY usedAt DESC")
    fun getUsedRewards(): Flow<List<RewardHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRewardHistory(history: RewardHistory): Long

    @Update
    suspend fun updateRewardHistory(history: RewardHistory)

    @Query("UPDATE reward_history SET isUsed = 1, usedAt = :usedAt WHERE id = :historyId")
    suspend fun markAsUsed(historyId: Long, usedAt: Long = System.currentTimeMillis())
}

@Dao
interface UserDao {
    @Query("SELECT * FROM user WHERE id = 1")
    fun getUser(): Flow<User?>

    @Query("SELECT * FROM user WHERE id = 1")
    suspend fun getUserSync(): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("UPDATE user SET totalPoints = totalPoints + :points WHERE id = 1")
    suspend fun addPoints(points: Int)

    @Query("UPDATE user SET totalPoints = totalPoints - :points WHERE id = 1")
    suspend fun subtractPoints(points: Int)

    @Query("UPDATE user SET streakDays = :days, lastCheckInDate = :date WHERE id = 1")
    suspend fun updateStreak(days: Int, date: String)

    @Query("UPDATE user SET level = :level WHERE id = 1")
    suspend fun updateLevel(level: Int)
}

@Dao
interface TaskQuestionDao {
    @Query("SELECT * FROM task_questions WHERE taskId = :taskId ORDER BY sortOrder ASC")
    fun getQuestionsByTask(taskId: Long): Flow<List<TaskQuestion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: TaskQuestion): Long

    @Delete
    suspend fun deleteQuestion(question: TaskQuestion)

    @Query("DELETE FROM task_questions WHERE taskId = :taskId")
    suspend fun deleteAllQuestionsForTask(taskId: Long)
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE isUnlocked = 1")
    fun getUnlockedAchievements(): Flow<List<Achievement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: Achievement)

    @Update
    suspend fun updateAchievement(achievement: Achievement)

    @Query("UPDATE achievements SET isUnlocked = 1, unlockedAt = :unlockedAt WHERE id = :achievementId")
    suspend fun unlockAchievement(achievementId: String, unlockedAt: Long = System.currentTimeMillis())
}
