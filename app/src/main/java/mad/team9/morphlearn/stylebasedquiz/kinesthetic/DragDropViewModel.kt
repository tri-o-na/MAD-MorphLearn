package mad.team9.morphlearn.stylebasedquiz.kinesthetic

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import mad.team9.morphlearn.notes.MaterialsRepository
import mad.team9.morphlearn.stylebasedquiz.common.QuizAnswerFeedbackControl
import mad.team9.morphlearn.stylebasedquiz.common.QuizFetchRepository
import mad.team9.morphlearn.stylebasedquiz.common.QuizResult
import mad.team9.morphlearn.stylebasedquiz.common.QuizResultRepository

class DragDropViewModel(
    private val fetchRepo: QuizFetchRepository = QuizFetchRepository(),
    private val resultRepo: QuizResultRepository = QuizResultRepository(),
    private val materialsRepo: MaterialsRepository = MaterialsRepository()
) : ViewModel() {

    private val feedbackControl = QuizAnswerFeedbackControl()

    // UI State
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var isFinished by mutableStateOf(false)
    var finalScore by mutableIntStateOf(0)
    var confirmed by mutableStateOf(false)

    // Pagination
    var currentPage by mutableIntStateOf(0)
    val pageSize = 3

    // Data State
    val allDropTargets = mutableStateListOf<DropTargetState>()
    private var currentMaterialId: String = ""
    private var materialTitle: String = "Unknown Topic"
    private var originalTotalCount: Int = 0

    val totalPages: Int
        get() = (originalTotalCount + pageSize - 1) / pageSize

    val currentTargets: List<DropTargetState>
        get() = allDropTargets.drop(currentPage * pageSize).take(pageSize)

    fun loadQuizData(materialId: String) {
        if (currentMaterialId == materialId && allDropTargets.isNotEmpty()) return

        currentMaterialId = materialId
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val material = materialsRepo.getMaterial(materialId)
                materialTitle = material?.title ?: "Unknown Topic"

                val quizId = fetchRepo.getQuizIdByMaterialId(materialId)
                if (quizId != null) {
                    val firebaseQuestions = fetchRepo.getQuizQuestions(quizId)
                    originalTotalCount = firebaseQuestions.size
                    
                    allDropTargets.clear()
                    allDropTargets.addAll(firebaseQuestions.map { q ->
                        DropTargetState(
                            questionId = q.question,
                            questionText = q.question,
                            correctAnswer = q.options.getOrElse(q.correctIndex) { "" }
                        )
                    })

                    if (allDropTargets.isEmpty()) {
                        errorMessage = "No questions found for this quiz."
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
        currentPage = 0
        isFinished = false
        confirmed = false
        finalScore = 0
    }

    fun handleConfirmOrNext() {
        if (!confirmed) {
            confirmed = true
        } else {
            val isLastPage = (currentPage + 1) * pageSize >= originalTotalCount
            if (isLastPage) {
                finishQuiz()
            } else {
                currentPage++
                confirmed = false
            }
        }
    }

    private fun finishQuiz() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        
        viewModelScope.launch {
            val resultsList = allDropTargets.map { target ->
                if (feedbackControl.isDragDropAnswerCorrect(target.currentAnswer, target.correctAnswer)) 1 else 0
            }

            val score = resultsList.sum()
            finalScore = score

            val quizId = fetchRepo.getQuizIdByMaterialId(currentMaterialId) ?: ""
            val attemptNumber = resultRepo.getNextAttemptNumber(userId, currentMaterialId)

            val result = QuizResult(
                userId = userId,
                quizId = quizId,
                materialId = currentMaterialId,
                score = score,
                totalQuestions = originalTotalCount,
                userAnswers = resultsList,
                attemptNumber = attemptNumber
            )

            resultRepo.saveQuizAttempt(result, materialTitle)
            isFinished = true
        }
    }

    fun handleMatch(target: DropTargetState, matchedValue: String) {
        if (!confirmed) {
            target.currentAnswer = matchedValue
        }
    }
}
