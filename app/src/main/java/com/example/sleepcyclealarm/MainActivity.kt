package com.example.sleepcyclealarm

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import com.example.sleepcyclealarm.alarm.AlarmScheduler
import com.example.sleepcyclealarm.alarm.AlarmStorage
import com.example.sleepcyclealarm.alarm.NotificationUtils
import com.example.sleepcyclealarm.ui.MainScreen
import com.example.sleepcyclealarm.ui.SleepCycleTheme
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    private val alarmScheduler = AlarmScheduler()
    private val hasExactAlarmAccess = mutableStateOf(true)
    private val scheduledAlarmMillis = mutableStateOf<Long?>(null)
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationUtils.createAlarmChannel(this)
        requestNotificationPermissionIfNeeded()

        setContent {
            val canSchedule by hasExactAlarmAccess
            val activeAlarmMillis by scheduledAlarmMillis

            SleepCycleTheme {
                MainScreen(
                    hasExactAlarmAccess = canSchedule,
                    scheduledAlarmText = activeAlarmMillis?.let(::formatTriggerTime),
                    onSetAlarm = ::setAlarm,
                    onCancelAlarm = ::cancelAlarm,
                    onOpenAlarmSettings = ::openExactAlarmSettings
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hasExactAlarmAccess.value = alarmScheduler.canScheduleExactAlarms(this)
        syncScheduledAlarmState()
    }

    private fun setAlarm(
        wakeTime: LocalTime,
        requireMathChallenge: Boolean
    ): String {
        val triggerAtMillis = alarmScheduler.schedule(
            context = this,
            wakeTime = wakeTime,
            requireMathChallenge = requireMathChallenge
        )

        if (triggerAtMillis == null) {
            openExactAlarmSettings()
            return "Abilita Allarmi e promemoria, poi torna qui e riprova."
        }

        scheduledAlarmMillis.value = triggerAtMillis
        return "Sveglia impostata per ${formatTriggerTime(triggerAtMillis)}."
    }

    private fun cancelAlarm(): String {
        alarmScheduler.cancel(this)
        scheduledAlarmMillis.value = null
        return "Sveglia eliminata."
    }

    private fun syncScheduledAlarmState() {
        val storedAlarm = AlarmStorage(this).load()

        if (storedAlarm == null) {
            scheduledAlarmMillis.value = null
            return
        }

        if (storedAlarm.triggerAtMillis <= System.currentTimeMillis()) {
            AlarmStorage(this).clear()
            scheduledAlarmMillis.value = null
            return
        }

        scheduledAlarmMillis.value = storedAlarm.triggerAtMillis
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return

        val exactAlarmIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.parse("package:$packageName")
        }

        try {
            startActivity(exactAlarmIntent)
        } catch (_: ActivityNotFoundException) {
            val appSettingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(appSettingsIntent)
        }
    }

    private fun formatTriggerTime(triggerAtMillis: Long): String {
        val formatter = DateTimeFormatter.ofPattern("dd/MM 'alle' HH:mm")
        val dateTimeText = Instant.ofEpochMilli(triggerAtMillis)
            .atZone(ZoneId.systemDefault())
            .format(formatter)

        val millisUntilAlarm = triggerAtMillis - System.currentTimeMillis()
        if (millisUntilAlarm <= 0L) return dateTimeText

        val relativeText = formatRemainingTime(millisUntilAlarm)
        return "$dateTimeText ($relativeText)"
    }

    private fun formatRemainingTime(millisUntilAlarm: Long): String {
        val duration = Duration.ofMillis(millisUntilAlarm)

        return when {
            duration.toMinutes() < 1 -> "tra meno di 1 minuto"
            duration.toMinutes() == 1L -> "tra 1 minuto"
            duration.toHours() < 1 -> "tra ${duration.toMinutes()} minuti"
            duration.toHours() == 1L -> "tra circa 1 ora"
            duration.toHours() < 24 -> "tra circa ${duration.toHours()} ore"
            else -> "domani"
        }
    }
}
