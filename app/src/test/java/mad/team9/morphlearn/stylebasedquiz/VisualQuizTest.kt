package mad.team9.morphlearn.stylebasedquiz

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.*
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import mad.team9.morphlearn.ai.MainDispatcherRule
import mad.team9.morphlearn.notes.Material
import mad.team9.morphlearn.notes.MaterialsRepository
import mad.team9.morphlearn.stylebasedquiz.common.QuizFetchRepository
import mad.team9.morphlearn.stylebasedquiz.common.QuizQuestion
import mad.team9.morphlearn.stylebasedquiz.common.QuizResultRepository
import mad.team9.morphlearn.stylebasedquiz.visual.Flashcard
import mad.team9.morphlearn.stylebasedquiz.visual.FlashcardsViewModel
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VisualQuizTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: FlashcardsViewModel
    private val fetchRepo = mockk<QuizFetchRepository>()
    private val resultRepo = mockk<QuizResultRepository>(relaxed = true)
    private val materialsRepo = mockk<MaterialsRepository>()
    private val firebaseAuth = mockk<FirebaseAuth>()
    private val firebaseUser = mockk<FirebaseUser>()

    @Before
    fun setup() {
        mockkStatic(FirebaseAuth::class)
        every { FirebaseAuth.getInstance() } returns firebaseAuth
        every { firebaseAuth.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns "testUserId"

        // Mock material retrieval since it's now called in loadQuizData
        coEvery { materialsRepo.getMaterial(any()) } returns Material("testMat", "Test Topic", "Notes")

        viewModel = FlashcardsViewModel(fetchRepo, resultRepo, materialsRepo)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `loadQuizData successful loads and shuffles cards`() = runTest {
        val materialId = "testMat"
        val quizId = "quiz123"
        val questions = listOf(
            QuizQuestion("Q1", listOf("A1", "W1"), 0),
            QuizQuestion("Q2", listOf("W2", "A2"), 1)
        )

        coEvery { fetchRepo.getQuizIdByMaterialId(materialId) } returns quizId
        coEvery { fetchRepo.getQuizQuestions(quizId) } returns questions

        viewModel.loadQuizData(materialId)
        advanceUntilIdle()

        assertEquals(2, viewModel.activeCards.size)
        assertFalse(viewModel.isLoading)
        
        // Find by original index since they are shuffled
        val card1 = viewModel.activeCards.find { it.originalIndex == 0 }?.card
        assertEquals("Q1", card1?.qn)
        assertEquals("A1", card1?.ans)

        val card2 = viewModel.activeCards.find { it.originalIndex == 1 }?.card
        assertEquals("Q2", card2?.qn)
        assertEquals("A2", card2?.ans)
    }

    @Test
    fun `onAnswered correct increases score and removes card`() = runTest {
        val materialId = "testMat"
        coEvery { fetchRepo.getQuizIdByMaterialId(materialId) } returns "q"
        coEvery { fetchRepo.getQuizQuestions(any()) } returns listOf(QuizQuestion("Q1", listOf("A1"), 0))
        
        viewModel.loadQuizData(materialId)
        advanceUntilIdle()

        viewModel.onAnswered(true)

        assertEquals(1, viewModel.correctCount)
        assertEquals(0, viewModel.activeCards.size)
        assertTrue(viewModel.isFinished)
        assertEquals(1, viewModel.userAnswersMap[0]) 
    }

    @Test
    fun `onAnswered wrong does not increase score and removes card`() = runTest {
        val materialId = "testMat"
        coEvery { fetchRepo.getQuizIdByMaterialId(materialId) } returns "q"
        coEvery { fetchRepo.getQuizQuestions(any()) } returns listOf(QuizQuestion("Q1", listOf("A1"), 0))
        
        viewModel.loadQuizData(materialId)
        advanceUntilIdle()

        viewModel.onAnswered(false)

        assertEquals(0, viewModel.correctCount)
        assertEquals(0, viewModel.activeCards.size)
        assertEquals(0, viewModel.userAnswersMap[0]) 
    }

    @Test
    fun `skipCard moves current card to the end`() = runTest {
        val materialId = "testMat"
        val questions = listOf(
            QuizQuestion("Q1", listOf("A1"), 0),
            QuizQuestion("Q2", listOf("A2"), 0)
        )
        coEvery { fetchRepo.getQuizIdByMaterialId(any()) } returns "q"
        coEvery { fetchRepo.getQuizQuestions(any()) } returns questions
        
        viewModel.loadQuizData(materialId)
        advanceUntilIdle()
        
        val firstCardWrapper = viewModel.activeCards[0]
        
        viewModel.skipCard()
        
        assertEquals(2, viewModel.activeCards.size)
        assertEquals(firstCardWrapper, viewModel.activeCards.last())
    }

    @Test
    fun `toggleAnswer changes isAnswerRevealed state`() {
        assertFalse(viewModel.isAnswerRevealed)
        viewModel.toggleAnswer()
        assertTrue(viewModel.isAnswerRevealed)
        viewModel.toggleAnswer()
        assertFalse(viewModel.isAnswerRevealed)
    }
}
