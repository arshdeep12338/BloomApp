package com.bloom.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bloom.app.data.DEFAULT_CYCLE_DAYS
import com.bloom.app.data.Goal
import com.bloom.app.data.GoalProgress
import com.bloom.app.data.LearnContent
import com.bloom.app.data.SymptomCategory
import com.bloom.app.ui.components.BloomFlower
import com.bloom.app.ui.components.CircleIconButton
import com.bloom.app.ui.components.IconTile
import com.bloom.app.ui.components.ProgressRing
import com.bloom.app.ui.components.SecondaryButton
import com.bloom.app.ui.components.SectionTitle
import com.bloom.app.ui.components.SoftCard
import com.bloom.app.ui.components.ThinProgress
import com.bloom.app.ui.components.accent
import com.bloom.app.ui.components.icon
import com.bloom.app.ui.main.MainTab
import com.bloom.app.ui.main.MainUiState
import com.bloom.app.ui.main.MainViewModel
import com.bloom.app.ui.main.SheetType
import com.bloom.app.ui.main.sheet
import com.bloom.app.ui.theme.Border
import com.bloom.app.ui.theme.Gold
import com.bloom.app.ui.theme.Muted
import com.bloom.app.ui.theme.Rose
import com.bloom.app.ui.theme.RoseMid
import com.bloom.app.ui.theme.RoseSoft
import com.bloom.app.ui.theme.TipBackground
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DashboardScreen(
    state: MainUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
) {
    val prefs = state.prefs
    val tracked = prefs.trackedGoals

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        GreetingRow(name = prefs.userName, onProfile = { viewModel.openSheet(SheetType.EditProfile) })
        Spacer(Modifier.height(16.dp))

        TodaysGoalsCard(done = state.completedCount, total = state.progress.size)
        Spacer(Modifier.height(20.dp))

        FaqCarousel(onOpen = { viewModel.selectTab(MainTab.Learn) })
        Spacer(Modifier.height(16.dp))

        if (Goal.Cycle in tracked) {
            val start = state.log.periodStart
            ShortcutCard(
                goal = Goal.Cycle,
                title = "Cycle",
                subtitle = if (start == null) {
                    "Log your last period date"
                } else {
                    "Last period ${start.format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()))} · " +
                        "next around ${start.plusDays(DEFAULT_CYCLE_DAYS).format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()))}"
                },
                onClick = { viewModel.openSheet(SheetType.Cycle) },
            )
            Spacer(Modifier.height(10.dp))
        }
        if (Goal.Symptoms in tracked) {
            val logged = state.log.symptoms.size
            ShortcutCard(
                goal = Goal.Symptoms,
                title = "Symptom check-in",
                subtitle = if (logged == 0) {
                    "Bloating, skin, mood, sleep quality"
                } else {
                    "$logged of ${SymptomCategory.entries.size} logged today"
                },
                onClick = { viewModel.openSheet(SheetType.Symptoms) },
            )
            Spacer(Modifier.height(10.dp))
        }

        Spacer(Modifier.height(10.dp))
        SectionTitle("Goal Progress")
        Spacer(Modifier.height(10.dp))
        if (state.progress.isEmpty()) {
            Text(
                "No daily goals selected. Turn some on in Settings to see them here.",
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
            )
        } else {
            state.progress.forEach { item ->
                GoalProgressRow(item, onClick = { viewModel.openSheet(item.goal.sheet()) })
                Spacer(Modifier.height(8.dp))
            }
        }

        if (Goal.Sleep in tracked && state.log.sleepHrs == 0 && !state.tipDismissed) {
            Spacer(Modifier.height(14.dp))
            SectionTitle("Today's tip")
            Spacer(Modifier.height(10.dp))
            TipCard(
                onRemindLater = {
                    viewModel.dismissTip()
                    viewModel.showMessage("OK — we'll remind you later")
                },
                onDone = {
                    viewModel.dismissTip()
                    viewModel.openSheet(SheetType.Sleep)
                },
            )
        }
        Spacer(Modifier.height(12.dp))
    }
}

private fun greetingFor(time: LocalTime): String = when (time.hour) {
    in 5..11 -> "GOOD MORNING,"
    in 12..16 -> "GOOD AFTERNOON,"
    else -> "GOOD EVENING,"
}

