package mad.team9.morphlearn.stylebasedquiz

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import mad.team9.morphlearn.ai.AIQuizQuestion

class QuizResultRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    /**
     * TASK: Validate answers
     * Compares user selection to the model merged from Adli's branch.
     */
    fun isAnswerCorrect(selectedOptionIndex: Int, question: AIQuizQuestion): Boolean {
        return selectedOptionIndex == question.correctIndex
    }

    /**
     * TASK: Store quiz attempt results
     * Takes the QuizResult object and converts it to a Map to use server timestamps.
     */
    fun saveQuizAttempt(result: QuizResult, topic: String) {
        // Mapping the data class to a HashMap for Firestore
        val attemptData = hashMapOf(
            "userId" to result.userId,
            "quizId" to result.quizId,
            "score" to result.score,
            "totalQuestions" to result.totalQuestions,
            "userAnswers" to result.userAnswers,
            "topic" to topic,
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("Users")
            .document(result.userId)        // Look for the specific user
            .collection("QuizAttempts")    // Create/Add to their personal attempts list
            .add(attemptData)
            .addOnSuccessListener { Log.d("QuizResultRepo", "Saved to user's profile!") }
            .addOnFailureListener { e -> Log.e("QuizResultRepo", "Save failed", e) }
    }

    fun calculatePercentage(score: Int, total: Int): Int {
        if (total == 0) return 0
        return (score.toDouble() / total * 100).toInt()
    }
}