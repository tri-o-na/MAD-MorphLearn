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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import mad.team9.morphlearn.stylebasedquiz.common.QuizAnswerFeedbackControl
import mad.team9.morphlearn.stylebasedquiz.common.QuizResultScreen

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DragDropQuizScreen(
    materialId: String,
    onBackClick: () -> Unit,
    onFinish: () -> Unit,
    viewModel: DragDropViewModel = viewModel()
) {
    val feedbackControl = remember { QuizAnswerFeedbackControl() }

    LaunchedEffect(materialId) {
        viewModel.loadQuizData(materialId)
    }

    if (viewModel.isFinished) {
        QuizResultScreen(
            score = viewModel.finalScore,
            totalQuestions = viewModel.allDropTargets.size,
            onDone = onFinish
        )
    } else if (viewModel.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else if (viewModel.errorMessage != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(viewModel.errorMessage!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onBackClick) {
                    Text("Go Back")
                }
            }
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Match the Answers",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Page ${viewModel.currentPage + 1} of ${viewModel.totalPages}",
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
            val shuffledAnswers = remember(viewModel.currentPage, viewModel.currentTargets) {
                viewModel.currentTargets.map { it.correctAnswer }.shuffled()
            }
            
            val availableAnswers = shuffledAnswers.filter { answer ->
                viewModel.allDropTargets.none { it.currentAnswer == answer }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(20.dp)
            ) {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(viewModel.currentTargets, key = { it.questionId }) { target ->
                        val isCorrect = if (viewModel.confirmed) {
                            feedbackControl.isDragDropAnswerCorrect(
                                target.currentAnswer,
                                target.correctAnswer
                            )
                        } else {
                            null
                        }

                        QuestionSlot(
                            state = target,
                            enabled = !viewModel.confirmed,
                            backgroundColor = feedbackControl.getFeedbackColor(viewModel.confirmed, isCorrect),
                            borderColor = feedbackControl.getBorderColor(viewModel.confirmed, isCorrect)
                        )
                    }
                }

                if (availableAnswers.isNotEmpty()) {
                    Text(
                        text = "Answer Bank",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableAnswers.forEach { answer ->
                            key(answer + viewModel.currentPage) {
                                DraggableAnswer(
                                    content = answer,
                                    dropTargets = viewModel.currentTargets,
                                    enabled = !viewModel.confirmed,
                                    onMatchFound = { target, matchedValue ->
                                        viewModel.handleMatch(target, matchedValue)
                                    }
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (viewModel.currentPage > 0) {
                        OutlinedButton(
                            onClick = {
                                if (!viewModel.confirmed) {
                                    viewModel.currentPage--
                                }
                            },
                            modifier = Modifier.weight(1f).height(56.dp),
                            enabled = !viewModel.confirmed
                        ) {
                            Text("Previous")
                        }
                    }

                    Button(
                        onClick = { viewModel.handleConfirmOrNext() },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        val isLastPage = (viewModel.currentPage + 1) * viewModel.pageSize >= viewModel.allDropTargets.size
                        Text(
                            when {
                                !viewModel.confirmed -> "Confirm"
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
