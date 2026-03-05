package mad.team9.morphlearn.notes

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeMaterialsRepository(
    private val materials: List<Material> = emptyList(),
    private val shouldThrow: Boolean = false
) : MaterialsDataSource {

    override suspend fun getAllMaterials(): List<Material> {
        if (shouldThrow) throw IllegalStateException("Test error")
        return materials
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class NotesUnitTest {

    @Test
    fun `loadMaterials should store materials when repository succeeds`() = runTest {
        val repo = FakeMaterialsRepository(
            materials = listOf(
                Material(
                    id = "1",
                    title = "OS Notes",
                    generatedNotes = "Processes and threads"
                ),
                Material(
                    id = "2",
                    title = "Networking Notes",
                    generatedNotes = "TCP vs UDP"
                )
            )
        )

        val viewModel = NotesViewModel(repo)

        viewModel.loadMaterials()

        assertEquals(2, viewModel.materials.value.size)
        assertEquals("OS Notes", viewModel.materials.value[0].title)
        assertEquals("Processes and threads", viewModel.materials.value[0].generatedNotes)
        assertEquals(null, viewModel.error.value)
    }

    @Test
    fun `loadMaterials should keep list empty when repository returns nothing`() = runTest {
        val repo = FakeMaterialsRepository(emptyList())
        val viewModel = NotesViewModel(repo)

        viewModel.loadMaterials()

        assertTrue(viewModel.materials.value.isEmpty())
        assertEquals(null, viewModel.error.value)
    }

    @Test
    fun `loadMaterials should store error when repository fails`() = runTest {
        val repo = FakeMaterialsRepository(shouldThrow = true)
        val viewModel = NotesViewModel(repo)

        viewModel.loadMaterials()

        assertTrue(viewModel.materials.value.isEmpty())
        assertEquals("Test error", viewModel.error.value)
    }
}