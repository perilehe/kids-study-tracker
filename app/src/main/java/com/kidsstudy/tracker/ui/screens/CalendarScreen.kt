package com.kidsstudy.tracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsstudy.tracker.data.DateCheckInCount
import com.kidsstudy.tracker.ui.theme.*
import com.kidsstudy.tracker.viewmodel.MainViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier  // Padding from parent Scaffold
) {
    val tasks by viewModel.allTasks.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val totalTasks = tasks.size

    // Currently displayed month
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    // Current view: week view or month view
    var showWeekView by remember { mutableStateOf(true) }

    // Query monthly check-in counts from database
    val monthStart = "${currentYearMonth.year}-${String.format("%02d", currentYearMonth.monthValue)}-01"
    val monthEnd = "${currentYearMonth.year}-${String.format("%02d", currentYearMonth.monthValue)}-${currentYearMonth.lengthOfMonth()}"
    val monthlyCounts by viewModel.getMonthlyCheckInCounts(monthStart, monthEnd)
        .collectAsState(initial = emptyList())

    // Build a date->count map for quick lookup
    val countByDate = remember(monthlyCounts) {
        monthlyCounts.associate { it.date to it.cnt }
    }

    // This week's date range
    val today = LocalDate.now()
    val weekStart = remember { today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) }
    val weekDays = remember { (0..6).map { weekStart.plusDays(it.toLong()) } }

    // Calculate weekly stats
    val weekCheckInCount = weekDays.sumOf { date ->
        val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        countByDate[dateStr] ?: 0
    }
    val weekDaysChecked = weekDays.count { date ->
        val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        (countByDate[dateStr] ?: 0) > 0
    }

    // Calculate monthly stats
    val monthCheckInCount = monthlyCounts.sumOf { it.cnt }
    val monthDaysChecked = monthlyCounts.count { it.cnt > 0 }

    // No nested Scaffold — use parent's padding
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Title
        item {
            Text(
                text = "📅 Check-in Calendar",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        // Week/Month toggle buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = showWeekView,
                    onClick = { showWeekView = true },
                    label = { Text("This Week") },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VibrantOrange,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = !showWeekView,
                    onClick = { showWeekView = false },
                    label = { Text("This Month") },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VibrantOrange,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        if (showWeekView) {
            // === Week View ===
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "📅 This Week (${weekDays.first().monthValue}/${weekDays.first().dayOfMonth} - ${weekDays.last().monthValue}/${weekDays.last().dayOfMonth})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        weekDays.forEachIndexed { index, date ->
                            val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                            val count = countByDate[dateStr] ?: 0
                            WeekDayRow(
                                date = date,
                                isToday = date == today,
                                dayNumber = index + 1,
                                checkInCount = count,
                                totalTasks = totalTasks
                            )
                            if (index < 6) Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            // Weekly stats
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Weekly Pts",
                        value = "$weekCheckInCount",
                        unit = "",
                        emoji = "⭐",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Days",
                        value = "$weekDaysChecked",
                        unit = "",
                        emoji = "✅",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Streak",
                        value = "${user?.streakDays ?: 0}",
                        unit = "",
                        emoji = "🔥",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            // === Month View ===
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Month navigation
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = {
                                currentYearMonth = currentYearMonth.minusMonths(1)
                            }) {
                                Text("◀", fontSize = 20.sp)
                            }
                            Text(
                                text = "${currentYearMonth.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${currentYearMonth.year}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = {
                                currentYearMonth = currentYearMonth.plusMonths(1)
                            }) {
                                Text("▶", fontSize = 20.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Day-of-week headers
                        Row(modifier = Modifier.fillMaxWidth()) {
                            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                                Text(
                                    text = day,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextHint
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Calendar grid with real data
                        CalendarGrid(
                            yearMonth = currentYearMonth,
                            today = today,
                            countByDate = countByDate,
                            totalTasks = totalTasks
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Legend
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            LegendItem(color = SuccessGreen, label = "All done")
                            LegendItem(color = SunnyYellow, label = "Partial")
                            LegendItem(color = Color(0xFFF5F5F5), label = "None")
                        }
                    }
                }
            }

            // Monthly stats
            item {
                Text(
                    text = "  Monthly Stats",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Check-ins",
                        value = "$monthCheckInCount",
                        unit = "",
                        emoji = "✅",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Days",
                        value = "$monthDaysChecked",
                        unit = "",
                        emoji = "📅",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Streak",
                        value = "${user?.streakDays ?: 0}",
                        unit = "",
                        emoji = "🔥",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Bottom spacing to avoid bottom nav overlap
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun WeekDayRow(
    date: LocalDate,
    isToday: Boolean,
    dayNumber: Int,
    checkInCount: Int,
    totalTasks: Int
) {
    val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val dayOfWeek = dayNames[(date.dayOfWeek.value - 1).coerceIn(0, 6)]
    val dateStr = "${date.monthValue}/${date.dayOfMonth}"

    val bgColor = when {
        isToday -> Color(0xFFFFF3E0)
        checkInCount > 0 -> Color(0xFFE8F5E9)
        else -> Color(0xFFF5F5F5)
    }
    val borderColor = if (isToday) VibrantOrange else Color.Transparent

    val statusIcon = when {
        checkInCount >= totalTasks && totalTasks > 0 -> "✅"
        checkInCount > 0 -> "📝"
        else -> "○"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(VibrantOrange),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$dayNumber",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$dayOfWeek · $dateStr",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Done $checkInCount/$totalTasks tasks",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        Text(
            text = statusIcon,
            fontSize = 20.sp
        )
    }
}

@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    today: LocalDate,
    countByDate: Map<String, Int>,
    totalTasks: Int
) {
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = LocalDate.of(yearMonth.year, yearMonth.month, 1)
    // DayOfWeek: Monday=1 ... Sunday=7, calendar starts from Sunday so Sunday=0
    val startOffset = (firstDayOfMonth.dayOfWeek.value % 7)

    val cells = mutableListOf<CalendarCell>()

    // Add empty cells before the first day
    for (i in 0 until startOffset) {
        cells.add(CalendarCell.Empty)
    }

    // Add day cells with real data
    for (day in 1..daysInMonth) {
        val date = LocalDate.of(yearMonth.year, yearMonth.month, day)
        val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val count = countByDate[dateStr] ?: 0
        val isToday = date == today

        val status = when {
            totalTasks > 0 && count >= totalTasks -> DayStatus.FULL
            count > 0 -> DayStatus.PARTIAL
            else -> DayStatus.NONE
        }

        cells.add(CalendarCell.Day(day = day, isToday = isToday, status = status, count = count))
    }

    // Fill the last row
    while (cells.size % 7 != 0) {
        cells.add(CalendarCell.Empty)
    }

    // 7-column grid
    val rows = cells.chunked(7)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row.forEach { cell ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        when (cell) {
                            is CalendarCell.Empty -> { /* empty */ }
                            is CalendarCell.Day -> {
                                val bgColor = when (cell.status) {
                                    DayStatus.FULL -> SuccessGreen
                                    DayStatus.PARTIAL -> SunnyYellow
                                    DayStatus.NONE -> Color(0xFFF5F5F5)
                                }
                                val textColor = when (cell.status) {
                                    DayStatus.FULL -> Color.White
                                    DayStatus.PARTIAL -> TextPrimary
                                    DayStatus.NONE -> TextSecondary
                                }
                                val borderMod = if (cell.isToday) {
                                    Modifier.border(2.dp, VibrantOrange, RoundedCornerShape(6.dp))
                                } else Modifier

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(bgColor)
                                        .then(borderMod),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "${cell.day}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = if (cell.status != DayStatus.NONE) FontWeight.Bold else FontWeight.Normal,
                                            color = textColor
                                        )
                                        if (cell.count > 0 && cell.status != DayStatus.FULL) {
                                            Text(
                                                text = "${cell.count}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = textColor,
                                                fontSize = 8.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

private sealed class CalendarCell {
    object Empty : CalendarCell()
    data class Day(val day: Int, val isToday: Boolean, val status: DayStatus, val count: Int = 0) : CalendarCell()
}

private enum class DayStatus {
    FULL,       // All tasks completed - green
    PARTIAL,    // Some tasks completed - yellow
    NONE        // No check-ins - gray
}
