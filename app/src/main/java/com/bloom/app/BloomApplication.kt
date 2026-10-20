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

        val appOpenAdManager = container.appOpenAdManager
        registerActivityLifecycleCallbacks(appOpenAdManager)
        ProcessLifecycleOwner.get().lifecycle.addObserver(appOpenAdManager)

        appScope.launch {
            MobileAds.initialize(this@BloomApplication) {
                appOpenAdManager.onSdkInitialized()
            }
        }
    }
}
