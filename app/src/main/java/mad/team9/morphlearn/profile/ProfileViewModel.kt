package mad.team9.morphlearn.profile

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileViewModel : ViewModel() {
    // State to hold user data
    var name by mutableStateOf("Loading...")
    var email by mutableStateOf("...")
    var learnerType by mutableStateOf("...")

    /**
     * Extracts the name from the email address for testing purposes.
     */
    fun parseDisplayName(email: String?): String {
        return email?.substringBefore("@") ?: "Learner"
    }

    /**
     * Manually update state (used for UI tests and state management).
     */
    fun updateState(newName: String, newEmail: String, newStyle: String) {
        name = newName
        email = newEmail
        learnerType = newStyle
    }

    /**
     * Fetches real data from Firestore.
     */
    fun fetchUserData() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val displayName = parseDisplayName(user.email)

        FirebaseFirestore.getInstance()
            .collection("Users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    updateState(
                        newName = displayName,
                        newEmail = document.getString("email") ?: "No Email",
                        newStyle = document.getString("learningStyle") ?: "Not Set"
                    )
                }
            }
    }
}