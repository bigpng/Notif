package com.bigpng.notif

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.edit
import com.bigpng.notif.ui.theme.NotifTheme
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import android.text.format.DateFormat
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            NotifTheme {
                MainScreen()
            }
        }
    }
}


@Composable
fun MainScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }

    var startTime by rememberSaveable { mutableStateOf(LocalTime.now()) }
    var endTime by rememberSaveable { mutableStateOf(LocalTime.now()) }
    var interval by rememberSaveable { mutableStateOf(5) }
    var message by rememberSaveable { mutableStateOf("Stay hydrated!") }
    var enabled by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        startTime = LocalTime.of(
            prefs.getInt("startHour", 9),
            prefs.getInt("startMinute", 0)
        )
        endTime = LocalTime.of(
            prefs.getInt("endHour", 17),
            prefs.getInt("endMinute", 0)
        )
        interval = prefs.getInt("interval", 5)
        message = prefs.getString("message", "Stay hydrated!") ?: ""
        enabled = prefs.getBoolean("enabled", false)
    }

    Column(modifier = Modifier.padding(16.dp)) {
        // Time pickers
        TimePickerRow("Start Time", startTime) {
            startTime = it
            prefs.edit {
                putInt("startHour", it.hour)
                putInt("startMinute", it.minute)
            }
        }

        TimePickerRow("End Time", endTime) {
            endTime = it
            prefs.edit {
                putInt("endHour", it.hour)
                putInt("endMinute", it.minute)
            }
        }

        // Interval selector
        IntervalPicker(interval) {
            interval = it
            prefs.edit { putInt("interval", it) }
        }

        // Message input
        OutlinedTextField(
            value = message,
            onValueChange = {
                message = it
                prefs.edit { putString("message", it) }
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Notification Message") }
        )

        Button(
            onClick = {
                enabled = !enabled
                prefs.edit { putBoolean("enabled", enabled) }
                if (enabled) scheduleAlarms(context, startTime, endTime, interval)
                else cancelAlarms(context)
            }
        ) {
            Text(if (enabled) "Disable Reminders" else "Enable Reminders")
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerRow(label: String, time: LocalTime, onTimeChange: (LocalTime) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            "$label: ${time.format(DateTimeFormatter.ofPattern("HH:mm"))}",
            modifier = Modifier.weight(1f)
        )
        Button(onClick = { showPicker = true }) {
            Text("Change")
        }
    }

    // if showpicker
    if (showPicker) {
        val is24HourFormat = DateFormat.is24HourFormat(context)
        val dialogState = rememberTimePickerState(
            initialHour = time.hour,
            initialMinute = time.minute,
            is24Hour = is24HourFormat
        )

        Dialog(
            onDismissRequest = { showPicker = false }
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimePicker(state = dialogState)

                    Row(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showPicker = false }
                        ) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                onTimeChange(
                                    LocalTime.of(
                                        dialogState.hour,
                                        dialogState.minute
                                    )
                                )
                                showPicker = false
                            }
                        ) {
                            Text("OK")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntervalPicker(current: Int, onSelect: (Int) -> Unit) {
    val intervals = listOf(5, 10, 15, 20, 25)
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            modifier = Modifier.menuAnchor(),
            readOnly = true,
            value = "$current minutes",
            onValueChange = {},
            label = { Text("Interval") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            intervals.forEach { interval ->
                DropdownMenuItem(
                    text = { Text("$interval minutes") },
                    onClick = {
                        onSelect(interval)
                        expanded = false
                    }
                )
            }
        }
    }
}

