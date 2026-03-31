package mad.team9.morphlearn.stylebasedquiz.common

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.AggregateSource
import kotlinx.coroutines.tasks.await
import mad.team9.morphlearn.ai.AIQuizQuestion

class QuizResultRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun isAnswerCorrect(selectedOptionIndex: Int, question: AIQuizQuestion): Boolean {
        return selectedOptionIndex == question.correctIndex
    }

    suspend fun getNextAttemptNumber(userId: String, materialId: String): Int {
        return try {
            val countQuery = db.collection("Users")
                .document(userId)
                .collection("QuizAttempts")
                .whereEqualTo("materialId", materialId)
                .count()

            val snapshot = countQuery.get(AggregateSource.SERVER).await()
            (snapshot.count + 1).toInt()
        } catch (e: Exception) {
            Log.e("QuizResultRepo", "Error getting attempt count", e)
            1
        }
    }

    fun saveQuizAttempt(result: QuizResult, topic: String) {
        val attemptData = hashMapOf(
            "userId" to result.userId,
            "quizId" to result.quizId,
            "materialId" to result.materialId,
            "score" to result.score,
            "totalQuestions" to result.totalQuestions,
            "userAnswers" to result.userAnswers,
            "attemptNumber" to result.attemptNumber,
            "topic" to topic,
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("Users")
            .document(result.userId)
            .collection("QuizAttempts")
            .add(attemptData)
            .addOnSuccessListener { Log.d("QuizResultRepo", "Saved to user's profile!") }
            .addOnFailureListener { e -> Log.e("QuizResultRepo", "Save failed", e) }
    }

    fun calculatePercentage(score: Int, total: Int): Int {
        if (total == 0) return 0
        return (score.toDouble() / total * 100).toInt()
    }
}
