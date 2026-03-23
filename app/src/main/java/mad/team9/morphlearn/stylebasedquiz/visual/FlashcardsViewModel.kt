package mad.team9.morphlearn.stylebasedquiz.visual

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import mad.team9.morphlearn.stylebasedquiz.QuizFetchRepository
import mad.team9.morphlearn.stylebasedquiz.QuizResult
import mad.team9.morphlearn.stylebasedquiz.QuizResultRepository

class FlashcardsViewModel : ViewModel() {
    private val fetchRepo = QuizFetchRepository()
    private val resultRepo = QuizResultRepository()
    
    val cards = mutableStateListOf<Flashcard>()
    
    var currentCardIndex by mutableIntStateOf(0)
    var isAnswerRevealed by mutableStateOf(false)
    var correctCount by mutableIntStateOf(0)
    var isFinished by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    private var currentMaterialId: String = ""

    val totalCards: Int get() = cards.size
    val currentCard: Flashcard?
        get() = if (currentCardIndex < cards.size) cards[currentCardIndex] else null

    fun loadQuizData(materialId: String) {
        if (currentMaterialId == materialId && cards.isNotEmpty()) return
        
        currentMaterialId = materialId
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val quizId = fetchRepo.getQuizIdByMaterialId(materialId)
                if (quizId != null) {
                    val quizQuestions = fetchRepo.getQuizQuestions(quizId)
                    val mappedCards = quizQuestions.mapNotNull { q ->
                        if (q.correctIndex in q.options.indices) {
                            Flashcard(qn = q.question, ans = q.options[q.correctIndex])
                        } else {
                            null
                        }
                    }
                    
                    if (mappedCards.isEmpty()) {
                        errorMessage = "This quiz has no valid questions."
                    } else {
                        cards.clear()
                        cards.addAll(mappedCards.shuffled())
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
    }

    fun toggleAnswer() {
        isAnswerRevealed = !isAnswerRevealed
    }

    fun onAnswered(isCorrect: Boolean) {
        if (isCorrect) {
            correctCount++
        }
        moveToNext()
    }

    fun skipCard() {
        if (currentCardIndex < cards.size) {
            val card = cards.removeAt(currentCardIndex)
            cards.add(card)
            isAnswerRevealed = false
        }
    }

    private fun moveToNext() {
        if (currentCardIndex < cards.size - 1) {
            currentCardIndex++
            isAnswerRevealed = false
        } else if (cards.isNotEmpty()) {
            isFinished = true
            saveAttempt()
        }
    }

    private fun saveAttempt() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            val quizId = fetchRepo.getQuizIdByMaterialId(currentMaterialId) ?: ""
            val attemptNumber = resultRepo.getNextAttemptNumber(userId, currentMaterialId)
            
            val result = QuizResult(
                userId = userId,
                quizId = quizId,
                materialId = currentMaterialId,
                score = correctCount,
                totalQuestions = totalCards,
                userAnswers = emptyList(),
                attemptNumber = attemptNumber
            )
            resultRepo.saveQuizAttempt(result, "Flashcards")
        }
    }

    fun restart() {
        resetState()
        cards.shuffle()
    }
}
