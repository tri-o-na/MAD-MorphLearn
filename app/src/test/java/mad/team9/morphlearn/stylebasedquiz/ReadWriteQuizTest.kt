package mad.team9.morphlearn.stylebasedquiz

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.*
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import mad.team9.morphlearn.stylebasedquiz.readwrite.FillBlank
import mad.team9.morphlearn.stylebasedquiz.readwrite.FillBlankViewModel
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReadWriteQuizTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        mockkStatic(FirebaseAuth::class)
        mockkStatic(FirebaseFirestore::class)

        val mockAuth = mockk<FirebaseAuth>(relaxed = true)
        val mockFirestore = mockk<FirebaseFirestore>(relaxed = true)

        every { FirebaseAuth.getInstance() } returns mockAuth
        every { FirebaseFirestore.getInstance() } returns mockFirestore
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun testInitialState() = runTest {
        val viewModel = FillBlankViewModel()
        assertEquals(0, viewModel.currentQuestionIndex)
        assertEquals("", viewModel.userAnswer)
        assertFalse(viewModel.isAnswered)
        assertEquals(0, viewModel.correctCount)
        assertFalse(viewModel.isFinished)
    }

    @Test
    fun testSubmitCorrectAnswer() = runTest {
        val viewModel = FillBlankViewModel()
        
        // Manually inject a question for testing
        val questionsField = viewModel.javaClass.getDeclaredField("_questions")
        questionsField.isAccessible = true
        val questions = questionsField.get(viewModel) as MutableList<FillBlank>
        questions.add(FillBlank("The sky is _.", "Blue"))
        
        viewModel.userAnswer = "Blue"
        viewModel.submitAnswer()
        
        assertTrue(viewModel.isAnswered)
        assertTrue(viewModel.isCorrect)
        assertEquals(1, viewModel.correctCount)
    }

    @Test
    fun testSubmitIncorrectAnswer() = runTest {
        val viewModel = FillBlankViewModel()
        
        val questionsField = viewModel.javaClass.getDeclaredField("_questions")
        questionsField.isAccessible = true
        val questions = questionsField.get(viewModel) as MutableList<FillBlank>
        questions.add(FillBlank("The sky is _.", "Blue"))
        
        viewModel.userAnswer = "Red"
        viewModel.submitAnswer()
        
        assertTrue(viewModel.isAnswered)
        assertFalse(viewModel.isCorrect)
        assertEquals(0, viewModel.correctCount)
    }

    @Test
    fun testSubmitAnswerCaseInsensitive() = runTest {
        val viewModel = FillBlankViewModel()
        
        val questionsField = viewModel.javaClass.getDeclaredField("_questions")
        questionsField.isAccessible = true
        val questions = questionsField.get(viewModel) as MutableList<FillBlank>
        questions.add(FillBlank("The sky is _.", "Blue"))
        
        viewModel.userAnswer = "blue" // Lowercase
        viewModel.submitAnswer()
        
        assertTrue(viewModel.isCorrect)
    }

    @Test
    fun testSubmitAnswerWithSpaces() = runTest {
        val viewModel = FillBlankViewModel()
        
        val questionsField = viewModel.javaClass.getDeclaredField("_questions")
        questionsField.isAccessible = true
        val questions = questionsField.get(viewModel) as MutableList<FillBlank>
        questions.add(FillBlank("The sky is _.", "Blue"))
        
        viewModel.userAnswer = "  Blue  " // With spaces
        viewModel.submitAnswer()
        
        assertTrue(viewModel.isCorrect)
    }

    @Test
    fun testNextQuestionIncrementsIndex() = runTest {
        val viewModel = FillBlankViewModel()
        
        val questionsField = viewModel.javaClass.getDeclaredField("_questions")
        questionsField.isAccessible = true
        val questions = questionsField.get(viewModel) as MutableList<FillBlank>
        questions.add(FillBlank("Q1", "A1"))
        questions.add(FillBlank("Q2", "A2"))
        
        viewModel.userAnswer = "A1"
        viewModel.submitAnswer()
        viewModel.nextQuestion()
        
        assertEquals(1, viewModel.currentQuestionIndex)
        assertEquals("", viewModel.userAnswer)
        assertFalse(viewModel.isAnswered)
    }

    @Test
    fun testQuizFinishedAfterLastQuestion() = runTest {
        val viewModel = FillBlankViewModel()
        
        val questionsField = viewModel.javaClass.getDeclaredField("_questions")
        questionsField.isAccessible = true
        val questions = questionsField.get(viewModel) as MutableList<FillBlank>
        questions.add(FillBlank("Q1", "A1"))
        
        viewModel.userAnswer = "A1"
        viewModel.submitAnswer()
        viewModel.nextQuestion()
        
        assertTrue(viewModel.isFinished)
    }

    @Test
    fun testRestartResetsState() = runTest {
        val viewModel = FillBlankViewModel()
        
        val questionsField = viewModel.javaClass.getDeclaredField("_questions")
        questionsField.isAccessible = true
        val questions = questionsField.get(viewModel) as MutableList<FillBlank>
        questions.add(FillBlank("Q1", "A1"))
        
        viewModel.userAnswer = "A1"
        viewModel.submitAnswer()
        viewModel.nextQuestion()
        
        viewModel.restart()
        
        assertEquals(0, viewModel.currentQuestionIndex)
        assertEquals("", viewModel.userAnswer)
        assertFalse(viewModel.isAnswered)
        assertEquals(0, viewModel.correctCount)
        assertFalse(viewModel.isFinished)
    }
}
