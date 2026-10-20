package com.bloom.app.ui.splash

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bloom.app.ads.AdConfig
import com.bloom.app.ads.AppOpenAdManager
import com.bloom.app.ui.components.BloomFlower
import com.bloom.app.ui.theme.Cream
import com.bloom.app.ui.theme.Ink
import com.bloom.app.ui.theme.Rose
import com.bloom.app.ui.theme.RoseMid
import com.bloom.app.ui.util.findComponentActivity
import kotlinx.coroutines.async
import kotlinx.coroutines.delay

private const val MIN_SPLASH_MS = 1_200L
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
            appOpenAdManager.showOnLaunch(activity, AdConfig.LAUNCH_AD_TIMEOUT_MS)
        }

        minimumSplash.await()
        onFinished(destination)
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Cream),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            listOf(
                Offset(0.22f, 0.18f), Offset(0.84f, 0.27f), Offset(0.17f, 0.74f),
                Offset(0.88f, 0.69f), Offset(0.52f, 0.9f),
            ).forEach { p ->
                drawCircle(
                    color = Ink.copy(alpha = 0.35f),
                    radius = 1.6.dp.toPx(),
                    center = Offset(size.width * p.x, size.height * p.y),
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth(0.62f)
                .aspectRatio(1f)
                .background(Brush.radialGradient(listOf(RoseMid.copy(alpha = 0.55f), Cream))),
        )
        BloomFlower(modifier = Modifier.fillMaxWidth(0.26f), petalColor = Rose)
    }
}
