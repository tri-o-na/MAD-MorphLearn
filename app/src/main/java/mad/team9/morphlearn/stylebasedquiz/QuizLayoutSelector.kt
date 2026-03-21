package mad.team9.morphlearn.stylebasedquiz

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mad.team9.morphlearn.stylebasedquiz.kinesthetic.DragDropQuizScreen

@Composable
fun QuizLayoutSelector(
    learningStyle: String,
    state: QuizPlayState,
    topic: String,
    onFinish: (Int) -> Unit,
    onDone: () -> Unit,
    standardQuizUI: @Composable () -> Unit
) {
    // Normalize the string for comparison
    val normalizedStyle = learningStyle.trim().lowercase()

    when (normalizedStyle) {
        "KINESTHETIC" -> {
            DragDropQuizScreen(
                questions = state.questions,
                onFinish = onFinish
            )
        }
        "VISUAL" -> {
            TextPlaceholder("Visual: Flashcard Quiz coming soon")
        }
        "READ_WRITE", "readwrite" -> {
            TextPlaceholder("Read/Write: Fill in the Blank Quiz coming soon")
        }
        "auditory" -> {
            TextPlaceholder("Auditory: MCQ with Text-to-Speech coming soon")
        }
        else -> {
            // Use the passed standard UI
            standardQuizUI()
        }
    }
}

@Composable
fun TextPlaceholder(text: String) {
    Text(text, modifier = Modifier.padding(16.dp))
}
