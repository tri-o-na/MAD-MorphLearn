package mad.team9.morphlearn.stylebasedquiz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import mad.team9.morphlearn.audio.rememberTextToSpeechController
import mad.team9.morphlearn.login.FirebaseAuthManager
import mad.team9.morphlearn.stylebasedquiz.kinesthetic.QuizResultScreen

@Composable
fun QuizPlayScreen(
    quizId: String,
    topic: String,
    onDone: () -> Unit,
    vm: QuizPlayViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    var learningStyle by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(quizId) {
        learningStyle = FirebaseAuthManager.getLearningStyle()
        vm.loadQuiz(quizId)
    }

    when {
        state.loading || learningStyle == null -> Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        state.error != null -> Column(Modifier.padding(16.dp)) {
            Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
        }

        state.finished -> {
            QuizResultScreen(
                score = state.finalScore ?: 0,
                totalQuestions = state.questions.size,
                onDone = onDone
            )
        }

        else -> {
            QuizLayoutSelector(
                learningStyle = learningStyle!!,
                state = state,
                topic = topic,
                onFinish = { score ->
                    vm.submitFinalScore(score, topic)
                },
                onDone = onDone,
                standardQuizUI = {
                    StandardQuizUI(
                        state = state,
                        topic = topic,
                        vm = vm,
                        // TEMP: enable TTS for KINESTHETIC while testing audio flow
                        enableTts = learningStyle?.trim()?.uppercase() in setOf("AUDITORY")
                    )
                }
            )
        }
    }
}

@Composable
fun StandardQuizUI(
    state: QuizPlayState,
    topic: String,
    vm: QuizPlayViewModel,
    enableTts: Boolean = false
) {
    val q = state.questions.getOrNull(state.index)
    if (q == null) {
        Column(Modifier.padding(16.dp)) {
            Text("No questions found.")
        }
        return
    }

    val ttsController = rememberTextToSpeechController()
    var isSpeaking by remember { mutableStateOf(false) }
    var hasStartedSpeaking by remember { mutableStateOf(false) }

    val selectedIndex = state.selectedAnswers.getOrNull(state.index) ?: -1
    val hasSelected = selectedIndex != -1
    val primaryTeal = Color(0xFF006064)

    fun buildQuestionSpeech(): String {
        return buildString {
            append(q.question)
            append(". ")
            q.options.forEachIndexed { index, option ->
                append("Option ${index + 1}. $option. ")
            }
        }
    }

    // Speak feedback after confirm
    LaunchedEffect(state.index, state.confirmed, state.lastAnswerCorrect, enableTts) {
        if (enableTts && state.confirmed) {
            val correctAnswerText = q.options.getOrElse(q.correctIndex) { "" }
            val feedbackText = if (state.lastAnswerCorrect == true) {
                "Correct. The answer is $correctAnswerText"
            } else {
                "Incorrect. The answer is $correctAnswerText"
            }
            ttsController.speak(feedbackText)
            isSpeaking = true
            hasStartedSpeaking = true
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Question ${state.index + 1} / ${state.questions.size}",
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = q.question,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (enableTts) {
                            Modifier.clickable {
                                ttsController.speak(q.question)
                                isSpeaking = true
                                hasStartedSpeaking = true
                            }
                        } else {
                            Modifier
                        }
                    )
            )

            if (enableTts) {
                IconButton(
                    onClick = {
                        if (isSpeaking) {
                            ttsController.stop()
                            isSpeaking = false
                        } else {
                            ttsController.speak(buildQuestionSpeech())
                            isSpeaking = true
                            hasStartedSpeaking = true
                        }
                    },
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .size(44.dp)
                        .border(
                            width = 1.5.dp,
                            color = primaryTeal,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = when {
                            isSpeaking -> Icons.Default.Stop
                            hasStartedSpeaking -> Icons.Default.Refresh
                            else -> Icons.Default.PlayArrow
                        },
                        contentDescription = when {
                            isSpeaking -> "Stop reading"
                            hasStartedSpeaking -> "Replay question and options"
                            else -> "Read question and options aloud"
                        },
                        tint = primaryTeal
                    )
                }
            }
        }

        q.options.forEachIndexed { idx, option ->
            val isSelected = idx == selectedIndex
            val isCorrect = idx == q.correctIndex

            val containerColor = when {
                !state.confirmed && isSelected -> Color(0xFFE3F2FD)
                state.confirmed && isCorrect -> Color(0xFFDFF5E1)
                state.confirmed && isSelected && !isCorrect -> Color(0xFFFDE2E1)
                else -> Color.White
            }

            val borderColor = when {
                !state.confirmed && isSelected -> Color(0xFF1976D2)
                state.confirmed && isCorrect -> Color(0xFF2E7D32)
                state.confirmed && isSelected && !isCorrect -> Color(0xFFC62828)
                else -> Color.LightGray
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (!state.confirmed) {
                            vm.selectAnswer(idx)
                        }
                        if (enableTts) {
                            ttsController.speak(option)
                            isSpeaking = true
                            hasStartedSpeaking = true
                        }
                    },
                shape = MaterialTheme.shapes.medium,
                color = containerColor,
                border = BorderStroke(1.dp, borderColor)
            ) {
                Text(
                    text = option,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Start
                )
            }
        }

        if (!state.confirmed) {
            Button(
                onClick = { vm.confirmAnswer() },
                enabled = hasSelected,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryTeal,
                    contentColor = Color.White
                )

            ) {
                Text("Confirm")
            }
        } else {
            Text(
                text = if (state.lastAnswerCorrect == true) "Correct!" else "Incorrect!",
                color = if (state.lastAnswerCorrect == true) Color(0xFF2E7D32) else Color(0xFFC62828),
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedButton(
                onClick = { vm.nextOrFinish(topic) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryTeal,
                    contentColor = Color.White
                )

            ) {
                Text(if (state.index == state.questions.lastIndex) "Finish Quiz" else "Next")
            }
        }
    }
}