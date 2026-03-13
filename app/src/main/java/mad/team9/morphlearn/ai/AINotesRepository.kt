package mad.team9.morphlearn.ai

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import org.json.JSONObject

class AINotesRepository(
    private val firestore: FirebaseFirestore
) {
    fun parseJson(json:String): AINote {
        val root = JSONObject(json)

        val title = root.optString("title")
        require(title.isNotBlank()) { "Generated title missing" }

        val notes = root.optString("generatedNotes")
        require(notes.isNotBlank()) { "Generated notes missing" }

        val questionsArray = root.optJSONArray("questions")
            ?: throw IllegalArgumentException("Generated questions missing")

        val questions = mutableListOf<AIQuizQuestion>()

        for (i in 0 until questionsArray.length()) {
            val obj = questionsArray.getJSONObject(i)

            val questionText = obj.optString("question")
            require(questionText.isNotBlank()){"Generated question text missing"}

            val optionsJson = obj.optJSONArray("options")

            if (optionsJson != null) {
                require(optionsJson.length() == 4){
                    "Each question must have 4 options"
                }
            } else throw IllegalArgumentException("Generated question options missing")

            val options = List(4){
                index -> optionsJson.getString(index)
            }

            val correctIndex = obj.optInt("correctIndex", -1)
            require(correctIndex in 0..3){
                "Correct index invalid value"
            }

            questions.add(
                AIQuizQuestion(
                    question = questionText,
                    options = options,
                    correctIndex = correctIndex
                )
            )
        }
        return AINote(title, notes, questions)
    }


    suspend fun saveNoteAndQuiz(userId: String, note: AINote){
        val userRef = firestore.collection("Users").document(userId)

        val materialRef = userRef.collection("Materials").document()
        val quizRef = userRef.collection("Quizzes").document()

        val materialData = mapOf(
            "title" to note.title,
            "generatedNotes" to note.generatedNotes,
            "timestamp" to Timestamp.now()
        )

        val quizData = mapOf(
            "materialId" to materialRef.id,
            "questions" to note.questions.map{
                mapOf(
                    "question" to it.question,
                    "options" to it.options,
                    "correctIndex" to it.correctIndex
                )
            },
            "timestamp" to Timestamp.now()
        )

        val batch = firestore.batch()
        batch.set(materialRef, materialData)
        batch.set(quizRef,quizData)

        batch.commit().await()
    }

//    suspend fun saveQuiz(userId: String, materialId: String, questions: List<AIQuizQuestion>){
//        val data = mapOf(
//            "materialId" to materialId,
//            "questions" to questions.map{
//                mapOf(
//                    "question" to it.question,
//                    "options" to it.options,
//                    "correctIndex" to it.correctIndex
//                )
//            },
//            "timestamp" to Timestamp.now()
//        )
//
//        firestore.collection("Users")
//            .document(userId)
//            .collection("Quizzes")
//            .add(data)
//            .await()
//    }
}