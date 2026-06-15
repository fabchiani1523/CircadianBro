package com.example.sleepcyclealarm.sleep

import java.time.LocalTime

data class SleepSuggestion(
    val cycles: Int,
    val bedtime: LocalTime,
    val sleepMinutes: Long
)

object SleepCycleAdvisor {
    private const val SLEEP_CYCLE_MINUTES = 90L
    private const val FALL_ASLEEP_MINUTES = 15L

    fun suggestBedtimes(wakeTime: LocalTime): List<SleepSuggestion> {
        return (6 downTo 3).map { cycles ->
            val sleepMinutes = cycles * SLEEP_CYCLE_MINUTES
            SleepSuggestion(
                cycles = cycles,
                bedtime = wakeTime.minusMinutes(sleepMinutes + FALL_ASLEEP_MINUTES),
                sleepMinutes = sleepMinutes
            )
        }
    }
}
