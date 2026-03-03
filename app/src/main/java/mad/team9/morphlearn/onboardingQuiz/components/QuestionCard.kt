package mad.team9.morphlearn.onboardingQuiz.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mad.team9.morphlearn.onboardingQuiz.Question

@Composable
fun QuestionCard(
    question: Question,
    currentIndex: Int,
    totalQuestions: Int
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Question ${currentIndex + 1}/$totalQuestions",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = question.text,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 32.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}