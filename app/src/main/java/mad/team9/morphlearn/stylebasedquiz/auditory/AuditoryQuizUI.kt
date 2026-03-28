package mad.team9.morphlearn.stylebasedquiz.auditory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import mad.team9.morphlearn.audio.rememberTextToSpeechController

@Composable
fun AuditoryQuizUI(
    state: QuizPlayState,
    topic: String,
    vm: QuizPlayViewModel,
    onBackToNotes: () -> Unit,
    enableTts: Boolean = true
) {
    val q = state.questions.getOrNull(state.index) ?: return
    val ttsController = rememberTextToSpeechController()
    var isSpeaking by remember { mutableStateOf(false) }
    var hasStartedSpeaking by remember { mutableStateOf(false) }

    val selectedIndex = state.selectedAnswers.getOrNull(state.index) ?: -1
    val hasSelected = selectedIndex != -1

    fun buildQuestionSpeech(): String = buildString {
        append(q.question).append(". ")
        q.options.forEachIndexed { i, opt -> append("Option ${i + 1}. $opt. ") }
    }

    LaunchedEffect(state.index, state.confirmed, state.lastAnswerCorrect) {
        if (enableTts && state.confirmed) {
            val feedback = if (state.lastAnswerCorrect == true) "Correct." else "Incorrect."
            ttsController.speak("$feedback The answer is ${q.options[q.correctIndex]}")
            isSpeaking = true
            hasStartedSpeaking = true
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(onClick = onBackToNotes, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
            Text("Back to Notes")
        }

        Text("Question ${state.index + 1} / ${state.questions.size}", style = MaterialTheme.typography.titleMedium)

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(q.question, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f).clickable { if (enableTts) ttsController.speak(q.question) })
            
            IconButton(
                onClick = {
                    if (isSpeaking) ttsController.stop() else ttsController.speak(buildQuestionSpeech())
                    isSpeaking = !isSpeaking
                    hasStartedSpeaking = true
                },
                modifier = Modifier.size(44.dp).border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Icon(imageVector = if (isSpeaking) Icons.Default.Stop else if (hasStartedSpeaking) Icons.Default.Refresh else Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }

        q.options.forEachIndexed { idx, option ->
            val isSelected = idx == selectedIndex
            val isCorrect = idx == q.correctIndex
            val color = when {
                state.confirmed && isCorrect -> Color(0xFFDFF5E1)
                state.confirmed && isSelected -> Color(0xFFFDE2E1)
                isSelected -> Color(0xFFE3F2FD)
                else -> MaterialTheme.colorScheme.surface
            }

            Surface(
                modifier = Modifier.fillMaxWidth().clickable { if (!state.confirmed) vm.selectAnswer(idx) },
                shape = MaterialTheme.shapes.medium,
                color = color,
                border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
            ) {
                Text(option, modifier = Modifier.padding(16.dp))
            }
        }

        if (!state.confirmed) {
            Button(onClick = { vm.confirmAnswer() }, enabled = hasSelected, modifier = Modifier.fillMaxWidth()) { Text("Confirm") }
        } else {
            Button(onClick = { vm.nextOrFinish(topic) }, modifier = Modifier.fillMaxWidth()) {
                Text(if (state.index == state.questions.lastIndex) "Finish Quiz" else "Next")
            }
        }
    }
}
