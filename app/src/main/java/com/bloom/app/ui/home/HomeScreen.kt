package com.bloom.app.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloom.app.ads.AdAnalytics
import com.bloom.app.ads.AdaptiveBannerAd

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    analytics: AdAnalytics,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onMessageShown()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Layout slot of its own: [content] → [banner] → [navigation]. The banner can
            // therefore never overlap content or the nav bar, and Scaffold insets the
            // content area by this bar's measured height.
            Column {
                if (state.selectedTab == HomeTab.Home) {
                    AdaptiveBannerAd(analytics = analytics)
                }
                BloomBottomBar(selected = state.selectedTab, onSelect = viewModel::onTabSelected)
            }
        },
    ) { innerPadding ->
        when (state.selectedTab) {
            HomeTab.Home -> HomeContent(
                state = state,
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding),
            )
            else -> ComingSoon(tab = state.selectedTab, modifier = Modifier.padding(innerPadding))
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        GreetingHeader(name = state.userName)
        Spacer(Modifier.height(20.dp))

        CycleCard(state = state, onLogPeriod = { viewModel.onQuickAction(QuickAction.LogPeriod) })
        Spacer(Modifier.height(16.dp))

        WeekStrip(days = state.week, selectedIndex = state.selectedDayIndex, onSelect = viewModel::onDaySelected)
        Spacer(Modifier.height(24.dp))

        SectionHeader(title = "Daily check-in")
        Spacer(Modifier.height(8.dp))
        CheckInCard(
            state = state,
            onMoodSelected = viewModel::onMoodSelected,
            onEnergyChanged = viewModel::onEnergyChanged,
            onSave = viewModel::onSaveCheckIn,
        )
        Spacer(Modifier.height(24.dp))

        SectionHeader(title = "Symptoms")
        Spacer(Modifier.height(4.dp))
        SymptomChips(selected = state.selectedSymptoms, onToggle = viewModel::onSymptomToggled)
        Spacer(Modifier.height(24.dp))

        SectionHeader(title = "Quick actions")
        Spacer(Modifier.height(8.dp))
        QuickActionsRow(onAction = viewModel::onQuickAction)
        Spacer(Modifier.height(24.dp))

        SectionHeader(title = "Health insights")
        Spacer(Modifier.height(8.dp))
        InsightsRow(insights = state.insights)
        Spacer(Modifier.height(24.dp))

        SectionHeader(title = "Recommended for you", actionText = "See all")
        Spacer(Modifier.height(8.dp))
        state.articles.forEach { article ->
            ArticleCard(article = article, onClick = { viewModel.onArticleClicked(article) })
            Spacer(Modifier.height(10.dp))
        }
        Spacer(Modifier.height(8.dp))
    }
}
