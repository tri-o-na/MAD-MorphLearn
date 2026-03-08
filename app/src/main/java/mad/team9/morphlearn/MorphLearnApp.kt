package mad.team9.morphlearn

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import mad.team9.morphlearn.home.HomeScreen
import mad.team9.morphlearn.login.LoginScreen
import mad.team9.morphlearn.login.RegisterScreen
import androidx.compose.ui.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.google.firebase.firestore.FirebaseFirestore
import mad.team9.morphlearn.ai.AIFloatingActionButton
import mad.team9.morphlearn.ai.AIGeneratedNotes
import mad.team9.morphlearn.ai.AINotesRepository
import mad.team9.morphlearn.ai.AINotesViewModel
import mad.team9.morphlearn.ai.AIUploadPDF

@Composable
fun MorphLearnApp(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route

    val showAIFAB = route in listOf(
        "home"
    )

    // Decide start destination based on current auth state
    val startDestination by remember {
        mutableStateOf(
            if (FirebaseAuth.getInstance().currentUser != null) "home" else "login"
        )
    }

    val firestore = FirebaseFirestore.getInstance()
    val repository = remember { AINotesRepository(firestore) }


    // Create the viewmodel
    val aiNotesViewModel: AINotesViewModel = viewModel(
        factory = object : ViewModelProvider.Factory{
            override fun<T: ViewModel> create(modelClass: Class<T>): T{
                return AINotesViewModel(repository) as T
            }
        }
    )


    AppScaffold(
        fab= {
            if (showAIFAB){
                AIFloatingActionButton(navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ){
            composable("login") {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                            popUpTo("register") { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate("register") }
                )
            }

            composable("register") {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                            popUpTo("register") { inclusive = true }
                        }
                    },
                    onBackToLogin = { navController.popBackStack() }
                )
            }

            composable("home") {
                val user = FirebaseAuth.getInstance().currentUser
                val displayName = user?.email?.substringBefore("@") ?: "Learner"

                HomeScreen(
                    username = displayName,
                    navController = navController,  // ← add this
                    modifier = Modifier
                )
            }

            composable("upload-PDF") {
                AIUploadPDF(navController,aiNotesViewModel)
            }

            composable(
                route= "ai-response-PDF",
            ) {
                AIGeneratedNotes(navController,aiNotesViewModel)
            }
        }
    }
}

@Composable
fun AppScaffold(
    fab: (@Composable () -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
){
    Scaffold(
        floatingActionButton = { fab?.invoke()}
    ) {
        padding -> content(padding)
    }
}