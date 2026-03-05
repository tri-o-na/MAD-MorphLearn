package mad.team9.morphlearn.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import mad.team9.morphlearn.login.FirebaseAuthManager  // ← ADD THIS IMPORT

@Composable
fun HomeScreen(
    username: String,
    navController: NavController? = null,  // ← make it optional for now
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome, $username!",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "We morph your learn 🚀",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(64.dp))

        // Placeholder for future content
        Text("Your personalized learning journey starts here...", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(48.dp))

        // Logout button
        Button(onClick = {
            FirebaseAuthManager.signOut()
            navController?.navigate("login") {
                popUpTo("home") { inclusive = true }
                // Optional: popUpTo("register") { inclusive = true }
            }
        }) {
            Text("Logout")
        }
        Button(onClick = {

            navController?.navigate("notes") {
                popUpTo("home") { inclusive = true }
                // Optional: popUpTo("register") { inclusive = true }
            }
        }) {
            Text("notes")
        }
    }
}