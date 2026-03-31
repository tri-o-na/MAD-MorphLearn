package mad.team9.morphlearn.ai

import android.adservices.topics.Topic
import com.google.firebase.Timestamp

data class AIQuizAttempt (
    val userId: String = "",
    val quizId: String = "",
    val materialId: String = "",
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val userAnswers: List<Int> = emptyList(),
    val attemptNumber: Int = 1,
    val timestamp: Timestamp = Timestamp.now(),
    val topic: String = "",
)
