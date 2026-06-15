package com.example.sleepcyclealarm.alarm

import android.content.Context

data class StoredAlarm(
    val triggerAtMillis: Long,
    val requireMathChallenge: Boolean
)

class AlarmStorage(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun save(storedAlarm: StoredAlarm) {
        preferences.edit()
            .putLong(KEY_TRIGGER_AT_MILLIS, storedAlarm.triggerAtMillis)
            .putBoolean(KEY_REQUIRE_MATH_CHALLENGE, storedAlarm.requireMathChallenge)
            .apply()
    }

    fun load(): StoredAlarm? {
        if (!preferences.contains(KEY_TRIGGER_AT_MILLIS)) return null

        return StoredAlarm(
            triggerAtMillis = preferences.getLong(KEY_TRIGGER_AT_MILLIS, 0L),
            requireMathChallenge = preferences.getBoolean(KEY_REQUIRE_MATH_CHALLENGE, false)
        )
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    companion object {
        private const val PREFERENCES_NAME = "alarm_storage"
        private const val KEY_TRIGGER_AT_MILLIS = "trigger_at_millis"
        private const val KEY_REQUIRE_MATH_CHALLENGE = "require_math_challenge"
    }
}
