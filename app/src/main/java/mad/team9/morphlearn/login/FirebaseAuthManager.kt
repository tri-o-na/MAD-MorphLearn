package mad.team9.morphlearn.login

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await

object FirebaseAuthManager {

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
        db.collection("Users")
            .document(userId)
            .set(mapOf("learningStyle" to style), SetOptions.merge())
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun isLearningStyleSet(): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            val document = db.collection("Users").document(uid).get().await()
            document.contains("learningStyle") && document.getString("learningStyle")?.isNotEmpty() == true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getLearningStyle(): String {
        val uid = auth.currentUser?.uid ?: return "Visual"
        return try {
            val document = db.collection("Users").document(uid).get().await()
            document.getString("learningStyle") ?: "Visual"
        } catch (e: Exception) {
            "Visual"
        }
    }

    fun signOut() {
        auth.signOut()
    }

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

    suspend fun updateLastLogin(uid: String) {
        db.collection("Users")
            .document(uid)
            .update("lastLogin", Timestamp.now())
            .await()
    }
}