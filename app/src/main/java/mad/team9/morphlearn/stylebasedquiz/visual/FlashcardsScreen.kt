package mad.team9.morphlearn.stylebasedquiz.visual

import android.annotation.SuppressLint
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import mad.team9.morphlearn.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardsScreen(
    onBackToLibrary: () -> Unit,
    onBackToHome: () -> Unit,
    viewModel: FlashcardsViewModel = viewModel()
) {
    // Handle system back press
    BackHandler {
        onBackToLibrary()
    }

    if (viewModel.isFinished) {
        FlashcardsResultScreen(
            correct = viewModel.correctCount,
            total = viewModel.totalCards,
            onBackToHome = onBackToHome
        )
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
                        containerColor = MorphTeal,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
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
                val progress = (viewModel.currentCardIndex + 1).toFloat() / viewModel.totalCards
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
                    text = "Question ${viewModel.currentCardIndex + 1} of ${viewModel.totalCards}",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 12.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Flashcard Area
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    viewModel.currentCard?.let { card ->
                        FlashcardItem(
                            card = card,
                            isAnswerRevealed = viewModel.isAnswerRevealed,
                            onReveal = { viewModel.revealAnswer() },
                            onSkip = { viewModel.skipCard() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Action Buttons (Correct / Wrong)
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
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
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
                                colors = ButtonDefaults.buttonColors(containerColor = MorphLightGreen),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Done, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Correct")
                            }
                        }
                    }
                    
                    if (!viewModel.isAnswerRevealed) {
                        Text(
                            text = "Tap the card to see the answer",
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
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
    onReveal: () -> Unit,
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
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .clickable(enabled = !isAnswerRevealed) { onReveal() }
            .padding(24.dp)
    ) {
        TextButton(
            onClick = onSkip,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .graphicsLayer { 
                    if (rotation > 90f) rotationY = 180f 
                }
        ) {
            Text("Skip", color = MorphTeal, fontWeight = FontWeight.Bold)
        }

        if (rotation <= 90f) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    color = MorphTeal.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "QUESTION",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MorphTeal,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = card.qn,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    color = TextDark,
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
                    color = MorphPurple.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "ANSWER",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MorphPurple,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = card.ans,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    color = TextDark,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun FlashcardsResultScreen(
    correct: Int,
    total: Int,
    onBackToHome: () -> Unit
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
            elevation = CardDefaults.elevatedCardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Session Complete!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.size(140.dp),
                        color = Color.LightGray.copy(alpha = 0.2f),
                        strokeWidth = 12.dp
                    )
                    CircularProgressIndicator(
                        progress = { percentage / 100f },
                        modifier = Modifier.size(140.dp),
                        color = if (percentage >= 50) MorphTeal else Color(0xFFE57373),
                        strokeWidth = 12.dp
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$percentage%",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextDark
                        )
                        Text(
                            text = "Accuracy",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "You got $correct out of $total flashcards correct.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(40.dp))
                
                Button(
                    onClick = onBackToHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MorphTeal),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Back to Home", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

//@SuppressLint("ViewModelConstructorInComposable")
//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//fun FlashcardsScreenPreview() {
//    // We create a local ViewModel instance for the preview
//    val previewViewModel = FlashcardsViewModel()
//
//    // We wrap your existing Screen function
//    FlashcardsScreen(
//        onBackToLibrary = {},
//        onBackToHome = {},
//        viewModel = previewViewModel
//    )
//}

//@SuppressLint("ViewModelConstructorInComposable")
//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//fun FlashcardsScreenAnswerPreview() {
//    val previewViewModel = FlashcardsViewModel().apply {
//        revealAnswer() // This forces the state to 'true' for this specific preview
//    }
//
//    FlashcardsScreen(
//        onBackToLibrary = {},
//        onBackToHome = {},
//        viewModel = previewViewModel
//    )
//}
