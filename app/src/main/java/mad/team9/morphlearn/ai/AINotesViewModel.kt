package mad.team9.morphlearn.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    fun processAndSave(json: String, userId: String?){
        viewModelScope.launch {
            try {
                _state.value = AINoteState.Loading

                if (userId == null) throw Exception("User not logged in")

                val note = repository.parseJson(json)

                repository.saveNote(userId,note)

                _state.value = AINoteState.Success

            } catch (e: Exception){
                _state.value = AINoteState.Error(e.message ?:"Unknown error")
            }
        }
    }
}