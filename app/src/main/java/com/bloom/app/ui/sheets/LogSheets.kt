package com.bloom.app.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.BubbleChart
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.Opacity
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bloom.app.data.DEFAULT_CYCLE_DAYS
import com.bloom.app.data.Goal
import com.bloom.app.data.MealQuality
import com.bloom.app.data.MealType
import com.bloom.app.data.SymptomCategory
import com.bloom.app.ui.components.CircleIconButton
import com.bloom.app.ui.components.InfoCard
import com.bloom.app.ui.components.OptionPill
import com.bloom.app.ui.components.PrimaryButton
import com.bloom.app.ui.components.SheetHeader
import com.bloom.app.ui.components.SoftCard
import com.bloom.app.ui.theme.Border
import com.bloom.app.ui.theme.LinkBlue
import com.bloom.app.ui.theme.Muted
import com.bloom.app.ui.theme.Rose
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
private data class CounterCopy(
    val title: String,
    val caption: String,
    val tipIcon: ImageVector,
    val tipTitle: String,
    val tipEmpty: String,
    val tipLogged: String,
)

private fun Goal.counterCopy(): CounterCopy = when (this) {
    Goal.Movement -> CounterCopy(
        "Log Movement", "Today's movement", Icons.Outlined.FitnessCenter,
        "Get moving today",
        "No movement logged yet today. Try a brisk 30-minute walk, a swim, or some light strength training — these are especially good for insulin sensitivity with PCOS.",
        "Nice — movement helps insulin sensitivity. Even a short walk after meals counts.",
    )
    Goal.Water -> CounterCopy(
        "Log Water", "Today's water", Icons.Outlined.WaterDrop,
        "Start hydrating",
        "No water logged yet today. Try starting with a glass first thing in the morning.",
        "Great — keep a bottle nearby and sip through the day.",
    )
    Goal.Relaxation -> CounterCopy(
        "Log Relaxation", "Today's relaxation", Icons.Outlined.SelfImprovement,
        "Make time to unwind",
        "No relaxation time logged yet. Even 5–10 minutes of deep breathing, meditation, or gentle yoga can help lower stress hormones.",
        "Lovely — taking time to unwind helps lower stress hormones.",
    )
    else -> CounterCopy(
        "Log Sleep", "Today's sleep", Icons.Outlined.Bedtime,
        "Log last night's sleep",
        "You haven't logged sleep yet. Consistent, close-to-8-hr nights can help keep your hormones more balanced — avoiding phones and screens before bed makes it easier to fall asleep.",
        "Good — consistent sleep helps keep your hormones balanced.",
    )
}

@Composable
fun CounterSheetContent(
    goal: Goal,
    current: Int,
    target: Int,
    onChange: (Int) -> Unit,
    onOpenSettings: () -> Unit,
    onClose: () -> Unit,
) {
    val copy = goal.counterCopy()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        SheetHeader(copy.title, onClose)
        Spacer(Modifier.height(16.dp))
        Text(copy.caption, style = MaterialTheme.typography.bodySmall, color = Muted)
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircleIconButton(
                icon = Icons.Filled.Remove, contentDescription = "Decrease",
                onClick = { onChange(-goal.logStep) }, size = 44.dp,
            )
            Spacer(Modifier.width(24.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontSize = 36.sp)) { append("$current") }
                    withStyle(SpanStyle(fontSize = 13.sp, color = Muted)) { append(" ${goal.unit}") }
                },
                style = MaterialTheme.typography.headlineLarge,
            )
            Spacer(Modifier.width(24.dp))
            CircleIconButton(
                icon = Icons.Filled.Add, contentDescription = "Increase",
                onClick = { onChange(goal.logStep) }, size = 44.dp,
            )
        }
        Spacer(Modifier.height(10.dp))
        Row {
            Text("Goal: $target ${goal.unit} · adjust in ", style = MaterialTheme.typography.bodySmall, color = Muted)
            Text(
                "Settings",
                style = MaterialTheme.typography.bodySmall,
                color = LinkBlue,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable(onClick = onOpenSettings),
            )
        }
        Spacer(Modifier.height(18.dp))
        InfoCard(
            icon = copy.tipIcon,
            title = copy.tipTitle,
            body = if (current == 0) copy.tipEmpty else copy.tipLogged,
            chipText = "+${goal.logStep} ${goal.unit}",
            onChip = { onChange(goal.logStep) },
        )
    }
}

