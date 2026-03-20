package mad.team9.morphlearn.stylebasedquiz.kinesthetic

import mad.team9.morphlearn.stylebasedquiz.QuizQuestion

object MockQuizProvider {
    fun getMockQuestions(): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                question = "What is the primary color of the MorphLearn logo?",
                options = listOf("Red", "Teal", "Blue", "Yellow"),
                correctIndex = 1 // Teal
            ),
            QuizQuestion(
                question = "Which learning style focuses on physical movement?",
                options = listOf("Visual", "Auditory", "Kinesthetic", "Read/Write"),
                correctIndex = 2 // Kinesthetic
            ),
            QuizQuestion(
                question = "Which database is used in the CalBot project?",
                options = listOf("SQL Server", "MongoDB", "Room & DataStore", "Oracle"),
                correctIndex = 2 // Room & DataStore
            )
        )
    }
}
