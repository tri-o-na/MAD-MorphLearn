package mad.team9.morphlearn.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    onNavigateToRegister: () -> Unit, // Callback to switch screens
    modifier: Modifier = Modifier
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(text = "Login", style = MaterialTheme.typography.displayLarge)

        OutlinedTextField(
            value = username,
            onValueChange = { username = it; errorMessage = null },
            label = { Text("Username") },
            modifier = Modifier.padding(8.dp).fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; errorMessage = null },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.padding(8.dp).fillMaxWidth()
        )

        errorMessage?.let {
            Text(text = it, color = Color.Red, style = MaterialTheme.typography.bodySmall)
        }

        Button(
            onClick = {
                // HARDCODED LOGIC
                if (username == "admin" && password == "123456") {
                    onLoginSuccess(username)
                } else {
                    errorMessage = "Invalid hardcoded credentials"
                }
            },
            enabled = username.isNotEmpty() && password.isNotEmpty(),
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            Text("Login")
        }

        // The button that triggers the switch to the Register Screen
        TextButton(onClick = onNavigateToRegister) {
            Text("Don't have an account? Register")
        }
    }
}