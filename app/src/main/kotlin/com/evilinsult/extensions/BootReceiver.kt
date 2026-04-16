package com.evilinsult.extensions

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.Calendar

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val prefs = context.getSharedPreferences("DailyInsultPrefs", Context.MODE_PRIVATE)
            val enabled = prefs.getBoolean("enabled", false)
            if (enabled) {
                val rhythm     = prefs.getString("rhythm", "daily") ?: "daily"
                val hour       = prefs.getInt("hour", 0)
                val minute     = prefs.getInt("minute", 0)
                val dayOfWeek  = prefs.getInt("dayOfWeek", Calendar.MONDAY)
                val dayOfMonth = prefs.getInt("dayOfMonth", 1)
                val langCode   = prefs.getString("languageCode", "") ?: ""
                AlarmScheduler.schedule(context, rhythm, hour, minute, dayOfWeek, dayOfMonth, langCode)
            }
        }
    }
}