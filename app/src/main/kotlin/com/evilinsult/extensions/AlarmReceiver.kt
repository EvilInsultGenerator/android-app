package com.evilinsult.extensions

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val rhythm     = intent.getStringExtra("rhythm") ?: "daily"
        val hour       = intent.getIntExtra("hour", 0)
        val minute     = intent.getIntExtra("minute", 0)
        val dayOfWeek  = intent.getIntExtra("dayOfWeek", Calendar.MONDAY)
        val dayOfMonth = intent.getIntExtra("dayOfMonth", 1)
        val langCode   = intent.getStringExtra("languageCode") ?: "en"

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val insult = fetchInsult(langCode)
                if (insult != null) showInsultNotification(context, insult)
            } finally {
                pendingResult.finish()
            }
        }

        AlarmScheduler.schedule(context, rhythm, hour, minute, dayOfWeek, dayOfMonth, langCode)
    }

    private fun fetchInsult(langCode: String): String? {
        return try {
            val url = "https://www.evilinsult.com/generate_insult.php?lang=$langCode"
            Jsoup.connect(url).get()?.text()?.trim()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}