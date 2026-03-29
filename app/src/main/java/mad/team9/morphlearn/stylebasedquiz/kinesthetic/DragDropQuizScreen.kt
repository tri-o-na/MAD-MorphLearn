package mad.team9.morphlearn.stylebasedquiz.kinesthetic

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mad.team9.morphlearn.stylebasedquiz.common.QuizQuestion
import mad.team9.morphlearn.stylebasedquiz.common.QuizResultScreen

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DragDropQuizScreen(
    questions: List<QuizQuestion>,
    topic: String,
    onBackClick: () -> Unit,
    onFinish: (Int) -> Unit
) {
    var isFinished by remember { mutableStateOf(false) }
    var finalScore by remember { mutableStateOf(0) }
    
    // Page state to limit to 3 questions
    var currentPage by remember { mutableStateOf(0) }
    val pageSize = 3
    
    // confirm state so user must confirm before going next
    var confirmed by remember { mutableStateOf(false) }

    val feedbackControl = remember { mad.team9.morphlearn.stylebasedquiz.common.QuizAnswerFeedbackControl() }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = topic,
                            style = MaterialTheme.typography.titleMedium
                        )
                        val totalPages = (questions.size + pageSize - 1) / pageSize
                        Text(
                            text = "Page ${currentPage + 1} of $totalPages",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (isFinished) {
            Box(modifier = Modifier.padding(innerPadding)) {
                QuizResultScreen(
                    score = finalScore,
                    totalQuestions = questions.size,
                    onDone = { onFinish(finalScore) }
                )
            }
        } else {
            // Shuffle answers per page
            val availableAnswers = remember(currentPage, currentQuestions) {
                currentQuestions.map { it.options[it.correctIndex] }.shuffled()
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(20.dp)
            ) {
                Text(
                    text = "Match the Answers",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

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

                Text(
                    text = "Answer Bank", 
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableAnswers.forEach { answer ->
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
                            enabled = !confirmed,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
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
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
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
}

