package com.bloom.app.ads

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

private enum class BannerState { Loading, Loaded, Failed }
@Composable
fun AdaptiveBannerAd(
    analytics: AdAnalytics,
    modifier: Modifier = Modifier,
    adUnitId: String = AdConfig.BANNER_AD_UNIT_ID,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var state by remember { mutableStateOf(BannerState.Loading) }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val widthDp = maxWidth.value.toInt()
        val adSize = remember(widthDp) {
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp)
        }

        // Recreate the AdView when the available width changes (e.g. rotation / split screen).
        val adView = remember(adSize, adUnitId) {
            state = BannerState.Loading
            AdView(context).apply {
                setAdSize(adSize)
                this.adUnitId = adUnitId
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        state = BannerState.Loaded
                        analytics.log(AdEvents.BANNER_LOADED)
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        state = BannerState.Failed
                        analytics.log(
                            AdEvents.BANNER_FAILED,
                            mapOf("code" to error.code, "message" to error.message),
                        )
                    }

                    override fun onAdImpression() {
                        analytics.log(AdEvents.BANNER_IMPRESSION)
                    }
                }
                loadAd(AdRequest.Builder().build())
            }
        }

        DisposableEffect(adView, lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_PAUSE -> adView.pause()
                    Lifecycle.Event.ON_RESUME -> adView.resume()
                    else -> Unit
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                adView.destroy()
            }
        }

        if (state != BannerState.Failed) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Visual separation from the wellness content above and the navigation below.
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                AndroidView(
                    factory = { adView },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(adSize.height.dp),
                )
            }
        }
    }
}
