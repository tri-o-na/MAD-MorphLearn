package mad.team9.morphlearn.ai

import com.google.firebase.Timestamp

data class AIQuiz(
    val materialId: String = "",
    val questions: List<AIQuizQuestion> = emptyList(),
    val timestamp: Timestamp = Timestamp.now()
)
