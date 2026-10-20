package com.bloom.app.ui.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.BubbleChart
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material.icons.outlined.Opacity
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bloom.app.data.DEFAULT_CYCLE_DAYS
import com.bloom.app.data.DemoData
import com.bloom.app.data.SymptomCategory
import com.bloom.app.data.overallPercent
import com.bloom.app.ui.components.IconTile
import com.bloom.app.ui.components.ScreenHeader
import com.bloom.app.ui.components.SectionTitle
import com.bloom.app.ui.components.SoftCard
import com.bloom.app.ui.main.MainUiState
import com.bloom.app.ui.main.MainViewModel
import com.bloom.app.ui.main.SheetType
import com.bloom.app.ui.theme.Border
import com.bloom.app.ui.theme.Muted
import com.bloom.app.ui.theme.Rose
import com.bloom.app.ui.theme.RoseMid
import com.bloom.app.ui.theme.RoseSoft
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class ScoreRange(val label: String) { Week("Week"), Month("Month") }

private data class Bar(val label: String, val percent: Int, val isToday: Boolean = false, val hasData: Boolean = true)

@Composable
fun InsightsScreen(
    state: MainUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
) {
    val todayPercent = state.progress.overallPercent()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        ScreenHeader("Insights", "Your week at a glance, and what to try next")
        Spacer(Modifier.height(16.dp))

        SectionTitle("Your score")
        Spacer(Modifier.height(8.dp))
        ScoreCard(todayPercent = todayPercent)
        Spacer(Modifier.height(18.dp))

        SectionTitle("Cycle")
        Spacer(Modifier.height(8.dp))
        CycleRow(
            periodStart = state.log.periodStart,
            onClick = { viewModel.openSheet(SheetType.Cycle) },
        )
        Spacer(Modifier.height(18.dp))

        SectionTitle("Daily summary")
        Spacer(Modifier.height(8.dp))
        DailySummary(
            percent = todayPercent,
            completed = state.completedCount,
            total = state.progress.size,
        )
        Spacer(Modifier.height(18.dp))

        SectionTitle("Symptom patterns")
        Spacer(Modifier.height(8.dp))
        SymptomPatterns(todaySymptoms = state.log.symptoms)
        Spacer(Modifier.height(8.dp))
        Text(
            "Weekly scores and past symptom days are sample data until history is stored.",
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
        )
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun ScoreCard(todayPercent: Int) {
    var range by remember { mutableStateOf(ScoreRange.Week) }
    var selected by remember { mutableStateOf<Int?>(null) }

    val bars = remember(range, todayPercent) { buildBars(range, todayPercent) }
    val average = bars.filter { it.hasData }.map { it.percent }.average().let { if (it.isNaN()) 0 else it.toInt() }

    SoftCard(modifier = Modifier.fillMaxWidth(), contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text("$average%", style = MaterialTheme.typography.headlineLarge)
                Text(
                    if (range == ScoreRange.Week) "week average" else "month average",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }
            RangeToggle(range) {
                range = it
                selected = null
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(96.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            bars.forEachIndexed { index, bar ->
                val fraction = if (bar.hasData) (bar.percent / 100f).coerceIn(0.04f, 1f) else 0.04f
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(fraction)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(
                            when {
                                selected == index || bar.isToday -> Rose
                                else -> RoseMid
                            },
                        )
                        .clickable { selected = index },
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            bars.forEach { bar ->
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        bar.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (bar.isToday) Rose else Muted,
                    )
                    Box(
                        Modifier
                            .padding(top = 2.dp)
                            .size(3.dp)
                            .clip(CircleShape)
                            .background(if (bar.isToday) Rose else Color.Transparent),
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        val pick = selected?.let { bars.getOrNull(it) }
        Text(
            text = when {
                pick == null -> "Tap a day to see its summary"
                !pick.hasData -> "${pick.label}: no data yet"
                else -> "${pick.label}: ${pick.percent}% of goals complete"
            },
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun buildBars(range: ScoreRange, todayPercent: Int): List<Bar> {
    if (range == ScoreRange.Month) {
        val previous = DemoData.previousWeekAverages
        val labels = listOf("W1", "W2", "W3", "W4")
        return labels.mapIndexed { i, label ->
            if (i < previous.size) Bar(label, previous[i]) else Bar(label, todayPercent, isToday = true)
        }
    }
    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
    val todayIndex = LocalDate.now().dayOfWeek.value - 1
    return dayLabels.mapIndexed { i, label ->
        when {
            i < todayIndex -> Bar(label, DemoData.weekScores[i])
            i == todayIndex -> Bar(label, todayPercent, isToday = true)
            else -> Bar(label, 0, hasData = false)
        }
    }
}

@Composable
private fun RangeToggle(selected: ScoreRange, onSelect: (ScoreRange) -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    Row(modifier = Modifier.clip(shape).background(RoseSoft).padding(2.dp)) {
        ScoreRange.entries.forEach { option ->
            val isSelected = option == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) Rose else Color.Transparent)
                    .clickable { onSelect(option) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    option.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) Color.White else Muted,
                )
            }
        }
    }
}

@Composable
private fun CycleRow(periodStart: LocalDate?, onClick: () -> Unit) {
    SoftCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(Icons.Outlined.Opacity)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (periodStart == null) {
                    Text("No cycle logged yet", style = MaterialTheme.typography.titleSmall)
                    Text("Log your last period date", style = MaterialTheme.typography.bodySmall, color = Muted)
                } else {
                    val fmt = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
                    Text("Last period ${periodStart.format(fmt)}", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Next expected around ${periodStart.plusDays(DEFAULT_CYCLE_DAYS).format(fmt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
                }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Muted)
        }
    }
}

@Composable
private fun DailySummary(percent: Int, completed: Int, total: Int) {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Rose),
                contentAlignment = Alignment.Center,
            ) {
                Text("$percent%", style = MaterialTheme.typography.labelMedium, color = Color.White)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Today", style = MaterialTheme.typography.titleSmall)
                Text(
                    text = if (completed == 0 && percent == 0) {
                        "Nothing logged yet today — no rush, tap a goal below whenever you're ready."
                    } else {
                        "$completed of $total goals complete. Every bit of progress counts."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }
        }
    }
}

@Composable
private fun SymptomPatterns(todaySymptoms: Map<SymptomCategory, Int>) {
    val icons = mapOf(
        SymptomCategory.Bloating to Icons.Outlined.BubbleChart,
        SymptomCategory.Skin to Icons.Outlined.Face,
        SymptomCategory.Mood to Icons.Outlined.Mood,
        SymptomCategory.SleepQuality to Icons.Outlined.Bedtime,
    )
    SymptomCategory.entries.forEach { category ->
        // 6 sample days + today (null = not logged today).
        val week: List<Int?> = DemoData.symptomHistory.getValue(category) + todaySymptoms[category]
        val highDays = week.count { it != null && it >= 2 }

        SoftCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconTile(icons.getValue(category))
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(category.label, style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Moderate or higher on $highDays of 7 days",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
                }
                Spacer(Modifier.width(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    week.forEach { severity ->
                        Box(
                            modifier = Modifier
                                .size(width = 4.dp, height = 22.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    if (severity == null) Border else lerp(RoseSoft, Rose, severity / 3f),
                                ),
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}
