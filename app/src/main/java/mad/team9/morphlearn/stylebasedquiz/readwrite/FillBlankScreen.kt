package mad.team9.morphlearn.stylebasedquiz.readwrite

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import mad.team9.morphlearn.ui.theme.*

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
        FillBlankResultScreen(
            correct = correctCount,
            total = totalQuestions,
            onBackToHome = onBackToHome,
            onRestart = onRestart
        )
    } else if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MorphTeal)
        }
    } else if (errorMessage != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(errorMessage, color = Color.Red)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onBackToLibrary, colors = ButtonDefaults.buttonColors(containerColor = MorphTeal)) {
                    Text("Go Back", color = Color.White)
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
                        containerColor = MorphTeal,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    ),
                    windowInsets = WindowInsets(0, 0, 0, 0)
                )
            },
            containerColor = BackgroundGray
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
                    color = MorphTeal,
                    trackColor = Color.LightGray.copy(alpha = 0.3f)
                )

                Text(
                    text = "Question ${currentQuestionIndex + 1} of $totalQuestions",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 12.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Question Area
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            color = MorphTeal.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "FILL IN THE BLANK",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = MorphTeal,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        currentQuestion?.let { question ->
                            Text(
                                text = question.qn,
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center,
                                color = TextDark,
                                lineHeight = 32.sp
                            )

                            Spacer(modifier = Modifier.height(40.dp))

                            OutlinedTextField(
                                value = userAnswer,
                                onValueChange = { if (!isAnswered) onUserAnswerChange(it) },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Type your answer here...", color = Color.Gray) },
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                enabled = !isAnswered,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MorphTeal,
                                    unfocusedBorderColor = Color.LightGray,
                                    focusedTextColor = TextDark,
                                    unfocusedTextColor = TextDark,
                                    disabledTextColor = TextDark
                                )
                            )

                            AnimatedVisibility(visible = isAnswered) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Error,
                                            contentDescription = null,
                                            tint = if (isCorrect) MorphLightGreen else Color.Red
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isCorrect) "Correct!" else "Incorrect",
                                            color = if (isCorrect) MorphLightGreen else Color.Red,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                    }
                                    if (!isCorrect) {
                                        Text(
                                            text = "Correct answer: ${question.ans}",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = Color.Black,
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
                        containerColor = if (isAnswered) MorphTeal else MorphPurple,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = userAnswer.isNotBlank()
                ) {
                    Text(
                        text = if (isAnswered) "Next Question" else "Submit Answer",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun FillBlankResultScreen(
    correct: Int,
    total: Int,
    onBackToHome: () -> Unit,
    onRestart: () -> Unit
) {
    val percentage = if (total > 0) (correct.toFloat() / total * 100).toInt() else 0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray),
        contentAlignment = Alignment.Center
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(24.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Quiz Completed!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MorphTeal
                )

                Spacer(modifier = Modifier.height(24.dp))

                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { percentage / 100f },
                        modifier = Modifier.size(120.dp),
                        color = if (percentage >= 50) MorphLightGreen else Color.Red,
                        strokeWidth = 10.dp,
                        trackColor = Color.LightGray.copy(alpha = 0.3f)
                    )
                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "You scored $correct out of $total",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = onRestart,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MorphTeal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Try Again", color = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onBackToHome,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MorphTeal)
                ) {
                    Text("Back to Home")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FillBlankScreenPreview() {
    FillBlankContent(
        isLoading = false,
        isFinished = false,
        errorMessage = null,
        subjectName = "Introduction to Android",
        currentQuestionIndex = 0,
        totalQuestions = 5,
        currentQuestion = FillBlank("_ is the primary color of MorphLearn.", "Teal"),
        userAnswer = "",
        isAnswered = false,
        isCorrect = false,
        correctCount = 0,
        onBackToLibrary = {},
        onBackToHome = {},
        onUserAnswerChange = {},
        onSubmitAnswer = {},
        onNextQuestion = {},
        onRestart = {}
    )
}

@Preview(showBackground = true)
@Composable
fun FillBlankResultPreview() {
    FillBlankResultScreen(
        correct = 4,
        total = 5,
        onBackToHome = {},
        onRestart = {}
    )
}