package mad.team9.morphlearn.notes

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue


class NotesTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `loadMaterials success updates materials and keeps error null`() = runTest {
        val fakeRepo = FakeMaterialsDataSource(
            materials = listOf(
                Material(id = "1", title = "Math", generatedNotes = "Algebra notes"),
                Material(id = "2", title = "Science", generatedNotes = "Physics notes")
            )
        )

        val viewModel = NotesViewModel(fakeRepo)

        viewModel.loadMaterials()
        advanceUntilIdle()

        assertEquals(2, viewModel.materials.value.size)
        assertEquals("Math", viewModel.materials.value[0].title)
        assertEquals("Algebra notes", viewModel.materials.value[0].generatedNotes)
        assertEquals("Science", viewModel.materials.value[1].title)
        assertEquals("Physics notes", viewModel.materials.value[1].generatedNotes)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `loadMaterials empty list updates materials with empty list`() = runTest {
        val fakeRepo = FakeMaterialsDataSource(materials = emptyList())
        val viewModel = NotesViewModel(fakeRepo)

        viewModel.loadMaterials()
        advanceUntilIdle()

        assertEquals(emptyList<Material>(), viewModel.materials.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `loadMaterials failure updates error message`() = runTest {
        val fakeRepo = FakeMaterialsDataSource(
            shouldThrow = true,
            exceptionMessage = "Failed to load materials"
        )
        val viewModel = NotesViewModel(fakeRepo)

        viewModel.loadMaterials()
        advanceUntilIdle()

        assertEquals(emptyList<Material>(), viewModel.materials.value)
        assertEquals("Failed to load materials", viewModel.error.value)
    }

    @Test
    fun `loadMaterials material with blank fields is returned correctly`() = runTest {
        val fakeRepo = FakeMaterialsDataSource(
            materials = listOf(
                Material(id = "3", title = "", generatedNotes = "")
            )
        )
        val viewModel = NotesViewModel(fakeRepo)

        viewModel.loadMaterials()
        advanceUntilIdle()

        assertEquals(1, viewModel.materials.value.size)
        assertEquals("", viewModel.materials.value[0].title)
        assertEquals("", viewModel.materials.value[0].generatedNotes)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `loadLearningStyle success updates learning style`() = runTest {
        val fakeRepo = FakeMaterialsDataSource(
            learningStyle = "AUDITORY"
        )
        val viewModel = NotesViewModel(fakeRepo)

        viewModel.loadLearningStyle()
        advanceUntilIdle()

        assertEquals("AUDITORY", viewModel.learningStyle.value)
    }

    @Test
    fun `loadLearningStyle failure keeps learning style null`() = runTest {
        val fakeRepo = FakeMaterialsDataSource(
            shouldThrow = true,
            exceptionMessage = "Failed to load learning style"
        )
        val viewModel = NotesViewModel(fakeRepo)

        viewModel.loadLearningStyle()
        advanceUntilIdle()

        assertNull(viewModel.learningStyle.value)
    }

    @Test
    fun `loadMaterials groups materials by subject and puts unmatched under others`() = runTest {
        val fakeRepo = FakeMaterialsDataSource(
            materials = listOf(
                Material(
                    id = "1",
                    title = "Arrays",
                    generatedNotes = "Array notes",
                    subjectName = "Data Structures"
                ),
                Material(
                    id = "2",
                    title = "Linked Lists",
                    generatedNotes = "Linked list notes",
                    subjectName = "Data Structures"
                ),
                Material(
                    id = "3",
                    title = "Misc Topic",
                    generatedNotes = "Misc notes",
                    subjectName = "Others"
                )
            )
        )
        val viewModel = NotesViewModel(fakeRepo)

        viewModel.loadMaterials()
        advanceUntilIdle()

        val grouped = viewModel.groupedMaterials.value

        assertEquals(2, grouped.size)

        val dataStructuresGroup = grouped.firstOrNull { it.subjectName == "Data Structures" }
        val othersGroup = grouped.firstOrNull { it.subjectName == "Others" }

        assertNotNull(dataStructuresGroup)
        assertNotNull(othersGroup)

        assertEquals(2, dataStructuresGroup!!.materials.size)
        assertEquals(1, othersGroup!!.materials.size)

        assertTrue(dataStructuresGroup.materials.any { it.title == "Arrays" })
        assertTrue(dataStructuresGroup.materials.any { it.title == "Linked Lists" })
        assertEquals("Misc Topic", othersGroup.materials.first().title)
    }

    private class FakeMaterialsDataSource(
        private val materials: List<Material> = emptyList(),
        private val shouldThrow: Boolean = false,
        private val exceptionMessage: String = "Unknown error",

        private var hasAttempted: Boolean = false,
        private var quizId: String? = null,
        private var learningStyle: String? = null

    ) : MaterialsDataSource {

        override suspend fun getAllMaterials(): List<Material> {
            if (shouldThrow) throw Exception(exceptionMessage)
            return materials
        }

        override suspend fun checkQuizAttempt(quizId: String?): Boolean {
            if (shouldThrow) throw Exception(exceptionMessage)
            return hasAttempted
        }

        override suspend fun getLatestQuiz(materialId: String): String? {
            if (shouldThrow) throw Exception(exceptionMessage)
            return quizId
        }
        override suspend fun getLearningStyle(): String? {
            if (shouldThrow) throw Exception(exceptionMessage)
            return learningStyle
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}