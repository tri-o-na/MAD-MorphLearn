package mad.team9.morphlearn.notes

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MaterialsRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    //private fun uid(): String =
        //auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
    private fun uid(): String {
        val debugUid = "Z98vdPxtc8CKmjyRhjw8" // your users doc id
        return auth.currentUser?.uid ?: debugUid
    }

    suspend fun getAllMaterials(): List<Material> {
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
        val doc = db.collection("users")
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
}