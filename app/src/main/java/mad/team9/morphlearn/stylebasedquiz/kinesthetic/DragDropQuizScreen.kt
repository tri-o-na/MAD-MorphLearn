package mad.team9.morphlearn.stylebasedquiz.kinesthetic

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mad.team9.morphlearn.stylebasedquiz.QuizQuestion
import mad.team9.morphlearn.stylebasedquiz.QuizResultScreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DragDropQuizScreen(
    questions: List<QuizQuestion>,
    onFinish: (Int) -> Unit
) {
    val primaryTeal = Color(0xFF006064)
    var isFinished by remember { mutableStateOf(false) }
    var finalScore by remember { mutableStateOf(0) }

    if (isFinished) {
        QuizResultScreen(
            score = finalScore,
            totalQuestions = questions.size,
            onDone = { onFinish(finalScore) }
        )
    } else {
        val dropTargets = remember {
            questions.map { q ->
                DropTargetState(
                    questionId = q.question,
                    questionText = q.question,
                    correctAnswer = q.options.getOrElse(q.correctIndex) { "" }
                )
            }
        }

        val availableAnswers = questions.map { it.options[it.correctIndex] }

        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            Text("Match the Answers", style = MaterialTheme.typography.headlineMedium, color = primaryTeal)

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(dropTargets) { target ->
                    QuestionSlot(state = target)
                }
            }

            Text("Answer Bank", style = MaterialTheme.typography.labelLarge)

            FlowRow(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                availableAnswers.forEach { answer ->
                    DraggableAnswer(
                        content = answer,
                        dropTargets = dropTargets,
                        onMatchFound = { target, matchedValue ->
                            target.currentAnswer = matchedValue
                        }
                    )
                }
            }

            Button(
                onClick = {
                    val score = dropTargets.count { it.currentAnswer == it.correctAnswer }
                    finalScore = score
                    isFinished = true
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryTeal)
            ) {
                Text("Finish Quiz")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DragDropPreview() {
    MaterialTheme {
        DragDropQuizScreen(
            questions = MockQuizProvider.getMockQuestions(),
            onFinish = {}
        )
    }
}
