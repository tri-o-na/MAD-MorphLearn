package mad.team9.morphlearn.onboardingQuiz.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mad.team9.morphlearn.onboardingQuiz.Option

@Composable
fun AnswerOption(
    option: Option,
    index: Int,
    isSelected: Boolean,
    onOptionSelected: () -> Unit
) {
    val letter = when(index) {
        0 -> "A"
        1 -> "B"
        2 -> "C"
        else -> "D"
    }

    OutlinedButton(
        onClick = onOptionSelected,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        // Visual feedback for selection (S1-T3)
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Color(0xFF006064) else Color(0xFFE0E0E0)
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) Color(0xFFE0F2F1) else Color.Transparent,
            contentColor = Color(0xFF2D3436)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // The Letter Circle (Matching your React UI)
            Surface(
                color = Color(0xFF006064),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = letter,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = option.text,
                fontSize = 15.sp,
                lineHeight = 20.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}