package com.bloom.app

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.bloom.app.di.AppContainer
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BloomApplication : Application() {

    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        // The manager tracks the current Activity and the app-wide foreground event.
        val appOpenAdManager = container.appOpenAdManager
        registerActivityLifecycleCallbacks(appOpenAdManager)
        ProcessLifecycleOwner.get().lifecycle.addObserver(appOpenAdManager)

        // Initialise the Mobile Ads SDK off the main thread (recommended by Google to
        // avoid ANRs). The completion callback runs on the main thread; it is the
        // point where we start preloading the first App Open Ad.
        appScope.launch {
            MobileAds.initialize(this@BloomApplication) {
                appOpenAdManager.onSdkInitialized()
            }
        }
    }
}
