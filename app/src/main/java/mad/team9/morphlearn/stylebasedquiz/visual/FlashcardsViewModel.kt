package mad.team9.morphlearn.stylebasedquiz.visual

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class FlashcardsViewModel : ViewModel() {
    private val cards = mockFlashcards.shuffled().toMutableList()
    
    var currentCardIndex by mutableIntStateOf(0)
    var isAnswerRevealed by mutableStateOf(false)
    var correctCount by mutableIntStateOf(0)
    var isFinished by mutableStateOf(false)
    
    val totalCards: Int get() = cards.size
    val currentCard: Flashcard?
        get() = if (currentCardIndex < cards.size) cards[currentCardIndex] else null

    fun revealAnswer() {
        isAnswerRevealed = true
    }

    fun onAnswered(isCorrect: Boolean) {
        if (isCorrect) {
            correctCount++
        }
        moveToNext()
    }

    fun skipCard() {
        // Move the current card to the end of the list
        // So user doesnt complete the flashcards if they jus skipped all of the questions
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
        } else {
            isFinished = true
        }
    }

    fun restart() {
        currentCardIndex = 0
        isAnswerRevealed = false
        correctCount = 0
        isFinished = false
    }
}
