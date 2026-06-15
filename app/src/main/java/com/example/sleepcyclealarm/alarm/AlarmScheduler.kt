package com.example.sleepcyclealarm.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.sleepcyclealarm.AlarmActivity
import com.example.sleepcyclealarm.MainActivity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class AlarmScheduler {
    fun schedule(
        context: Context,
        wakeTime: LocalTime,
        requireMathChallenge: Boolean
    ): Long? {
        cancel(context)

        val triggerAtMillis = nextTriggerMillis(wakeTime)

        if (!scheduleAt(context, triggerAtMillis, requireMathChallenge)) {
            return null
        }

        AlarmStorage(context).save(
            StoredAlarm(
                triggerAtMillis = triggerAtMillis,
                requireMathChallenge = requireMathChallenge
            )
        )
        return triggerAtMillis
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val flags = PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE

        cancelPendingIntent(
            alarmManager,
            alarmActivityPendingIntent(context, false, ACTIVITY_ALARM_REQUEST_CODE, flags)
        )
        cancelPendingIntent(
            alarmManager,
            alarmBroadcastPendingIntent(context, false, LEGACY_ALARM_REQUEST_CODE, flags)
        )
        cancelPendingIntent(
            alarmManager,
            alarmBroadcastPendingIntent(context, false, FALLBACK_ALARM_REQUEST_CODE, flags)
        )

        AlarmStorage(context).clear()
    }

    fun rescheduleStoredAlarm(context: Context) {
        val storedAlarm = AlarmStorage(context).load() ?: return
        if (storedAlarm.triggerAtMillis <= System.currentTimeMillis()) {
            AlarmStorage(context).clear()
            return
        }
        scheduleAt(context, storedAlarm.triggerAtMillis, storedAlarm.requireMathChallenge)
    }

    fun canScheduleExactAlarms(context: Context): Boolean = true

    private fun scheduleAt(
        context: Context,
        triggerAtMillis: Long,
        requireMathChallenge: Boolean
    ): Boolean {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val alarmIntent = alarmActivityPendingIntent(
            context = context,
            requireMathChallenge = requireMathChallenge,
            requestCode = ACTIVITY_ALARM_REQUEST_CODE
        )
            ?: return false
        val fallbackIntent = alarmBroadcastPendingIntent(
            context = context,
            requireMathChallenge = requireMathChallenge,
            requestCode = FALLBACK_ALARM_REQUEST_CODE
        )
        val showIntent = PendingIntent.getActivity(
            context,
            SHOW_ALARM_REQUEST_CODE,
            Intent(context, MainActivity::class.java),
            pendingIntentFlags()
        )

        return try {
            val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAtMillis, showIntent)
            alarmManager.setAlarmClock(alarmClockInfo, alarmIntent)
            scheduleFallbackAlarm(alarmManager, triggerAtMillis, fallbackIntent)
            Log.d(
                TAG,
                "Alarm scheduled for $triggerAtMillis. requireMathChallenge=$requireMathChallenge"
            )
            true
        } catch (exception: SecurityException) {
            Log.e(TAG, "Unable to schedule alarm", exception)
            false
        }
    }

    private fun alarmBroadcastPendingIntent(
        context: Context,
        requireMathChallenge: Boolean,
        requestCode: Int,
        flags: Int = pendingIntentFlags()
    ): PendingIntent? {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_ALARM_TRIGGERED
            putExtra(AlarmReceiver.EXTRA_REQUIRE_MATH_CHALLENGE, requireMathChallenge)
        }

        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            flags
        )
    }

    private fun alarmActivityPendingIntent(
        context: Context,
        requireMathChallenge: Boolean,
        requestCode: Int,
        flags: Int = pendingIntentFlags()
    ): PendingIntent? {
        return PendingIntent.getActivity(
            context,
            requestCode,
            AlarmActivity.createIntent(context, requireMathChallenge),
            flags
        )
    }

    private fun scheduleFallbackAlarm(
        alarmManager: AlarmManager,
        triggerAtMillis: Long,
        fallbackIntent: PendingIntent?
    ) {
        if (fallbackIntent == null) return

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                fallbackIntent
            )
        } catch (exception: SecurityException) {
            Log.w(TAG, "Exact fallback alarm failed, using inexact fallback", exception)
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                fallbackIntent
            )
        }
    }

    private fun cancelPendingIntent(
        alarmManager: AlarmManager,
        pendingIntent: PendingIntent?
    ) {
        if (pendingIntent == null) return
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun nextTriggerMillis(wakeTime: LocalTime): Long {
        val now = LocalDateTime.now()
        var triggerDateTime = LocalDate.now().atTime(wakeTime)

        if (!triggerDateTime.isAfter(now)) {
            triggerDateTime = triggerDateTime.plusDays(1)
        }

        return triggerDateTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    private fun pendingIntentFlags(): Int {
        return PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    }

    companion object {
        private const val TAG = "SleepCycleAlarm"
        private const val ACTION_ALARM_TRIGGERED = "com.example.sleepcyclealarm.action.ALARM_TRIGGERED"
        private const val LEGACY_ALARM_REQUEST_CODE = 1001
        private const val ACTIVITY_ALARM_REQUEST_CODE = 1101
        private const val FALLBACK_ALARM_REQUEST_CODE = 1102
        private const val SHOW_ALARM_REQUEST_CODE = 1002
    }
}
