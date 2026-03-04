package mad.team9.morphlearn.onboardingQuiz

//import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelTest {

    @Test
    fun testInitialState() = runTest {
        val viewModel = QuizViewModel()
        assertEquals(0, viewModel.currentQuestionIndex)
        assertEquals(null, viewModel.selectedOptionIndex)
    }

    @Test
    fun testOptionSelection() = runTest {
        val viewModel = QuizViewModel()
        viewModel.selectOption(2)
        assertEquals(2, viewModel.selectedOptionIndex)
    }

    @Test
    fun testScoringLogicReadWriteMajority() = runTest {
        val viewModel = QuizViewModel()
        val totalQuestions = viewModel.questions.size
        var resultStyle: LearningStyle? = null

        for (i in 0 until totalQuestions) {
            val readWriteIndex = viewModel.questions[i].options.indexOfFirst {
                it.style == LearningStyle.READ_WRITE
            }
            viewModel.selectOption(readWriteIndex)
            viewModel.moveToNext { result -> resultStyle = result }
        }

        assertEquals(LearningStyle.READ_WRITE, resultStyle)
    }

    @Test
    fun testScoringLogicKinestheticMajority() = runTest {
        val viewModel = QuizViewModel()
        val totalQuestions = viewModel.questions.size
        var resultStyle: LearningStyle? = null

        for (i in 0 until totalQuestions) {
            val kinestheticIndex = viewModel.questions[i].options.indexOfFirst {
                it.style == LearningStyle.KINESTHETIC
            }
            viewModel.selectOption(kinestheticIndex)
            viewModel.moveToNext { result -> resultStyle = result }
        }

        assertEquals(LearningStyle.KINESTHETIC, resultStyle)
    }
    @Test
    fun testStateResetOnNavigation() = runTest {
        val viewModel = QuizViewModel()
        viewModel.selectOption(0)

        // Move to the second question
        viewModel.moveToNext { }

        // Assert state is reset for the new question
        assertEquals(1, viewModel.currentQuestionIndex)
        assertEquals(null, viewModel.selectedOptionIndex)
    }

    @Test
    fun testScoringLogicTieDefaultsToReadWrite() = runTest {
        val viewModel = QuizViewModel()
        var resultStyle: LearningStyle? = null

        // Pick READ_WRITE for the first two questions
        for (i in 0..1) {
            val idx = viewModel.questions[i].options.indexOfFirst { it.style == LearningStyle.READ_WRITE }
            viewModel.selectOption(idx)
            viewModel.moveToNext { }
        }
        // Pick KINESTHETIC for the last two questions
        for (i in 2..3) {
            val idx = viewModel.questions[i].options.indexOfFirst { it.style == LearningStyle.KINESTHETIC }
            viewModel.selectOption(idx)
            viewModel.moveToNext { result -> resultStyle = result }
        }

        // 2 vs 2 tie should be READ_WRITE per logic: (readWriteCount >= kinestheticCount)
        assertEquals(LearningStyle.READ_WRITE, resultStyle)
    }
}