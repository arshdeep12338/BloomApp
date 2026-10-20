package com.bloom.app.ui.components

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bloom.app.data.ReminderSlot
import com.bloom.app.ui.theme.Border
import com.bloom.app.ui.theme.Muted
import com.bloom.app.ui.util.formatMinutes

/**
 * Time-of-day choices plus a custom time field. Shared by the last onboarding step and the
 * "Reminder Time" sheet. [minutes] is minutes after midnight, or null when nothing is chosen.
 */
@Composable
fun ReminderPicker(
    minutes: Int?,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(modifier = modifier.fillMaxWidth()) {
        ReminderSlot.entries.forEach { slot ->
            ChoiceCard(
                title = slot.label,
                hint = "${formatMinutes(slot.minutes)} — ${slot.hint}",
                selected = minutes == slot.minutes,
                onClick = { onChange(slot.minutes) },
            )
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(4.dp))
        Text("Or set a custom time", style = MaterialTheme.typography.bodySmall, color = Muted)
        Spacer(Modifier.height(8.dp))

        val shape = RoundedCornerShape(12.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(Color.White)
                .border(1.dp, Border, shape)
                .clickable {
                    val initial = minutes ?: 8 * 60
                    TimePickerDialog(
                        context,
                        { _, hour, minute -> onChange(hour * 60 + minute) },
                        initial / 60,
                        initial % 60,
                        false,
                    ).show()
                }
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = minutes?.let { formatMinutes(it, "hh:mm a") } ?: "Select a time",
                style = MaterialTheme.typography.bodyMedium,
                color = if (minutes == null) Muted else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Icon(
                Icons.Outlined.AccessTime,
                contentDescription = "Pick a time",
                tint = Muted,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
