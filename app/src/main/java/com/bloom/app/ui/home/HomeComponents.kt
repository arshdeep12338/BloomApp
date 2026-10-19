package com.bloom.app.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/* ------------------------------------------------------------------------ *
 * Reusable building blocks for the Home screen. None of them hardcode a
 * screen width: sizes come from weights, fractions and aspect ratios.
 * ------------------------------------------------------------------------ */

@Composable
fun BloomCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, actionText: String? = null, onAction: () -> Unit = {}) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        if (actionText != null) {
            TextButton(onClick = onAction) { Text(actionText) }
        }
    }
}

@Composable
fun GreetingHeader(name: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Hello, $name 🌸",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "Here's how your day looks",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "🔔", fontSize = 20.sp)
        }
    }
}

@Composable
fun CycleCard(state: HomeUiState, onLogPeriod: () -> Unit, modifier: Modifier = Modifier) {
    BloomCard(modifier = modifier, containerColor = MaterialTheme.colorScheme.primary) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.phase,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.85f),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Next period in ${state.nextPeriodInDays} days",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Cycle length: ${state.cycleLength} days",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onLogPeriod,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                ) { Text("Log period") }
            }
            Spacer(Modifier.width(16.dp))
            CycleRing(
                progress = state.cycleProgress,
                day = state.cycleDay,
                modifier = Modifier.weight(0.7f),
            )
        }
    }
}

@Composable
private fun CycleRing(progress: Float, day: Int, modifier: Modifier = Modifier) {
    Box(modifier = modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            val strokeWidth = 12.dp.toPx()
            val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            val inset = strokeWidth / 2f
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = androidx.compose.ui.geometry.Offset(inset, inset)
            drawArc(
                color = Color.White.copy(alpha = 0.3f),
                startAngle = -90f, sweepAngle = 360f, useCenter = false,
                topLeft = topLeft, size = arcSize, style = stroke,
            )
            drawArc(
                color = Color.White,
                startAngle = -90f, sweepAngle = 360f * progress, useCenter = false,
                topLeft = topLeft, size = arcSize, style = stroke,
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Day", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.85f))
            Text("$day", style = MaterialTheme.typography.headlineLarge, color = Color.White)
        }
    }
}

@Composable
fun WeekStrip(
    days: List<WeekDay>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        days.forEachIndexed { index, day ->
            val selected = index == selectedIndex
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                    .clickable { onSelect(index) }
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = day.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${day.dayOfMonth}",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .height(5.dp)
                        .width(5.dp)
                        .clip(CircleShape)
                        .background(
                            if (day.isToday) {
                                if (selected) Color.White else MaterialTheme.colorScheme.primary
                            } else {
                                Color.Transparent
                            },
                        ),
                )
            }
        }
    }
}

@Composable
fun CheckInCard(
    state: HomeUiState,
    onMoodSelected: (Mood) -> Unit,
    onEnergyChanged: (Float) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BloomCard(modifier = modifier) {
        Text("How are you feeling today?", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Mood.entries.forEach { mood ->
                val selected = state.selectedMood == mood
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (selected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.background,
                        )
                        .clickable { onMoodSelected(mood) }
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(mood.emoji, fontSize = 26.sp)
                    Text(
                        text = mood.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Energy", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
            Text(
                text = when {
                    state.energy < 0.34f -> "Low"
                    state.energy < 0.67f -> "Medium"
                    else -> "High"
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Slider(value = state.energy, onValueChange = onEnergyChanged)
        Spacer(Modifier.height(8.dp))
        Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
            Text(if (state.checkInSaved) "Saved ✓" else "Save check-in")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SymptomChips(
    selected: Set<Symptom>,
    onToggle: (Symptom) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Symptom.entries.forEach { symptom ->
            FilterChip(
                selected = symptom in selected,
                onClick = { onToggle(symptom) },
                label = { Text(symptom.label) },
            )
        }
    }
}

@Composable
fun QuickActionsRow(onAction: (QuickAction) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        QuickAction.entries.forEach { action ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onAction(action) },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(action.emoji, fontSize = 26.sp)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = action.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
fun InsightsRow(insights: List<Insight>, modifier: Modifier = Modifier) {
    // Card width is a share of the available width: ~1.3 cards visible hints at scrolling.
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val cardWidth = maxWidth * 0.72f
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            insights.forEach { insight ->
                Card(
                    modifier = Modifier.width(cardWidth).heightIn(min = 150.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = insight.background),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(insight.emoji, fontSize = 28.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(insight.title, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = insight.body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ArticleCard(article: Article, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .weight(0.2f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(article.background),
                contentAlignment = Alignment.Center,
            ) {
                Text(article.emoji, fontSize = 28.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(0.8f)) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = article.meta,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
fun BloomBottomBar(selected: HomeTab, onSelect: (HomeTab) -> Unit, modifier: Modifier = Modifier) {
    val icons: Map<HomeTab, ImageVector> = mapOf(
        HomeTab.Home to Icons.Filled.Home,
        HomeTab.Track to Icons.Filled.DateRange,
        HomeTab.Insights to Icons.Filled.Star,
        HomeTab.Profile to Icons.Filled.Person,
    )
    NavigationBar(modifier = modifier, containerColor = MaterialTheme.colorScheme.surface) {
        HomeTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = tab == selected,
                onClick = { onSelect(tab) },
                icon = { Icon(icons.getValue(tab), contentDescription = tab.label) },
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }
    }
}

@Composable
fun ComingSoon(tab: HomeTab, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text("${tab.label} is coming soon", style = MaterialTheme.typography.titleMedium)
        }
    }
}
