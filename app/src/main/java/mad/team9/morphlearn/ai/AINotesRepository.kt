package mad.team9.morphlearn.ai

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import org.json.JSONObject

class AINotesRepository(
    private val firestore: FirebaseFirestore
) {
    fun parseJson(json:String): AINote{
        val root = JSONObject(json)

        val title = root.optString("title","Missing Title")
        val notes = root.optString("generatedNotes","Missing Notes")

        return AINote(title,notes)
    }

    suspend fun saveNote(userId: String, note: AINote){
        val data = mapOf(
            "title" to note.title,
            "generatedNotes" to note.generatedNotes,
            "timestamp" to Timestamp.now()
        )

        firestore.collection("Users")
            .document(userId)
            .collection("Materials")
            .add(data)
            .await()
    }
}