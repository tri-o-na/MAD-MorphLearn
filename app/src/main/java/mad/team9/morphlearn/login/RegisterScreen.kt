package mad.team9.morphlearn.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Register", style = MaterialTheme.typography.displayLarge)

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )

        errorMessage?.let {
            Text(
                text = it,
                color = Color.Red,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        }

        Button(
            onClick = {
                if (email.isBlank() || password.length < 6) {
                    errorMessage = "Email required & password ≥ 6 characters"
                    return@Button
                }
                isLoading = true
                errorMessage = null

                coroutineScope.launch {
                    val result = FirebaseAuthManager.signUp(email.trim(), password)
                    isLoading = false

                    result.onSuccess {
                        onRegisterSuccess()           // usually go to main screen
                        // Optionally: show toast "Account created!"
                    }.onFailure { e ->
                        errorMessage = when {
                            e.message?.contains("EMAIL_EXISTS") == true -> "Email already in use"
                            e.message?.contains("INVALID_EMAIL") == true -> "Invalid email format"
                            e.message?.contains("WEAK_PASSWORD") == true -> "Password too weak"
                            else -> e.message ?: "Registration failed"
                        }
                    }
                }
            },
            enabled = !isLoading && email.isNotEmpty() && password.length >= 6,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("Register")
        }

        TextButton(onClick = onBackToLogin) {
            Text("Back to Login")
        }
    }
}