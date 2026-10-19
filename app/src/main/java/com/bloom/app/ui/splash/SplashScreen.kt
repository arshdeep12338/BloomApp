package com.bloom.app.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bloom.app.ads.AdConfig
import com.bloom.app.ads.AppOpenAdManager
import com.bloom.app.ui.util.findComponentActivity
import kotlinx.coroutines.async
import kotlinx.coroutines.delay

private const val MIN_SPLASH_MS = 1_200L

/**
 * Splash + launch routing.
 *
 *  First launch : Splash → Onboarding            (no App Open Ad)
 *  Returning    : Splash → [App Open Ad] → Home  (ad only if ready within the timeout)
 */
@Composable
fun SplashScreen(
    viewModel: SplashViewModel,
    appOpenAdManager: AppOpenAdManager,
    onFinished: (StartDestination) -> Unit,
) {
    val activity = LocalContext.current.findComponentActivity()

    LaunchedEffect(Unit) {
        val minimumSplash = async { delay(MIN_SPLASH_MS) }

        val destination = viewModel.resolveStartDestination()
        if (destination == StartDestination.Home && activity != null) {
            // Bounded wait; returns immediately if there is no ad, no network or on failure.
            appOpenAdManager.showOnLaunch(activity, AdConfig.LAUNCH_AD_TIMEOUT_MS)
        }

        minimumSplash.await()
        onFinished(destination)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.34f)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "🌸", fontSize = 56.sp)
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = "Bloom",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Your cycle. Your wellness.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
