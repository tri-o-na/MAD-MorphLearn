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
import com.google.firebase.firestore.DocumentSnapshot
import androidx.compose.ui.graphics.Color
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

    // Latest 3 Subject Progress
    val latestSubjectProgress = mutableStateListOf<SubjectProgress>()

    private var userListener: ListenerRegistration? = null
    private var materialsListener: ListenerRegistration? = null
    private var subjectsListener: ListenerRegistration? = null
    private var quizListener: ListenerRegistration? = null

    // Cache for mapping
    private var currentMatToSubId = mapOf<String, String>()
    private var currentSubIdToName = mapOf<String, String>()
    private var currentSubIdToTotalMaterials = mapOf<String, Int>()
    private var currentAttempts = emptyList<DocumentSnapshot>()

    var radarDataList = mutableStateListOf<RadarData>()
    var selectedSubjectForLineChart by mutableStateOf<String?>(null)
    var topicTrends = mutableStateListOf<TopicTrend>()

    // Cache for dropdown switches
    private var allTopicData = mutableMapOf<String, Map<String, List<ProgressPoint>>>()

    fun fetchUserData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val userDocRef = db.collection("Users").document(userId)

        // 1. Learning Style
        userListener = userDocRef.addSnapshotListener { snapshot, _ ->
            learningStyle = snapshot?.getString("learningStyle") ?: "Not Set"
        }

        // 2. Subjects Mapping
        subjectsListener = userDocRef.collection("Subjects").addSnapshotListener { snapshot, _ ->
            currentSubIdToName = snapshot?.associate { it.id to (it.getString("name") ?: "Unknown") } ?: emptyMap()
            updateAggregatedStats()
        }

        // 3. Materials Mapping and Count
        materialsListener = userDocRef.collection("Materials").addSnapshotListener { snapshot, _ ->
            totalMaterials = snapshot?.size() ?: 0
            val matMap = mutableMapOf<String, String>()
            val counts = mutableMapOf<String, Int>()
            snapshot?.forEach { doc ->
                val sId = doc.getString("subjectId") ?: ""
                matMap[doc.id] = sId
                if (sId.isNotEmpty()) {
                    counts[sId] = (counts[sId] ?: 0) + 1
                }
            }
            currentMatToSubId = matMap
            currentSubIdToTotalMaterials = counts
            updateAggregatedStats()
        }

        // 4. Quiz Stats, Subject Streaks, and Subject Progress
        quizListener = userDocRef.collection("QuizAttempts").addSnapshotListener { snapshot, _ ->
            currentAttempts = snapshot?.documents ?: emptyList()
            updateAggregatedStats()
        }
    }

    private fun updateAggregatedStats() {
        val attempts = currentAttempts
        if (attempts.isEmpty()) {
            successRate = "0%"
            completedQuizzes = 0
            subjectStreaks.clear()
            latestSubjectProgress.clear()
            return
        }

        var totalPoints = 0.0
        var totalQuestions = 0.0
        val uniqueMaterialIds = mutableSetOf<String>()
        val subjectAggMap = mutableMapOf<String, SubjectAggregator>()

        for (doc in attempts) {
            val mId = doc.getString("materialId") ?: ""
            if (mId.isNotEmpty()) uniqueMaterialIds.add(mId)

            val score = doc.getLong("score")?.toDouble() ?: 0.0
            val questions = doc.getLong("totalQuestions")?.toDouble() ?: 1.0
            val timestamp = doc.getTimestamp("timestamp")?.toDate() ?: Date()

            totalPoints += score
            totalQuestions += questions

            // Get Subject Name using material mapping
            val sId = currentMatToSubId[mId] ?: ""
            val subjectName = currentSubIdToName[sId] ?: "General"

            // Update Aggregator for Subject Progress
            val agg = subjectAggMap.getOrPut(subjectName) { SubjectAggregator(timestamp, subjectId = sId) }
            agg.totalScore += score
            agg.totalQuestions += questions
            if (timestamp.after(agg.lastAttempt)) agg.lastAttempt = timestamp
            if (mId.isNotEmpty()) agg.uniqueMaterials.add(mId)
            agg.dates.add(normalizeToMidnight(timestamp))
        }

        // Update Stats
        completedQuizzes = uniqueMaterialIds.size
        successRate = if (totalQuestions > 0) "${((totalPoints / totalQuestions) * 100).toInt()}%" else "0%"

        // Update Streaks
        subjectStreaks.clear()
        val tempStreaks = mutableMapOf<String, Int>()
        subjectAggMap.forEach { (name, agg) ->
            val streak = calculateStreakCount(agg.dates)
            if (streak > 0) {
                subjectStreaks[name] = streak
            }
            tempStreaks[name] = streak
        }

        // Update Subject Progress List (Latest 3)
        latestSubjectProgress.clear()
        val sortedList = subjectAggMap.toList()
            .sortedByDescending { it.second.lastAttempt }
            .take(3)

        sortedList.forEach { (name, data) ->
            val accuracy = if (data.totalQuestions > 0) ((data.totalScore / data.totalQuestions) * 100).toInt() else 0
            val totalMaterialsInSubject = currentSubIdToTotalMaterials[data.subjectId] ?: data.uniqueMaterials.size
            
            latestSubjectProgress.add(
                SubjectProgress(
                    subject = name,
                    completedTopics = data.uniqueMaterials.size,
                    totalTopics = totalMaterialsInSubject,
                    accuracy = accuracy,
                    currentStreak = tempStreaks[name] ?: 0,
                    longestStreak = 0
                )
            )
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

    // Colors for the 3 lines in the chart
    private val topicColors = listOf(Color(0xFF006064), Color(0xFFA78BFA), Color(0xFFFFCC80))

    fun fetchChartData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("Users").document(userId)

        // Helper: Map MaterialID -> SubjectName
        userRef.collection("Materials").get().addOnSuccessListener { materialsSnap ->
            val matToSubId = materialsSnap.associate { it.id to (it.getString("subjectId") ?: "") }

            userRef.collection("Subjects").get().addOnSuccessListener { subjectsSnap ->
                val subIdToName = subjectsSnap.associate { it.id to (it.getString("name") ?: "Unknown") }

                // Now listen to QuizAttempts to build the charts
                userRef.collection("QuizAttempts").addSnapshotListener { quizSnap, _ ->
                    val attempts = quizSnap?.documents ?: return@addSnapshotListener

                    val subjectAggregator = mutableMapOf<String, MutableList<Double>>()
                    val topicLineAggregator = mutableMapOf<String, MutableMap<String, MutableList<ProgressPoint>>>()

                    attempts.forEach { doc ->
                        val mId = doc.getString("materialId") ?: ""
                        val sId = matToSubId[mId] ?: ""
                        val subjectName = subIdToName[sId] ?: "General"
                        val topicName = doc.getString("topic")?.replace("+", " ") ?: "General"
                        val score = doc.getLong("score")?.toDouble() ?: 0.0
                        val total = doc.getLong("totalQuestions")?.toDouble() ?: 1.0
                        val accuracy = (score / total) * 100
                        val date = doc.getTimestamp("timestamp")?.toDate()?.let { normalizeToMidnight(it) } ?: 0L

                        // For Radar Chart
                        subjectAggregator.getOrPut(subjectName) { mutableListOf() }.add(accuracy)

                        // For Line Chart (Subject -> Topic -> Points)
                        val subjectMap = topicLineAggregator.getOrPut(subjectName) { mutableMapOf() }
                        val points = subjectMap.getOrPut(topicName) { mutableListOf() }
                        points.add(ProgressPoint(date.toString(), accuracy.toInt()))
                    }

                    // Update Radar Data
                    radarDataList.clear()
                    subjectAggregator.forEach { (name, list) ->
                        radarDataList.add(RadarData(name, list.average().toFloat()))
                    }

                    // Cache for line charts
                    allTopicData = topicLineAggregator.mapValues { entry ->
                        entry.value.mapValues { it.value.toList() }
                    }.toMutableMap()

                    // Initialize dropdown if empty
                    if (selectedSubjectForLineChart == null && radarDataList.isNotEmpty()) {
                        selectedSubjectForLineChart = radarDataList.first().subjectName
                    }

                    // Update Line Chart based on selection
                    updateTopicLines(allTopicData)
                }
            }
        }
    }

    fun onSubjectSelected(subject: String) {
        selectedSubjectForLineChart = subject
        updateTopicLines(allTopicData)
    }

    private fun updateTopicLines(agg: Map<String, Map<String, List<ProgressPoint>>>) {
        val selected = selectedSubjectForLineChart ?: return
        topicTrends.clear()
        val topicsForSubject = agg[selected]?.toList()
            ?.sortedByDescending { it.second.lastOrNull()?.date ?: "" } // Latest 3 based on date
            ?.take(3) ?: emptyList()

        topicsForSubject.forEachIndexed { index, (name, points) ->
            topicTrends.add(TopicTrend(name, points.sortedBy { it.date }, topicColors[index % topicColors.size]))
        }
    }

    override fun onCleared() {
        super.onCleared()
        userListener?.remove()
        materialsListener?.remove()
        subjectsListener?.remove()
        quizListener?.remove()
    }

    // Helper class for data processing
    private data class SubjectAggregator(
        var lastAttempt: Date,
        var totalScore: Double = 0.0,
        var totalQuestions: Double = 0.0,
        val uniqueMaterials: MutableSet<String> = mutableSetOf(),
        val dates: MutableSet<Long> = mutableSetOf(),
        var subjectId: String = ""
    )
}
