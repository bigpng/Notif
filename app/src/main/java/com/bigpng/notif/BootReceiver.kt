package com.bigpng.notif

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.time.LocalTime

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            if (prefs.getBoolean("enabled", false)) {
                val startHour = prefs.getInt("startHour", 9)
                val startMinute = prefs.getInt("startMinute", 0)
                val endHour = prefs.getInt("endHour", 17)
                val endMinute = prefs.getInt("endMinute", 0)
                val interval = prefs.getInt("interval", 5)

                scheduleAlarms(
                    context,
                    LocalTime.of(startHour, startMinute),
                    LocalTime.of(endHour, endMinute),
                    interval
                )
            }
        }
    }
}