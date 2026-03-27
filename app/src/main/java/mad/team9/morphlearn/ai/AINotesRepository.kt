package mad.team9.morphlearn.ai

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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

        val subjectId = root.optString("subjectId")
        require(subjectId.isNotBlank()) {"Subject ID missing"}

        val questionsArray = root.optJSONArray("questions")
            ?: throw IllegalArgumentException("Generated questions missing")

        val questions = mutableListOf<AIQuizQuestion>()

        for (i in 0 until questionsArray.length()) {
            val obj = questionsArray.getJSONObject(i)

            val questionText = obj.optString("question")
            require(questionText.isNotBlank()){"Generated question text missing"}

            val optionsJson = obj.optJSONArray("options")
                ?: throw IllegalArgumentException("Generated question options missing")

            val options = List(optionsJson.length()){
                index -> optionsJson.getString(index)
            }

            val correctIndex = obj.optInt("correctIndex", -1)
            require(correctIndex in 0..<options.size){
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
        return AINote(title, generatedNotes = notes, subjectId, questions = questions)
    }

    fun parseNewQuizJson(json: String): AIQuiz{
        val root = JSONObject(json)

        val materialId = root.optString("materialId")
        require(materialId.isNotBlank()){"Material ID missing"}

        val questionsArray = root.optJSONArray("questions")
            ?: throw IllegalArgumentException("Generated questions missing")

        val questions = mutableListOf<AIQuizQuestion>()

        for (i in 0 until questionsArray.length()) {
            val obj = questionsArray.getJSONObject(i)

            val questionText = obj.optString("question")
            require(questionText.isNotBlank()){"Generated question text missing"}

            val optionsJson = obj.optJSONArray("options")
                ?: throw IllegalArgumentException("Generated question options missing")

            val options = List(optionsJson.length()){
                    index -> optionsJson.getString(index)
            }

            val correctIndex = obj.optInt("correctIndex", -1)
            require(correctIndex in 0..<options.size){
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

        return AIQuiz(materialId, questions)
    }

    suspend fun saveNoteAndQuiz(userId: String, note: AINote){
        val userRef = firestore.collection("Users").document(userId)

        val materialRef = userRef.collection("Materials").document()
        val quizRef = userRef.collection("Quizzes").document()

        val materialData = mapOf(
            "title" to note.title,
            "generatedNotes" to note.generatedNotes,
            "subjectId" to note.subjectId,
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

    suspend fun getOrCreateSubjectId(userId: String, subject: String): String{
        val userSubjectsRef = firestore.collection("Users").document(userId).collection("Subjects")

        val query = userSubjectsRef.whereEqualTo("name", subject).limit(1).get().await()

        if (query.isEmpty){
            val newSubjectRef = userSubjectsRef.document()
            val subjectData = mapOf(
                "name" to subject,
                "timestamp" to Timestamp.now()
            )
            newSubjectRef.set(subjectData).await()
            return newSubjectRef.id
        }
        else return query.documents[0].id
    }

    suspend fun getNotes(userId: String, materialId: String): String {
        val materialRef = firestore.collection("Users").document(userId)
            .collection("Materials").document(materialId).get().await()

        val materialData = materialRef.toObject(AINote::class.java) ?: return ""
        return materialData.generatedNotes
    }
    suspend fun getWrongQuestions(userId: String, materialId: String): List<String>{
        val latestAttemptRef = firestore.collection("Users").document(userId)
            .collection("QuizAttempts")
            .whereEqualTo("materialId", materialId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1).get().await().documents.firstOrNull() ?: return emptyList()

        val quizResult = latestAttemptRef.toObject(AIQuizAttempt::class.java) ?: return emptyList()
        val userAnswers = quizResult.userAnswers
        val quizId = quizResult.quizId

        val quizRef = firestore.collection("Users").document(userId)
            .collection("Quizzes").document(quizId).get().await()

        val quizData = quizRef.toObject(AIQuiz::class.java) ?: return emptyList()
        val questions = quizData.questions

        return questions.filterIndexed { index, questionObj ->
            index < userAnswers.size && userAnswers[index] != questionObj.correctIndex
        }.map { it.question }
    }

    suspend fun saveNewQuiz(userId: String, newQuiz: AIQuiz): String {
        try{
            val quizRef = firestore.collection("Users").document(userId)
                .collection("Quizzes").document()

            val newId = quizRef.id

            val quizData = mapOf(
                "materialId" to newQuiz.materialId,
                "questions" to newQuiz.questions.map{
                    mapOf(
                        "question" to it.question,
                        "options" to it.options,
                        "correctIndex" to it.correctIndex
                    )
                },
                "timestamp" to Timestamp.now()
            )

            quizRef.set(quizData).await()

            return newId
        } catch (e: Exception){
            throw e
        }
    }
}