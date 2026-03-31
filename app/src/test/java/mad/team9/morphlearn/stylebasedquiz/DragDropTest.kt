package mad.team9.morphlearn.stylebasedquiz

import androidx.compose.ui.graphics.Color
import mad.team9.morphlearn.stylebasedquiz.common.QuizAnswerFeedbackControl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DragDropTest {

    private val feedbackControl = QuizAnswerFeedbackControl()

    @Test
    fun `isDragDropAnswerCorrect returns true for identical strings`() {
        val result = feedbackControl.isDragDropAnswerCorrect("Kotlin", "Kotlin")
        assertTrue("Expected identical strings to be correct", result)
    }

    @Test
    fun `isDragDropAnswerCorrect returns true for strings with whitespace`() {
        val result = feedbackControl.isDragDropAnswerCorrect("  Kotlin  ", "Kotlin")
        assertTrue("Expected strings with whitespace to be trimmed and correct", result)
    }

    @Test
    fun `isDragDropAnswerCorrect returns false for different strings`() {
        val result = feedbackControl.isDragDropAnswerCorrect("Java", "Kotlin")
        assertFalse("Expected different strings to be incorrect", result)
    }

    @Test
    fun `isDragDropAnswerCorrect returns false for null currentAnswer`() {
        val result = feedbackControl.isDragDropAnswerCorrect(null, "Kotlin")
        assertFalse("Expected null current answer to be incorrect", result)
    }

    @Test
    fun `getFeedbackColor returns Transparent when not confirmed`() {
        val result = feedbackControl.getFeedbackColor(isConfirmed = false, isCorrect = true)
        assertEquals("Expected Transparent when not confirmed", Color.Transparent, result)
    }

    @Test
    fun `getFeedbackColor returns Green when confirmed and correct`() {
        val result = feedbackControl.getFeedbackColor(isConfirmed = true, isCorrect = true)
        assertEquals("Expected light green background for correct answer", Color(0xFFDFF5E1), result)
    }

    @Test
    fun `getFeedbackColor returns Red when confirmed and incorrect`() {
        val result = feedbackControl.getFeedbackColor(isConfirmed = true, isCorrect = false)
        assertEquals("Expected light red background for incorrect answer", Color(0xFFFDE2E1), result)
    }

    @Test
    fun `getBorderColor returns LightGray when not confirmed`() {
        val result = feedbackControl.getBorderColor(isConfirmed = false, isCorrect = true)
        assertEquals("Expected LightGray border when not confirmed", Color.LightGray, result)
    }

    @Test
    fun `getBorderColor returns DarkGreen when confirmed and correct`() {
        val result = feedbackControl.getBorderColor(isConfirmed = true, isCorrect = true)
        assertEquals("Expected dark green border for correct answer", Color(0xFF2E7D32), result)
    }

    @Test
    fun `getBorderColor returns DarkRed when confirmed and incorrect`() {
        val result = feedbackControl.getBorderColor(isConfirmed = true, isCorrect = false)
        assertEquals("Expected dark red border for incorrect answer", Color(0xFFC62828), result)
    }
}
