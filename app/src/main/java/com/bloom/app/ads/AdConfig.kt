package com.bloom.app.ads

import com.bloom.app.BuildConfig
object AdConfig {
    const val APP_OPEN_AD_UNIT_ID: String = BuildConfig.APP_OPEN_AD_UNIT_ID
    const val BANNER_AD_UNIT_ID: String = BuildConfig.BANNER_AD_UNIT_ID
    const val LAUNCH_AD_TIMEOUT_MS: Long = 4_000L
    const val APP_OPEN_AD_EXPIRY_MS: Long = 4L * 60L * 60L * 1000L
    const val MIN_INTERVAL_BETWEEN_APP_OPEN_ADS_MS: Long = 0L
}
