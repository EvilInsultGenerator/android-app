package com.evilinsult.extensions

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat

fun Context.openLink(url: String?) {
    val link = url ?: return
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
    if (browserIntent.resolveActivity(packageManager) != null)
        startActivity(browserIntent)
    else Toast.makeText(this, "Cannot find a browser", Toast.LENGTH_LONG).show()
}

fun Context.resolveColor(@AttrRes attr: Int, @ColorInt fallback: Int = 0): Int {
    val a = theme.obtainStyledAttributes(intArrayOf(attr))
    try {
        return a.getColor(0, fallback)
    } finally {
        a.recycle()
    }
}

@Suppress("DEPRECATION")
val Context.isNetworkAvailable: Boolean
    get() {
        try {
            var connectivityManager: ConnectivityManager? = try {
                ContextCompat.getSystemService(this, ConnectivityManager::class.java)
            } catch (ignored: Exception) {
                null
            }
            if (connectivityManager == null)
                try {
                    connectivityManager =
                        getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager?
                } catch (ignored: Exception) {
                }
            connectivityManager ?: return false

            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val activeNetwork = connectivityManager.activeNetwork ?: return false
                val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
                capabilities?.let {
                    it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                            || it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                            || it.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                            || it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                } ?: false
            } else {
                val activeNetworkInfo = connectivityManager.activeNetworkInfo ?: return false
                val connected =
                    activeNetworkInfo.isAvailable && activeNetworkInfo.isConnectedOrConnecting
                return connected || connectivityManager.allNetworkInfo.any { it.isConnectedOrConnecting }
            }
        } catch (ignored: Exception) {
            return false
        }
    }
