package mad.team9.morphlearn.home

data class SubjectProgress(
    val subject: String,
    val completedTopics: Int,
    val totalTopics: Int,
    val accuracy: Int,
    val currentStreak: Int,
    val longestStreak: Int
)

data class ProgressPoint(val date: String, val score: Int)

data class RadarData(val subjectName: String, val averageAccuracy: Float)

data class TopicTrend(
    val topicName: String,
    val points: List<ProgressPoint>,
    val color: androidx.compose.ui.graphics.Color
)