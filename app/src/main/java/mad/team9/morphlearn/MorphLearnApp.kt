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
                // FIX: Pass the missing navigation logic here
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
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = "home") {
            HomeScreen(
                username = username,
                modifier = modifier
            )
        }
    }
}