package mad.team9.morphlearn.ai


import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import mad.team9.morphlearn.login.FirebaseAuthManager
import org.json.JSONObject

class AIQuizService (
    private val aiNotesRepository: AINotesRepository,
    private val authManager: FirebaseAuthManager = FirebaseAuthManager,
) {
    private fun getCurrentUserId(): String{
        return FirebaseAuth.getInstance().currentUser?.uid ?: throw Exception("User not logged in")
    }

    suspend fun getGenerateNotesAndQuiz(subject: String, topic:String, context: Context, uri: Uri) {
        val userId = getCurrentUserId()
        Log.d("AIQuizService", "getGenerateNotesAndQuiz called. UserID: $userId")

        // Get Subject Id
        val subjectId = aiNotesRepository.getOrCreateSubjectId(userId,subject)

        // Get AI response
        val responseJson = uploadPDFToAI(context, uri, authManager.getLearningStyle())

        // Inject Subject id into response json
        val editedResponse = JSONObject(responseJson).apply {
            put("subjectId", subjectId)
            if (topic.isNotEmpty()) put("title",topic)
        }

        // Process and Save
        val note = aiNotesRepository.parseJson(editedResponse.toString())
        aiNotesRepository.saveNoteAndQuiz(userId,note)
    }

    suspend fun regenerateQuiz(materialId: String){
        val userId = getCurrentUserId()
        Log.d("AIQuizService", "regenerateQuiz called. UserID: $userId")

        // Fetch data
        val weakQuestions = aiNotesRepository.getWrongQuestions(userId,materialId)
        val notes = aiNotesRepository.getNotes(userId,materialId)
        val learningStyle = authManager.getLearningStyle()

        // Generate new quiz
        val newQuizJson = generateNewQuiz(weakQuestions, notes, learningStyle)

        // Inject Material id into new quiz json
        val editedQuizJson = JSONObject(newQuizJson).apply {
            put("materialId", materialId)
        }

        // Process and Save
        val newQuiz = aiNotesRepository.parseNewQuizJson(editedQuizJson.toString())
        aiNotesRepository.saveNewQuiz(userId,newQuiz)
    }
}