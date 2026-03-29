package mad.team9.morphlearn.stylebasedquiz.auditory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mad.team9.morphlearn.stylebasedquiz.common.QuizAnswerFeedbackControl
import mad.team9.morphlearn.stylebasedquiz.common.QuizFetchRepository
import mad.team9.morphlearn.stylebasedquiz.common.QuizQuestion
import mad.team9.morphlearn.stylebasedquiz.common.QuizResult
import mad.team9.morphlearn.stylebasedquiz.common.QuizResultRepository

data class QuizPlayState(
    val loading: Boolean = false,
    val error: String? = null,
    val quizId: String = "",
    val materialId: String = "",
    val questions: List<QuizQuestion> = emptyList(),
    val index: Int = 0,
    val selectedAnswers: List<Int> = emptyList(),
    val confirmed: Boolean = false,
    val lastAnswerCorrect: Boolean? = null,
    val finalScore: Int? = null,
    val finished: Boolean = false
)

class QuizPlayViewModel(
    private val fetchRepo: QuizFetchRepository = QuizFetchRepository(),
    private val resultRepo: QuizResultRepository = QuizResultRepository()
) : ViewModel() {

    private val feedbackControl = QuizAnswerFeedbackControl()
    private val _state = MutableStateFlow(QuizPlayState())
    val state: StateFlow<QuizPlayState> = _state

    fun loadQuiz(quizId: String) {
        viewModelScope.launch {
            try {
                _state.value = QuizPlayState(loading = true, quizId = quizId)
                val questions = fetchRepo.getQuizQuestions(quizId)
                val meta = fetchRepo.getAllQuizzes().firstOrNull { it.quizId == quizId }
                val selections = List(questions.size) { -1 }

                _state.value = QuizPlayState(
                    loading = false,
                    quizId = quizId,
                    materialId = meta?.materialId ?: "",
                    questions = questions,
                    selectedAnswers = selections,
                    index = 0,
                    confirmed = false,
                    lastAnswerCorrect = null,
                    finished = false,
                    finalScore = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false, error = e.message)
            }
        }
    }

    fun selectAnswer(selectedIndex: Int) {
        val s = _state.value
        if (s.finished || s.confirmed || s.questions.isEmpty() || s.index !in s.questions.indices) return

        val updated = s.selectedAnswers.toMutableList()
        updated[s.index] = selectedIndex
        _state.value = s.copy(selectedAnswers = updated)
    }

    fun confirmAnswer() {
        val s = _state.value
        val q = s.questions.getOrNull(s.index) ?: return
        val selected = s.selectedAnswers.getOrNull(s.index) ?: -1
        if (selected == -1) return

        val correct = feedbackControl.isMcqAnswerCorrect(selected, q.correctIndex)
        _state.value = s.copy(confirmed = true, lastAnswerCorrect = correct)
    }

    fun nextOrFinish(topic: String) {
        val s = _state.value
        if (!s.confirmed) return

        val next = s.index + 1
        val done = next >= s.questions.size

        if (done) {
            viewModelScope.launch {
                val answerResults = s.questions.indices.map { i ->
                    val isCorrect = feedbackControl.isMcqAnswerCorrect(
                        s.selectedAnswers.getOrElse(i) { -1 },
                        s.questions[i].correctIndex
                    )
                    if (isCorrect) 1 else 0
                }

                val score = answerResults.sum()
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                val nextAttempt = resultRepo.getNextAttemptNumber(uid, s.materialId)

                val result = QuizResult(
                    userId = uid,
                    quizId = s.quizId,
                    materialId = s.materialId,
                    score = score,
                    totalQuestions = s.questions.size,
                    userAnswers = answerResults,
                    attemptNumber = nextAttempt
                )
                resultRepo.saveQuizAttempt(result, topic)
                _state.value = s.copy(finished = true, finalScore = score)
            }
        } else {
            _state.value = s.copy(index = next, confirmed = false, lastAnswerCorrect = null)
        }
    }

    fun submitFinalScore(score: Int, topic: String) {
        val s = _state.value
        viewModelScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val nextAttempt = resultRepo.getNextAttemptNumber(uid, s.materialId)
            val result = QuizResult(
                userId = uid,
                quizId = s.quizId,
                materialId = s.materialId,
                score = score,
                totalQuestions = s.questions.size,
                userAnswers = emptyList(),
                attemptNumber = nextAttempt
            )
            resultRepo.saveQuizAttempt(result, topic)
            _state.value = s.copy(finished = true, finalScore = score)
        }
    }
}
