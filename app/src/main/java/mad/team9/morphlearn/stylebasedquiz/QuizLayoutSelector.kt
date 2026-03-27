package mad.team9.morphlearn.stylebasedquiz

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mad.team9.morphlearn.stylebasedquiz.kinesthetic.DragDropQuizScreen
import mad.team9.morphlearn.stylebasedquiz.visual.FlashcardsScreen
import mad.team9.morphlearn.stylebasedquiz.readwrite.FillBlankScreen

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
    val normalizedStyle = learningStyle.trim().uppercase()

    when (normalizedStyle) {
        "KINESTHETIC" -> {
            DragDropQuizScreen(
                questions = state.questions,
                onFinish = onFinish
            )
        }
        "VISUAL" -> {
            FlashcardsScreen(
                materialId = state.materialId,
                onBackToLibrary = onDone,
                onBackToHome = onDone
            )
        }
        "READ_WRITE", "readwrite" -> {
            FillBlankScreen(
                materialId = state.materialId,
                onBackToLibrary = onDone,
                onBackToHome = onDone
            )
        }
        "AUDITORY" -> {
            standardQuizUI()
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
