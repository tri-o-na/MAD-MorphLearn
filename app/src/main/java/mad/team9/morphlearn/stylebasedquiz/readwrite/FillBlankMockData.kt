package mad.team9.morphlearn.stylebasedquiz.readwrite

data class FillBlank(val qn: String, val ans: String)

val mockFillBlank = listOf(
    FillBlank(
        qn = "_ is the primary color of the MorphLearn logo.",
        ans = "Teal"
    ),
    FillBlank(
        qn = "_ learning style focuses on physical movement.",
        ans = "Kinesthetic"
    ),
    FillBlank(
        qn = "_ database is used in the CalBot project.",
        ans = "Room & DataStore"
    ),
    FillBlank(
        qn = "_ does the 'V' in the VARK learning model stand for.",
        ans = "Visual"
    ),
    FillBlank(
        qn = "_ icon represents the Auditory learning style.",
        ans = "Hearing"
    )
)