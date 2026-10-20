package com.bloom.app.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloom.app.data.Goal
import com.bloom.app.ui.components.BloomFlower
import com.bloom.app.ui.components.BloomSwitch
import com.bloom.app.ui.components.CircleIconButton
import com.bloom.app.ui.components.IconTile
import com.bloom.app.ui.components.PrimaryButton
import com.bloom.app.ui.components.ReminderPicker
import com.bloom.app.ui.components.SoftCard
import com.bloom.app.ui.components.StepProgress
import com.bloom.app.ui.components.accent
import com.bloom.app.ui.components.icon
import com.bloom.app.ui.theme.Border
import com.bloom.app.ui.theme.Cream
import com.bloom.app.ui.theme.Muted
import com.bloom.app.ui.theme.Rose
import com.bloom.app.ui.theme.RoseMid

/**
 * Four-step onboarding from the Figma:
 *  1 Welcome → 2 "A little about you" → 3 "What would you like to track daily?" → 4 reminder time.
 * "Enter Bloom" / "I'll set this up later" persist the choices and the completed flag, then call [onFinished].
 */
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onFinished: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    BackHandler(enabled = state.step > 0) { viewModel.back() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .systemBarsPadding()
            .imePadding()
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Keeps the progress bar from shifting when the back button appears.
            Box(modifier = Modifier.size(32.dp)) {
                if (state.step > 0) {
                    CircleIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        onClick = viewModel::back,
                        size = 28.dp,
                        iconSize = 14.dp,
                    )
                }
            }
            StepProgress(
                total = ONBOARDING_STEPS,
                filled = state.step + 1,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(32.dp))
        }
        Spacer(Modifier.height(12.dp))

        AnimatedContent(
            targetState = state.step,
            modifier = Modifier.weight(1f),
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "onboardingStep",
        ) { step ->
            when (step) {
                0 -> WelcomeStep(onContinue = viewModel::next)
                1 -> NameStep(
                    name = state.name,
                    onNameChange = viewModel::setName,
                    canContinue = state.canContinue,
                    onContinue = viewModel::next,
                )
                2 -> GoalsStep(
                    selected = state.goals,
                    onToggle = viewModel::toggleGoal,
                    onContinue = viewModel::next,
                )
                else -> ReminderStep(
                    minutes = state.reminderMinutes,
                    onChange = viewModel::setReminder,
                    saving = state.saving,
                    onEnter = { viewModel.finish(skipReminder = false, onDone = onFinished) },
                    onLater = { viewModel.finish(skipReminder = true, onDone = onFinished) },
                )
            }
        }
    }
}

/** Common layout: scrollable content that takes the free space, bottom actions pinned. */
@Composable
private fun StepLayout(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    // Weighted spacers only work in a non-scrolling column (welcome step), so scrolling is opt-out.
    scrollable: Boolean = true,
    actions: @Composable ColumnScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .then(if (scrollable) Modifier.verticalScroll(scrollState) else Modifier),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = verticalArrangement,
            content = content,
        )
        Spacer(Modifier.height(12.dp))
        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = actions,
        )
    }
}

@Composable
private fun WelcomeStep(onContinue: () -> Unit) {
    StepLayout(
        scrollable = false,
        actions = { PrimaryButton("Get started", onContinue) },
    ) {
        Spacer(Modifier.weight(1f))
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .aspectRatio(1f)
                    .background(Brush.radialGradient(listOf(RoseMid.copy(alpha = 0.5f), Cream))),
            )
            BloomFlower(modifier = Modifier.fillMaxWidth(0.2f))
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Welcome to Bloom",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "A simple daily companion for managing PCOS — track your cycle, meals, movement, water, relaxation and sleep.",
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.weight(1.2f))
    }
}

@Composable
private fun NameStep(
    name: String,
    onNameChange: (String) -> Unit,
    canContinue: Boolean,
    onContinue: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    StepLayout(
        actions = { PrimaryButton("Continue", onContinue, enabled = canContinue) },
    ) {
        Spacer(Modifier.height(36.dp))
        Text(
            text = "A little about you",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Your first name", color = Muted) },
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    if (canContinue) onContinue()
                },
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Rose,
                unfocusedBorderColor = Border,
                cursorColor = Rose,
            ),
        )
    }
}

@Composable
private fun GoalsStep(
    selected: Set<Goal>,
    onToggle: (Goal, Boolean) -> Unit,
    onContinue: () -> Unit,
) {
    StepLayout(
        actions = { PrimaryButton("Continue", onContinue) },
    ) {
        Icon(
            imageVector = Icons.Outlined.Spa,
            contentDescription = null,
            tint = Rose,
            modifier = Modifier.size(28.dp),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "What would you like to track daily?",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Pick the goals that matter to you — only these will show up on your dashboard. Partial progress still counts, and your flower fills in as you go.",
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        SoftCard(modifier = Modifier.fillMaxWidth(), contentPadding = androidx.compose.foundation.layout.PaddingValues(10.dp)) {
            Goal.entries.forEachIndexed { index, goal ->
                if (index > 0) Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconTile(goal.icon(), tint = goal.accent())
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(goal.onboardingTitle, style = MaterialTheme.typography.titleSmall)
                        Text(goal.onboardingDescription, style = MaterialTheme.typography.bodySmall, color = Muted)
                    }
                    Spacer(Modifier.width(8.dp))
                    BloomSwitch(
                        checked = goal in selected,
                        onCheckedChange = { onToggle(goal, it) },
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Targets and what you track can be adjusted any time in Settings.",
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ReminderStep(
    minutes: Int?,
    onChange: (Int) -> Unit,
    saving: Boolean,
    onEnter: () -> Unit,
    onLater: () -> Unit,
) {
    StepLayout(
        actions = {
            PrimaryButton("Enter Bloom", onEnter, enabled = !saving)
            TextButton(onClick = onLater, enabled = !saving) {
                Text("I'll set this up later", color = Muted, style = MaterialTheme.typography.bodySmall)
            }
        },
    ) {
        Icon(
            imageVector = Icons.Outlined.AccessTime,
            contentDescription = null,
            tint = Rose,
            modifier = Modifier.size(28.dp),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "When should we remind you?",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "We'll send one daily nudge to log how you're doing.",
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(18.dp))
        Text(
            text = "Pick a time of day",
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        ReminderPicker(minutes = minutes, onChange = onChange)
    }
}
