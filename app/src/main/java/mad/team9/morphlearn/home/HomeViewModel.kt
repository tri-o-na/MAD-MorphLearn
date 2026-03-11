package mad.team9.morphlearn.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.Calendar
import java.util.Date

class HomeViewModel : ViewModel() {
    // UI States
    var learningStyle by mutableStateOf("Loading...")
    var completedQuizzes by mutableIntStateOf(0)
    var totalMaterials by mutableIntStateOf(0)
    var successRate by mutableStateOf("0%")

    // Streaks per subject
    val subjectStreaks = mutableStateMapOf<String, Int>()

    private var userListener: ListenerRegistration? = null
    private var materialsListener: ListenerRegistration? = null
    private var quizListener: ListenerRegistration? = null

    fun fetchUserData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val userDocRef = db.collection("Users").document(userId)

        // 1. Learning Style
        userListener = userDocRef.addSnapshotListener { snapshot, _ ->
            learningStyle = snapshot?.getString("learningStyle") ?: "Not Set"
        }

        // 2. Materials Count
        materialsListener = userDocRef.collection("Materials").addSnapshotListener { snapshot, _ ->
            totalMaterials = snapshot?.size() ?: 0
        }

        // 3. Quiz Stats & Subject Streaks
        quizListener = userDocRef.collection("QuizAttempts").addSnapshotListener { snapshot, _ ->
            val attempts = snapshot?.documents ?: emptyList()
            completedQuizzes = attempts.size

            if (attempts.isNotEmpty()) {
                var totalPoints = 0.0
                var totalQuestions = 0.0
                val subjectDateMap = mutableMapOf<String, MutableSet<Long>>()

                for (doc in attempts) {
                    totalPoints += doc.getLong("score")?.toDouble() ?: 0.0
                    totalQuestions += doc.getLong("totalQuestions")?.toDouble() ?: 1.0

                    // Clean Subject Title (Remove + signs)
                    val rawTopic = doc.getString("topic") ?: "General"
                    val cleanTopic = rawTopic.replace("+", " ")

                    val timestamp = doc.getTimestamp("timestamp")?.toDate()
                    if (timestamp != null) {
                        val normalizedDate = normalizeToMidnight(timestamp)
                        subjectDateMap.getOrPut(cleanTopic) { mutableSetOf() }.add(normalizedDate)
                    }
                }

                successRate = "${((totalPoints / totalQuestions) * 100).toInt()}%"

                // Calculate streaks for each subject
                subjectStreaks.clear()
                subjectDateMap.forEach { (topic, dates) ->
                    val streak = calculateStreakCount(dates)
                    if (streak > 0) {
                        subjectStreaks[topic] = streak
                    }
                }
            } else {
                successRate = "0%"
                subjectStreaks.clear()
            }
        }
    }

    // API 24 Friendly: Use Calendar to remove time components
    private fun normalizeToMidnight(date: Date): Long {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0) // Fixed reference
        return cal.timeInMillis
    }

    private fun calculateStreakCount(dates: Set<Long>): Int {
        if (dates.isEmpty()) return 0

        val cal = Calendar.getInstance()
        val today = normalizeToMidnight(cal.time)
        cal.add(Calendar.DATE, -1)
        val yesterday = normalizeToMidnight(cal.time)

        // Streak broken if no activity today or yesterday
        if (!dates.contains(today) && !dates.contains(yesterday)) return 0

        var streak = 0
        val checkCal = Calendar.getInstance()
        checkCal.timeInMillis = if (dates.contains(today)) today else yesterday

        while (dates.contains(normalizeToMidnight(checkCal.time))) {
            streak++
            checkCal.add(Calendar.DATE, -1)
        }
        return streak
    }

    override fun onCleared() {
        super.onCleared()
        userListener?.remove()
        materialsListener?.remove()
        quizListener?.remove()
    }
}