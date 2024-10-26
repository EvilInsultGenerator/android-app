package com.evilinsult.activities

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.RemoteViews
import com.evilinsult.R

class InsultWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val SHARED_PREFS_NAME = "InsultPrefs"
        private const val INSULT_KEY = "lastInsult"
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        // Fetch the insult from SharedPreferences
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        val lastInsult = sharedPreferences.getString(INSULT_KEY, "No insult generated yet")

        // Set up the RemoteViews object for the widget
        val views = RemoteViews(context.packageName, R.layout.insult_widget)
        views.setTextViewText(R.id.insultTextView, lastInsult)

        // Set up the refresh button PendingIntent
        val intent = Intent(context, InsultWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
//        views.setOnClickPendingIntent(R.id.generateInsultButton, pendingIntent)

        // Update the widget with the latest insult
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        // This will handle refresh or update actions, if necessary
        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE == intent?.action) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)
            if (appWidgetIds != null) {
                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(context!!, appWidgetManager, appWidgetId)
                }
            }
        }
    }
}
