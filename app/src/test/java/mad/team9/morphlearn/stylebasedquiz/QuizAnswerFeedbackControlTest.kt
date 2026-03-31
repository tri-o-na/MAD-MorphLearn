package mad.team9.morphlearn.stylebasedquiz

import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import mad.team9.morphlearn.stylebasedquiz.common.QuizAnswerFeedbackControl
import org.junit.Test

class QuizAnswerFeedbackControlTest {

    @Test
    fun `mcq answer returns true when selected index matches correct index`() {
        val control = QuizAnswerFeedbackControl()

        val result = control.isMcqAnswerCorrect(2, 2)

        assertTrue(result)
    }

    @Test
    fun `mcq answer returns false when selected index does not match correct index`() {
        val control = QuizAnswerFeedbackControl()

        val result = control.isMcqAnswerCorrect(1, 2)

        assertFalse(result)
    }

    @Test
    fun `drag drop answer returns true for exact match`() {
        val control = QuizAnswerFeedbackControl()

        val result = control.isDragDropAnswerCorrect("Paris", "Paris")

        assertTrue(result)
    }

    @Test
    fun `drag drop answer returns true for trimmed match`() {
        val control = QuizAnswerFeedbackControl()

        val result = control.isDragDropAnswerCorrect(" Paris ", "Paris")

        assertTrue(result)
    }

    @Test
    fun `drag drop answer returns false for wrong answer`() {
        val control = QuizAnswerFeedbackControl()

        val result = control.isDragDropAnswerCorrect("London", "Paris")

        assertFalse(result)
    }

    @Test
    fun `drag drop answer returns false when current answer is null`() {
        val control = QuizAnswerFeedbackControl()

        val result = control.isDragDropAnswerCorrect(null, "Paris")

        assertFalse(result)
    }
}