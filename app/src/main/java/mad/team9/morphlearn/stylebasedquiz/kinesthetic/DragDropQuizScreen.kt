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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DragDropQuizScreen(
    questions: List<QuizQuestion>,
    onFinish: (Int) -> Unit
) {
    val primaryTeal = Color(0xFF006064)
    val feedbackControl = remember { mad.team9.morphlearn.stylebasedquiz.QuizAnswerFeedbackControl() }
    var isFinished by remember { mutableStateOf(false) }
    var finalScore by remember { mutableStateOf(0) }

    // Page state to limit to 3 questions
    var currentPage by remember { mutableStateOf(0) }
    val pageSize = 3

    // ADDED: confirm state so user must confirm before going next
    var confirmed by remember { mutableStateOf(false) }

    // Track total user answers across pages - using remember with questions as key
    val allDropTargets = remember(questions) {
        questions.map { q ->
            DropTargetState(
                questionId = q.question,
                questionText = q.question,
                correctAnswer = q.options.getOrElse(q.correctIndex) { "" }
            )
        }
    }

    val currentTargets = allDropTargets.drop(currentPage * pageSize).take(pageSize)
    val currentQuestions = questions.drop(currentPage * pageSize).take(pageSize)

    if (isFinished) {
        QuizResultScreen(
            score = finalScore,
            totalQuestions = questions.size,
            onDone = { onFinish(finalScore) }
        )
    } else {
        // Shuffle answers per page
        val availableAnswers = remember(currentPage, currentQuestions) {
            currentQuestions.map { it.options[it.correctIndex] }.shuffled()
        }

        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            val totalPages = (questions.size + pageSize - 1) / pageSize
            Text(
                text = "Match the Answers (${currentPage + 1}/$totalPages)",
                style = MaterialTheme.typography.headlineMedium,
                color = primaryTeal
            )

            LazyColumn(modifier = Modifier.weight(1f)) {
                // ADDED KEY: This ensures each question slot refreshes its position on page change
                items(currentTargets, key = { it.questionId }) { target ->
                    val isCorrect = if (confirmed) {
                        feedbackControl.isDragDropAnswerCorrect(
                            target.currentAnswer,
                            target.correctAnswer
                        )
                    } else {
                        null
                    }

                    QuestionSlot(
                        state = target,
                        enabled = !confirmed,
                        backgroundColor = feedbackControl.getFeedbackColor(confirmed, isCorrect),
                        borderColor = feedbackControl.getBorderColor(confirmed, isCorrect)
                    )
                }
            }

            Text("Answer Bank", style = MaterialTheme.typography.labelLarge)

            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableAnswers.forEach { answer ->
                    // ADDED KEY: Ensures draggable state doesn't leak between pages
                    key(answer + currentPage) {
                        DraggableAnswer(
                            content = answer,
                            dropTargets = currentTargets,
                            enabled = !confirmed,
                            onMatchFound = { target, matchedValue ->
                                if (!confirmed) {
                                    target.currentAnswer = matchedValue
                                }
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (currentPage > 0) {
                    OutlinedButton(
                        onClick = {
                            if (!confirmed) {
                                currentPage--
                            }
                        },
                        modifier = Modifier.weight(1f).height(56.dp),
                        enabled = !confirmed
                    ) {
                        Text("Previous")
                    }
                }

                val isLastPage = (currentPage + 1) * pageSize >= questions.size
                Button(
                    onClick = {
                        if (!confirmed) {
                            confirmed = true
                        } else {
                            if (isLastPage) {
                                finalScore = allDropTargets.count {
                                    feedbackControl.isDragDropAnswerCorrect(
                                        it.currentAnswer,
                                        it.correctAnswer
                                    )
                                }
                                isFinished = true
                            } else {
                                currentPage++
                                confirmed = false
                            }
                        }
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryTeal)
                ) {
                    Text(
                        when {
                            !confirmed -> "Confirm"
                            isLastPage -> "Finish Quiz"
                            else -> "Next"
                        }
                    )
                }
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