package mad.team9.morphlearn.ai

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mad.team9.morphlearn.login.FirebaseAuthManager
import mad.team9.morphlearn.notes.NotesViewModel
import org.json.JSONObject

sealed class AINoteState{
    object Idle: AINoteState()
    object Loading: AINoteState()
    object Success: AINoteState()
    data class Error(val message: String): AINoteState()
}

class AINotesViewModel(private val aiQuizService: AIQuizService): ViewModel() {
    private val _state = MutableStateFlow<AINoteState>(AINoteState.Idle)
    val state: StateFlow<AINoteState> = _state

    var isLoading by mutableStateOf(false)
        private set

    fun regenerateQuiz(materialId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                isLoading = true
                _state.value = AINoteState.Loading

                aiQuizService.regenerateQuiz(materialId)

                _state.value = AINoteState.Success
                onComplete()
            } catch (e: Exception) {
                _state.value = AINoteState.Error(e.message ?: "Failed to regenerate quiz")
            } finally {
                isLoading = false
            }
        }
    }

    fun generateNotesAndQuiz(subject: String, topic: String, context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                isLoading = true
                _state.value = AINoteState.Loading

                aiQuizService.getGenerateNotesAndQuiz(subject, topic, context, uri)

                _state.value = AINoteState.Success
            } catch (e: Exception) {
                _state.value = AINoteState.Error(e.message ?: "Failed to generate notes and quiz")
            } finally {
                isLoading = false
            }
        }
    }
}