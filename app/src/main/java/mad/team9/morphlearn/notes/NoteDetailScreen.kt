package mad.team9.morphlearn.notes

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import mad.team9.morphlearn.stylebasedquiz.QuizFetchRepository

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

    val quizId by viewModel.quizId.collectAsState()
    val hasAttemptedQuiz by viewModel.hasAttemptedQuiz.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadMaterials() }
    LaunchedEffect(materialId) { viewModel.getQuizIdByMaterialId(materialId) }
    LaunchedEffect(quizId) { viewModel.checkQuizAttempt(quizId) }

    Log.d("CHECK_QUIZ_ID", "Quiz ID: $quizId")

    val material = materials.firstOrNull { it.id == materialId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(onClick = onBack) { Text("Back") }
        Spacer(Modifier.height(12.dp))

        if (error != null) {
            Text("Error: $error", color = MaterialTheme.colorScheme.error)
            return
        }

        if (material == null) {
            Text("Loading topic...", style = MaterialTheme.typography.bodyLarge)
            return
        }

        Text(
            text = material.title.ifBlank { "Untitled Topic" },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(12.dp))

        // Notes container (better spacing + alignment)
        ElevatedCard(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val notes = material.generatedNotes.ifBlank { "(No generated notes yet)" }
                NotesFormattedText(text = notes)
            }
        }

        Spacer(Modifier.height(12.dp))

        Row() {
            val scope = rememberCoroutineScope()
            val quizRepo = remember { QuizFetchRepository() }

            Button(
                onClick = {
                    scope.launch {

                        if (quizId != null) onTakeQuiz(quizId!!, material.title)
                        else android.util.Log.e(
                            "NOTE_DETAILS",
                            "No quiz found for materialId=$materialId"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                Text("Take Quiz")
            }

            if (hasAttemptedQuiz) {
                Spacer(Modifier.weight(0.5f))

                Button(
                    onClick = {
                        viewModel.resetForNewQuiz()
                        onRegenerateQuiz(materialId)
                    },
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    Text("Regenerate Quiz")
                }
            }
        }
    }
}

@Composable
private fun NotesFormattedText(
    text: String,
    modifier: Modifier = Modifier
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
                        style = MaterialTheme.typography.titleMedium, // bigger heading
                        fontWeight = FontWeight.SemiBold
                    )
                }

                line.startsWith("* ") || line.startsWith("- ") || line.startsWith("• ") -> {
                    BulletLine(
                        content = line.drop(2).trim(),
                        textStyle = MaterialTheme.typography.bodyLarge // bigger body
                    )
                }

                line.startsWith("*") -> { // handles "*" without a space
                    BulletLine(
                        content = line.drop(1).trim(),
                        textStyle = MaterialTheme.typography.bodyLarge
                    )
                }

                else -> {
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodyLarge // bigger body
                    )
                }
            }
        }
    }
}

@Composable
private fun BulletLine(
    content: String,
    textStyle: TextStyle
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text("•", style = textStyle)
        Text(
            text = content,
            style = textStyle,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start
        )
    }
}