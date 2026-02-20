package mad.team9.morphlearn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import mad.team9.morphlearn.login.LoginScreen
import mad.team9.morphlearn.login.RegisterScreen
import mad.team9.morphlearn.ui.theme.MorphLearnTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MorphLearnTheme {
                // State must be inside the setContent / Theme block
                var isLoggedIn by remember { mutableStateOf(false) }
                var currentScreen by remember { mutableStateOf("login") }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val modifier = Modifier.padding(innerPadding)

                    if (isLoggedIn) {
                        // Once logged in, show the main navigation app
                        MorphLearnApp(modifier = modifier)
                    } else {
                        // Switch between Login and Register screens
                        when (currentScreen) {
                            "login" -> LoginScreen(
                                onLoginSuccess = { username ->
                                    isLoggedIn = true
                                },
                                onNavigateToRegister = {
                                    currentScreen = "register"
                                },
                                modifier = modifier
                            )
                            "register" -> RegisterScreen(
                                onRegisterSuccess = {
                                    isLoggedIn = true
                                },
                                onBackToLogin = {
                                    currentScreen = "login"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}