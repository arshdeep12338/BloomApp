package com.bloom.app.di

import android.app.Application
import com.bloom.app.ads.AdAnalytics
import com.bloom.app.ads.AdConfig
import com.bloom.app.ads.AppOpenAdManager
import com.bloom.app.ads.LogcatAdAnalytics
import com.bloom.app.data.DataStoreUserPreferencesRepository
import com.bloom.app.data.UserPreferencesRepository
import com.bloom.app.data.WellnessRepository

/** Minimal manual DI container; keeps the demo free of a DI framework. */
class AppContainer(application: Application) {

    val userPreferences: UserPreferencesRepository = DataStoreUserPreferencesRepository(application)

    val wellness: WellnessRepository = WellnessRepository()

    /** Swap for a Firebase-backed implementation without touching call sites. */
    val adAnalytics: AdAnalytics = LogcatAdAnalytics()

    val appOpenAdManager: AppOpenAdManager = AppOpenAdManager(
        appContext = application,
        adUnitId = AdConfig.APP_OPEN_AD_UNIT_ID,
        analytics = adAnalytics,
    )
}
