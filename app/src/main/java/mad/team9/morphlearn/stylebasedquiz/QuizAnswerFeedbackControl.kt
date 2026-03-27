package mad.team9.morphlearn.stylebasedquiz

class QuizAnswerFeedbackControl {

    fun isMcqAnswerCorrect(selectedIndex: Int, correctIndex: Int): Boolean {
        return selectedIndex == correctIndex
    }

    fun isDragDropAnswerCorrect(currentAnswer: String?, correctAnswer: String?): Boolean {
        return currentAnswer?.trim() == correctAnswer?.trim()
    }

    fun getFeedbackColor(isConfirmed: Boolean, isCorrect: Boolean?): androidx.compose.ui.graphics.Color {
        return when {
            !isConfirmed -> androidx.compose.ui.graphics.Color.Transparent
            isCorrect == true -> androidx.compose.ui.graphics.Color(0xFFDFF5E1)
            isCorrect == false -> androidx.compose.ui.graphics.Color(0xFFFDE2E1)
            else -> androidx.compose.ui.graphics.Color.Transparent
        }
    }

    fun getBorderColor(isConfirmed: Boolean, isCorrect: Boolean?): androidx.compose.ui.graphics.Color {
        return when {
            !isConfirmed -> androidx.compose.ui.graphics.Color.LightGray
            isCorrect == true -> androidx.compose.ui.graphics.Color(0xFF2E7D32)
            isCorrect == false -> androidx.compose.ui.graphics.Color(0xFFC62828)
            else -> androidx.compose.ui.graphics.Color.LightGray
        }
    }
}