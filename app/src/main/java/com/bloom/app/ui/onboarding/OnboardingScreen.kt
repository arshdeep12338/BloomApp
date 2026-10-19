package com.bloom.app.ui.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bloom.app.ui.theme.LavenderLight
import com.bloom.app.ui.theme.MintLight
import com.bloom.app.ui.theme.PeachLight
import com.bloom.app.ui.theme.RoseLight
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val emoji: String,
    val title: String,
    val description: String,
    val background: Color,
)

// Placeholder copy; replace with the Figma content.
private val onboardingPages = listOf(
    OnboardingPage(
        emoji = "🌸",
        title = "Understand your cycle",
        description = "Track your period, ovulation and phases, and know what to expect each day of your cycle.",
        background = RoseLight,
    ),
    OnboardingPage(
        emoji = "🩺",
        title = "Support for PCOS",
        description = "Log symptoms, mood and energy to spot patterns and share clearer insights with your doctor.",
        background = LavenderLight,
    ),
    OnboardingPage(
        emoji = "🌿",
        title = "Daily wellness, simplified",
        description = "Gentle daily check-ins and tips on nutrition, sleep and movement tailored to you.",
        background = MintLight,
    ),
    OnboardingPage(
        emoji = "✨",
        title = "Let's bloom together",
        description = "Your data stays on your device. Start your first check-in and build healthy habits.",
        background = PeachLight,
    ),
)

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onFinished: () -> Unit,
) {
    val pages = remember { onboardingPages }
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.lastIndex
    val finish = { viewModel.completeOnboarding(onFinished) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
    ) {
        // Skip (hidden on the last page, but the row keeps its height so nothing jumps).
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!isLastPage) {
                TextButton(onClick = finish) { Text("Skip") }
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
        ) { index ->
            OnboardingPageContent(pages[index])
        }

        PagerIndicator(
            pagerState = pagerState,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 16.dp),
        )

        Button(
            onClick = {
                if (isLastPage) {
                    finish()
                } else {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .height(56.dp),
        ) {
            Text(
                text = if (isLastPage) "Get Started" else "Next",
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Hero size is a fraction of the available width, so it scales on every screen.
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .aspectRatio(1f)
                .clip(CircleShape)
                .background(page.background),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = page.emoji, fontSize = 88.sp)
        }
        Spacer(Modifier.height(40.dp))
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PagerIndicator(pagerState: PagerState, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pagerState.pageCount) { index ->
            val selected = pagerState.currentPage == index
            val width by animateDpAsState(if (selected) 28.dp else 8.dp, label = "indicatorWidth")
            val color by animateColorAsState(
                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                label = "indicatorColor",
            )
            Box(
                modifier = Modifier
                    .size(width = width, height = 8.dp)
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}
