package com.bloom.app.ui.learn

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import androidx.compose.ui.unit.dp
import com.bloom.app.data.FaqItem
import com.bloom.app.data.LearnContent
import com.bloom.app.ui.components.ScreenHeader
import com.bloom.app.ui.components.SectionTitle
import com.bloom.app.ui.components.SoftCard
import com.bloom.app.ui.theme.Muted

@Composable
fun LearnScreen(modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 12.dp),
    ) {
        item {
            ScreenHeader("Learn", "Understand PCOS, at your own pace")
            Spacer(Modifier.height(16.dp))
        }
        LearnContent.sections.forEach { section ->
            item(key = "header-${section.title}") {
                SectionTitle(section.title)
                Spacer(Modifier.height(8.dp))
            }
            items(section.items, key = { it.question }) { faq ->
                FaqRow(
                    faq = faq,
                    expanded = expanded == faq.question,
                    onToggle = { expanded = if (expanded == faq.question) null else faq.question },
                )
                Spacer(Modifier.height(8.dp))
            }
            item(key = "gap-${section.title}") { Spacer(Modifier.height(10.dp)) }
        }
        item {
            Text(
                "General information only — not medical advice. Talk to your doctor about your own care.",
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
            )
        }
    }
}

@Composable
private fun FaqRow(faq: FaqItem, expanded: Boolean, onToggle: () -> Unit) {
    SoftCard(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        onClick = onToggle,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                faq.question,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = if (expanded) Icons.Filled.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = Muted,
            )
        }
        AnimatedVisibility(visible = expanded) {
            Text(
                faq.answer,
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}
