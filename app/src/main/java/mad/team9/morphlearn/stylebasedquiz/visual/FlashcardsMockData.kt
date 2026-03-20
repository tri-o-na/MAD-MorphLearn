package mad.team9.morphlearn.stylebasedquiz.visual

data class Flashcard(val qn: String, val ans: String)

val mockFlashcards = listOf(
    Flashcard(
        qn = "What is the primary color of the MorphLearn logo?",
        ans = "Teal"
    ),
    Flashcard(
        qn = "Which learning style focuses on physical movement?",
        ans = "Kinesthetic"
    ),
    Flashcard(
        qn = "Which database is used in the CalBot project?",
        ans = "Room & DataStore"
    ),
    Flashcard(
        qn = "What does the 'V' in the VARK learning model stand for?",
        ans = "Visual"
    ),
    Flashcard(
        qn = "Which icon represents the Auditory learning style?",
        ans = "Hearing"
    )
)