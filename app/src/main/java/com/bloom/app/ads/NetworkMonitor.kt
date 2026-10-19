package com.bloom.app.ads

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/** Cheap connectivity check used to skip pointless ad requests/waits while offline. */
object NetworkMonitor {
    fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
