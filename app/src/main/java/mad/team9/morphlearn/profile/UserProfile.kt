package mad.team9.morphlearn.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import mad.team9.morphlearn.R
import mad.team9.morphlearn.ui.theme.*
import java.util.Locale

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = viewModel()) {
    // Fetch data from Firestore when screen loads
    LaunchedEffect(Unit) {
        viewModel.fetchUserData()
    }

    // Match based on case-insensitive string and account for different naming conventions
    val learnerStyle = viewModel.learnerType.uppercase(Locale.ROOT)
    
    val learningStyleInfo = when {
        learnerStyle.contains("READ") || learnerStyle.contains("WRITE") -> 
            "The Word Smith" to "You process the world through language. You learn best by translating concepts into your own words, whether through note-taking or reading in-depth explanations. To match your style, we prioritize comprehensive text summaries and fill-in-the-blank activities that help cement your vocabulary and logical understanding."
        
        learnerStyle.contains("KINESTHETIC") -> 
            "The Hands-On Explorer" to "For you, learning is a physical experience. You prefer \"learning by doing\" and find that you remember concepts much better when you can interact with them directly. Your path is tailored with drag-and-drop exercises and simulation-based tasks that turn abstract ideas into concrete, lived experiences."
        
        learnerStyle.contains("VISUAL") -> 
            "The Spatial Architect" to "You have a keen eye for patterns and learn best when information is presented graphically. You often remember the \"big picture\" through diagrams and spatial layouts. Your profile features rich flashcard decks and visual cues, helping you map out and visualize your progress more effectively."
        
        learnerStyle.contains("AUDITORY") -> 
            "The Attuned Listener" to "Your ears are your greatest learning tool. You find that hearing an explanation is often more effective than reading it, and you likely benefit from talking through problems. We’ve enhanced your experience with text-to-speech notes and conversational quiz formats that resonate with your verbal strengths."
        
        else -> null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // MorphLearn Logo
        Image(
            painter = painterResource(id = R.drawable.morphlearn_logo),
            contentDescription = "MorphLearn Logo",
            modifier = Modifier.size(120.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Profile Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileDetailItem(label = "Name", value = viewModel.name)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = BackgroundGray)
                ProfileDetailItem(label = "Email", value = viewModel.email)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = BackgroundGray)
                ProfileDetailItem(
                    label = "Learner Type",
                    value = viewModel.learnerType.replace("_", "/")
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Learning Style Description Card
        learningStyleInfo?.let { (title, description) ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MorphTeal.copy(alpha = 0.05f)),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MorphTeal
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextDark,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }
        }
        
        // Extra spacer for scrollability
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ProfileDetailItem(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MorphTeal)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = TextDark)
    }
}
