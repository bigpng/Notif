package com.bigpng.notif

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import java.time.LocalTime

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            if (prefs.getBoolean("enabled", false)) {
                // Wait for system services to initialize
                Handler(Looper.getMainLooper()).postDelayed({
                    val start = LocalTime.of(
                        prefs.getInt("startHour", 9),
                        prefs.getInt("startMinute", 0)
                    )
                    val end = LocalTime.of(
                        prefs.getInt("endHour", 17),
                        prefs.getInt("endMinute", 0)
                    )
                    val interval = prefs.getInt("interval", 5)
                    scheduleAlarms(context, start, end, interval)
                }, 5000)  // 5 second delay
            }
        }
    }
}