package com.example.sleepcyclealarm.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sleepcyclealarm.sleep.SleepCycleAdvisor
import com.example.sleepcyclealarm.sleep.SleepSuggestion
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun MainScreen(
    hasExactAlarmAccess: Boolean,
    scheduledAlarmText: String?,
    onSetAlarm: (wakeTime: LocalTime, requireMathChallenge: Boolean) -> String,
    onCancelAlarm: () -> String,
    onOpenAlarmSettings: () -> Unit
) {
    var selectedHour by rememberSaveable { mutableIntStateOf(7) }
    var selectedMinute by rememberSaveable { mutableIntStateOf(0) }
    var requireMathChallenge by rememberSaveable { mutableStateOf(true) }
    var showTimeDialog by rememberSaveable { mutableStateOf(false) }
    var statusMessage by rememberSaveable { mutableStateOf<String?>(null) }

    val wakeTime = LocalTime.of(selectedHour, selectedMinute)
    val suggestions = SleepCycleAdvisor.suggestBedtimes(wakeTime)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PurplePale)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Scegli quando svegliarti. Ti suggerisco quando andare a dormire per completare cicli da 90 minuti.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )

            WakeTimeSelector(
                wakeTime = wakeTime,
                onClick = { showTimeDialog = true }
            )

            if (scheduledAlarmText != null) {
                ActiveAlarmPanel(
                    scheduledAlarmText = scheduledAlarmText,
                    onCancelAlarm = {
                        statusMessage = onCancelAlarm()
                    }
                )
            }

            if (!hasExactAlarmAccess) {
                PermissionPanel(onOpenSettings = onOpenAlarmSettings)
            }

            SleepSuggestionsList(suggestions = suggestions)

            MathChallengeToggle(
                checked = requireMathChallenge,
                onCheckedChange = { requireMathChallenge = it }
            )

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                shape = RoundedCornerShape(8.dp),
                onClick = {
                    statusMessage = onSetAlarm(wakeTime, requireMathChallenge)
                }
            ) {
                Text(
                    text = "Imposta sveglia",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            statusMessage?.let {
                StatusPanel(message = it)
            }
        }
    }

    if (showTimeDialog) {
        PreciseTimeDialog(
            initialHour = selectedHour,
            initialMinute = selectedMinute,
            onDismiss = { showTimeDialog = false },
            onConfirm = { hour, minute ->
                selectedHour = hour
                selectedMinute = minute
                showTimeDialog = false
            }
        )
    }
}

@Composable
private fun WakeTimeSelector(
    wakeTime: LocalTime,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = White,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, PurpleLine)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Orario sveglia",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge
            )

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = wakeTime.format(TimeFormatter),
                color = PurplePrimary,
                fontSize = 56.sp,
                lineHeight = 60.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, PurplePrimary),
                onClick = onClick
            ) {
                Text("Cambia orario")
            }
        }
    }
}

@Composable
private fun ActiveAlarmPanel(
    scheduledAlarmText: String,
    onCancelAlarm: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = White,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, PurpleLine)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Sveglia attiva",
                color = PurpleDark,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = scheduledAlarmText,
                color = PurplePrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, PurplePrimary),
                onClick = onCancelAlarm
            ) {
                Text("Elimina sveglia")
            }
        }
    }
}

@Composable
private fun PermissionPanel(onOpenSettings: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFFFFBFE),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, PurpleSoft)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Permesso necessario",
                color = PurpleDark,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Per suonare all'ora esatta, Android richiede l'accesso Allarmi e promemoria.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onOpenSettings) {
                Text("Apri impostazioni")
            }
        }
    }
}

@Composable
private fun SleepSuggestionsList(suggestions: List<SleepSuggestion>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Quando andare a dormire",
            color = PurpleDark,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        suggestions.forEach { suggestion ->
            SleepSuggestionRow(suggestion)
        }
    }
}

@Composable
private fun SleepSuggestionRow(suggestion: SleepSuggestion) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = White,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, PurpleLine)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = suggestion.bedtime.format(TimeFormatter),
                    color = PurpleDark,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${suggestion.cycles} cicli completi",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                color = PurplePale,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    text = "${suggestion.sleepMinutes / 60}h ${(suggestion.sleepMinutes % 60)}m",
                    color = PurplePrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun MathChallengeToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = White,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, PurpleLine)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Sfida matematica",
                    color = PurpleDark,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Risolverai 3 operazioni per spegnere la sveglia.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = White,
                    checkedTrackColor = PurplePrimary,
                    uncheckedThumbColor = PurpleSoft,
                    uncheckedTrackColor = PurplePale
                )
            )
        }
    }
}

@Composable
private fun StatusPanel(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = PurplePrimary,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = message,
            color = White,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun PreciseTimeDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit
) {
    var hourText by rememberSaveable { mutableStateOf(twoDigits(initialHour)) }
    var minuteText by rememberSaveable { mutableStateOf(twoDigits(initialMinute)) }

    val hour = hourText.toIntOrNull()
    val minute = minuteText.toIntOrNull()
    val isHourValid = hour != null && hour in 0..23
    val isMinuteValid = minute != null && minute in 0..59
    val isValid = isHourValid && isMinuteValid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Imposta orario",
                color = PurpleDark,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TimeInputBlock(
                        modifier = Modifier.weight(1f),
                        label = "Ore",
                        value = hourText,
                        isError = !isHourValid,
                        onValueChange = { hourText = sanitizeTimeInput(it) },
                        onDecrease = { hourText = stepTimeValue(hourText, -1, 24) },
                        onIncrease = { hourText = stepTimeValue(hourText, 1, 24) }
                    )
                    TimeInputBlock(
                        modifier = Modifier.weight(1f),
                        label = "Minuti",
                        value = minuteText,
                        isError = !isMinuteValid,
                        onValueChange = { minuteText = sanitizeTimeInput(it) },
                        onDecrease = { minuteText = stepTimeValue(minuteText, -1, 60) },
                        onIncrease = { minuteText = stepTimeValue(minuteText, 1, 60) }
                    )
                }
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "${hourText.ifBlank { "--" }}:${minuteText.ifBlank { "--" }}",
                    color = PurplePrimary,
                    fontSize = 36.sp,
                    lineHeight = 40.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = isValid,
                onClick = {
                    onConfirm(hour ?: 0, minute ?: 0)
                }
            ) {
                Text("Conferma")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}

@Composable
private fun TimeInputBlock(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    isError: Boolean,
    onValueChange: (String) -> Unit,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            singleLine = true,
            isError = isError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            textStyle = MaterialTheme.typography.headlineSmall.copy(
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                onClick = onDecrease
            ) {
                Text("-")
            }
            OutlinedButton(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                onClick = onIncrease
            ) {
                Text("+")
            }
        }
    }
}

private fun sanitizeTimeInput(value: String): String {
    return value.filter { it.isDigit() }.take(2)
}

private fun stepTimeValue(value: String, delta: Int, maxExclusive: Int): String {
    val current = value.toIntOrNull() ?: 0
    val next = (current + delta + maxExclusive) % maxExclusive
    return twoDigits(next)
}

private fun twoDigits(value: Int): String {
    return value.coerceAtLeast(0).toString().padStart(2, '0')
}

private val TimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
