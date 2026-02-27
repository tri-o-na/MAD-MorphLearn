package mad.team9.morphlearn.home

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class HomeViewModel : ViewModel() {
    private val db = Firebase.firestore

    // We can store the fetched data here to update the UI later
    var isDataLoaded = mutableStateOf(false)

    fun fetchUserData() {
        db.collection("Users")
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Log.d("FirestoreTest", "Connection successful, but collection is EMPTY.")
                } else {
                    for (document in result) {
                        Log.d("FirestoreTest", "Success! ID: ${document.id} => Data: ${document.data}")
                    }
                    isDataLoaded.value = true
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreTest", "Connection failed!", exception)
            }
    }
}