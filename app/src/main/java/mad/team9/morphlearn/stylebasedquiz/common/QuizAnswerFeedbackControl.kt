package mad.team9.morphlearn.stylebasedquiz.common

import androidx.compose.ui.graphics.Color

class QuizAnswerFeedbackControl {

    fun isMcqAnswerCorrect(selectedIndex: Int, correctIndex: Int): Boolean {
        return selectedIndex == correctIndex
    }

    fun isDragDropAnswerCorrect(currentAnswer: String?, correctAnswer: String?): Boolean {
        return currentAnswer?.trim() == correctAnswer?.trim()
    }

    fun getFeedbackColor(isConfirmed: Boolean, isCorrect: Boolean?): Color {
        return when {
            !isConfirmed -> Color.Transparent
            isCorrect == true -> Color(0xFFDFF5E1)
            isCorrect == false -> Color(0xFFFDE2E1)
            else -> Color.Transparent
        }
    }

    fun getBorderColor(isConfirmed: Boolean, isCorrect: Boolean?): Color {
        return when {
            !isConfirmed -> Color.LightGray
            isCorrect == true -> Color(0xFF2E7D32)
            isCorrect == false -> Color(0xFFC62828)
            else -> Color.LightGray
        }
    }
}
