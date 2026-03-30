package mad.team9.morphlearn.onboardingQuiz

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import mad.team9.morphlearn.home.HomeScreen
import mad.team9.morphlearn.login.FirebaseAuthManager
import mad.team9.morphlearn.onboardingQuiz.components.AnswerOption
import mad.team9.morphlearn.onboardingQuiz.components.QuestionCard


@Composable
fun OnboardingQuizScreen(viewModel: QuizViewModel = viewModel(), onQuizComplete: (String) -> Unit) {
    val currentQuestion = viewModel.questions[viewModel.currentQuestionIndex]
    val progress = (viewModel.currentQuestionIndex + 1).toFloat() / viewModel.questions.size
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Use the QuestionCard component
        QuestionCard(
            question = currentQuestion,
            currentIndex = viewModel.currentQuestionIndex,
            totalQuestions = viewModel.questions.size
        )

        Spacer(modifier = Modifier.height(24.dp))

        // List of AnswerOption components
        Column(modifier = Modifier.weight(1f)) {
            currentQuestion.options.forEachIndexed { index, option ->
                AnswerOption(
                    option = option,
                    index = index,
                    isSelected = viewModel.selectedOptionIndex == index,
                    onOptionSelected = { viewModel.selectOption(index) }
                )
            }
        }

        // THE NEXT BUTTON
        Button(
            onClick = {
                viewModel.moveToNext { result ->
                    coroutineScope.launch {
                        // 1. Save style to Firebase
                        val saveResult = FirebaseAuthManager.saveLearningStyle(result.toString())

                        // 2. Navigate regardless of success (or handle error with a Toast)
                        if (saveResult.isFailure) {
                            println("DB Error: ${saveResult.exceptionOrNull()?.message}")
                        }
                        onQuizComplete(result.toString())
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = viewModel.selectedOptionIndex != null,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(if (viewModel.currentQuestionIndex == viewModel.questions.size - 1) "Finish" else "Next")
        }
    }
}
