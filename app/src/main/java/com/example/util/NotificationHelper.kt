package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationHelper {
    private const val CHANNEL_ID = "flood_service_channel"
    private const val ERROR_CHANNEL_ID = "flood_error_channel"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Active running flood channel
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Flood Çalışma Durumu",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Flood aracı arka planda çalışırken gösterilen bildirim."
            }
            manager.createNotificationChannel(serviceChannel)

            // Dynamic instant operational alert channel (e.g., error alert)
            val errorChannel = NotificationChannel(
                ERROR_CHANNEL_ID,
                "Flood Hataları ve Bildirimler",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Spam durdurulduğunda veya hata alındığında anlık uyarı."
                enableVibration(true)
            }
            manager.createNotificationChannel(errorChannel)
        }
    }

    fun showInstantErrorNotification(context: Context, title: String, message: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, ERROR_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    fun getServiceNotificationBuilder(context: Context, contentText: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Oyun Flood Aracı")
            .setContentText(contentText)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
    }
}
