package mad.team9.morphlearn.UserProfile

import mad.team9.morphlearn.profile.ProfileViewModel
import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileTest {

    @Test
    fun `test email parsing logic for display name`() {
        val viewModel = ProfileViewModel()
        val testEmail = "abctest@example.com"

        // Tests the internal logic without needing Android/Firebase environment
        val result = viewModel.parseDisplayName(testEmail)

        assertEquals("abctest", result)
    }

    @Test
    fun `test state updates correctly`() {
        val viewModel = ProfileViewModel()
        viewModel.updateState("abctest", "abctest@mail.com", "Visual")

        assertEquals("abctest", viewModel.name)
        assertEquals("Visual", viewModel.learnerType)
    }
}