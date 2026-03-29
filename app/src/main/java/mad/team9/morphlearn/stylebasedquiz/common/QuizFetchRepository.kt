package mad.team9.morphlearn.stylebasedquiz.common

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class QuizFetchRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private fun uid(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")

    suspend fun getAllQuizzes(): List<QuizMeta> {
        val snap = db.collection("Users")
            .document(uid())
            .collection("Quizzes")
            .get()
            .await()

        return snap.documents.map { doc ->
            QuizMeta(
                quizId = doc.id,
                materialId = doc.getString("materialId") ?: ""
            )
        }
    }

    suspend fun getQuizQuestions(quizId: String): List<QuizQuestion> {
        val doc = db.collection("Users")
            .document(uid())
            .collection("Quizzes")
            .document(quizId)
            .get()
            .await()

        if (!doc.exists()) return emptyList()

        @Suppress("UNCHECKED_CAST")
        val rawQuestions = doc.get("questions") as? List<Map<String, Any>> ?: emptyList()

        return rawQuestions.map { q ->
            val questionText = q["question"] as? String ?: ""
            val correctIndex = (q["correctIndex"] as? Number)?.toInt() ?: -1
            @Suppress("UNCHECKED_CAST")
            val options = q["options"] as? List<String> ?: emptyList()

            QuizQuestion(
                question = questionText,
                options = options,
                correctIndex = correctIndex
            )
        }
    }

    suspend fun getQuizIdByMaterialId(materialId: String): String? {
        val snap = db.collection("Users")
            .document(uid())
            .collection("Quizzes")
            .whereEqualTo("materialId", materialId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .await()

        return snap.documents.firstOrNull()?.id
    }
}
