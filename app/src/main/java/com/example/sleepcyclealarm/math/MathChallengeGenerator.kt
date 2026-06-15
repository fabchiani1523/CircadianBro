package com.example.sleepcyclealarm.math

import kotlin.random.Random

data class MathQuestion(
    val text: String,
    val answer: Int
)

object MathChallengeGenerator {
    fun generate(count: Int = 3, random: Random = Random.Default): List<MathQuestion> {
        return List(count) { generateQuestion(random) }
    }

    private fun generateQuestion(random: Random): MathQuestion {
        return when (random.nextInt(4)) {
            0 -> addition(random)
            1 -> subtraction(random)
            2 -> multiplication(random)
            else -> division(random)
        }
    }

    private fun addition(random: Random): MathQuestion {
        val first = random.nextInt(12, 70)
        val second = random.nextInt(8, 50)
        return MathQuestion("$first + $second", first + second)
    }

    private fun subtraction(random: Random): MathQuestion {
        val answer = random.nextInt(5, 75)
        val second = random.nextInt(4, 45)
        val first = answer + second
        return MathQuestion("$first - $second", answer)
    }

    private fun multiplication(random: Random): MathQuestion {
        val first = random.nextInt(3, 13)
        val second = random.nextInt(3, 13)
        return MathQuestion("$first x $second", first * second)
    }

    private fun division(random: Random): MathQuestion {
        val answer = random.nextInt(2, 13)
        val divisor = random.nextInt(2, 13)
        val dividend = answer * divisor
        return MathQuestion("$dividend / $divisor", answer)
    }
}
