package mad.team9.morphlearn.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotesViewModel(
    private val repo: MaterialsDataSource = MaterialsRepository()
) : ViewModel() {

    private val _materials = MutableStateFlow<List<Material>>(emptyList())
    val materials: StateFlow<List<Material>> = _materials

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadMaterials() {
        viewModelScope.launch {
            try {
                _error.value = null
                _materials.value = repo.getAllMaterials()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}