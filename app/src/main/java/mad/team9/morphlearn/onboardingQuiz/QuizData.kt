package mad.team9.morphlearn.onboardingQuiz
enum class LearningStyle { READ_WRITE, KINESTHETIC }

data class Option(
    val text: String,
    val style: LearningStyle
)

data class Question(
    val id: Int,
    val text: String,
    val options: List<Option>
)

// Helper to get shuffled questions
fun getOnboardingQuestions(): List<Question> {
    return listOf(
        Question(1, "You are learning how to use a new piece of complex software. What is your first instinct?", listOf(
            Option("Open the \"Help\" documentation or user manual and read the instructions.", LearningStyle.READ_WRITE),
            Option("Start clicking buttons and exploring the interface to see what happens.", LearningStyle.KINESTHETIC),
            Option("Search for a written step-by-step tutorial or a \"Quick Start\" blog post.", LearningStyle.READ_WRITE),
            Option("Look for a physical demo or try to perform a task immediately while learning.", LearningStyle.KINESTHETIC),
        ).shuffled()), // Shuffles options within the question
        Question(2, "You need to assemble a new piece of furniture. How do you approach the task?", listOf(
            Option("Read every word of the instruction booklet before touching the parts.", LearningStyle.READ_WRITE),
            Option("Lay all the pieces out and start putting them together based on how they look.", LearningStyle.KINESTHETIC),
            Option("Keep the written guide nearby to refer to the text descriptions of each step.", LearningStyle.READ_WRITE),
            Option("Ignore the manual unless you get stuck; you prefer to feel how the pieces fit.", LearningStyle.KINESTHETIC),
        ).shuffled()), // Shuffles options within the question
        Question(3, "When you are trying to memorize a new concept for a presentation, you usually:", listOf(
            Option("Write out your notes over and over again until they stick.", LearningStyle.READ_WRITE),
            Option("Walk around the room or use hand gestures while reciting the points.", LearningStyle.KINESTHETIC),
            Option("Read your slides or a transcript of your speech multiple times.", LearningStyle.READ_WRITE),
            Option("Practice the actual physical delivery: standing up and moving as if you were on stage.", LearningStyle.KINESTHETIC),
        ).shuffled()), // Shuffles options within the question
        Question(4, "If you were attending a workshop on a new hobby (like cooking or coding), which part would you enjoy most?", listOf(
            Option("The detailed handouts and written recipes/code snippets provided.", LearningStyle.READ_WRITE),
            Option("The lab portion where you get to handle the tools and try it yourself.", LearningStyle.KINESTHETIC),
            Option("Taking your own exhaustive notes during the lecture.", LearningStyle.READ_WRITE),
            Option("A hands-on exercise where you move through the process physically.", LearningStyle.KINESTHETIC),
        ).shuffled()), // Shuffles options within the question
        // ... add other questions
    )
}