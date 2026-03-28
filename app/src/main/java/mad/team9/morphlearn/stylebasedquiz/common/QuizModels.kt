package mad.team9.morphlearn.stylebasedquiz.common

data class QuizMeta(
    val quizId: String = "",
    val materialId: String = ""
)

data class QuizQuestion(
    val question: String = "",
    val options: List<String> = emptyList(),
    val correctIndex: Int = -1
)
