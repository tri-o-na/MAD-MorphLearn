package mad.team9.morphlearn.stylebasedquiz.readwrite

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import mad.team9.morphlearn.stylebasedquiz.common.QuizAnswerFeedbackControl
import mad.team9.morphlearn.stylebasedquiz.common.QuizResultScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FillBlankScreen(
    materialId: String,
    onBackToLibrary: () -> Unit,
    onBackToHome: () -> Unit,
    viewModel: FillBlankViewModel = viewModel()
) {
    val feedbackControl = remember { QuizAnswerFeedbackControl() }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    LaunchedEffect(materialId) {
        viewModel.loadQuizData(materialId)
    }

    // Auto-focus the text field when a new question is shown
    LaunchedEffect(viewModel.currentQuestionIndex) {
        if (!viewModel.isAnswered && !viewModel.isLoading) {
            focusRequester.requestFocus()
        }
    }

    BackHandler {
        onBackToLibrary()
    }

    if (viewModel.isFinished) {
        QuizResultScreen(
            score = viewModel.correctCount,
            totalQuestions = viewModel.totalQuestions,
            onDone = onBackToHome
        )
    } else if (viewModel.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else if (viewModel.errorMessage != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(viewModel.errorMessage!!, color = MaterialTheme.colorScheme.error)
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
                            Text(
                                text = "Fill in the Blank",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Question ${viewModel.currentQuestionIndex + 1} of ${viewModel.totalQuestions}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackToLibrary) {
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
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding() // Ensure content is pushed up by the keyboard
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress Bar
                val progress = if (viewModel.totalQuestions > 0)
                    (viewModel.currentQuestionIndex + 1).toFloat() / viewModel.totalQuestions else 0f

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Question Area
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState) // Allow scrolling if the keyboard reduces available height
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "TEST YOUR KNOWLEDGE",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        viewModel.currentQuestion?.let { question ->
                            Text(
                                text = question.qn,
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 32.sp
                            )

                            Spacer(modifier = Modifier.height(40.dp))

                            val borderColor = feedbackControl.getBorderColor(
                                viewModel.isAnswered,
                                if (viewModel.isAnswered) viewModel.isCorrect else null
                            )

                            OutlinedTextField(
                                value = viewModel.userAnswer,
                                onValueChange = { if (!viewModel.isAnswered) viewModel.userAnswer = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                                placeholder = { Text("Type your answer here...") },
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                enabled = !viewModel.isAnswered,
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Done,
                                    capitalization = KeyboardCapitalization.Sentences
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (viewModel.userAnswer.isNotBlank() && !viewModel.isAnswered) {
                                            focusManager.clearFocus()
                                            viewModel.submitAnswer()
                                        }
                                    }
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (viewModel.isAnswered) borderColor else MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = if (viewModel.isAnswered) borderColor else MaterialTheme.colorScheme.outline,
                                    disabledBorderColor = borderColor,
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface
                                )
                            )

                            AnimatedVisibility(
                                visible = viewModel.isAnswered,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (viewModel.isCorrect) Icons.Default.CheckCircle else Icons.Default.Error,
                                            contentDescription = null,
                                            tint = if (viewModel.isCorrect) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (viewModel.isCorrect) "Correct!" else "Incorrect",
                                            color = if (viewModel.isCorrect) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                    }
                                    if (!viewModel.isCorrect) {
                                        Text(
                                            text = "Correct answer: ${question.ans}",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        if (viewModel.isAnswered) {
                            viewModel.nextQuestion()
                        } else {
                            focusManager.clearFocus()
                            viewModel.submitAnswer()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = viewModel.userAnswer.isNotBlank() || viewModel.isAnswered
                ) {
                    Text(
                        text = if (viewModel.isAnswered) {
                            if (viewModel.currentQuestionIndex < viewModel.totalQuestions - 1) "Next Question" else "Finish Quiz"
                        } else "Submit Answer",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
