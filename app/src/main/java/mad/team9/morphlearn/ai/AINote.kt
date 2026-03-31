package mad.team9.morphlearn.ai

import com.google.firebase.Timestamp

data class AINote(
    val title: String = "",
    val generatedNotes: String = "",
    val subjectId: String = "",
    val questions: List<AIQuizQuestion> = emptyList(),
    val timestamp: Timestamp = Timestamp.now()
)
