package mad.team9.morphlearn.ai

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

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

    fun regenerateQuiz(materialId: String){
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: throw Exception("User not logged in")

        viewModelScope.launch {
            try{
                isLoading = true
                _state.value = AINoteState.Loading

                val weakQuestions = repository.getWrongQuestions(userId,materialId)
                val notes = repository.getNotes(userId,materialId)

                val newQuizJson = generateNewQuiz(weakQuestions, notes)

                // Inject material id  into newQuiz json
                val editNewQuizJson = JSONObject(newQuizJson).apply {
                    put("materialId", materialId)
                }

                val newQuiz = repository.parseNewQuizJson(editNewQuizJson.toString())

                repository.saveNewQuiz(userId,newQuiz)

                _state.value = AINoteState.Success

            } catch(e: Exception){
                Log.e("AIQuiz", "Error in regenerateQuiz: ${e.message}", e)
                _state.value = AINoteState.Error(e.message ?: "Failed to regenerate quiz")

            } finally {
                isLoading = false
            }
        }
    }
}