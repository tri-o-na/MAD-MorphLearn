package mad.team9.morphlearn

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
// Import your ProfileScreen here (adjust the package name if it's different)
import mad.team9.morphlearn.profile.ProfileScreen
import mad.team9.morphlearn.login.RegisterScreen // Ensure this is imported
import mad.team9.morphlearn.onboardingQuiz.OnboardingQuizScreen

@Composable
fun MorphLearnApp(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    // Decide start destination based on current auth state
    val startDestination by remember {
        mutableStateOf(
            if (FirebaseAuth.getInstance().currentUser != null) "home" else "login"
        )
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
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
                    navController.navigate("onboarding_quiz") { // user is directed to do the onboard quiz as soon as they register
                        popUpTo("register") { inclusive = true }
                    }
                },
                onBackToLogin = { navController.popBackStack() }
            )
        }
        composable(route = "onboarding_quiz") {
            OnboardingQuizScreen(
                onQuizComplete = { style ->
                    // Navigate to home and pass the style as an argument
                    navController.navigate("home") {
                        popUpTo("onboarding_quiz") { inclusive = true }
                    }
                }
            )
        }

//        composable(route = "home") { backStackEntry ->
            // Extract the string from the navigation arguments
//            val style = backStackEntry.arguments?.getString("learningStyle") ?: "Read/Write"

            composable("home") {
                val user = FirebaseAuth.getInstance().currentUser
                val displayName = user?.email?.substringBefore("@") ?: "Learner"

                HomeScreen(
                    username = displayName,
                    navController = navController,  // ← add this
                    modifier = Modifier
                )
            }


        }
    }
