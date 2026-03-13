package mad.team9.morphlearn.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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

    // NEW: Latest 3 Subject Progress
    val latestSubjectProgress = mutableStateListOf<SubjectProgress>()

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

        // 3. Quiz Stats, Subject Streaks, and Subject Progress
        quizListener = userDocRef.collection("QuizAttempts").addSnapshotListener { snapshot, _ ->
            val attempts = snapshot?.documents ?: emptyList()

            if (attempts.isNotEmpty()) {
                var totalPoints = 0.0
                var totalQuestions = 0.0
                val subjectDateMap = mutableMapOf<String, MutableSet<Long>>()
                val uniqueMaterialIds = mutableSetOf<String>()

                // Aggregation map for Subject Progress
                val subjectAggMap = mutableMapOf<String, SubjectAggregator>()

                for (doc in attempts) {
                    val mId = doc.getString("materialId") ?: ""
                    if (mId.isNotEmpty()) uniqueMaterialIds.add(mId)

                    val score = doc.getLong("score")?.toDouble() ?: 0.0
                    val questions = doc.getLong("totalQuestions")?.toDouble() ?: 1.0
                    val timestamp = doc.getTimestamp("timestamp")?.toDate() ?: Date()

                    totalPoints += score
                    totalQuestions += questions

                    // Clean Topic Name
                    val rawTopic = doc.getString("topic") ?: "General"
                    val cleanTopic = rawTopic.replace("+", " ")

                    // Update Aggregator for Subject Progress
                    val agg = subjectAggMap.getOrPut(cleanTopic) { SubjectAggregator(timestamp) }
                    agg.totalScore += score
                    agg.totalQuestions += questions
                    if (timestamp.after(agg.lastAttempt)) agg.lastAttempt = timestamp

                    val normalizedDate = normalizeToMidnight(timestamp)
                    subjectDateMap.getOrPut(cleanTopic) { mutableSetOf() }.add(normalizedDate)
                }

                // Update Stats
                completedQuizzes = uniqueMaterialIds.size
                successRate = "${((totalPoints / totalQuestions) * 100).toInt()}%"

                // Update Streaks
                subjectStreaks.clear()
                subjectDateMap.forEach { (topic, dates) ->
                    val streak = calculateStreakCount(dates)
                    if (streak > 0) subjectStreaks[topic] = streak
                }

                // NEW: Update Subject Progress List (Latest 3)
                latestSubjectProgress.clear()
                val sortedList = subjectAggMap.toList()
                    .sortedByDescending { it.second.lastAttempt }
                    .take(3)

                sortedList.forEach { (topic, data) ->
                    val accuracy = ((data.totalScore / data.totalQuestions) * 100).toInt()
                    latestSubjectProgress.add(
                        SubjectProgress(
                            subject = topic,
                            completedTopics = 1, // Logic can be expanded to count unique materialIds per topic
                            totalTopics = 1,
                            accuracy = accuracy,
                            currentStreak = 0,
                            longestStreak = 0
                        )
                    )
                }
            } else {
                successRate = "0%"
                completedQuizzes = 0
                subjectStreaks.clear()
                latestSubjectProgress.clear()
            }
        }
    }

    private fun normalizeToMidnight(date: Date): Long {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun calculateStreakCount(dates: Set<Long>): Int {
        if (dates.isEmpty()) return 0
        val cal = Calendar.getInstance()
        val today = normalizeToMidnight(cal.time)
        cal.add(Calendar.DATE, -1)
        val yesterday = normalizeToMidnight(cal.time)

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

    // Helper class for data processing
    private data class SubjectAggregator(
        var lastAttempt: Date,
        var totalScore: Double = 0.0,
        var totalQuestions: Double = 0.0
    )
}