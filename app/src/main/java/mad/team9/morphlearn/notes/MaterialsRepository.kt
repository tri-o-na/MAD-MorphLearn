package mad.team9.morphlearn.notes

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MaterialsRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : MaterialsDataSource{
    private fun uid(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")


    override suspend fun getAllMaterials(): List<Material> {
        val snapshot = db.collection("Users")
            .document(uid())
            .collection("Materials")
            .get()
            .await()

        return snapshot.documents.map { doc ->
            Material(
                id = doc.id,
                title = doc.getString("title") ?: "",
                generatedNotes = doc.getString("generatedNotes") ?: ""
            )
        }
    }

    suspend fun getMaterial(materialId: String): Material? {
        val doc = db.collection("Users")
            .document(uid())
            .collection("Materials")
            .document(materialId)
            .get()
            .await()

        if (!doc.exists()) return null

        return Material(
            id = doc.id,
            title = doc.getString("title") ?: "",
            generatedNotes = doc.getString("generatedNotes") ?: ""
        )
    }

    override suspend fun getLatestQuiz(materialId: String): String? {
        val doc = db.collection("Users")
            .document(uid())
            .collection("Quizzes")
            .whereEqualTo("materialId", materialId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .await()

        if (doc.isEmpty) return null

        return doc.documents.first().id
    }

    override suspend fun checkQuizAttempt(quizId: String?): Boolean {
        val snapshot = db.collection("Users")
            .document(uid())
            .collection("QuizAttempts")
            .whereEqualTo("quizId", quizId)
            .orderBy("timestamp")
            .limit(1)
            .get()
            .await()

        // if no documents, return false. else return true
        if (snapshot.isEmpty) return false

        return true
    }
}