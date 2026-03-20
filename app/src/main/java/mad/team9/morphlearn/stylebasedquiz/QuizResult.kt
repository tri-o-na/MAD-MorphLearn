package mad.team9.morphlearn.stylebasedquiz

data class QuizResult(
    val userId: String,           // The ID from login-register work
    val quizId: String,           // The ID of the quiz document
    val materialId: String,       // The ID of the material this quiz was generated from
    val score: Int,               // Calculated by your validation logic
    val totalQuestions: Int,      // Number of questions in the quiz
    val userAnswers: List<Int>,   // The indices the user actually clicked
    val attemptNumber: Int = 1,   // To track if this is the 1st, 2nd, etc. attempt
    val timestamp: Long = System.currentTimeMillis()
)