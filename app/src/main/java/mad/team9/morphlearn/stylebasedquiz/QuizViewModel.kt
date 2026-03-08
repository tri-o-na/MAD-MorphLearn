package mad.team9.morphlearn.stylebasedquiz

import androidx.lifecycle.ViewModel
import mad.team9.morphlearn.ai.AIQuizQuestion
import mad.team9.morphlearn.ai.AINote

class QuizViewModel(private val repository: QuizResultRepository) : ViewModel() {

    private var score = 0
    private var currentQuestionIndex = 0
    private val userSelectedIndices = mutableListOf<Int>()

    // TASK: Validate answers and track progress
    fun submitAnswer(selectedIndex: Int, question: AIQuizQuestion): Boolean {
        val isCorrect = repository.isAnswerCorrect(selectedIndex, question)

        if (isCorrect) {
            score++
        }

        userSelectedIndices.add(selectedIndex)
        return isCorrect // UI person (Bryan) uses this Boolean to show Green/Red feedback
    }

    fun finishQuiz(userId: String, quizId: String, totalQuestions: Int, topic: String) {
        // 1. Create the QuizResult object (the "box")
        val result = QuizResult(
            userId = userId,
            quizId = quizId,
            score = score,
            totalQuestions = totalQuestions,
            userAnswers = userSelectedIndices
        )
        // 2. Pass the object and the topic to the repository
        repository.saveQuizAttempt(result, topic)
    }
    }
