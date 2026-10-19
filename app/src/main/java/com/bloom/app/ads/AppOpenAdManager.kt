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

/**
 * Owns the whole App Open Ad lifecycle: preload, expiry, show, reload.
 *
 * It plays two roles:
 *  1. [ActivityLifecycleCallbacks] – tracks the foreground Activity (needed to show the ad).
 *  2. [DefaultLifecycleObserver] on ProcessLifecycleOwner – fires once each time the whole
 *     app returns to the foreground (background → foreground), never on rotation or when
 *     moving between Activities.
 *
 * Two entry points show ads:
 *  - [showOnLaunch]: called by the splash screen for RETURNING users only.
 *  - the process-foreground callback: shows an ad on later foreground events, but only
 *    after the user has reached Home ([foregroundAdsEnabled]). That flag is what keeps
 *    first-time users (splash → onboarding) from ever being interrupted.
 */
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

    /**
     * Foreground (background → foreground) ads are suppressed until this is true.
     * Set by the Home destination, so splash and onboarding are never interrupted.
     */
    @Volatile var foregroundAdsEnabled = false

    // ---------------------------------------------------------------- loading

    /** Called by the Application once MobileAds.initialize() has completed (main thread). */
    fun onSdkInitialized() {
        sdkReady = true
        loadAd()
    }

    fun isAdAvailable(): Boolean = appOpenAd != null && !isAdExpired()

    private fun isAdExpired(): Boolean =
        SystemClock.elapsedRealtime() - loadedAtElapsedMs >= AdConfig.APP_OPEN_AD_EXPIRY_MS

    /** Safe to call any time: it no-ops if the SDK isn't ready, a load is running, or an ad is ready. */
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
                    // No automatic retry loop (avoids hammering the ad server). The next
                    // launch/foreground attempt calls loadAd() again.
                }
            },
        )
    }

    // ---------------------------------------------------------------- showing

    /**
     * Launch flow for returning users. Waits at most [timeoutMs] for an ad and ALWAYS
     * returns (after the ad is dismissed, or immediately when no ad can be shown) so the
     * caller can continue to Home. Must be called from the main thread.
     */
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

        // The app may have been backgrounded while we waited; an ad can't be shown then.
        if (!activity.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) return

        suspendCancellableCoroutine { continuation ->
            showAdIfAvailable(activity) {
                if (continuation.isActive) continuation.resume(Unit)
            }
        }
    }

    /**
     * Shows the ad if one is ready and none is already on screen.
     * [onComplete] runs exactly once: after dismissal, after a show failure, or immediately
     * if nothing was shown.
     */
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

        // Single-flight guard: set BEFORE show() so a second foreground event can't re-enter.
        isShowingAd = true
        try {
            ad.show(activity)
        } catch (t: Throwable) {
            analytics.log(AdEvents.APP_OPEN_FAILED, mapOf("stage" to "show", "message" to t.message))
            finishShowing(onComplete)
        }
    }

    /** Common tail of dismiss / show-failure: release the ad, reload the next one, continue. */
    private fun finishShowing(onComplete: () -> Unit) {
        isShowingAd = false
        appOpenAd = null
        lastDismissedAtElapsedMs = SystemClock.elapsedRealtime()
        _loadState.value = LoadState.Idle
        loadAd() // preload the next ad
        onComplete()
    }

    // ------------------------------------------------- background → foreground

    override fun onStart(owner: LifecycleOwner) {
        // Post so the Activity callback (which records the current Activity) has run first.
        mainHandler.post { showOnForegroundIfEligible() }
    }

    private fun showOnForegroundIfEligible() {
        if (!foregroundAdsEnabled) return // splash / onboarding / first-time user
        if (isShowingAd) return
        val activity = currentActivity?.get() ?: return
        if (activity.isFinishing || activity.isDestroyed) return

        val minInterval = AdConfig.MIN_INTERVAL_BETWEEN_APP_OPEN_ADS_MS
        if (minInterval > 0 && SystemClock.elapsedRealtime() - lastDismissedAtElapsedMs < minInterval) return

        if (isAdAvailable()) {
            showAdIfAvailable(activity)
        } else {
            loadAd() // nothing to show right now; make sure the next one is on its way
        }
    }

    // ------------------------------------------------ ActivityLifecycleCallbacks

    override fun onActivityStarted(activity: Activity) {
        // Ignore the full-screen ad's own Activity so we never try to show an ad "on" it.
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
