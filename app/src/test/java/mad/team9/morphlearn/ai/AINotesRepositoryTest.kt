package mad.team9.morphlearn.ai

import com.google.firebase.auth.FirebaseAuth
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import mad.team9.morphlearn.MainDispatcherRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AINotesRepositoryTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: AINotesRepository
    private lateinit var fakeService: AIQuizService
    private lateinit var mockAuth: FirebaseAuth

    @Before
    fun setup() {
        // Create mock instances
        mockkStatic(FirebaseAuth::class)
        mockAuth = mockk(relaxed = true)

        // Inject so that any get instances returns mockAuth instead
        every { FirebaseAuth.getInstance() } returns mockAuth

        fakeRepository = AINotesRepository(mockk(relaxed = true))
        fakeService = mockk<AIQuizService>(relaxed = true)
    }

    @After
    fun tearDown() {
        // Clean up static mocks to avoid leaking into other tests
        unmockkStatic(FirebaseAuth::class)
    }

    @Test
    fun `parseJson extracts correct values`() {
        val json = """
            {
                "title": "Test Title",
                "generatedNotes": "Test Generated Notes",
                "subjectId": "sGn2NITo0swbuyZw0DFP",
                "questions": [
                    {
                        "question": "Test Question 1",
                        "options": ["Q1-1","Q1-2","Q1-3","Q1-4"],
                        "correctIndex": 1
                    }
                ]
            }
        """.trimIndent()

        val note = fakeRepository.parseJson(json)
        assertEquals("Test Title", note.title)
        assertEquals("sGn2NITo0swbuyZw0DFP", note.subjectId)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `valid generation sets success state`() = runTest {
        // Mock service to throw success
        coEvery {
            fakeService.getGenerateNotesAndQuiz(any(), any(), any(), any())
        } returns Unit

        val viewModel = AINotesViewModel(fakeService)

        viewModel.generateNotesAndQuiz(
            subject = "Subject 1",
            topic = "Topic 1",
            context = mockk(),
            uri = mockk()
        )

        advanceUntilIdle()
        assertTrue(viewModel.state.value is AINoteState.Success)

        coVerify(exactly = 1) {
            fakeService.getGenerateNotesAndQuiz("Subject 1", "Topic 1", any(), any())
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `failed generation sets error state`() = runTest {
        // Mock the service to throw exception
        coEvery {
            fakeService.getGenerateNotesAndQuiz(any(), any(), any(), any())
        } throws Exception("AI Generation Failed")

        val viewModel = AINotesViewModel(fakeService)

        viewModel.generateNotesAndQuiz("Sub 1", "Topic 1", mockk(), mockk())

        advanceUntilIdle()
        assertTrue(viewModel.state.value is AINoteState.Error)
    }
}