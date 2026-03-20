package mad.team9.morphlearn.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import mad.team9.morphlearn.ui.theme.*

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = viewModel()) {
    // Fetch data from Firestore when screen loads
    LaunchedEffect(Unit) {
        viewModel.fetchUserData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Profile Picture Placeholder
        Surface(
            modifier = Modifier.size(120.dp).clip(CircleShape),
            color = MorphTeal.copy(alpha = 0.1f)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Picture",
                modifier = Modifier.padding(24.dp),
                tint = MorphTeal
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

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
    }
}

@Composable
fun ProfileDetailItem(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MorphTeal)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = TextDark)
    }
}
