package mad.team9.morphlearn.ai

import com.google.firebase.Timestamp

data class AIQuizQuestion(
    val question: String = "",
    val options: List<String> = emptyList(),
    val correctIndex: Int = 0,
    val timestamp: Timestamp = Timestamp.now()
)
