package mad.team9.morphlearn.stylebasedquiz.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mad.team9.morphlearn.ai.AIQuizQuestion

class QuizViewModel(private val repository: QuizResultRepository) : ViewModel() {

    private var score = 0
    private var currentQuestionIndex = 0
    private val userSelectedIndices = mutableListOf<Int>()

    fun submitAnswer(selectedIndex: Int, question: AIQuizQuestion): Boolean {
        val isCorrect = repository.isAnswerCorrect(selectedIndex, question)
        if (isCorrect) {
            score++
        }
        userSelectedIndices.add(selectedIndex)
        return isCorrect
    }

    fun finishQuiz(userId: String, quizId: String, materialId: String, totalQuestions: Int, topic: String) {
        viewModelScope.launch {
            val nextAttempt = repository.getNextAttemptNumber(userId, materialId)
            val result = QuizResult(
                userId = userId,
                quizId = quizId,
                materialId = materialId,
                score = score,
                totalQuestions = totalQuestions,
                userAnswers = userSelectedIndices,
                attemptNumber = nextAttempt
            )
            repository.saveQuizAttempt(result, topic)
        }
    }
}
