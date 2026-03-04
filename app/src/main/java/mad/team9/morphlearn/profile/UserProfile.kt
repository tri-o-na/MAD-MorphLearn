package mad.team9.morphlearn.profile // <--- ADD THIS LINE

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import mad.team9.morphlearn.login.FirebaseAuthManager

@Composable
fun ProfileScreen() {
    val db = Firebase.firestore
    // State to hold user data
    var name by remember { mutableStateOf("Loading...") }
    var email by remember { mutableStateOf("...") }
    var learnerType by remember { mutableStateOf("...") }
    val user = FirebaseAuth.getInstance().currentUser
    val displayName = user?.email?.substringBefore("@") ?: "Learner"

    // Fetch data from Firestore
    LaunchedEffect(Unit) {
        // Using the exact collection name "Users" we found earlier
        val userId = FirebaseAuth.getInstance().currentUser?.uid?: return@LaunchedEffect
        FirebaseFirestore.getInstance()
            .collection("Users").document(userId) // Use your actual Doc ID here
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    name = displayName
                    email = document.getString("email") ?: "No Email"
                    learnerType = document.getString("learningStyle") ?: "Not Set"
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Profile Picture Placeholder
        Surface(
            modifier = Modifier.size(120.dp).clip(CircleShape),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Picture",
                modifier = Modifier.padding(24.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Profile Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileDetailItem(label = "Name", value = name)
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                ProfileDetailItem(label = "Email", value = email)
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                ProfileDetailItem(label = "Learner Type", value = learnerType)
            }
        }
    }
}

@Composable
fun ProfileDetailItem(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}