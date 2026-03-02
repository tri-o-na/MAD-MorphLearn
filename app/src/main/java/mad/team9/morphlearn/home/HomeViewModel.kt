package mad.team9.morphlearn.home

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class HomeViewModel : ViewModel() {
    var learningStyle by mutableStateOf("Loading...")
        private set

    // We can store the fetched data here to update the UI later
    var isDataLoaded = mutableStateOf(false)

    fun fetchUserData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                learningStyle = document.getString("learningStyle") ?: "Not Set"
            }
    }
}
