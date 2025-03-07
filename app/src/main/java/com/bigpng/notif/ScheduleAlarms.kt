package com.bigpng.notif

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

@SuppressLint("ScheduleExactAlarm")
fun scheduleAlarms(
    context: Context,
    startTime: LocalTime,
    endTime: LocalTime,
    interval: Int
) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // Check for Android 12+ permissions
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
        context.startActivity(Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
        Toast.makeText(context, "Exact alarm permission required", Toast.LENGTH_LONG).show()
        return
    }

    // Validate time range
    if (startTime >= endTime) {
        Toast.makeText(context, "End time must be after start time", Toast.LENGTH_SHORT).show()
        return
    }

    val now = LocalDateTime.now()
    val zoneId = ZoneId.systemDefault()
    var currentTrigger = now.with(startTime)

    // Adjust to next occurrence if in the past
    if (currentTrigger.isBefore(now)) {
        currentTrigger = currentTrigger.plusDays(1)
    }

    var requestCode = 0
    val intent = Intent(context, AlarmReceiver::class.java)

    while (currentTrigger.toLocalTime().isBefore(endTime)) {
        val triggerAtMillis = currentTrigger.atZone(zoneId).toInstant().toEpochMilli()

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode++,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )

        currentTrigger = currentTrigger.plusMinutes(interval.toLong())

        // Prevent infinite loop
        if (requestCode > 1000) break
    }
}

fun cancelAlarms(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // Cancel all possible alarms
    repeat(1000) { requestCode ->
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }
}