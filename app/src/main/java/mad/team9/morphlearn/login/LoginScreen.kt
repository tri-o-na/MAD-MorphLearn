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
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

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

                if (errorMessage != null) {
                    Text(errorMessage!!, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        isLoading = true
                        scope.launch {
                            val result = FirebaseAuthManager.signIn(email.trim(), password)
                            isLoading = false
                            result.onSuccess { onLoginSuccess(it.uid) }
                                .onFailure { errorMessage = "Invalid email or password" }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = !isLoading,
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
                        Text("Log In")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onNavigateToRegister) {
                    Text("Don't have an account? Sign up", color = MorphPurple)
                }
            }
        }
    }
}
