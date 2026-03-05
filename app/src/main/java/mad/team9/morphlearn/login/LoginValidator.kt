package mad.team9.morphlearn.login

object LoginValidator {

    /**
     * Matches the logic in LoginScreen: trims and checks for basic email structure.
     */
    fun isValidEmail(email: String): Boolean {
        val trimmed = email.trim()
        return trimmed.isNotEmpty() &&
                trimmed.contains("@") &&
                trimmed.endsWith(".com")
    }

    /**
     * Ensures password is not blank and meets length requirements.
     */
    fun isValidPassword(password: String): Boolean {
        return password.isNotBlank() && password.length >= 8
    }
}