package mad.team9.morphlearn.ai

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import mad.team9.morphlearn.login.FirebaseAuthManager
import org.json.JSONObject
import java.net.URLDecoder

@Composable
fun AIGeneratedNotes (notesJSON: String, navController: NavController) {
    val decodedJSON = remember(notesJSON) {
        URLDecoder.decode(notesJSON,"UTF-8")
    }

    val db = remember { FirebaseFirestore.getInstance() }

    LaunchedEffect(Unit) {
        val userId = FirebaseAuthManager.currentUser?.uid

        if (userId != null) {
            try {
                val root = JSONObject(decodedJSON)

                val title = root.optString("title", "Missing Title")

                val generatedNotes = root.optString("generatedNotes", "Missing Notes")

                val data = mapOf(
                    "title" to title,
                    "generatedNotes" to generatedNotes,
                    "timestamp" to Timestamp.now()
                )

                db.collection("Users")
                    .document(userId)
                    .collection("Materials")
                    .add(data)
            } catch (e: Exception) {
                System.err.println(e.message)
            } finally {
                navController.navigate("home") {
                    popUpTo("home") { inclusive = true }
                }
            }
        }
        else {
            // Not logged in
            navController.navigate("login") {
                popUpTo("home") {inclusive = true}
            }
        }
    }

    // Show loading icon while saving to db
    Box(
        modifier= Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

