package com.example.sleepcyclealarm.alarm

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.sleepcyclealarm.AlarmActivity
import com.example.sleepcyclealarm.R

class AlarmSoundService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var toneGenerator: ToneGenerator? = null
    private var vibrator: Vibrator? = null
    private val handler = Handler(Looper.getMainLooper())
    private val fallbackToneRunnable = object : Runnable {
        override fun run() {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 900)
            handler.postDelayed(this, 1200)
        }
    }

    override fun onCreate() {
        super.onCreate()
        NotificationUtils.createAlarmChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        val requireMathChallenge = intent?.getBooleanExtra(
            AlarmReceiver.EXTRA_REQUIRE_MATH_CHALLENGE,
            false
        ) ?: false
        Log.d(TAG, "Alarm sound service started. requireMathChallenge=$requireMathChallenge")

        startForeground(
            ALARM_NOTIFICATION_ID,
            createAlarmNotification(requireMathChallenge)
        )
        startAlarmSound()
        startVibration()
        return START_STICKY
    }

    override fun onDestroy() {
        stopAlarmSound()
        stopVibration()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createAlarmNotification(requireMathChallenge: Boolean): android.app.Notification {
        val alarmActivityIntent = alarmActivityPendingIntent(requireMathChallenge)

        return NotificationCompat.Builder(this, NotificationUtils.ALARM_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Sveglia")
            .setContentText("Tocca per spegnere la sveglia.")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(alarmActivityIntent, true)
            .setContentIntent(alarmActivityIntent)
            .build()
    }

    private fun alarmActivityPendingIntent(requireMathChallenge: Boolean): PendingIntent {
        return PendingIntent.getActivity(
            this,
            ALARM_ACTIVITY_REQUEST_CODE,
            AlarmActivity.createIntent(this, requireMathChallenge),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun startAlarmSound() {
        if (mediaPlayer?.isPlaying == true || toneGenerator != null) return

        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val player = MediaPlayer()

        try {
            player.setDataSource(this, alarmUri)
            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            player.isLooping = true
            player.prepare()
            player.start()
            mediaPlayer = player
        } catch (exception: Exception) {
            Log.e(TAG, "Unable to play system alarm sound, using fallback tone", exception)
            player.release()
            startFallbackTone()
        }
    }

    private fun stopAlarmSound() {
        handler.removeCallbacks(fallbackToneRunnable)
        toneGenerator?.release()
        toneGenerator = null

        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.stop()
            }
            player.release()
        }
        mediaPlayer = null
    }

    private fun startFallbackTone() {
        toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
        fallbackToneRunnable.run()
    }

    private fun startVibration() {
        vibrator = currentVibrator()
        val pattern = longArrayOf(0, 700, 500, 700, 900)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    private fun stopVibration() {
        vibrator?.cancel()
        vibrator = null
    }

    private fun currentVibrator(): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(VibratorManager::class.java).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
    }

    companion object {
        private const val TAG = "SleepCycleAlarm"
        const val ACTION_STOP = "com.example.sleepcyclealarm.action.STOP_ALARM"
        private const val ALARM_ACTIVITY_REQUEST_CODE = 2001
        private const val ALARM_NOTIFICATION_ID = 3001
    }
}
