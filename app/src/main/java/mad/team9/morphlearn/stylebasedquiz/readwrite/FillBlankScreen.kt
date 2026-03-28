package mad.team9.morphlearn.stylebasedquiz.readwrite

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import mad.team9.morphlearn.stylebasedquiz.common.QuizResultScreen

@Composable
fun FillBlankScreen(
    materialId: String,
    onBackToLibrary: () -> Unit,
    onBackToHome: () -> Unit,
    viewModel: FillBlankViewModel = viewModel()
) {
    LaunchedEffect(materialId) {
        viewModel.loadQuizData(materialId)
    }

    BackHandler {
        onBackToLibrary()
    }

    FillBlankContent(
        isLoading = viewModel.isLoading,
        isFinished = viewModel.isFinished,
        errorMessage = viewModel.errorMessage,
        subjectName = viewModel.subjectName,
        currentQuestionIndex = viewModel.currentQuestionIndex,
        totalQuestions = viewModel.totalQuestions,
        currentQuestion = viewModel.currentQuestion,
        userAnswer = viewModel.userAnswer,
        isAnswered = viewModel.isAnswered,
        isCorrect = viewModel.isCorrect,
        correctCount = viewModel.correctCount,
        onBackToLibrary = onBackToLibrary,
        onBackToHome = onBackToHome,
        onUserAnswerChange = { viewModel.userAnswer = it },
        onSubmitAnswer = { viewModel.submitAnswer() },
        onNextQuestion = { viewModel.nextQuestion() },
        onRestart = { viewModel.restart() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FillBlankContent(
    isLoading: Boolean,
    isFinished: Boolean,
    errorMessage: String?,
    subjectName: String?,
    currentQuestionIndex: Int,
    totalQuestions: Int,
    currentQuestion: FillBlank?,
    userAnswer: String,
    isAnswered: Boolean,
    isCorrect: Boolean,
    correctCount: Int,
    onBackToLibrary: () -> Unit,
    onBackToHome: () -> Unit,
    onUserAnswerChange: (String) -> Unit,
    onSubmitAnswer: () -> Unit,
    onNextQuestion: () -> Unit,
    onRestart: () -> Unit
) {
    if (isFinished) {
        QuizResultScreen(
            score = correctCount,
            totalQuestions = totalQuestions,
            onDone = onBackToHome,
            onRestart = onRestart
        )
    } else if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else if (errorMessage != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onBackToLibrary) {
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
                            Text("Fill in the Blank", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            if (subjectName != null) {
                                Text(subjectName, fontSize = 12.sp, fontWeight = FontWeight.Normal)
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackToLibrary) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    windowInsets = WindowInsets(0, 0, 0, 0)
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress Bar
                val progress = if (totalQuestions > 0) (currentQuestionIndex + 1).toFloat() / totalQuestions else 0f
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Text(
                    text = "Question ${currentQuestionIndex + 1} of $totalQuestions",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Question Area
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "FILL IN THE BLANK",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        currentQuestion?.let { question ->
                            Text(
                                text = question.qn,
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 32.sp
                            )

                            Spacer(modifier = Modifier.height(40.dp))

                            OutlinedTextField(
                                value = userAnswer,
                                onValueChange = { if (!isAnswered) onUserAnswerChange(it) },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Type your answer here...") },
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                enabled = !isAnswered,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )

                            AnimatedVisibility(visible = isAnswered) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Error,
                                            contentDescription = null,
                                            tint = if (isCorrect) Color(0xFF9CCC65) else MaterialTheme.colorScheme.error
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isCorrect) "Correct!" else "Incorrect",
                                            color = if (isCorrect) Color(0xFF9CCC65) else MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                    }
                                    if (!isCorrect) {
                                        Text(
                                            text = "Correct answer: ${question.ans}",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(top = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Action Button
                Button(
                    onClick = {
                        if (isAnswered) {
                            onNextQuestion()
                        } else {
                            onSubmitAnswer()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAnswered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = userAnswer.isNotBlank()
                ) {
                    Text(
                        text = if (isAnswered) "Next Question" else "Submit Answer",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
