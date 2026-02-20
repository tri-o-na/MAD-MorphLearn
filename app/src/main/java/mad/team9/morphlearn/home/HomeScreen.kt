package mad.team9.morphlearn.home

import android.content.ContentValues.TAG
import android.util.Log
import android.widget.TextView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.R
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

@Composable
fun HomeScreen(
    username: String,
    modifier: Modifier
){
    val db = Firebase.firestore

    // This block runs once when the Composable enters the Composition
    LaunchedEffect(Unit) {
        db.collection("Users")
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Log.d("FirestoreTest", "Connection successful, but collection is EMPTY.")
                } else {
                    for (document in result) {
                        Log.d(
                            "FirestoreTest",
                            "Success! ID: ${document.id} => Data: ${document.data}"
                        )
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
    }
}
@Preview(
    showBackground = true,
    device = "id:pixel_8",
    showSystemUi = true
)
@Composable
fun HomeScreenPreview(){
    HomeScreen(username = "User", modifier = Modifier)
}