package mad.team9.morphlearn.stylebasedquiz.visual

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mad.team9.morphlearn.notes.MaterialsRepository
import mad.team9.morphlearn.stylebasedquiz.common.QuizFetchRepository
import mad.team9.morphlearn.stylebasedquiz.common.QuizResult
import mad.team9.morphlearn.stylebasedquiz.common.QuizResultRepository

// Local wrapper to track original index without modifying the global Flashcard class
data class FlashcardWithIndex(val card: Flashcard, val originalIndex: Int)

class FlashcardsViewModel(
    private val fetchRepo: QuizFetchRepository = QuizFetchRepository(),
    private val resultRepo: QuizResultRepository = QuizResultRepository(),
    private val materialsRepo: MaterialsRepository = MaterialsRepository()
) : ViewModel() {

    // Tracks cards that haven't been answered correctly/wrong yet
    val activeCards = mutableStateListOf<FlashcardWithIndex>()

    // Tracks results: originalIndex -> (1 for correct, 0 for wrong)
    val userAnswersMap = mutableStateMapOf<Int, Int>()

    var currentCardIndex by mutableIntStateOf(0)
    var isAnswerRevealed by mutableStateOf(false)
    var correctCount by mutableIntStateOf(0)
    var isFinished by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    private var currentMaterialId: String = ""
    private var originalTotalCount: Int = 0
    private var materialTitle: String = "Unknown Topic"

    val currentCard: Flashcard?
        get() = activeCards.getOrNull(currentCardIndex)?.card

    fun loadQuizData(materialId: String) {
        if (currentMaterialId == materialId && (activeCards.isNotEmpty() || userAnswersMap.isNotEmpty())) return

        currentMaterialId = materialId
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // Fetch material title
                val material = materialsRepo.getMaterial(materialId)
                materialTitle = material?.title ?: "Unknown Topic"

                val quizId = fetchRepo.getQuizIdByMaterialId(materialId)
                if (quizId != null) {
                    val quizQuestions = fetchRepo.getQuizQuestions(quizId)
                    val mappedCards = quizQuestions.mapIndexedNotNull { index, q ->
                        if (q.correctIndex in q.options.indices) {
                            FlashcardWithIndex(
                                card = Flashcard(qn = q.question, ans = q.options[q.correctIndex]),
                                originalIndex = index
                            )
                        } else {
                            null
                        }
                    }

                    if (mappedCards.isEmpty()) {
                        errorMessage = "This quiz has no valid questions."
                    } else {
                        originalTotalCount = mappedCards.size
                        activeCards.clear()
                        userAnswersMap.clear()
                        activeCards.addAll(mappedCards.shuffled())
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
        currentCardIndex = 0
        isAnswerRevealed = false
        correctCount = 0
        isFinished = false
        userAnswersMap.clear()
    }

    fun toggleAnswer() {
        isAnswerRevealed = !isAnswerRevealed
    }

    /**
     * Called when user marks a card as Correct or Wrong.
     * Records the answer and initiates a flip-back animation before moving to the next card.
     */
    fun onAnswered(isCorrect: Boolean) {
        val currentWrapper = activeCards.getOrNull(currentCardIndex) ?: return

        // Record the answer immediately
        userAnswersMap[currentWrapper.originalIndex] = if (isCorrect) 1 else 0

        if (isCorrect) {
            correctCount++
        }

        // Start flip back animation
        isAnswerRevealed = false

        // Delay removing the card to allow the flip animation to hide the content
        viewModelScope.launch {
            // Wait for approximately half of the 500ms animation (the 90-degree point)
            delay(150)

            // Remove from current session queue
            if (activeCards.isNotEmpty() && currentCardIndex < activeCards.size) {
                activeCards.removeAt(currentCardIndex)

                if (activeCards.isEmpty()) {
                    isFinished = true
                    saveAttempt()
                } else {
                    // After removal, currentCardIndex now points to what was the next card.
                    if (currentCardIndex >= activeCards.size) {
                        currentCardIndex = 0
                    }
                }
            }
        }
    }

    /**
     * Moves the current card to the back of the deck while preserving its original index.
     */
    fun skipCard() {
        if (activeCards.size > 1) {
            val wasRevealed = isAnswerRevealed
            isAnswerRevealed = false
            
            if (wasRevealed) {
                viewModelScope.launch {
                    delay(250)
                    if (activeCards.size > 1 && currentCardIndex < activeCards.size) {
                        val card = activeCards.removeAt(currentCardIndex)
                        activeCards.add(card)
                    }
                }
            } else {
                val card = activeCards.removeAt(currentCardIndex)
                activeCards.add(card)
            }
        } else if (activeCards.size == 1) {
            isAnswerRevealed = false
        }
    }

    private fun saveAttempt() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            val quizId = fetchRepo.getQuizIdByMaterialId(currentMaterialId) ?: ""
            val attemptNumber = resultRepo.getNextAttemptNumber(userId, currentMaterialId)

            // Sequential array of answers (0 or 1) ordered by original index
            val answersList = (0 until originalTotalCount).map { userAnswersMap[it] ?: 0 }

            val result = QuizResult(
                userId = userId,
                quizId = quizId,
                materialId = currentMaterialId,
                score = correctCount,
                totalQuestions = originalTotalCount,
                userAnswers = answersList,
                attemptNumber = attemptNumber
            )
            // Saves to Users/{userId}/QuizAttempts/{materialId}
            resultRepo.saveQuizAttempt(result, materialTitle)
        }
    }

    fun restart() {
        val mid = currentMaterialId
        currentMaterialId = ""
        loadQuizData(mid)
    }
}
