package com.example.sleepcyclealarm.sleep

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalTime

class SleepCycleAdvisorTest {
    @Test
    fun suggestBedtimes_returnsCyclesFromSixToThree() {
        val suggestions = SleepCycleAdvisor.suggestBedtimes(LocalTime.of(7, 0))

        assertEquals(4, suggestions.size)
        assertEquals(6, suggestions[0].cycles)
        assertEquals(LocalTime.of(21, 45), suggestions[0].bedtime)
        assertEquals(3, suggestions[3].cycles)
        assertEquals(LocalTime.of(2, 15), suggestions[3].bedtime)
    }
}
