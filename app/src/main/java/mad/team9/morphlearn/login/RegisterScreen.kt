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
import mad.team9.morphlearn.login.FirebaseAuthManager


@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Register", style = MaterialTheme.typography.displayLarge)

        OutlinedTextField(
            value = email,
            onValueChange = { email = it.trim() },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password (min 8 chars)") },
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
                if (email.isBlank() || password.length < 8) {
                    errorMessage = "Email required & password ≥ 8 characters"
                    return@Button
                }

                isLoading = true
                errorMessage = null

                scope.launch {
                    val result = FirebaseAuthManager.signUp(email, password)
                    isLoading = false

                    result.onSuccess { user ->
                        // Save minimal profile to Firestore
                        try {
                            FirebaseAuthManager.createMinimalUserProfile(
                                uid = user.uid,
                                email = user.email ?: ""
                            )
                            onRegisterSuccess()
                        } catch (e: Exception) {
                            // Don't block navigation if Firestore fails
                            println("Failed to save profile: ${e.message}")
                            onRegisterSuccess()
                        }
                    }

                    result.onFailure { e ->
                        errorMessage = when {
                            e.message?.contains("EMAIL_EXISTS", ignoreCase = true) == true ->
                                "Email already in use"
                            e.message?.contains("INVALID_EMAIL", ignoreCase = true) == true ->
                                "Invalid email format"
                            e.message?.contains("WEAK_PASSWORD", ignoreCase = true) == true ->
                                "Password too weak"
                            else -> e.localizedMessage ?: "Registration failed"
                        }
                    }
                }
            },
            enabled = !isLoading && email.isNotBlank() && password.length >= 6,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("Register")
        }

        TextButton(onClick = onBackToLogin) {
            Text("Back to Login")
        }
    }
}