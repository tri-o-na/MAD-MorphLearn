package mad.team9.morphlearn.stylebasedquiz.readwrite

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mad.team9.morphlearn.stylebasedquiz.QuizFetchRepository
import mad.team9.morphlearn.stylebasedquiz.QuizResult
import mad.team9.morphlearn.stylebasedquiz.QuizResultRepository

class FillBlankViewModel : ViewModel() {
    private val fetchRepo = QuizFetchRepository()
    private val resultRepo = QuizResultRepository()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _questions = mutableStateListOf<FillBlank>()
    val questions: List<FillBlank> = _questions

    // Track results for each question: 1 for correct, 0 for wrong
    private val _userResults = mutableListOf<Int>()

    var currentQuestionIndex by mutableIntStateOf(0)
    var userAnswer by mutableStateOf("")
    var isAnswered by mutableStateOf(false)
    var isCorrect by mutableStateOf(false)
    var correctCount by mutableIntStateOf(0)
    var isFinished by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var subjectName by mutableStateOf<String?>(null)

    private var currentMaterialId: String = ""

    val currentQuestion: FillBlank?
        get() = if (currentQuestionIndex < _questions.size) _questions[currentQuestionIndex] else null

    val totalQuestions: Int get() = _questions.size

    fun loadQuizData(materialId: String) {
        if (currentMaterialId == materialId && _questions.isNotEmpty()) return
        
        currentMaterialId = materialId
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
                
                // Fetch Subject Name via Material path: /Users/{userId}/Materials/{materialId}
                launch {
                    try {
                        val materialDoc = db.collection("Users")
                            .document(userId)
                            .collection("Materials")
                            .document(materialId)
                            .get()
                            .await()
                            
                        val subjectId = materialDoc.getString("subjectId")
                        if (subjectId != null) {
                            // Fetch Subject path: /Users/{userId}/Subjects/{subjectId}
                            val subjectDoc = db.collection("Users")
                                .document(userId)
                                .collection("Subjects")
                                .document(subjectId)
                                .get()
                                .await()
                            subjectName = subjectDoc.getString("name")
                        }
                    } catch (e: Exception) {
                        // Metadata fetching is optional, don't block the quiz
                    }
                }

                // Fetch Quiz Data via Repository
                val quizId = fetchRepo.getQuizIdByMaterialId(materialId)
                if (quizId != null) {
                    val firebaseQuestions = fetchRepo.getQuizQuestions(quizId)
                    _questions.clear()
                    _questions.addAll(firebaseQuestions.map { q ->
                        // Map multiple choice data structure to Fill in the Blank
                        FillBlank(
                            qn = q.question,
                            ans = q.options.getOrElse(q.correctIndex) { "" }
                        )
                    }.filter { it.ans.isNotBlank() })
                    
                    if (_questions.isEmpty()) {
                        errorMessage = "No valid fill-in-the-blank questions found."
                    } else {
                        resetState()
                    }
                } else {
                    errorMessage = "No quiz found for this material."
                }
            } catch (e: Exception) {
                errorMessage = "Error loading quiz: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    private fun resetState() {
        currentQuestionIndex = 0
        userAnswer = ""
        isAnswered = false
        isCorrect = false
        correctCount = 0
        isFinished = false
        _userResults.clear()
    }

    fun submitAnswer() {
        val current = currentQuestion ?: return
        if (userAnswer.isBlank()) return
        
        isAnswered = true
        isCorrect = userAnswer.trim().equals(current.ans.trim(), ignoreCase = true)
        if (isCorrect) {
            correctCount++
        }
        // Record result: 1 for correct, 0 for wrong
        _userResults.add(if (isCorrect) 1 else 0)
    }

    fun nextQuestion() {
        if (currentQuestionIndex < _questions.size - 1) {
            currentQuestionIndex++
            userAnswer = ""
            isAnswered = false
            isCorrect = false
        } else {
            isFinished = true
            saveAttempt()
        }
    }

    private fun saveAttempt() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val quizId = fetchRepo.getQuizIdByMaterialId(currentMaterialId) ?: ""
            val attemptNumber = resultRepo.getNextAttemptNumber(userId, currentMaterialId)
            
            val result = QuizResult(
                userId = userId,
                quizId = quizId,
                materialId = currentMaterialId,
                score = correctCount,
                totalQuestions = totalQuestions,
                userAnswers = _userResults.toList(), // Pass the recorded results list
                attemptNumber = attemptNumber
            )
            // Saves to /Users/{userId}/QuizAttempts as per Repository architecture
            resultRepo.saveQuizAttempt(result, "Fill in the Blank")
        }
    }

    fun restart() {
        resetState()
    }
}
