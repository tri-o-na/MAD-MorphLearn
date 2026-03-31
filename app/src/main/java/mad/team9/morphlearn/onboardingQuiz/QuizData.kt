package mad.team9.morphlearn.onboardingQuiz
enum class LearningStyle(val displayName: String) {
    VISUAL("VISUAL"),
    AUDITORY("AUDITORY"),
    READ_WRITE("READ/WRITE"),
    KINESTHETIC("KINESTHETIC")
}

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
            Option("Look for a diagram or a flowchart showing how the software's features connect.", LearningStyle.VISUAL),
            Option("Find a podcast or a video where someone explains the features out loud.", LearningStyle.AUDITORY),
            Option("Open the \"Help\" documentation or user manual and read the text instructions.", LearningStyle.READ_WRITE),
            Option("Start clicking buttons and exploring the interface to see what happens.", LearningStyle.KINESTHETIC)
        ).shuffled()),  // Shuffles options within the question

        Question(2, "You need to find your way to a new coffee shop in a part of town you don't know. You would:", listOf(
            Option("Open a map app and look at the layout of the streets and landmarks.", LearningStyle.VISUAL),
            Option("Ask a friend for spoken directions or use voice-guided GPS.", LearningStyle.AUDITORY),
            Option("Read a list of written turn-by-turn directions.", LearningStyle.READ_WRITE),
            Option("Just start driving in the general direction and find it by 'feel'.", LearningStyle.KINESTHETIC)
        ).shuffled()),

        Question(3, "When you are trying to memorize a new concept for a presentation, you usually:", listOf(
            Option("Draw a mind map or use different colored highlighters to organize ideas.", LearningStyle.VISUAL),
            Option("Talk through the points out loud or record yourself and listen back.", LearningStyle.AUDITORY),
            Option("Write out your notes over and over again until they stick.", LearningStyle.READ_WRITE),
            Option("Walk around the room or use hand gestures while reciting the points.", LearningStyle.KINESTHETIC)
        ).shuffled()),

        Question(4, "If you were attending a workshop on a new hobby (like cooking), which part would you enjoy most?", listOf(
            Option("Watching a live demonstration or looking at photos of the finished dish.", LearningStyle.VISUAL),
            Option("Listening to the chef explain the history and science of the ingredients.", LearningStyle.AUDITORY),
            Option("The detailed handouts and written recipes provided.", LearningStyle.READ_WRITE),
            Option("The hands-on portion where you actually chop, stir, and cook.", LearningStyle.KINESTHETIC)
        ).shuffled()),

        Question(5, "You are choosing a new phone and want to learn about its features. You prefer to:", listOf(
            Option("Look at a comparison infographic or a gallery of the UI design.", LearningStyle.VISUAL),
            Option("Listen to a tech reviewer talk about their experience with it.", LearningStyle.AUDITORY),
            Option("Read a detailed article or the technical specifications list.", LearningStyle.READ_WRITE),
            Option("Go to a store and hold the phone to see how it feels in your hand.", LearningStyle.KINESTHETIC)
        ).shuffled())
        // Add more qns here
    )
}