@Composable
private fun GreetingRow(name: String, onProfile: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.Bottom) {
            Text(
                text = greetingFor(LocalTime.now()),
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
                modifier = Modifier.padding(bottom = 3.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = name.ifBlank { "friend" },
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        CircleIconButton(
            icon = Icons.Outlined.Person,
            contentDescription = "Edit profile",
            onClick = onProfile,
            size = 34.dp,
            container = RoseMid.copy(alpha = 0.6f),
        )
    }
}

@Composable
private fun TodaysGoalsCard(done: Int, total: Int) {
    val shape = RoundedCornerShape(24.dp)
    val headline = when {
        total == 0 -> "Pick your goals in Settings"
        done >= total -> "Fully bloomed"
        done > 0 -> "Keep going"
        else -> "Let's get blooming"
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = Color.White,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(Color.White, RoseSoft)))
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Today's Goals", style = MaterialTheme.typography.headlineMedium)
                Text(
                    LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())),
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
                Spacer(Modifier.height(14.dp))
                Text(headline, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(2.dp))
                Text(
                    "$done of $total complete",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
                Spacer(Modifier.height(8.dp))
                ThinProgress(fraction = if (total == 0) 0f else done / total.toFloat())
            }
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier.weight(0.8f),
                contentAlignment = Alignment.Center,
            ) {
                BloomFlower(
                    modifier = Modifier.fillMaxWidth(0.8f),
                    petalColor = RoseMid.copy(alpha = if (done == 0) 0.55f else 1f),
                    centerColor = Gold.copy(alpha = if (done == 0) 0.5f else 1f),
                )
                if (done == 0 && total > 0) {
                    Text(
                        "Start here",
                        style = MaterialTheme.typography.titleLarge,
                        color = Rose,
                    )
                }
            }
        }
    }
}

@Composable
private fun FaqCarousel(onOpen: () -> Unit) {
    val faqs = LearnContent.allFaqs
    val pagerState = rememberPagerState(pageCount = { faqs.size })

    SectionTitle("PCOS FAQ")
    Spacer(Modifier.height(10.dp))
    HorizontalPager(
        state = pagerState,
        pageSpacing = 12.dp,
        modifier = Modifier.fillMaxWidth(),
    ) { index ->
        val faq = faqs[index]
        SoftCard(modifier = Modifier.fillMaxWidth(), onClick = onOpen) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        faq.question,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        faq.answer,
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Muted,
                )
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(faqs.size) { index ->
            val selected = pagerState.currentPage == index
            Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .size(width = if (selected) 12.dp else 4.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(if (selected) Rose else RoseMid),
            )
        }
    }
}

@Composable
private fun ShortcutCard(goal: Goal, title: String, subtitle: String, onClick: () -> Unit) {
    SoftCard(
        modifier = Modifier.fillMaxWidth(),
        color = RoseSoft,
        onClick = onClick,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(goal.icon(), tint = goal.accent(), container = Color.White)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Muted)
        }
    }
}

@Composable
private fun GoalProgressRow(item: GoalProgress, onClick: () -> Unit) {
    SoftCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProgressRing(
                fraction = item.fraction,
                color = item.goal.accent(),
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    item.goal.icon(),
                    contentDescription = null,
                    tint = item.goal.accent(),
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.goal.title, style = MaterialTheme.typography.titleSmall)
                Text(item.label, style = MaterialTheme.typography.bodySmall, color = Muted)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Muted)
        }
    }
}

@Composable
private fun TipCard(onRemindLater: () -> Unit, onDone: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TipBackground)
            .padding(14.dp),
    ) {
        Text(
            "LOG LAST NIGHT'S SLEEP",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Rose,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "You haven't logged sleep yet. Consistent, close-to-8-hr nights can help keep your hormones more balanced — avoiding phones and screens before bed makes it easier to fall asleep.",
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SecondaryButton("Remind me later", onRemindLater, modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Rose)
                    .clickable(onClick = onDone),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text("Mark as done", style = MaterialTheme.typography.titleSmall, color = Color.White)
            }
        }
    }
}
