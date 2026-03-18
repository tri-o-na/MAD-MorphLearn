package mad.team9.morphlearn.ai

data class AINote(
    val title: String,
    val generatedNotes: String,
    val subjectId: String,
    val questions: List<AIQuizQuestion>,
    val timestamp: Long = System.currentTimeMillis()
)
