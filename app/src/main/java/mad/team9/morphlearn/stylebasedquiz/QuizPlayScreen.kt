package mad.team9.morphlearn.stylebasedquiz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.graphics.Color
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
                    // For non-MCQ quizzes, we might need a way to submit the final score directly
                },
                onDone = onDone,
                standardQuizUI = {
                    StandardQuizUI(state, topic, vm)
                }
            )
        }
    }
}

@Composable
fun StandardQuizUI(
    state: QuizPlayState,
    topic: String,
    vm: QuizPlayViewModel
) {
    val q = state.questions.getOrNull(state.index)
    if (q == null) {
        Column(Modifier.padding(16.dp)) { Text("No questions found.") }
        return
    }

    val selectedIndex = state.selectedAnswers.getOrNull(state.index) ?: -1
    val hasSelected = selectedIndex != -1

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
        Text(q.question, style = MaterialTheme.typography.titleLarge)

        q.options.forEachIndexed { i, opt ->
            val isSelected = (i == selectedIndex)
            val isCorrectOption = (i == q.correctIndex)

            val correctBg = Color(0xFFC8E6C9)     // light green
            val correctBorder = Color(0xFF2E7D32) // dark green

            val wrongBg = MaterialTheme.colorScheme.errorContainer
            val wrongBorder = MaterialTheme.colorScheme.error

            val bg = when {
                state.confirmed && isCorrectOption -> correctBg
                state.confirmed && isSelected && !isCorrectOption -> wrongBg
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }

            val border = when {
                state.confirmed && isCorrectOption -> correctBorder
                state.confirmed && isSelected && !isCorrectOption -> wrongBorder
                isSelected -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.outline
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !state.confirmed) { vm.selectAnswer(i) },
                colors = CardDefaults.cardColors(containerColor = bg),
                border = BorderStroke(1.dp, border)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${'A' + i}.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(end = 10.dp)
                    )
                    Text(
                        text = opt,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start
                    )
                }
            }
        }

        if (!state.confirmed) {
            Button(
                onClick = { vm.confirmAnswer() },
                enabled = hasSelected,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirm")
            }
        } else {
            val correct = state.lastAnswerCorrect == true
            Text(
                text = if (correct) "Correct ✅" else "Wrong ❌",
                color = if (correct) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )

            Button(
                onClick = { vm.nextOrFinish(topic) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.index == state.questions.size - 1) "Finish" else "Next")
            }
        }
    }
}
