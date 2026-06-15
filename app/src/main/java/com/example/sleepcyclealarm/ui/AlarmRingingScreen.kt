package com.example.sleepcyclealarm.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sleepcyclealarm.math.MathChallengeGenerator
import com.example.sleepcyclealarm.math.MathQuestion

@Composable
fun AlarmRingingScreen(
    requireMathChallenge: Boolean,
    onStopAlarm: () -> Unit
) {
    if (requireMathChallenge) {
        MathChallengeAlarm(onStopAlarm = onStopAlarm)
    } else {
        SimpleAlarm(onStopAlarm = onStopAlarm)
    }
}

@Composable
private fun SimpleAlarm(onStopAlarm: () -> Unit) {
    AlarmScaffold {
        Text(
            text = "Sveglia",
            color = PurpleDark,
            fontSize = 44.sp,
            lineHeight = 48.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "E' ora di svegliarsi.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        StopButton(text = "Spegni sveglia", onClick = onStopAlarm)
    }
}

@Composable
private fun MathChallengeAlarm(onStopAlarm: () -> Unit) {
    val questions = remember { MathChallengeGenerator.generate() }
    var currentIndex by rememberSaveable { mutableIntStateOf(0) }
    var answerText by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    val currentQuestion = questions[currentIndex]

    fun submitAnswer() {
        val answer = answerText.trim().toIntOrNull()

        if (answer != currentQuestion.answer) {
            errorMessage = "Risposta non corretta. Riprova."
            return
        }

        errorMessage = null
        answerText = ""

        if (currentIndex == questions.lastIndex) {
            onStopAlarm()
        } else {
            currentIndex += 1
        }
    }

    AlarmScaffold {
        Text(
            text = "Sveglia",
            color = PurpleDark,
            fontSize = 40.sp,
            lineHeight = 44.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Operazione ${currentIndex + 1} di ${questions.size}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
        MathQuestionPanel(question = currentQuestion)
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = answerText,
            onValueChange = { value ->
                answerText = value.filter { it.isDigit() }.take(4)
            },
            label = { Text("Risposta") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { submitAnswer() }),
            isError = errorMessage != null
        )
        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        StopButton(text = "Conferma", onClick = ::submitAnswer)
    }
}

@Composable
private fun AlarmScaffold(content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PurplePale)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = White,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, PurpleLine)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp),
                content = content
            )
        }
    }
}

@Composable
private fun MathQuestionPanel(question: MathQuestion) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = PurplePale,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, PurpleLine)
    ) {
        Text(
            modifier = Modifier.padding(24.dp),
            text = question.text,
            color = PurplePrimary,
            fontSize = 42.sp,
            lineHeight = 46.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StopButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
        shape = RoundedCornerShape(8.dp),
        onClick = onClick
    ) {
        Text(
            text = text,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
