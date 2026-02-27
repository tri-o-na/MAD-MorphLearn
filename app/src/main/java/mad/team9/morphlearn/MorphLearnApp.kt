package mad.team9.morphlearn

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mad.team9.morphlearn.home.HomeScreen
import mad.team9.morphlearn.login.LoginScreen
import mad.team9.morphlearn.login.RegisterScreen // Ensure this is imported
import mad.team9.morphlearn.onboardingQuiz.OnboardingQuizScreen

@Composable
fun MorphLearnApp(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    var username by rememberSaveable { mutableStateOf("") }

    NavHost(
        navController = navController,
        startDestination = "login",
        modifier = modifier
    ) {
        composable(route = "login") {
            LoginScreen(
                onLoginSuccess = { typedUsername ->
                    username = typedUsername
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                modifier = modifier
            )
        }

        composable(route = "register") {
            RegisterScreen(
                onRegisterSuccess = {
                    // Navigate to home after successful registration
                    navController.navigate("onboarding_quiz") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onBackToLogin = {
                    navController.popBackStack()
                }
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

        composable(route = "home") { backStackEntry ->
            // Extract the string from the navigation arguments
//            val style = backStackEntry.arguments?.getString("learningStyle") ?: "Read/Write"

            HomeScreen(
                username = username,
                learningStyle = "Read/Write", // HARDCODED HERE
                modifier = modifier
            )
        }
    }
}