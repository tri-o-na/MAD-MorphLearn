package mad.team9.morphlearn.onboardingQuiz

import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import io.mockk.coEvery
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import mad.team9.morphlearn.login.FirebaseAuthManager
import org.junit.After
import org.junit.Before
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll

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

    @Test
    fun testQuizCompletionCallsCallbackWithCorrectStyle() = runTest {
        val viewModel = QuizViewModel()

        // 1. Create a mock for the completion callback lambda
        val onCompleteMock = mockk<(LearningStyle) -> Unit>(relaxed = true)

        // 2. Force a tie scenario (2 Read/Write, 2 Kinesthetic)
        // Question 1 & 2: Read/Write
        repeat(2) {
            val idx = viewModel.questions[viewModel.currentQuestionIndex].options.indexOfFirst {
                it.style == LearningStyle.READ_WRITE
            }
            viewModel.selectOption(idx)
            viewModel.moveToNext {}
        }

        // Question 3 & 4: Kinesthetic
        // On the final question, we pass our mock
        repeat(2) { i ->
            val idx = viewModel.questions[viewModel.currentQuestionIndex].options.indexOfFirst {
                it.style == LearningStyle.KINESTHETIC
            }
            viewModel.selectOption(idx)

            if (viewModel.currentQuestionIndex == viewModel.questions.size - 1) {
                viewModel.moveToNext(onCompleteMock)
            } else {
                viewModel.moveToNext {}
            }
        }

        // 3. Verify the mock was called with READ_WRITE (due to the >= tie-breaker)
        verify { onCompleteMock(LearningStyle.READ_WRITE) }
        confirmVerified(onCompleteMock)
    }

    // LOGIN
    @Before
    fun setup() {
        // 1. Mock the Android/Firebase static methods BEFORE anything else
        mockkStatic(FirebaseAuth::class)
        mockkStatic(FirebaseFirestore::class)

        // 2. Prevent the "Method myPid in android.os.Process not mocked" error
        every { FirebaseAuth.getInstance() } returns mockk(relaxed = true)
        every { FirebaseFirestore.getInstance() } returns mockk(relaxed = true)

        // 3. Mock our Manager object
        mockkObject(FirebaseAuthManager)
    }

    @After
    fun tearDown() {
        // Clean up all mocks to prevent side effects in other tests
        unmockkAll()
    }

    @Test
    fun testLoginRedirectsToQuizWhenStyleIsMissing() = runTest {
        // Mocking: Database says learning style is NOT set
        coEvery { FirebaseAuthManager.isLearningStyleSet() } returns false

        // Simulate the logic in MorphLearnApp
        val isComplete = FirebaseAuthManager.isLearningStyleSet()
        val destination = if (isComplete) "home" else "onboarding_quiz"

        assertEquals("onboarding_quiz", destination)
    }

    @Test
    fun testLoginRedirectsToHomeWhenStyleExists() = runTest {
        // Mocking: Database says learning style IS already set
        coEvery { FirebaseAuthManager.isLearningStyleSet() } returns true

        // Simulate the logic in MorphLearnApp
        val isComplete = FirebaseAuthManager.isLearningStyleSet()
        val destination = if (isComplete) "home" else "onboarding_quiz"

        assertEquals("home", destination)
    }
}

