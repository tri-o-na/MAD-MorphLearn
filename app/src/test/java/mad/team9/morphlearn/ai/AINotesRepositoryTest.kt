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
                "generatedNotes": "Test Generated Notes"
            }
        """.trimIndent()

        val note = repository.parseJson(json)

        assertEquals("Test Title", note.title)
        assertEquals("Test Generated Notes", note.generatedNotes)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `invalid JSON sets error state`() = runTest{
        val fakeRepository = mockk<AINotesRepository>()
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