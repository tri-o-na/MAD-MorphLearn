package mad.team9.morphlearn.stylebasedquiz

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import mad.team9.morphlearn.ai.AIQuizQuestion
import org.junit.Test
import kotlinx.coroutines.test.runTest

class QuizTest {

    @Test
    fun `correct answer returns true`() {
        val repository = QuizResultRepository(mockk(relaxed = true))
        val question = AIQuizQuestion(
            question = "What is 2 + 2?",
            options = listOf("1", "2", "3", "4"),
            correctIndex = 3
        )

        val result = repository.isAnswerCorrect(3, question)

        assertTrue(result)
    }

    @Test
    fun `wrong answer returns false`() {
        val repository = QuizResultRepository(mockk(relaxed = true))
        val question = AIQuizQuestion(
            question = "What is 2 + 2?",
            options = listOf("1", "2", "3", "4"),
            correctIndex = 3
        )

        val result = repository.isAnswerCorrect(1, question)

        assertFalse(result)
    }

    @Test
    fun `calculatePercentage returns correct percentage`() {
        val repository = QuizResultRepository(mockk(relaxed = true))

        val result = repository.calculatePercentage(3, 4)

        assertEquals(75, result)
    }

    @Test
    fun `calculatePercentage returns zero when total is zero`() {
        val repository = QuizResultRepository(mockk(relaxed = true))

        val result = repository.calculatePercentage(3, 0)

        assertEquals(0, result)
    }

    @Test
    fun `submitAnswer returns true for correct answer`() {
        val fakeRepository = mockk<QuizResultRepository>()
        val question = AIQuizQuestion(
            question = "Capital of France?",
            options = listOf("Berlin", "Madrid", "Paris", "Rome"),
            correctIndex = 2
        )

        every { fakeRepository.isAnswerCorrect(2, question) } returns true
        every { fakeRepository.saveQuizAttempt(any(), any()) } just Runs

        val viewModel = QuizViewModel(fakeRepository)

        val result = viewModel.submitAnswer(2, question)

        assertTrue(result)
    }

    @Test
    fun `submitAnswer returns false for wrong answer`() {
        val fakeRepository = mockk<QuizResultRepository>()
        val question = AIQuizQuestion(
            question = "Capital of France?",
            options = listOf("Berlin", "Madrid", "Paris", "Rome"),
            correctIndex = 2
        )

        every { fakeRepository.isAnswerCorrect(1, question) } returns false
        every { fakeRepository.saveQuizAttempt(any(), any()) } just Runs

        val viewModel = QuizViewModel(fakeRepository)

        val result = viewModel.submitAnswer(1, question)

        assertFalse(result)
    }

    @Test
    fun `finishQuiz saves correct quiz result`() = runTest {
        val fakeRepository = mockk<QuizResultRepository>()
        val q1 = AIQuizQuestion(
            question = "Capital of France?",
            options = listOf("Berlin", "Madrid", "Paris", "Rome"),
            correctIndex = 2
        )
        val q2 = AIQuizQuestion(
            question = "5 plus 5?",
            options = listOf("8", "9", "10", "11"),
            correctIndex = 2
        )

        every { fakeRepository.isAnswerCorrect(2, q1) } returns true
        every { fakeRepository.isAnswerCorrect(1, q2) } returns false
        coEvery { fakeRepository.getNextAttemptNumber("user123", "material123") } returns 1
        every { fakeRepository.saveQuizAttempt(any(), any()) } just Runs

        val viewModel = QuizViewModel(fakeRepository)

        viewModel.submitAnswer(2, q1)
        viewModel.submitAnswer(1, q2)
        
        // Removed attemptNumber parameter as it is now handled internally by ViewModel
        viewModel.finishQuiz(
            userId = "user123",
            quizId = "quiz123",
            materialId = "material123",
            totalQuestions = 2,
            topic = "Geography"
        )

        coVerify {
            fakeRepository.saveQuizAttempt(
                match {
                    it.userId == "user123" &&
                            it.quizId == "quiz123" &&
                            it.materialId == "material123" &&
                            it.score == 1 &&
                            it.totalQuestions == 2 &&
                            it.userAnswers == listOf(2, 1) &&
                            it.attemptNumber == 1
                },
                "Geography"
            )
        }
    }
}