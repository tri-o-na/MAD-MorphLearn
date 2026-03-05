package mad.team9.morphlearn.ai

import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Test

class AINotesRepositoryTest {
    @get:org.junit.Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val repository = AINotesRepository(mockk(relaxed = true))

    @Test
    fun `parseJson extracts correct values`(){
        val json = """
            {
                "title": "Test Title",
                "generatedNotes": "Test Generated Notes",
                "questions": [
                    {
                        "question": "Test Question 1",
                        "options": ["Q1-1","Q1-2","Q1-3","Q1-4"],
                        "correctIndex": 1
                    },
                    {
                        "question": "Test Question 2",
                        "options": ["Q2-1","Q2-2","Q2-3","Q2-4"],
                        "correctIndex": 0
                    }
                ]
            }
        """.trimIndent()

        val note = repository.parseJson(json)

        assertEquals("Test Title", note.title)
        assertEquals("Test Generated Notes", note.generatedNotes)

        val questions: List<AIQuizQuestion> = note.questions

        assertEquals(2,questions.size)
        assertEquals(questions[0].question,"Test Question 1")

        assertEquals(4,questions[0].options.size)
        assertEquals("Q1-1",questions[0].options[0])
        assertEquals("Q1-2",questions[0].options[1])
        assertEquals("Q1-3",questions[0].options[2])
        assertEquals("Q1-4",questions[0].options[3])

        assertEquals(1,questions[0].correctIndex)

        assertEquals(questions[1].question,"Test Question 2")

        assertEquals(4,questions[1].options.size)
        assertEquals("Q2-1",questions[1].options[0])
        assertEquals("Q2-2",questions[1].options[1])
        assertEquals("Q2-3",questions[1].options[2])
        assertEquals("Q2-4",questions[1].options[3])

        assertEquals(0,questions[1].correctIndex)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `valid JSON set success state`() = runTest {
        val fakeRepository = mockk<AINotesRepository>()

        val validJson = AINote(
            title = "Valid Title",
            generatedNotes = "Valid Notes",
            questions = listOf(
                AIQuizQuestion(
                    question = "Valid Question",
                    options = listOf("A","B","C","D"),
                    correctIndex = 0
                )
            )
        )

        // Hijack parseJson and saveNoteAndQuiz function to return our fake data
        io.mockk.every { fakeRepository.parseJson(any()) } returns validJson
        io.mockk.coEvery { fakeRepository.saveNoteAndQuiz(any(),any()) } returns Unit

        val viewModel = AINotesViewModel(fakeRepository)

        viewModel.processAndSave("valid json","fakeUserId")

        advanceUntilIdle()
        assertTrue(viewModel.state.value is AINoteState.Success)
        io.mockk.coVerify { fakeRepository.saveNoteAndQuiz("fakeUserId", validJson) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `invalid JSON sets error state`() = runTest{
        val fakeRepository = mockk<AINotesRepository>()

        // Hijack parseJson function to return our fake data
        io.mockk.every { fakeRepository.parseJson(any()) } throws Exception("Invalid JSON")
        val viewModel = AINotesViewModel(fakeRepository)

        viewModel.processAndSave("{ invalid json }","fakeUserId")

        advanceUntilIdle()
        assertTrue(viewModel.state.value is AINoteState.Error)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : org.junit.rules.TestWatcher() {
    override fun starting(description: org.junit.runner.Description) {
        kotlinx.coroutines.Dispatchers.setMain(testDispatcher)
    }
    override fun finished(description: org.junit.runner.Description) {
        kotlinx.coroutines.Dispatchers.resetMain()
    }
}