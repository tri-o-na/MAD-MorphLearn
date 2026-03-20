package mad.team9.morphlearn.stylebasedquiz.kinesthetic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect

data class DragItem(
    val id: String,
    val content: String
)

class DropTargetState(
    val questionId: String,
    val questionText: String,
    val correctAnswer: String
) {
    var currentAnswer by mutableStateOf<String?>(null)
    var isCorrect by mutableStateOf<Boolean?>(null)
    var screenBounds by mutableStateOf(Rect.Zero)
}