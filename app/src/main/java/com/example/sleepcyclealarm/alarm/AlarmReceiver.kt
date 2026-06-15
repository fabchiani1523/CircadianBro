package com.example.sleepcyclealarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.sleepcyclealarm.AlarmActivity

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val requireMathChallenge = intent.getBooleanExtra(EXTRA_REQUIRE_MATH_CHALLENGE, false)
        Log.d(TAG, "Alarm received. requireMathChallenge=$requireMathChallenge")

        AlarmScheduler().cancel(context)

        val serviceIntent = Intent(context, AlarmSoundService::class.java).apply {
            putExtra(EXTRA_REQUIRE_MATH_CHALLENGE, requireMathChallenge)
        }

        try {
            ContextCompat.startForegroundService(context, serviceIntent)
        } catch (exception: Exception) {
            Log.e(TAG, "Unable to start alarm sound service", exception)
        }

        try {
            context.startActivity(AlarmActivity.createIntent(context, requireMathChallenge))
        } catch (exception: Exception) {
            Log.e(TAG, "Unable to open alarm activity", exception)
        }
    }

    companion object {
        private const val TAG = "SleepCycleAlarm"
        const val EXTRA_REQUIRE_MATH_CHALLENGE = "extra_require_math_challenge"
    }
}
