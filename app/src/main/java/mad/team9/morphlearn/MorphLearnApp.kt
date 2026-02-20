package mad.team9.morphlearn

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHost
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import mad.team9.morphlearn.ai.AIGeneratedNotes
import mad.team9.morphlearn.ai.AIUploadPDF
import mad.team9.morphlearn.home.HomeScreen
import mad.team9.morphlearn.login.LoginScreen


@Composable
fun MorphLearnApp (
    modifier: Modifier
){
    val navController = rememberNavController()
    var username by rememberSaveable { mutableStateOf("") }

    NavHost(
        navController,
        startDestination = "login"
    ){
        composable("login"){
            LoginScreen(
                onLoginSuccess = {
                    username = it
                    navController.navigate("upload") {
                        popUpTo("login") {inclusive = true}
                    }
                },
                modifier = modifier
            )
        }

        composable("home") {
            HomeScreen(
                username = username,
                modifier = modifier
            )
        }

        composable("upload") {
            AIUploadPDF(navController)
        }

        composable(
            route = "notes/{text}",
            arguments = listOf(
                navArgument("text") {type = NavType.StringType})
        ) {
            val text = it.arguments?.getString("text") ?: ""
            AIGeneratedNotes(text)
        }

    }
}

@Composable
fun AINavGraph(){
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "upload"
    ) {
        composable("upload") {
            AIUploadPDF(navController)
        }

        composable(
            route = "notes/{text}",
            arguments = listOf(
                navArgument("text") {type = NavType.StringType})
        ) {
            val text = it.arguments?.getString("text") ?: ""
            AIGeneratedNotes(text)
        }
    }
}