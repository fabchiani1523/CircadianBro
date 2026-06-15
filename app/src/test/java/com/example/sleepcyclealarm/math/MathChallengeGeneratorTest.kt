package com.example.sleepcyclealarm.math

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MathChallengeGeneratorTest {
    @Test
    fun generate_returnsRequestedNumberOfQuestions() {
        val questions = MathChallengeGenerator.generate(count = 3, random = Random(7))

        assertEquals(3, questions.size)
        assertTrue(questions.all { it.text.isNotBlank() })
    }
}
