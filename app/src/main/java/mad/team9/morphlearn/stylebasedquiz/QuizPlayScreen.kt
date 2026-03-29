package mad.team9.morphlearn.stylebasedquiz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import mad.team9.morphlearn.login.FirebaseAuthManager
import mad.team9.morphlearn.stylebasedquiz.common.*
import mad.team9.morphlearn.stylebasedquiz.auditory.AuditoryQuizUI
import mad.team9.morphlearn.stylebasedquiz.auditory.QuizPlayViewModel

@Composable
fun QuizPlayScreen(
    quizId: String,
    topic: String,
    onDone: () -> Unit,
    onBackToNotes: () -> Unit,
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
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        state.error != null -> Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Error: ${state.error}",
                color = MaterialTheme.colorScheme.error
            )
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
                    vm.submitFinalScore(score, topic)
                },
                onDone = onDone,
                standardQuizUI = {
                    AuditoryQuizUI(
                        state = state,
                        topic = topic,
                        vm = vm,
                        onBackToNotes = onBackToNotes,
                        enableTts = learningStyle?.trim()?.uppercase() == "AUDITORY"
                    )
                }
            )
        }
    }
}
