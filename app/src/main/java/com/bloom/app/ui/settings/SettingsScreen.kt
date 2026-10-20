package com.bloom.app.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bloom.app.data.Goal
import com.bloom.app.ui.components.BloomSwitch
import com.bloom.app.ui.components.CircleIconButton
import com.bloom.app.ui.components.IconTile
import com.bloom.app.ui.components.ScreenHeader
import com.bloom.app.ui.components.SectionTitle
import com.bloom.app.ui.components.SoftCard
import com.bloom.app.ui.components.accent
import com.bloom.app.ui.components.icon
import com.bloom.app.ui.main.MainUiState
import com.bloom.app.ui.main.MainViewModel
import com.bloom.app.ui.main.SheetType
import com.bloom.app.ui.sheets.reminderLabel
import com.bloom.app.ui.theme.Border
import com.bloom.app.ui.theme.Muted
import com.bloom.app.ui.theme.Rose
import com.bloom.app.ui.theme.RoseMid
import com.bloom.app.ui.theme.RoseSoft

private const val APP_VERSION = "1.0 · Prototype"

@Composable
fun SettingsScreen(
    state: MainUiState,
    viewModel: MainViewModel,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val prefs = state.prefs

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        ScreenHeader("Settings", "Your profile and daily goal targets")
        Spacer(Modifier.height(14.dp))

        // Profile
        SoftCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(0.dp)) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(RoseMid.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.Person, null, tint = Rose, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(prefs.userName.ifBlank { "Your name" }, style = MaterialTheme.typography.titleSmall)
                    Text(
                        "PCOS journey · ${prefs.journeyStage?.label ?: "Not set"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
                }
            }
            HorizontalDivider(color = Border)
            SettingsRow(
                title = "Edit profile",
                onClick = { viewModel.openSheet(SheetType.EditProfile) },
            )
        }
        Spacer(Modifier.height(20.dp))

        SectionTitle("What you're tracking")
        Spacer(Modifier.height(8.dp))
        SoftCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = 4.dp)) {
            Goal.entries.forEach { goal ->
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconTile(goal.icon(), tint = goal.accent(), size = 30.dp)
                    Spacer(Modifier.width(12.dp))
                    Text(goal.title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    BloomSwitch(
                        checked = goal in prefs.trackedGoals,
                        onCheckedChange = { viewModel.setGoalTracked(goal, it) },
                    )
                }
            }
        }
        Spacer(Modifier.height(20.dp))

        SectionTitle("Daily goal targets")
        Spacer(Modifier.height(8.dp))
        SoftCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = 4.dp)) {
            Goal.entries.filter { it.measurable }.forEach { goal ->
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconTile(goal.icon(), tint = goal.accent(), size = 30.dp)
                    Spacer(Modifier.width(12.dp))
                    Text(goal.title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    CircleIconButton(
                        Icons.Filled.Remove, "Decrease ${goal.title} target",
                        { viewModel.adjustTarget(goal, -1) }, size = 24.dp, iconSize = 12.dp,
                    )
                    Text(
                        "${prefs.targetOf(goal)} ${goal.unit}",
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(min = 72.dp),
                    )
                    CircleIconButton(
                        Icons.Filled.Add, "Increase ${goal.title} target",
                        { viewModel.adjustTarget(goal, +1) }, size = 24.dp, iconSize = 12.dp,
                    )
                }
            }
        }
        Spacer(Modifier.height(20.dp))

        // Notifications & app
        SoftCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(0.dp)) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Notifications", style = MaterialTheme.typography.titleSmall)
                    Text("Daily reminders to log", style = MaterialTheme.typography.bodySmall, color = Muted)
                }
                BloomSwitch(
                    checked = prefs.notificationsEnabled,
                    onCheckedChange = viewModel::setNotificationsEnabled,
                )
            }
            HorizontalDivider(color = Border)
            SettingsRow(
                title = "Reminder time",
                subtitle = reminderLabel(prefs.reminderMinutes),
                onClick = { viewModel.openSheet(SheetType.ReminderTime) },
            )
            HorizontalDivider(color = Border)
            SettingsRow(
                title = "Live notification preview",
                subtitle = "See lock-screen updates",
                onClick = { viewModel.openSheet(SheetType.NotificationPreview) },
            )
            HorizontalDivider(color = Border)
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("App version", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Text(APP_VERSION, style = MaterialTheme.typography.bodySmall, color = Muted)
            }
        }
        Spacer(Modifier.height(14.dp))

        OutlinedButton(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Border),
        ) {
            Text("Sign out", style = MaterialTheme.typography.titleSmall, color = Rose)
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun SettingsRow(title: String, onClick: () -> Unit, subtitle: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Muted)
            }
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Muted)
    }
}
