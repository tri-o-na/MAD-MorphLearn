package mad.team9.morphlearn.login

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await

object FirebaseAuthManager {

    // ───────────────────────────────────────────────
    // Authentication part
    // ───────────────────────────────────────────────

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser? get() = auth.currentUser

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

    fun signOut() {
        auth.signOut()
    }

    // ───────────────────────────────────────────────
    // Firestore profile helpers
    // ───────────────────────────────────────────────

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * Creates a minimal user profile document in Firestore after signup.
     * You can easily extend this later by adding more fields.
     */
    suspend fun createMinimalUserProfile(uid: String, email: String) {
        val userData = mapOf(
            "email" to email,
            "createdAt" to Timestamp.now()
            // You can add more fields here in the future, for example:
            // "displayName" to displayName,
            // "username" to username,
            // "avatarUrl" to "",
            // "role" to "learner",
            // "points" to 0
        )

        db.collection("users")
            .document(uid)
            .set(userData)
            .await()
    }

    /**
     * Optional: Update last login time (can be called after successful signIn)
     */
    suspend fun updateLastLogin(uid: String) {
        db.collection("users")
            .document(uid)
            .update("lastLogin", Timestamp.now())
            .await()
    }
}