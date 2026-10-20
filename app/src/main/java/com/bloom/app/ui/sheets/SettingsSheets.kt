package com.bloom.app.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bloom.app.data.JourneyStage
import com.bloom.app.ui.components.ChoiceCard
import com.bloom.app.ui.components.PrimaryButton
import com.bloom.app.ui.components.ReminderPicker
import com.bloom.app.ui.components.SecondaryButton
import com.bloom.app.ui.components.SheetHeader
import com.bloom.app.ui.theme.Border
import com.bloom.app.ui.theme.Muted
import com.bloom.app.ui.theme.Rose
import com.bloom.app.ui.theme.RoseMid
import com.bloom.app.ui.theme.RoseSoft
import com.bloom.app.ui.util.formatMinutes
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun EditProfileSheetContent(
    initialName: String,
    initialStage: JourneyStage?,
    onSave: (name: String, stage: JourneyStage?) -> Unit,
    onClose: () -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var stage by remember { mutableStateOf(initialStage) }

    Column {
        SheetHeader("Edit Profile", onClose)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it.take(30) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Your first name", color = Muted) },
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Rose,
                unfocusedBorderColor = Border,
                cursorColor = Rose,
            ),
        )
        Spacer(Modifier.height(16.dp))
        Text("Journey stage", style = MaterialTheme.typography.bodySmall, color = Muted)
        Spacer(Modifier.height(8.dp))
        JourneyStage.entries.forEach { option ->
            ChoiceCard(
                title = option.label,
                hint = option.hint,
                selected = stage == option,
                onClick = { stage = option },
            )
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.height(8.dp))
        PrimaryButton(
            text = "Save",
            enabled = name.isNotBlank(),
            onClick = {
                onSave(name.trim(), stage)
                onClose()
            },
        )
    }
}
@Composable
fun ReminderSheetContent(
    initialMinutes: Int?,
    onSave: (Int?) -> Unit,
    onClose: () -> Unit,
) {
    var minutes by remember { mutableStateOf(initialMinutes) }

    Column {
        SheetHeader("Reminder Time", onClose)
        Spacer(Modifier.height(6.dp))
        Text(
            "When should Bloom nudge you to log your day?",
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
        )
        Spacer(Modifier.height(14.dp))
        ReminderPicker(minutes = minutes, onChange = { minutes = it })
        Spacer(Modifier.height(16.dp))
        PrimaryButton(
            text = "Save",
            onClick = {
                onSave(minutes)
                onClose()
            },
        )
    }
}

private enum class PreviewTab(val label: String) { Meditation("Meditation"), DailyGoal("Daily goal"), Water("Water") }

private const val MEDITATION_SECONDS = 300

@Composable
fun NotificationPreviewSheetContent(
    completedGoals: Int,
    totalGoals: Int,
    waterCurrent: Int,
    waterTarget: Int,
    onClose: () -> Unit,
) {
    var tab by remember { mutableStateOf(PreviewTab.Meditation) }
    var remaining by remember { mutableIntStateOf(MEDITATION_SECONDS) }
    var running by remember { mutableStateOf(false) }

    // Counts down once a second while a "session" is running (the live-update demo).
    LaunchedEffect(running) {
        while (running && remaining > 0) {
            delay(1_000)
            remaining -= 1
        }
        if (remaining <= 0) running = false
    }

    val now = remember { LocalTime.now() }
    val today = remember { LocalDate.now() }

    Column {
        SheetHeader("Live Notification Preview", onClose)
        Spacer(Modifier.height(6.dp))
        Text(
            "A preview of how Bloom could show live, real-time updates on the lock screen.",
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
        )
        Spacer(Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            PreviewTab.entries.forEach { option ->
                val selected = option == tab
                val shape = RoundedCornerShape(10.dp)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(shape)
                        .background(if (selected) Color.White else RoseSoft)
                        .border(1.dp, if (selected) Rose else Color.Transparent, shape)
                        .clickable { tab = option }
                        .padding(vertical = 9.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center,
                ) {
                    Text(
                        option.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) Rose else Muted,
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth(0.72f)
                .align(androidx.compose.ui.Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF0B0B0B))
                .padding(16.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        ) {
            Text(
                now.format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())),
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(
                today.format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())),
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp,
            )
            Spacer(Modifier.height(14.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF2A2A2A))
                    .padding(12.dp),
            ) {
                Row(Modifier.fillMaxWidth()) {
                    Text("Bloom", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, modifier = Modifier.weight(1f))
                    Text("now", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = when (tab) {
                        PreviewTab.Meditation -> "Meditation: ${formatCountdown(remaining)} remaining"
                        PreviewTab.DailyGoal -> "Today's goals: $completedGoals of $totalGoals complete"
                        PreviewTab.Water -> "Water: $waterCurrent of $waterTarget glasses"
                    },
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(8.dp))
                val fraction = when (tab) {
                    PreviewTab.Meditation -> 1f - remaining / MEDITATION_SECONDS.toFloat()
                    PreviewTab.DailyGoal -> if (totalGoals == 0) 0f else completedGoals / totalGoals.toFloat()
                    PreviewTab.Water -> if (waterTarget == 0) 0f else (waterCurrent / waterTarget.toFloat()).coerceIn(0f, 1f)
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.2f)),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(fraction.coerceIn(0f, 1f))
                            .height(3.dp)
                            .background(Color.White),
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        val isMeditation = tab == PreviewTab.Meditation
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            PrimaryButton(
                text = if (running) "Pause" else "Begin",
                onClick = { if (remaining > 0) running = !running },
                enabled = isMeditation,
                modifier = Modifier.weight(1f),
            )
            SecondaryButton(
                text = "Reset",
                onClick = { running = false; remaining = MEDITATION_SECONDS },
                enabled = isMeditation,
                modifier = Modifier.weight(1f).height(52.dp),
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "Updates live while a meditation session is running — same pattern as an order-tracking or workout notification.",
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
    }
}

private fun formatCountdown(seconds: Int): String = "%d:%02d".format(seconds / 60, seconds % 60)

fun reminderLabel(minutes: Int?): String = minutes?.let { formatMinutes(it) } ?: "Not set"
