package com.kidsstudy.tracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kidsstudy.tracker.data.*
import com.kidsstudy.tracker.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val taskRepository = TaskRepository(database.taskDao())
    private val checkInRepository = CheckInRepository(database.checkInDao(), database.userDao())
    private val rewardRepository = RewardRepository(
        database.rewardCardDao(),
        database.rewardHistoryDao(),
        database.userDao()
    )
    private val userRepository = UserRepository(database.userDao())

    // 任务相关
    val activeTasks: StateFlow<List<Task>> = taskRepository.getAllActiveTasks()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allTasks: StateFlow<List<Task>> = taskRepository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // 用户相关
    val currentUser: StateFlow<User?> = userRepository.getUser()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    // 打卡记录相关
    val recentCheckIns: StateFlow<List<CheckIn>> = checkInRepository.getRecentCheckIns()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // 奖励相关
    val rewardCards: StateFlow<List<RewardCard>> = rewardRepository.getAllActiveRewardCards()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val unusedRewards: StateFlow<List<RewardHistory>> = rewardRepository.getUnusedRewards()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val usedRewards: StateFlow<List<RewardHistory>> = rewardRepository.getUsedRewards()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // 事件流
    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()

    // 今日打卡状态
    private val _todayCheckIns = MutableStateFlow<List<CheckIn>>(emptyList())
    val todayCheckIns: StateFlow<List<CheckIn>> = _todayCheckIns.asStateFlow()

    init {
        // 加载今日打卡记录
        loadTodayCheckIns()
        // 初始化默认用户
        initDefaultUser()
    }

    private fun initDefaultUser() {
        viewModelScope.launch {
            val user = userRepository.getUserSync()
            if (user == null) {
                userRepository.insertUser(
                    User(
                        name = "小朋友",
                        avatarEmoji = "🦁"
                    )
                )
            }
        }
    }

    fun loadTodayCheckIns() {
        viewModelScope.launch {
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            _todayCheckIns.value = checkInRepository.getCheckInsByDate(today)
        }
    }

    fun isTaskCompletedToday(taskId: Long): Boolean {
        return _todayCheckIns.value.any { it.taskId == taskId }
    }

    // 任务操作
    fun addTask(
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
    ) {
        viewModelScope.launch {
            val task = Task(
                name = name,
                description = description,
                duration = duration,
                frequency = frequency,
                points = points,
                weekDays = weekDays,
                iconType = iconType,
                iconEmoji = iconEmoji,
                iconImageUri = iconImageUri,
                iconColor = iconColor
            )
            taskRepository.insertTask(task)
            _events.emit(UiEvent.TaskAdded)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            taskRepository.updateTask(task)
            _events.emit(UiEvent.TaskUpdated)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskRepository.deleteTask(task)
            _events.emit(UiEvent.TaskDeleted)
        }
    }

    // 打卡操作
    fun checkInTask(
        taskId: Long,
        duration: Int?,
        note: String?,
        mood: Int?,
        answers: String?,
        noteMode: NoteMode
    ) {
        viewModelScope.launch {
            val task = taskRepository.getTaskById(taskId) ?: return@launch
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

            val checkIn = CheckIn(
                taskId = taskId,
                date = today,
                duration = duration,
                points = task.points + if (note != null && note.isNotEmpty()) 2 else 0,
                note = note,
                mood = mood,
                answers = answers,
                noteMode = noteMode
            )

            checkInRepository.insertCheckIn(checkIn, task)
            loadTodayCheckIns()
            _events.emit(UiEvent.CheckInCompleted(task.points + if (note != null && note.isNotEmpty()) 2 else 0))
        }
    }

    // 奖励操作
    fun addReward(
        name: String,
        description: String?,
        cost: Int,
        iconType: IconType,
        iconEmoji: String?,
        iconImageUri: String?,
        iconColor: String?,
        stock: Int?
    ) {
        viewModelScope.launch {
            val card = RewardCard(
                name = name,
                description = description,
                cost = cost,
                iconType = iconType,
                iconEmoji = iconEmoji,
                iconImageUri = iconImageUri,
                iconColor = iconColor,
                stock = stock
            )
            rewardRepository.insertRewardCard(card)
            _events.emit(UiEvent.RewardAdded)
        }
    }

    fun redeemReward(card: RewardCard) {
        viewModelScope.launch {
            val result = rewardRepository.redeemReward(card)
            result.fold(
                onSuccess = {
                    _events.emit(UiEvent.RewardRedeemed)
                },
                onFailure = { error ->
                    _events.emit(UiEvent.Error(error.message ?: "兑换失败"))
                }
            )
        }
    }

    fun markRewardAsUsed(historyId: Long) {
        viewModelScope.launch {
            rewardRepository.markRewardAsUsed(historyId)
            _events.emit(UiEvent.RewardUsed)
        }
    }

    // 用户操作
    fun updateUser(
        name: String,
        avatarType: IconType,
        avatarEmoji: String?,
        avatarImageUri: String?,
        avatarBorderColor: String?,
        avatarBorderStyle: AvatarStyle
    ) {
        viewModelScope.launch {
            val user = userRepository.getUserSync() ?: return@launch
            userRepository.updateUser(
                user.copy(
                    name = name,
                    avatarType = avatarType,
                    avatarEmoji = avatarEmoji,
                    avatarImageUri = avatarImageUri,
                    avatarBorderColor = avatarBorderColor,
                    avatarBorderStyle = avatarBorderStyle
                )
            )
            _events.emit(UiEvent.UserUpdated)
        }
    }
}

// UI事件
sealed class UiEvent {
    object TaskAdded : UiEvent()
    object TaskUpdated : UiEvent()
    object TaskDeleted : UiEvent()
    data class CheckInCompleted(val points: Int) : UiEvent()
    object RewardAdded : UiEvent()
    object RewardRedeemed : UiEvent()
    object RewardUsed : UiEvent()
    object UserUpdated : UiEvent()
    data class Error(val message: String) : UiEvent()
}