@Composable
fun MealsSheetContent(
    meals: Map<MealType, MealQuality>,
    onSelect: (MealType, MealQuality) -> Unit,
    onClose: () -> Unit,
) {
    val icons = mapOf(
        MealType.Breakfast to Icons.Outlined.LightMode,
        MealType.Lunch to Icons.Outlined.WbSunny,
        MealType.Dinner to Icons.Outlined.NightsStay,
    )
    Column {
        SheetHeader("Log Meals", onClose)
        Spacer(Modifier.height(6.dp))
        Text("How would you describe each meal today?", style = MaterialTheme.typography.bodySmall, color = Muted)
        Spacer(Modifier.height(14.dp))
        MealType.entries.forEach { type ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icons.getValue(type), null, tint = Rose, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(type.label, style = MaterialTheme.typography.titleSmall)
            }
            Spacer(Modifier.height(8.dp))
            val options = MealQuality.entries
            options.chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    pair.forEach { quality ->
                        OptionPill(
                            text = quality.label,
                            selected = meals[type] == quality,
                            onClick = { onSelect(type, quality) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            Spacer(Modifier.height(6.dp))
        }
        Text(
            "${meals.size} of ${MealType.entries.size} meals logged today",
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun SymptomsSheetContent(
    symptoms: Map<SymptomCategory, Int>,
    onSelect: (SymptomCategory, Int) -> Unit,
    onClose: () -> Unit,
) {
    val icons = mapOf(
        SymptomCategory.Bloating to Icons.Outlined.BubbleChart,
        SymptomCategory.Skin to Icons.Outlined.Face,
        SymptomCategory.Mood to Icons.Outlined.Mood,
        SymptomCategory.SleepQuality to Icons.Outlined.Bedtime,
    )
    Column {
        SheetHeader("Today's symptoms", onClose)
        Spacer(Modifier.height(6.dp))
        Text(
            "These are separate from your daily goals — logging them just helps you (and eventually your doctor) spot patterns over time.",
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
        )
        Spacer(Modifier.height(14.dp))
        SymptomCategory.entries.forEach { category ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icons.getValue(category), null, tint = Rose, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(category.label, style = MaterialTheme.typography.titleSmall)
            }
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                category.options.forEachIndexed { index, label ->
                    OptionPill(
                        text = label,
                        selected = symptoms[category] == index,
                        onClick = { onSelect(category, index) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
        PrimaryButton("Done", onClose)
    }
}

@Composable
fun CycleSheetContent(
    periodStart: LocalDate?,
    onSelect: (LocalDate) -> Unit,
    onClose: () -> Unit,
) {
    val today = remember { LocalDate.now() }
    var month by remember { mutableStateOf(YearMonth.from(periodStart ?: today)) }
    val expected = periodStart?.plusDays(DEFAULT_CYCLE_DAYS)

    Column {
        SheetHeader("Log Cycle", onClose)
        Spacer(Modifier.height(6.dp))
        Text("When did your last period start?", style = MaterialTheme.typography.bodySmall, color = Muted)
        Spacer(Modifier.height(14.dp))

        SoftCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircleIconButton(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous month",
                    { month = month.minusMonths(1) }, size = 28.dp, iconSize = 16.dp,
                )
                Text(
                    text = month.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
                CircleIconButton(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next month",
                    { month = month.plusMonths(1) }, size = 28.dp, iconSize = 16.dp,
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth()) {
                listOf("M", "T", "W", "T", "F", "S", "S").forEach { d ->
                    Text(
                        d, style = MaterialTheme.typography.labelSmall, color = Muted,
                        textAlign = TextAlign.Center, modifier = Modifier.weight(1f),
                    )
                }
            }
            Spacer(Modifier.height(4.dp))

            val offset = month.atDay(1).dayOfWeek.value - 1 // Monday-first grid
            val daysInMonth = month.lengthOfMonth()
            val rows = (offset + daysInMonth + 6) / 7
            repeat(rows) { r ->
                Row(Modifier.fillMaxWidth()) {
                    repeat(7) { c ->
                        val day = r * 7 + c - offset + 1
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f)) {
                            if (day in 1..daysInMonth) {
                                val date = month.atDay(day)
                                CalendarDay(
                                    day = day,
                                    isSelected = date == periodStart,
                                    isExpected = date == expected,
                                    isToday = date == today,
                                    isFuture = date.isAfter(today),
                                    onClick = { onSelect(date) },
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(Rose))
            Spacer(Modifier.width(6.dp))
            Text("Last period start", style = MaterialTheme.typography.bodySmall, color = Muted)
            Spacer(Modifier.width(16.dp))
            Box(Modifier.size(8.dp).clip(CircleShape).border(1.dp, Rose, CircleShape))
            Spacer(Modifier.width(6.dp))
            Text("Next expected", style = MaterialTheme.typography.bodySmall, color = Muted)
        }

        Spacer(Modifier.height(14.dp))
        InfoCard(
            icon = Icons.Outlined.Opacity,
            title = if (periodStart == null) "Log your last period" else "Next period expected",
            body = if (periodStart == null || expected == null) {
                "Tap a date on the calendar above and Bloom will estimate your next period and let you know if it looks delayed."
            } else {
                "Based on a ${DEFAULT_CYCLE_DAYS}-day cycle, your next period is expected around " +
                    "${expected.format(DateTimeFormatter.ofPattern("MMMM d"))}."
            },
        )
        Spacer(Modifier.height(16.dp))
        PrimaryButton("Done", onClose)
    }
}

@Composable
private fun CalendarDay(
    day: Int,
    isSelected: Boolean,
    isExpected: Boolean,
    isToday: Boolean,
    isFuture: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(3.dp)
            .clip(CircleShape)
            .background(if (isSelected) Rose else Color.Transparent)
            .then(if (isExpected && !isSelected) Modifier.border(1.dp, Rose, CircleShape) else Modifier)
            .clickable(enabled = !isFuture, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "$day",
            style = MaterialTheme.typography.bodySmall,
            fontStyle = FontStyle.Normal,
            color = when {
                isSelected -> Color.White
                isFuture -> Muted.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.onSurface
            },
        )
        if (isToday && !isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 3.dp)
                    .size(3.dp)
                    .clip(CircleShape)
                    .background(Rose),
            )
        }
    }
}
