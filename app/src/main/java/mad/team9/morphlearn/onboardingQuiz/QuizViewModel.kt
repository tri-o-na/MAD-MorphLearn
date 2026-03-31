package mad.team9.morphlearn.onboardingQuiz

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class QuizViewModel : ViewModel() {
    var currentQuestionIndex by mutableStateOf(0)
    // Tracks which index (0, 1, 2, or 3) is currently highlighted
    var selectedOptionIndex by mutableStateOf<Int?>(null)

    val questions = getOnboardingQuestions()
    private val selectedStyles = mutableListOf<LearningStyle>()

    fun selectOption(index: Int) {
        selectedOptionIndex = index
    }

    fun moveToNext(onComplete: (LearningStyle) -> Unit) {
        val selectedIndex = selectedOptionIndex ?: return
        val style = questions[currentQuestionIndex].options[selectedIndex].style

        selectedStyles.add(style)

        if (currentQuestionIndex < questions.size - 1) {
            currentQuestionIndex++
            selectedOptionIndex = null
        } else {
            // Scoring Logic: Find the most frequent learning style
            val styleCounts = selectedStyles.groupingBy { it }.eachCount()
            val finalStyle = styleCounts.maxByOrNull { it.value }?.key ?: LearningStyle.READ_WRITE

            onComplete(finalStyle)
        }
    }
}