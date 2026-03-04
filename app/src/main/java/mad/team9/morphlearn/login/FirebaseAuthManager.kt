package mad.team9.morphlearn.login  // or a better package like auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await

object FirebaseAuthManager {

    // ───────────────────────────────────────────────
    // Authentication part
    // ───────────────────────────────────────────────

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> = try {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        Result.success(result.user!!)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun signUp(email: String, password: String): Result<FirebaseUser> = try {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        Result.success(result.user!!)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun saveLearningStyle(style: String): Result<Unit> = try {
        val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")

        // Use SetOptions.merge() to avoid overwriting existing fields (like email)
        db.collection("Users")
            .document(userId)
            .set(mapOf("learningStyle" to style), SetOptions.merge())
            .await()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun signOut() {
        auth.signOut()
    }

    // ───────────────────────────────────────────────
    // Firestore profile helpers
    // ───────────────────────────────────────────────



    /**
     * Creates a minimal user profile document in Firestore after signup.
     * You can easily extend this later by adding more fields.
     */
    suspend fun createMinimalUserProfile(uid: String, email: String) {
        val userData = mapOf(
            "email" to email,
            "createdAt" to Timestamp.now()
        )

        db.collection("Users")
            .document(uid)
            .set(userData)
            .await()
    }

    /**
     * Optional: Update last login time (can be called after successful signIn)
     */
    suspend fun updateLastLogin(uid: String) {
        db.collection("Users")
            .document(uid)
            .update("lastLogin", Timestamp.now())
            .await()
    }
}