package mad.team9.morphlearn.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    modifier: Modifier
){
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isMatched by remember { mutableStateOf(false) }
        var attempted by remember { mutableStateOf(false) }

        Text(
            text = "Login",
            style = MaterialTheme.typography.displayLarge,
            modifier = modifier
        )

        TextField(
            value = username,
            onValueChange = {
                username = it
                attempted = false
            },
            label = { Text("Username") },
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        )

        TextField(
            value = password,
            onValueChange = {
                password = it
                attempted = false
            },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .testTag("passwordField")
        )

        if (!isMatched && attempted) {
            Text(
                text = "Password not matched",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Red,
                modifier = Modifier.testTag("errorText")
            )
        }

        Button(
            onClick = {
                isMatched = isPasswordMatched(username, password)
                if (isMatched) {
                    password = ""
                    onLoginSuccess(username)
                }

                attempted = true
            },
            enabled = isLoginFieldFilled(username, password),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .testTag("loginButton")
        ) {
            Text("Login")
        }
    }
}

fun isPasswordMatched(username: String, password: String): Boolean {
    return true
}

fun isLoginFieldFilled(username: String, password: String): Boolean {
    return username.isNotEmpty() && password.isNotEmpty()
}

@Preview(
    showBackground = true,
    device = "id:pixel_8",
    showSystemUi = true
)
@Composable
fun LoginScreenPreview(){
    LoginScreen(
        onLoginSuccess = {},
        modifier = Modifier
    )
}
