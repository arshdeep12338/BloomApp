package com.bloom.app.di

import android.app.Application
import com.bloom.app.ads.AdAnalytics
import com.bloom.app.ads.AdConfig
import com.bloom.app.ads.AppOpenAdManager
import com.bloom.app.ads.LogcatAdAnalytics
import com.bloom.app.data.DataStoreOnboardingRepository
import com.bloom.app.data.OnboardingRepository

/** Minimal manual DI container; keeps the demo free of a DI framework. */
class AppContainer(application: Application) {

    val onboardingRepository: OnboardingRepository = DataStoreOnboardingRepository(application)

    /** Swap for a Firebase-backed implementation without touching call sites. */
    val adAnalytics: AdAnalytics = LogcatAdAnalytics()

    val appOpenAdManager: AppOpenAdManager = AppOpenAdManager(
        appContext = application,
        adUnitId = AdConfig.APP_OPEN_AD_UNIT_ID,
        analytics = adAnalytics,
    )
}
