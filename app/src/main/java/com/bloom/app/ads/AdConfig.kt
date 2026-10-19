package com.bloom.app.ads

import com.bloom.app.BuildConfig

/**
 * Single place for every ad-related setting.
 *
 * Ad unit IDs come from BuildConfig, which is filled in app/build.gradle.kts:
 * debug builds always use Google's test IDs; release builds read overrides from
 * local.properties / gradle.properties and fall back to the test IDs.
 */
object AdConfig {
    const val APP_OPEN_AD_UNIT_ID: String = BuildConfig.APP_OPEN_AD_UNIT_ID
    const val BANNER_AD_UNIT_ID: String = BuildConfig.BANNER_AD_UNIT_ID

    /** Max time the splash screen waits for an App Open Ad before moving on. */
    const val LAUNCH_AD_TIMEOUT_MS: Long = 4_000L

    /** App Open Ads expire 4 hours after loading (Google policy). */
    const val APP_OPEN_AD_EXPIRY_MS: Long = 4L * 60L * 60L * 1000L

    /** Optional frequency cap between App Open Ads. 0 = no cap. */
    const val MIN_INTERVAL_BETWEEN_APP_OPEN_ADS_MS: Long = 0L
}
