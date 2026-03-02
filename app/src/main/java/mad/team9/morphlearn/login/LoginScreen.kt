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
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,           // e.g. pass UID or email
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(text = "Login", style = MaterialTheme.typography.displayLarge)

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; errorMessage = null },
            label = { Text("Email") },
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
            Text(
                text = it,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        }

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Please fill in all fields"
                    return@Button
                }
                isLoading = true
                errorMessage = null

                coroutineScope.launch {
                    val result = FirebaseAuthManager.signIn(email.trim(), password)
                    isLoading = false

                    result.onSuccess { user ->
                        onLoginSuccess(user.uid)           // or user.email!!
                    }.onFailure { e ->
                        errorMessage = when {
                            e.message?.contains("INVALID_LOGIN_CREDENTIALS") == true ||
                                    e.message?.contains("INVALID_EMAIL") == true -> "Invalid email or password"
                            e.message?.contains("network") == true -> "Network error. Check connection"
                            else -> e.message ?: "Login failed"
                        }
                    }
                }
            },
            enabled = !isLoading && email.isNotEmpty() && password.isNotEmpty(),
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            Text("Login")
        }

        TextButton(onClick = onNavigateToRegister) {
            Text("Don't have an account? Register")
        }
    }
}