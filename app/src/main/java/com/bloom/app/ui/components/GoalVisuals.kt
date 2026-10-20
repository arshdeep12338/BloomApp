package com.bloom.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material.icons.outlined.Opacity
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.bloom.app.data.Goal
import com.bloom.app.ui.theme.GoalCycleColor
import com.bloom.app.ui.theme.GoalMealsColor
import com.bloom.app.ui.theme.GoalMovementColor
import com.bloom.app.ui.theme.GoalRelaxationColor
import com.bloom.app.ui.theme.GoalSleepColor
import com.bloom.app.ui.theme.GoalSymptomsColor
import com.bloom.app.ui.theme.GoalWaterColor

fun Goal.icon(): ImageVector = when (this) {
    Goal.Meals -> Icons.Outlined.Restaurant
    Goal.Movement -> Icons.Outlined.FitnessCenter
    Goal.Water -> Icons.Outlined.WaterDrop
    Goal.Relaxation -> Icons.Outlined.SelfImprovement
    Goal.Sleep -> Icons.Outlined.Bedtime
    Goal.Cycle -> Icons.Outlined.Opacity
    Goal.Symptoms -> Icons.Outlined.Mood
}

fun Goal.accent(): Color = when (this) {
    Goal.Meals -> GoalMealsColor
    Goal.Movement -> GoalMovementColor
    Goal.Water -> GoalWaterColor
    Goal.Relaxation -> GoalRelaxationColor
    Goal.Sleep -> GoalSleepColor
    Goal.Cycle -> GoalCycleColor
    Goal.Symptoms -> GoalSymptomsColor
}
