package com.bloom.app.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloom.app.ads.AdAnalytics
import com.bloom.app.ads.AdaptiveBannerAd
import com.bloom.app.data.Goal
import com.bloom.app.ui.dashboard.DashboardScreen
import com.bloom.app.ui.insights.InsightsScreen
import com.bloom.app.ui.learn.LearnScreen
import com.bloom.app.ui.settings.SettingsScreen
import com.bloom.app.ui.sheets.CounterSheetContent
import com.bloom.app.ui.sheets.CycleSheetContent
import com.bloom.app.ui.sheets.EditProfileSheetContent
import com.bloom.app.ui.sheets.MealsSheetContent
import com.bloom.app.ui.sheets.NotificationPreviewSheetContent
import com.bloom.app.ui.sheets.ReminderSheetContent
import com.bloom.app.ui.sheets.SymptomsSheetContent
import com.bloom.app.ui.theme.Cream
import com.bloom.app.ui.theme.Muted
import com.bloom.app.ui.theme.Rose
import com.bloom.app.ui.theme.RoseMid
import kotlinx.coroutines.launch

/**
 * The signed-in area. Layout, top to bottom:
 *
 *   [ tab content ]
 *   [ adaptive banner ]   ← Dashboard only, in its own layout slot (never overlaps content)
 *   [ bottom navigation ]
 */
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    analytics: AdAnalytics,
    onSignedOut: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.messageShown()
        }
    }

    Scaffold(
        containerColor = Cream,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column {
                if (state.selectedTab == MainTab.Dashboard) {
                    AdaptiveBannerAd(analytics = analytics)
                }
                BloomNavBar(selected = state.selectedTab, onSelect = viewModel::selectTab)
            }
        },
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding).statusBarsPadding()
        when (state.selectedTab) {
            MainTab.Dashboard -> DashboardScreen(state, viewModel, contentModifier)
            MainTab.Insights -> InsightsScreen(state, viewModel, contentModifier)
            MainTab.Learn -> LearnScreen(contentModifier)
            MainTab.Settings -> SettingsScreen(
                state = state,
                viewModel = viewModel,
                onSignOut = { viewModel.signOut(onSignedOut) },
                modifier = contentModifier,
            )
        }
    }

    SheetHost(state = state, viewModel = viewModel)
}

@Composable
private fun BloomNavBar(selected: MainTab, onSelect: (MainTab) -> Unit) {
    NavigationBar(containerColor = Color.White) {
        MainTab.entries.forEach { tab ->
            val isSelected = tab == selected
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelect(tab) },
                icon = { Icon(tab.icon(isSelected), contentDescription = tab.label) },
                label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Rose,
                    selectedTextColor = Rose,
                    unselectedIconColor = Muted,
                    unselectedTextColor = Muted,
                    indicatorColor = Color.Transparent,
                ),
            )
        }
    }
}

private fun MainTab.icon(selected: Boolean): ImageVector = when (this) {
    MainTab.Dashboard -> if (selected) Icons.Filled.Home else Icons.Outlined.Home
    MainTab.Insights -> if (selected) Icons.Filled.Insights else Icons.Outlined.Insights
    MainTab.Learn -> if (selected) Icons.Filled.AutoStories else Icons.Outlined.AutoStories
    MainTab.Settings -> if (selected) Icons.Filled.Settings else Icons.Outlined.Settings
}

/** Shows whichever bottom sheet [MainUiState.sheet] points at. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SheetHost(state: MainUiState, viewModel: MainViewModel) {
    val sheet = state.sheet ?: return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // Animate the sheet away, then clear the state.
    val close: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion { viewModel.closeSheet() }
    }
    val prefs = state.prefs
    val log = state.log

    ModalBottomSheet(
        onDismissRequest = viewModel::closeSheet,
        sheetState = sheetState,
        containerColor = Cream,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .background(RoseMid, CircleShape),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
        ) {
            when (sheet) {
                SheetType.Meals -> MealsSheetContent(log.meals, viewModel::setMeal, close)

                SheetType.Movement, SheetType.Water, SheetType.Relaxation, SheetType.Sleep -> {
                    val goal = when (sheet) {
                        SheetType.Movement -> Goal.Movement
                        SheetType.Water -> Goal.Water
                        SheetType.Relaxation -> Goal.Relaxation
                        else -> Goal.Sleep
                    }
                    CounterSheetContent(
                        goal = goal,
                        current = log.progressOf(goal),
                        target = prefs.targetOf(goal),
                        onChange = { delta -> viewModel.addProgress(goal, delta) },
                        onOpenSettings = { viewModel.selectTab(MainTab.Settings) },
                        onClose = close,
                    )
                }

                SheetType.Cycle -> CycleSheetContent(log.periodStart, viewModel::setPeriodStart, close)
                SheetType.Symptoms -> SymptomsSheetContent(log.symptoms, viewModel::setSymptom, close)

                SheetType.EditProfile -> EditProfileSheetContent(
                    initialName = prefs.userName,
                    initialStage = prefs.journeyStage,
                    onSave = viewModel::saveProfile,
                    onClose = close,
                )

                SheetType.ReminderTime -> ReminderSheetContent(
                    initialMinutes = prefs.reminderMinutes,
                    onSave = viewModel::setReminder,
                    onClose = close,
                )

                SheetType.NotificationPreview -> NotificationPreviewSheetContent(
                    completedGoals = state.completedCount,
                    totalGoals = state.progress.size,
                    waterCurrent = log.waterGlasses,
                    waterTarget = prefs.targetOf(Goal.Water),
                    onClose = close,
                )
            }
        }
    }
}
