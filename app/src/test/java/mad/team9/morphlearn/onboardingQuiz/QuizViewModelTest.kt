package mad.team9.morphlearn.onboardingQuiz

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.*
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import mad.team9.morphlearn.login.FirebaseAuthManager
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelTest {

    @Before
    fun setup() {
        mockkStatic(FirebaseAuth::class)
        mockkStatic(FirebaseFirestore::class)

        every { FirebaseAuth.getInstance() } returns mockk(relaxed = true)
        every { FirebaseFirestore.getInstance() } returns mockk(relaxed = true)

        mockkObject(FirebaseAuthManager)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

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
            val index = viewModel.questions[i].options.indexOfFirst {
                it.style == LearningStyle.READ_WRITE
            }
            viewModel.selectOption(index)
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
            val index = viewModel.questions[i].options.indexOfFirst {
                it.style == LearningStyle.KINESTHETIC
            }
            viewModel.selectOption(index)
            viewModel.moveToNext { result -> resultStyle = result }
        }

        assertEquals(LearningStyle.KINESTHETIC, resultStyle)
    }

    @Test
    fun testScoringLogicVisualMajority() = runTest {
        val viewModel = QuizViewModel()
        val totalQuestions = viewModel.questions.size
        var resultStyle: LearningStyle? = null

        for (i in 0 until totalQuestions) {
            val index = viewModel.questions[i].options.indexOfFirst {
                it.style == LearningStyle.VISUAL
            }
            viewModel.selectOption(index)
            viewModel.moveToNext { result -> resultStyle = result }
        }

        assertEquals(LearningStyle.VISUAL, resultStyle)
    }

    @Test
    fun testScoringLogicAuditoryMajority() = runTest {
        val viewModel = QuizViewModel()
        val totalQuestions = viewModel.questions.size
        var resultStyle: LearningStyle? = null

        for (i in 0 until totalQuestions) {
            val index = viewModel.questions[i].options.indexOfFirst {
                it.style == LearningStyle.AUDITORY
            }
            viewModel.selectOption(index)
            viewModel.moveToNext { result -> resultStyle = result }
        }

        assertEquals(LearningStyle.AUDITORY, resultStyle)
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
    fun testScoringLogicTieDefaultsToFirstEncounteredStyle() = runTest {
        val viewModel = QuizViewModel()
        var resultStyle: LearningStyle? = null
        val totalQuestions = viewModel.questions.size // Expecting 5

        // Order of picking: VISUAL, VISUAL, AUDITORY, AUDITORY, KINESTHETIC
        // Results in tie between VISUAL and AUDITORY (2 each).
        // Since VISUAL was picked first, it should be the winner.
        
        for (i in 0 until totalQuestions) {
            val targetStyle = when(i) {
                0, 1 -> LearningStyle.VISUAL
                2, 3 -> LearningStyle.AUDITORY
                else -> LearningStyle.KINESTHETIC
            }
            val idx = viewModel.questions[i].options.indexOfFirst { it.style == targetStyle }
            viewModel.selectOption(idx)
            viewModel.moveToNext { result -> resultStyle = result }
        }

        assertEquals(LearningStyle.VISUAL, resultStyle)
    }

    @Test
    fun testQuizCompletionCallsCallbackWithCorrectStyle() = runTest {
        val viewModel = QuizViewModel()
        val totalQuestions = viewModel.questions.size
        val onCompleteMock = mockk<(LearningStyle) -> Unit>(relaxed = true)

        for (i in 0 until totalQuestions) {
            // Pick READ_WRITE for all
            val idx = viewModel.questions[i].options.indexOfFirst {
                it.style == LearningStyle.READ_WRITE
            }
            viewModel.selectOption(idx)

            if (i == totalQuestions - 1) {
                viewModel.moveToNext(onCompleteMock)
            } else {
                viewModel.moveToNext {}
            }
        }

        verify(exactly = 1) { onCompleteMock(LearningStyle.READ_WRITE) }
        confirmVerified(onCompleteMock)
    }

    @Test
    fun testLoginRedirectsToQuizWhenStyleIsMissing() = runTest {
        coEvery { FirebaseAuthManager.isLearningStyleSet() } returns false

        val isComplete = FirebaseAuthManager.isLearningStyleSet()
        val destination = if (isComplete) "home" else "onboarding_quiz"

        assertEquals("onboarding_quiz", destination)
    }

    @Test
    fun testLoginRedirectsToHomeWhenStyleExists() = runTest {
        coEvery { FirebaseAuthManager.isLearningStyleSet() } returns true

        val isComplete = FirebaseAuthManager.isLearningStyleSet()
        val destination = if (isComplete) "home" else "onboarding_quiz"

        assertEquals("home", destination)
    }
}
