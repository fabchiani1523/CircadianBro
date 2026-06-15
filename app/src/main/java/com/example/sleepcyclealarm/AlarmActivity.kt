package com.example.sleepcyclealarm

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.content.ContextCompat
import com.example.sleepcyclealarm.alarm.AlarmReceiver
import com.example.sleepcyclealarm.alarm.AlarmScheduler
import com.example.sleepcyclealarm.alarm.AlarmSoundService
import com.example.sleepcyclealarm.ui.AlarmRingingScreen
import com.example.sleepcyclealarm.ui.SleepCycleTheme

class AlarmActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prepareLockScreen()
        handleAlarmIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAlarmIntent(intent)
    }

    private fun handleAlarmIntent(intent: Intent) {
        val requireMathChallenge = intent.getBooleanExtra(EXTRA_REQUIRE_MATH_CHALLENGE, false)
        Log.d(TAG, "Alarm activity opened. requireMathChallenge=$requireMathChallenge")

        AlarmScheduler().cancel(this)
        startAlarmSound(requireMathChallenge)

        setContent {
            SleepCycleTheme {
                AlarmRingingScreen(
                    requireMathChallenge = requireMathChallenge,
                    onStopAlarm = ::stopAlarmAndFinish
                )
            }
        }
    }

    private fun startAlarmSound(requireMathChallenge: Boolean) {
        val serviceIntent = Intent(this, AlarmSoundService::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_REQUIRE_MATH_CHALLENGE, requireMathChallenge)
        }
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    private fun prepareLockScreen() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            getSystemService(KeyguardManager::class.java)?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }

    private fun stopAlarmAndFinish() {
        stopService(Intent(this, AlarmSoundService::class.java))
        finish()
    }

    companion object {
        private const val TAG = "SleepCycleAlarm"
        const val EXTRA_REQUIRE_MATH_CHALLENGE = "extra_require_math_challenge"

        fun createIntent(context: Context, requireMathChallenge: Boolean): Intent {
            return Intent(context, AlarmActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_REQUIRE_MATH_CHALLENGE, requireMathChallenge)
            }
        }
    }
}
