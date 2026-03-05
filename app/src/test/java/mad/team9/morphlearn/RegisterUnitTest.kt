package mad.team9.morphlearn


import mad.team9.morphlearn.login.LoginValidator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RegisterValidatorTest {

    @Test
    fun `empty email should fail registration`() {
        val result = LoginValidator.isValidEmail("")
        assertFalse(result)
    }

    @Test
    fun `registration password less than 8 characters should now fail`() {
        // This previously passed when the limit was 6, now it must fail
        val result = LoginValidator.isValidPassword("123456")
        assertFalse("Registration now requires 8 characters", result)
    }

    @Test
    fun `valid email and password should pass registration`() {
        val emailResult = LoginValidator.isValidEmail("newuser@student.com")
        val passResult = LoginValidator.isValidPassword("secure123")
        assertTrue(emailResult && passResult)
    }
}