package mad.team9.morphlearn.notes

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import mad.team9.morphlearn.audio.rememberTextToSpeechController
import mad.team9.morphlearn.ui.theme.*

@Composable
fun NoteDetailsScreen(
    materialId: String,
    viewModel: NotesViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onTakeQuiz: (quizId: String, topicTitle: String) -> Unit,
    onRegenerateQuiz: (materialId: String) -> Unit,
    onBack: () -> Unit = {}
) {
    val materials by viewModel.materials.collectAsState()
    val error by viewModel.error.collectAsState()
    val learningStyle by viewModel.learningStyle.collectAsState()

    val quizId by viewModel.quizId.collectAsState()
    val hasAttemptedQuiz by viewModel.hasAttemptedQuiz.collectAsState()

    val ttsController = rememberTextToSpeechController()
    var isSpeaking by remember { mutableStateOf(false) }
    var hasStartedSpeaking by remember { mutableStateOf(false) }

    val isAuditoryLearner = learningStyle?.trim()?.uppercase() == "AUDITORY"

    val isLoadingCompleted by viewModel.isLoadingCompleted.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initializeNoteData(materialId)
    }

    val material = materials.firstOrNull { it.id == materialId }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(16.dp)
    ) {
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(
                containerColor = MorphTeal,
                contentColor = Color.White
            )
        ) {
            Text("Back")
        }
        Spacer(Modifier.height(12.dp))

        if (error != null) {
            Text("Error: $error", color = MaterialTheme.colorScheme.error)
            return
        }

        if (material == null) {
            Text(
                "Loading topic.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextDark
            )
            return
        }

        val notes = material.generatedNotes.ifBlank { "(No generated notes yet)" }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = material.title.ifBlank { "Untitled Topic" },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextDark,
                modifier = Modifier.weight(1f)
            )

            if (isAuditoryLearner) {
                IconButton(
                    onClick = {
                        if (isSpeaking) {
                            ttsController.stop()
                            isSpeaking = false
                        } else {
                            ttsController.speak(notes)
                            isSpeaking = true
                            hasStartedSpeaking = true
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .border(
                            width = 1.5.dp,
                            color = MorphTeal,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = when {
                            isSpeaking -> Icons.Default.Stop
                            hasStartedSpeaking -> Icons.Default.Refresh
                            else -> Icons.Default.PlayArrow
                        },
                        contentDescription = when {
                            isSpeaking -> "Stop reading"
                            hasStartedSpeaking -> "Replay notes"
                            else -> "Read notes aloud"
                        },
                        tint = MorphTeal
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        ElevatedCard(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = Color.White,
                contentColor = TextDark
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NotesFormattedText(
                    text = notes,
                    onLineClick = { line ->
                        if (isAuditoryLearner) {
                            ttsController.speak(line)
                            isSpeaking = true
                            hasStartedSpeaking = true
                        }
                    }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Row {
            Button(
                onClick = {
                    if (quizId != null) onTakeQuiz(quizId!!, material.title)
                    else Log.e(
                        "NOTE_DETAILS",
                        "No quiz found for materialId=$materialId"
                    )
                },
                enabled = isLoadingCompleted,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MorphTeal,
                    contentColor = Color.White
                )
            ) {
                Text("Take Quiz")
            }

            Spacer(Modifier.weight(0.5f))

            Button(
                onClick = {
                    viewModel.resetForNewQuiz()
                    onRegenerateQuiz(materialId)
                },
                enabled = hasAttemptedQuiz,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MorphTeal,
                    contentColor = Color.White
                )
            ) {
                Text("Regenerate Quiz")
            }

        }
    }

    if (!isLoadingCompleted){
        NoteLoadingScreen()
    }
}

@Composable
private fun NotesFormattedText(
    text: String,
    modifier: Modifier = Modifier,
    onLineClick: (String) -> Unit = {}
) {
    val lines = text
        .replace("\r\n", "\n")
        .trim()
        .lines()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        lines.forEach { raw ->
            val line = raw.trim()
            if (line.isBlank()) return@forEach

            val isNumberHeading = Regex("""^\d+\.\s+.+""").matches(line)
            val isHeading = isNumberHeading || line.endsWith(":")

            when {
                isHeading -> {
                    Text(
                        text = line,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark,
                        modifier = Modifier.clickable { onLineClick(line) }
                    )
                }

                line.startsWith("* ") || line.startsWith("- ") || line.startsWith("• ") -> {
                    BulletLine(
                        content = line.drop(2).trim(),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        onClick = { onLineClick(line.drop(2).trim()) }
                    )
                }

                line.startsWith("*") -> {
                    BulletLine(
                        content = line.drop(1).trim(),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        onClick = { onLineClick(line.drop(1).trim()) }
                    )
                }

                else -> {
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextDark,
                        modifier = Modifier.clickable { onLineClick(line) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BulletLine(
    content: String,
    textStyle: TextStyle,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text("•", style = textStyle, color = TextDark)
        Text(
            text = content,
            style = textStyle,
            color = TextDark,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start
        )
    }
}

@Composable
fun NoteLoadingScreen(){
    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black.copy(alpha = 0.6f)) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            CircularProgressIndicator(color = Color.White, strokeWidth = 4.dp)
            Spacer(modifier= Modifier.height(16.dp))
            Text(
                text="Fetching data...",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "This may take a minute",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}