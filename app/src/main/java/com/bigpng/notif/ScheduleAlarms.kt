package com.bigpng.notif

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

//fun scheduleAlarms(
//    context: Context,
//    startTime: LocalTime,
//    endTime: LocalTime,
//    interval: Int
//) {
//    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
//        context.startActivity(Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
//        return
//    }
//
//    cancelAlarms(context) // Clear existing first
//
//    val now = LocalDateTime.now()
//    val zoneId = ZoneId.systemDefault()
//    var currentTrigger = LocalDate.now().atTime(startTime)
//
//    // Handle overnight schedules
//    val isOvernight = endTime.isBefore(startTime)
//    val endDateTime = if (isOvernight) {
//        // End time is on the next day
//        LocalDate.now().plusDays(1).atTime(endTime)
//    } else {
//        LocalDate.now().atTime(endTime)
//    }
//
//    // Adjust initial trigger
////    if (currentTrigger.isBefore(now)) {
////        currentTrigger = if (isOvernight) currentTrigger.plusDays(1) else return
////    }
//
//    val baseAction = "FULLSCREEN_ALARM_ACTION"
//    var requestCode = 0
//    val intent = Intent(context, AlarmReceiver::class.java).apply {
//        action = "FULLSCREEN_ALARM_ACTION_${System.currentTimeMillis()}"
//    }
//
//    while (currentTrigger.isBefore(endDateTime)) {
//        val triggerAtMillis = currentTrigger.atZone(zoneId).toInstant().toEpochMilli()
//
//        val pendingIntent = PendingIntent.getBroadcast(
//            context,
//            requestCode++,
//            intent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//
//        alarmManager.setExactAndAllowWhileIdle(
//            AlarmManager.RTC_WAKEUP,
//            triggerAtMillis,
//            pendingIntent
//        )
//
//        Log.d("AlarmDebug", "Scheduled alarm at: ${currentTrigger.format(DateTimeFormatter.ISO_DATE_TIME)}")
//
//        currentTrigger = currentTrigger.plusMinutes(interval.toLong())
//        if (requestCode > 1000) break // Prevent infinite loops
//    }
//}
//
//
//
//fun cancelAlarms(context: Context) {
//    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//    val intent = Intent(context, AlarmReceiver::class.java).apply {
//        action = "FULLSCREEN_ALARM_ACTION_${System.currentTimeMillis()}"
//    }
//
//    // Cancel all possible alarms
//    val pendingIntent = PendingIntent.getBroadcast(
//        context,
//        0,
//        intent,
//        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
//    )
//
//    if (pendingIntent != null) {
//        alarmManager.cancel(pendingIntent)
//        pendingIntent.cancel()
//    }
//
//    // Cancel any remaining alarms
//    repeat(1000) { requestCode ->
//        val pi = PendingIntent.getBroadcast(
//            context,
//            requestCode,
//            intent,
//            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
//        )
//        pi?.let {
//            alarmManager.cancel(it)
//            it.cancel()
//        }
//    }
//}

fun scheduleAlarms(
    context: Context,
    startTime: LocalTime,
    endTime: LocalTime,
    interval: Int
) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
        context.startActivity(Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
        return
    }

    // Cancel existing alarms first
    cancelAlarms(context)

    val now = LocalDateTime.now()
    val zoneId = ZoneId.systemDefault()
    var currentTrigger = LocalDate.now().atTime(startTime)

    // Determine if the schedule is overnight
    val isOvernight = endTime.isBefore(startTime)
    val endDateTime = if (isOvernight) {
        LocalDate.now().plusDays(1).atTime(endTime)
    } else {
        LocalDate.now().atTime(endTime)
    }

    // Adjust the first trigger time:
    // If the computed trigger is before now but we're still in the window, start from now + 1 second.
    // Otherwise, if the window is already over, schedule for the next day.
    if (currentTrigger.isBefore(now)) {
        currentTrigger = if (now.isBefore(endDateTime)) {
            now.plusSeconds(1)
        } else {
            LocalDate.now().plusDays(1).atTime(startTime)
        }
    }

    // Use a constant action so that all alarms share the same identifier.
    val baseAction = "FULLSCREEN_ALARM_ACTION"
    var requestCode = 0

    while (currentTrigger.isBefore(endDateTime)) {
        val triggerAtMillis = currentTrigger.atZone(zoneId).toInstant().toEpochMilli()

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = baseAction
        }
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

        Log.d("AlarmDebug", "Scheduled alarm at: ${currentTrigger.format(DateTimeFormatter.ISO_DATE_TIME)}")

        currentTrigger = currentTrigger.plusMinutes(interval.toLong())
        if (requestCode > 1000) break // Safety measure to avoid infinite loops.
    }
}

fun cancelAlarms(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val baseAction = "FULLSCREEN_ALARM_ACTION"
    // Attempt to cancel alarms using request codes from 0 to 1000.
    repeat(1000) { requestCode ->
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = baseAction
        }
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
