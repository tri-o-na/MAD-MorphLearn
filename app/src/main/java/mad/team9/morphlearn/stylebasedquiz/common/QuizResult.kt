package mad.team9.morphlearn.stylebasedquiz.common

data class QuizResult(
    val userId: String,
    val quizId: String,
    val materialId: String,
    val score: Int,
    val totalQuestions: Int,
    val userAnswers: List<Int>,
    val attemptNumber: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
)
