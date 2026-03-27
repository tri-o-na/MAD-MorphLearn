package mad.team9.morphlearn.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SubjectGroup(
    val subjectName: String,
    val materials: List<Material>
)

class NotesViewModel(
    private val repo: MaterialsDataSource = MaterialsRepository()
) : ViewModel() {

    private val _materials = MutableStateFlow<List<Material>>(emptyList())
    val materials: StateFlow<List<Material>> = _materials

    private val _quizId = MutableStateFlow<String?>(null)
    val quizId: StateFlow<String?> = _quizId

    private val _hasAttemptedQuiz = MutableStateFlow<Boolean>(false)
    val hasAttemptedQuiz: StateFlow<Boolean> = _hasAttemptedQuiz

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _groupedMaterials = MutableStateFlow<List<SubjectGroup>>(emptyList())
    val groupedMaterials: StateFlow<List<SubjectGroup>> = _groupedMaterials

    private val _learningStyle = MutableStateFlow<String?>(null)
    val learningStyle: StateFlow<String?> = _learningStyle

    fun loadLearningStyle() {
        viewModelScope.launch {
            try {
                _learningStyle.value = repo.getLearningStyle()
            } catch (e: Exception) {
                _learningStyle.value = null
            }
        }
    }
    fun loadMaterials() {
        viewModelScope.launch {
            try {
                _error.value = null
                val loadedMaterials = repo.getAllMaterials()
                _materials.value = loadedMaterials

                _groupedMaterials.value = loadedMaterials
                    .groupBy { it.subjectName.ifBlank { "Others" } }
                    .map { (subject, materials) ->
                        SubjectGroup(
                            subjectName = subject,
                            materials = materials.sortedBy { it.title.lowercase() }
                        )
                    }
                    .sortedBy {
                        if (it.subjectName.equals("Others", ignoreCase = true)) "zzz_others"
                        else it.subjectName.lowercase()
                    }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun getQuizIdByMaterialId(materialId: String) {
        viewModelScope.launch {
            try{
                _error.value = null
                _quizId.value = repo.getLatestQuiz(materialId)
            } catch (e: Exception){
                _error.value = e.message
            }
        }
    }

    fun resetForNewQuiz() {
        _quizId.value = null
        _hasAttemptedQuiz.value = false
    }

    fun checkQuizAttempt(quizId: String?) {
        if (quizId == null) {
            _hasAttemptedQuiz.value = false
            return
        }

        viewModelScope.launch {
            try {
                _error.value = null
                _hasAttemptedQuiz.value = repo.checkQuizAttempt(quizId)
            } catch (e: Exception){
                _error.value = e.message
            }
        }
    }
}