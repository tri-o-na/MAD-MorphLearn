package mad.team9.morphlearn.ai

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mad.team9.morphlearn.login.FirebaseAuthManager

sealed class AINoteState{
    object Idle: AINoteState()
    object Loading: AINoteState()
    object Success: AINoteState()
    data class Error(val message: String): AINoteState()
}

class AINotesViewModel(private val repository: AINotesRepository): ViewModel() {
    private val _state = MutableStateFlow<AINoteState>(AINoteState.Idle)
    val state: StateFlow<AINoteState> = _state

    var isLoading by mutableStateOf(false)
        private set

    fun startLoading(){
        isLoading = true
    }

    fun endLoading(){
        isLoading = false
    }

    private val _response = MutableStateFlow("")
    val response: StateFlow<String> = _response

    fun setResponse(response: String){
        _response.value = response;
    }

    fun processAndSave(json: String, userId: String?){
        viewModelScope.launch {
            try {
                _state.value = AINoteState.Loading

                if (userId == null) throw Exception("User not logged in")

                val note = repository.parseJson(json)

                repository.saveNoteAndQuiz(userId,note)

                _state.value = AINoteState.Success

            } catch (e: Exception){
                _state.value = AINoteState.Error(e.message ?:"Invalid AI Response")
            }
        }
    }

    suspend fun getOrCreateSubject(subject: String): String{
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: throw Exception("User not logged in")

        return repository.getOrCreateSubjectId(userId,subject)
    }
}