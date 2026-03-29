package mad.team9.morphlearn.stylebasedquiz.visual

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import mad.team9.morphlearn.stylebasedquiz.common.QuizResultScreen
import mad.team9.morphlearn.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardsScreen(
    materialId: String,
    onBackToLibrary: () -> Unit,
    onBackToHome: () -> Unit,
    viewModel: FlashcardsViewModel = viewModel()
) {
    LaunchedEffect(materialId) {
        viewModel.loadQuizData(materialId)
    }

    BackHandler {
        onBackToLibrary()
    }

    if (viewModel.isFinished) {
        QuizResultScreen(
            score = viewModel.correctCount,
            totalQuestions = viewModel.userAnswersMap.size,
            onDone = onBackToHome
        )
    } else if (viewModel.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else if (viewModel.errorMessage != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(viewModel.errorMessage!!, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onBackToLibrary) {
                    Text("Go Back")
                }
            }
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Visual Learning: Flashcards", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
                // Progress Bar: Percentage of questions answered
                val answeredCount = viewModel.userAnswersMap.size
                val totalCount = answeredCount + viewModel.activeCards.size
                val progress = if (totalCount > 0) answeredCount.toFloat() / totalCount else 0f

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
                    text = "${viewModel.activeCards.size} cards remaining",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Flashcard Area
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    viewModel.currentCard?.let { card ->
                        FlashcardItem(
                            card = card,
                            isAnswerRevealed = viewModel.isAnswerRevealed,
                            onToggle = { viewModel.toggleAnswer() },
                            onSkip = { viewModel.skipCard() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Action Buttons
                Box(modifier = Modifier.height(80.dp)) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = viewModel.isAnswerRevealed,
                        enter = fadeIn() + slideInVertically { it / 2 },
                        exit = fadeOut()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = { viewModel.onAnswered(false) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Wrong")
                            }

                            Button(
                                onClick = { viewModel.onAnswered(true) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9CCC65)), // Light Green
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Done, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Correct")
                            }
                        }
                    }

                    if (!viewModel.isAnswerRevealed && !viewModel.isLoading) {
                        Text(
                            text = "Tap the card to see the answer",
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FlashcardItem(
    card: Flashcard,
    isAnswerRevealed: Boolean,
    onToggle: () -> Unit,
    onSkip: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isAnswerRevealed) 180f else 0f,
        animationSpec = tween(durationMillis = 500)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 12f * density
                }
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable { onToggle() }
                .padding(24.dp)
        ) {
            if (rotation <= 90f) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "QUESTION",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = card.qn,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 32.sp
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationY = 180f },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "ANSWER",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = card.ans,
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        TextButton(
            onClick = onSkip,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        ) {
            Text("Skip", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}