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
import com.google.firebase.firestore.FirebaseFirestore
import mad.team9.morphlearn.login.FirebaseAuthManager
import java.net.URLDecoder

@Composable
fun AIGeneratedNotes(
    navController: NavController,
    aiNotesViewModel: AINotesViewModel
){
    val userId = FirebaseAuthManager.currentUser?.uid

    val state by aiNotesViewModel.state.collectAsState()
    val response by aiNotesViewModel.response.collectAsState()

    LaunchedEffect(response) {
        if (response.isNotEmpty())
            aiNotesViewModel.processAndSave(response, userId)
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