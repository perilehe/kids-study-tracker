package com.kidsstudy.tracker.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Stats : Screen("stats")
    object Rewards : Screen("rewards")
    object Settings : Screen("settings")
    object TaskDetail : Screen("task_detail/{taskId}") {
        fun createRoute(taskId: Long) = "task_detail/$taskId"
    }
    object AddTask : Screen("add_task")
    object AddReward : Screen("add_reward")
    object ScratchCard : Screen("scratch_card/{rewardId}") {
        fun createRoute(rewardId: Long) = "scratch_card/$rewardId"
    }
    object CheckInNote : Screen("checkin_note/{taskId}") {
        fun createRoute(taskId: Long) = "checkin_note/$taskId"
    }
    object MyRewards : Screen("my_rewards")
    object EditAvatar : Screen("edit_avatar")
    object IconPicker : Screen("icon_picker/{type}/{id}") {
        fun createRoute(type: String, id: Long) = "icon_picker/$type/$id"
    }
}
