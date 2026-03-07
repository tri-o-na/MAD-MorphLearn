package mad.team9.morphlearn.ai

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import mad.team9.morphlearn.login.FirebaseAuthManager
import java.net.URLDecoder

@Composable
fun AIGeneratedNotes(
    json: String,
    navController: NavController,
){
    val firestore = FirebaseFirestore.getInstance()
    val decodedJson = remember(json) { URLDecoder.decode(json,"UTF-8") }
    val repository = remember { AINotesRepository(firestore) }

    val userId = FirebaseAuth.getInstance().currentUser?.uid

    // Create the viewmodel
    val viewModel: AINotesViewModel = viewModel(
        factory = object : ViewModelProvider.Factory{
            override fun<T: ViewModel> create(modelClass: Class<T>): T{
                return AINotesViewModel(repository) as T
            }
        }
    )

    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.processAndSave(decodedJson,userId)
    }

    when (state) {
        is AINoteState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                CircularProgressIndicator()
            }
        }

        is AINoteState.Success -> {
            LaunchedEffect(Unit) {
                navController.navigate("home") {
                    popUpTo("home") {inclusive = true}
                }
            }
        }

        is AINoteState.Error -> {
            Text("Error saving notes: ${(state as AINoteState.Error).message}")
        }

        else -> {}
    }
}