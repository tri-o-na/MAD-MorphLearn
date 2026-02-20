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
// Import your ProfileScreen here (adjust the package name if it's different)
import mad.team9.morphlearn.profile.ProfileScreen

@Composable
fun MorphLearnApp (
    modifier: Modifier
){
    val navController = rememberNavController()
    var username by rememberSaveable { mutableStateOf("") }

    NavHost(
        navController = navController,
        startDestination = "login"
    ){
        composable("login"){
            LoginScreen(
                onLoginSuccess = {
                    username = it
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                modifier = modifier
            )
        }

        composable("home") {
            HomeScreen(
                username = username,
                modifier = modifier,
                // Pass the navigation action to the button click
                onNavigateToProfile = {
                    navController.navigate("profile")
                }
            )
        }

        // 1. Add the new Profile destination
        composable("profile") {
            ProfileScreen()
        }
    }
}