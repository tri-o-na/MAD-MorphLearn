package mad.team9.morphlearn.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import mad.team9.morphlearn.R
import mad.team9.morphlearn.ui.theme.*

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var hasConsented by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text("AI Usage Consent", color = TextDark) },
            text = { Text("MorphLearn uses AI to personalize your study materials based on your learning diagnostics. Your data is strictly used for educational optimization.", color = TextDark) },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) { Text("Close", color = MorphTeal) }
            },
            containerColor = Color.White
        )
    }

    Box(
        modifier = Modifier.fillMaxSize().background(BackgroundGray),
        contentAlignment = Alignment.Center
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(0.85f).padding(vertical = 32.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Logo - Increased size from 80.dp to 120.dp
                Image(
                    painter = painterResource(id = R.drawable.morphlearn_logo),
                    contentDescription = "MorphLearn Logo",
                    modifier = Modifier.size(120.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("MorphLearn", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = TextDark)
                Text("Personalized Learning", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(24.dp))

                // Email Field
                Text("Email", modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.SemiBold, color = TextDark)
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("your@email.com", color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MorphTeal,
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password Field
                Text("Password", modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.SemiBold, color = TextDark)
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("••••••••", color = Color.LightGray) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MorphTeal,
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                // Consent Section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Checkbox(
                        checked = hasConsented,
                        onCheckedChange = { hasConsented = it },
                        colors = CheckboxDefaults.colors(checkedColor = MorphTeal)
                    )
                    TextButton(onClick = { showTermsDialog = true }, contentPadding = PaddingValues(0.dp)) {
                        Text("I consent to AI data collection", style = MaterialTheme.typography.bodySmall, color = MorphTeal)
                    }
                }

                if (errorMessage != null) {
                    Text(errorMessage!!, color = Color.Red, style = MaterialTheme.typography.labelSmall)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        isLoading = true
                        scope.launch {
                            val result = FirebaseAuthManager.signUp(email.trim(), password)
                            isLoading = false
                            result.onSuccess { user ->
                                FirebaseAuthManager.createMinimalUserProfile(user.uid, user.email ?: "")
                                onRegisterSuccess()
                            }.onFailure { errorMessage = it.localizedMessage }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = !isLoading && hasConsented,
                    colors = ButtonDefaults.buttonColors(containerColor = MorphTeal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Sign Up")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onBackToLogin) {
                    Text("Already have an account? Log in", color = MorphPurple)
                }
            }
        }
    }
}
