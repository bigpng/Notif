package com.bigpng.notif

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import compose.icons.TablerIcons
import compose.icons.tablericons.Alarm
import kotlin.random.Random

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val message = prefs.getString("message", "Stay hydrated!") ?: ""
        showFullScreenNotification(context, message)
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun showFullScreenNotification(context: Context, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create high priority channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(
                "reminders",
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Scheduled reminders"
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                enableVibration(true)
                notificationManager.createNotificationChannel(this)
            }
        }

        // Full-screen intent
        val fullScreenIntent = Intent(context, FullScreenActivity::class.java).apply {
            putExtra("message", message)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            Random.nextInt(),
            fullScreenIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        NotificationCompat.Builder(context, "reminders")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Reminder")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(true)
            .setOngoing(true)
            .setTimeoutAfter(30000)
            .build()
            .let { notificationManager.notify(Random.nextInt(), it) }
    }
}