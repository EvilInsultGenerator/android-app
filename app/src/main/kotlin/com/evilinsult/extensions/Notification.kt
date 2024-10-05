package com.evilinsult.extensions

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.evilinsult.R

fun showInsultNotification(context: Context, insult: String) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channelId = "insult_channel"
    val channelName = "Insult Notifications"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)
    }
    val bigTextStyle = NotificationCompat.BigTextStyle()
        .bigText(insult)

    val notification = NotificationCompat.Builder(context, channelId)
        .setContentTitle("Insult of the Day")
        .setContentText(insult)
        .setStyle(bigTextStyle)
        .setSmallIcon(R.mipmap.ic_launcher_round)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .build()
    notificationManager.notify(1, notification)
}



