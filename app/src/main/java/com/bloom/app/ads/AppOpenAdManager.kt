package com.bloom.app.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.lang.ref.WeakReference
import kotlin.coroutines.resume
class AppOpenAdManager(
    private val appContext: Context,
    private val adUnitId: String,
    private val analytics: AdAnalytics,
) : Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    enum class LoadState { Idle, Loading, Loaded, Failed }

    private val mainHandler = Handler(Looper.getMainLooper())

    private val _loadState = MutableStateFlow(LoadState.Idle)
    val loadState: StateFlow<LoadState> = _loadState.asStateFlow()

    private var appOpenAd: AppOpenAd? = null
    private var loadedAtElapsedMs: Long = 0L
    private var lastDismissedAtElapsedMs: Long = 0L
    private var currentActivity: WeakReference<Activity>? = null

    @Volatile private var sdkReady = false
    @Volatile private var isShowingAd = false
    @Volatile var foregroundAdsEnabled = false

    fun onSdkInitialized() {
        sdkReady = true
        loadAd()
    }

    fun isAdAvailable(): Boolean = appOpenAd != null && !isAdExpired()

    private fun isAdExpired(): Boolean =
        SystemClock.elapsedRealtime() - loadedAtElapsedMs >= AdConfig.APP_OPEN_AD_EXPIRY_MS
    fun loadAd() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post { loadAd() }
            return
        }
        if (!sdkReady || isShowingAd) return
        if (_loadState.value == LoadState.Loading) return
        if (isAdAvailable()) return

        // Drop an expired ad so we never show a stale one.
        appOpenAd = null

        if (!NetworkMonitor.isOnline(appContext)) {
            analytics.log(AdEvents.APP_OPEN_FAILED, mapOf("stage" to "load", "reason" to "offline"))
            _loadState.value = LoadState.Failed
            return
        }

        _loadState.value = LoadState.Loading
        analytics.log(AdEvents.APP_OPEN_REQUESTED)

        AppOpenAd.load(
            appContext,
            adUnitId,
            AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    loadedAtElapsedMs = SystemClock.elapsedRealtime()
                    _loadState.value = LoadState.Loaded
                    analytics.log(AdEvents.APP_OPEN_LOADED)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    appOpenAd = null
                    _loadState.value = LoadState.Failed
                    analytics.log(
                        AdEvents.APP_OPEN_FAILED,
                        mapOf("stage" to "load", "code" to error.code, "message" to error.message),
                    )
                }
            },
        )
    }
    suspend fun showOnLaunch(activity: ComponentActivity, timeoutMs: Long) {
        // No connection → don't make the user wait for something that cannot arrive.
        if (!isAdAvailable() && !NetworkMonitor.isOnline(appContext)) return

        if (!isAdAvailable()) {
            // Not ready yet: make sure a load is in flight, then wait (bounded) for the result.
            if (_loadState.value != LoadState.Loading) loadAd()
            withTimeoutOrNull(timeoutMs) {
                _loadState.first { it == LoadState.Loaded || it == LoadState.Failed }
            }
        }

        if (!isAdAvailable()) return
        if (!activity.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) return

        suspendCancellableCoroutine { continuation ->
            showAdIfAvailable(activity) {
                if (continuation.isActive) continuation.resume(Unit)
            }
        }
    }
    fun showAdIfAvailable(activity: Activity, onComplete: () -> Unit = {}) {
        if (isShowingAd) {
            onComplete()
            return
        }
        val ad = appOpenAd
        if (ad == null || isAdExpired()) {
            loadAd()
            onComplete()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                analytics.log(AdEvents.APP_OPEN_SHOWN)
            }

            override fun onAdDismissedFullScreenContent() {
                analytics.log(AdEvents.APP_OPEN_DISMISSED)
                finishShowing(onComplete)
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                analytics.log(
                    AdEvents.APP_OPEN_FAILED,
                    mapOf("stage" to "show", "code" to error.code, "message" to error.message),
                )
                finishShowing(onComplete)
            }
        }
        isShowingAd = true
        try {
            ad.show(activity)
        } catch (t: Throwable) {
            analytics.log(AdEvents.APP_OPEN_FAILED, mapOf("stage" to "show", "message" to t.message))
            finishShowing(onComplete)
        }
    }
    private fun finishShowing(onComplete: () -> Unit) {
        isShowingAd = false
        appOpenAd = null
        lastDismissedAtElapsedMs = SystemClock.elapsedRealtime()
        _loadState.value = LoadState.Idle
        loadAd()
        onComplete()
    }
    override fun onStart(owner: LifecycleOwner) {
        mainHandler.post { showOnForegroundIfEligible() }
    }

    private fun showOnForegroundIfEligible() {
        if (!foregroundAdsEnabled) return
        if (isShowingAd) return
        val activity = currentActivity?.get() ?: return
        if (activity.isFinishing || activity.isDestroyed) return

        val minInterval = AdConfig.MIN_INTERVAL_BETWEEN_APP_OPEN_ADS_MS
        if (minInterval > 0 && SystemClock.elapsedRealtime() - lastDismissedAtElapsedMs < minInterval) return

        if (isAdAvailable()) {
            showAdIfAvailable(activity)
        } else {
            loadAd()
        }
    }

    override fun onActivityStarted(activity: Activity) {
        if (!isShowingAd) currentActivity = WeakReference(activity)
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity?.get() === activity) currentActivity = null
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
}
