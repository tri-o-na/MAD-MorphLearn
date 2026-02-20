package mad.team9.morphlearn.home

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

@Composable
fun HomeScreen(
    username: String,
    modifier: Modifier = Modifier,
    onNavigateToProfile: () -> Unit // 1. Added navigation callback
){
    val db = Firebase.firestore

    LaunchedEffect(Unit) {
        db.collection("Users")
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    for (document in result) {
                        Log.d("FirestoreTest", "Success! ID: ${document.id} => ${document.data}")
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreTest", "Connection failed!", exception)
            }
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Welcome \n$username!",
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center,
            modifier = modifier
        )

        Text(
            text = "We morph your learn",
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center,
            modifier = modifier
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 2. The Navigation Button
        Button(
            onClick = { onNavigateToProfile() },
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text(text = "View Profile")
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_8", showSystemUi = true)
@Composable
fun HomeScreenPreview(){
    // Pass an empty lambda for the preview
    HomeScreen(username = "User", onNavigateToProfile = {})
}