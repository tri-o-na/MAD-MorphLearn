package mad.team9.morphlearn.loginregister

import mad.team9.morphlearn.login.LoginValidator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginValidatorTest {

    @Test
    fun `empty email returns false`() {
        assertFalse(LoginValidator.isValidEmail(""))
    }

    @Test
    fun `email without at symbol returns false`() {
        assertFalse(LoginValidator.isValidEmail("teststudent.com"))
    }

    @Test
    fun `valid email returns true`() {
        assertTrue(LoginValidator.isValidEmail("user@gmail.com"))
    }

    @Test
    fun `email with trailing space returns true because of trim`() {
        assertTrue(LoginValidator.isValidEmail("user@gmail.com "))
    }

    @Test
    fun `short password returns false`() {
        assertFalse(LoginValidator.isValidPassword("1234567"))
    }

    @Test
    fun `password with exactly 8 characters returns true`() {
        assertTrue(LoginValidator.isValidPassword("12345678"))
    }

    @Test
    fun `blank password returns false`() {
        assertFalse(LoginValidator.isValidPassword("        "))
    }
}