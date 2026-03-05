package mad.team9.morphlearn.stylebasedquiz

data class QuizResult(
    val userId: String,           // The ID from your login-register work
    val quizId: String,           // The ID of the quiz document Adli created
    val score: Int,               // Calculated by your validation logic
    val totalQuestions: Int,      // Number of questions in the quiz
    val userAnswers: List<Int>,   // The indices the user actually clicked
    val timestamp: Long = System.currentTimeMillis()
)