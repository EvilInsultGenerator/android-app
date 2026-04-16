package com.evilinsult.extensions

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import java.util.Calendar
import androidx.core.net.toUri

object AlarmScheduler {

    private const val REQUEST_CODE = 1001

    fun schedule(
        context: Context,
        rhythm: String,
        hour: Int,
        minute: Int,
        dayOfWeek: Int,
        dayOfMonth: Int,
        languageCode: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("rhythm", rhythm)
            putExtra("hour", hour)
            putExtra("minute", minute)
            putExtra("dayOfWeek", dayOfWeek)
            putExtra("dayOfMonth", dayOfMonth)
            putExtra("languageCode", languageCode)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAt = nextTriggerMillis(rhythm, hour, minute, dayOfWeek, dayOfMonth)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                // Permission granted — schedule exact
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent
                )
            } else {
                // Permission not granted — send user to settings to enable it
                val settingsIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = "package:${context.packageName}".toUri()
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(settingsIntent)
            }
        } else {
            // Below Android 12 — no permission needed
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent
            )
        }
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun nextTriggerMillis(
        rhythm: String,
        hour: Int,
        minute: Int,
        dayOfWeek: Int,
        dayOfMonth: Int
    ): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            when (rhythm) {
                "weekly" -> {
                    set(Calendar.DAY_OF_WEEK, dayOfWeek)
                    if (!after(now)) add(Calendar.WEEK_OF_YEAR, 1)
                }
                "monthly" -> {
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    if (!after(now)) add(Calendar.MONTH, 1)
                }
                else -> {
                    if (!after(now)) add(Calendar.DAY_OF_MONTH, 1)
                }
            }
        }
        return target.timeInMillis
    }
}