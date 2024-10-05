package com.evilinsult.extensions

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.concurrent.TimeUnit

class NotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
):Worker(context,workerParams){
    override fun doWork(): Result {
        val insult = fetchInsultFromApi() ?: "Your random insult"
        showInsultNotification(context,insult)
        return Result.success()
    }

    private fun fetchInsultFromApi(): String? {
        return try {
            val insultUrl = "https://www.evilinsult.com/generate_insult.php?lang=en"
            val doc: Document? = Jsoup.connect(insultUrl).get()
            doc?.text()?.trim()
        }catch (e:Exception){
            e.printStackTrace()
            null
        }
    }
}
fun scheduleDailyInsultNotification(context: Context){
    val dailyWorkRequest = PeriodicWorkRequestBuilder<NotificationWorker>(15, TimeUnit.MINUTES)
        .build()

    WorkManager.getInstance(context)
        .enqueueUniquePeriodicWork(
            "dailyInsultNotification",
            ExistingPeriodicWorkPolicy.REPLACE,
            dailyWorkRequest
        )
}