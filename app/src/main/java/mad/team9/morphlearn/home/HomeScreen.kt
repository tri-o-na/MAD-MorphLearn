package mad.team9.morphlearn.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import mad.team9.morphlearn.login.FirebaseAuthManager

@Composable
fun HomeScreen(
    username: String,
    navController: NavController? = null,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
    // These parameters are still here to maintain compatibility with your NavHost call,
    // but the UI logic for the bottom bar has been moved to MorphLearnApp.kt
    bottomNavItems: List<String> = listOf("Library", "Home", "Profile"),
    onBottomNavItemSelected: (String) -> Unit = {}
) {
    // Trigger Firestore logic
    LaunchedEffect(Unit) {
        viewModel.fetchUserData()
    }

    val style = viewModel.learningStyle

    // Mock Data
    val subjects = listOf(
        SubjectProgress("Computer Science", 1, 1, 85, 3, 3),
        SubjectProgress("Mathematics", 0, 1, 92, 2, 2)
    )

    // IMPORTANT: No Scaffold here. We use a Box or Column because MorphLearnApp handles the Scaffold.
    // The modifier here already contains the 'innerPadding' from MorphLearnApp's NavHost.
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // 1. Teal Header
        item {
            HeaderSection(
                username = username,
                style = style,
                onLogout = {
                    FirebaseAuthManager.signOut()
                    navController?.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // 2. Stats Cards (Floating over the header)
        item {
            StatsGrid()
        }

        // 3. Daily Streaks Section
        item {
            SectionTitle("Daily Streaks 🔥")
        }
        items(subjects.filter { it.currentStreak >= 2 }) { subject ->
            StreakCard(subject)
        }

        // 4. Subject Progress Section
        item {
            SectionTitle("Subject Progress")
        }
        item {
            Card(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    subjects.forEachIndexed { index, subject ->
                        SubjectProgressItem(
                            subject = subject,
                            onViewProfile = { onBottomNavItemSelected("Profile") }
                        )
                        if (index < subjects.lastIndex) Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }

        // Extra spacing at the bottom to ensure nothing is cut off
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
fun StatsGrid() {
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .offset(y = (-40).dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard("2", "Completed", Icons.Default.MenuBook, Color(0xFF006064), Modifier.weight(1f))
        StatCard("2", "Total", Icons.Default.ShowChart, Color(0xFFA78BFA), Modifier.weight(1f))
        StatCard("100%", "Rate", Icons.Default.EmojiEvents, Color(0xFFA78BFA), Modifier.weight(1f))
    }
}

@Composable
fun StatCard(value: String, label: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2D3436))
            Text(label, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        title,
        modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp),
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
    )
}

@Composable
fun StreakCard(subject: SubjectProgress) {
    Card(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFFFE0B2))
    ) {
        Row(
            modifier = Modifier
                .background(Brush.horizontalGradient(listOf(Color(0xFFFFF3E0), Color(0xFFFFEBEE))))
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(subject.subject, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Keep it going!", fontSize = 12.sp, color = Color.Gray)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Whatshot, null, tint = Color(0xFFFF7043), modifier = Modifier.size(28.dp))
                Text(subject.currentStreak.toString(), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFF7043))
            }
        }
    }
}

@Composable
fun SubjectProgressItem(
    subject: SubjectProgress,
    onViewProfile: () -> Unit
) {
    Column {
        Text(subject.subject, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(
            text = "${subject.completedTopics}/${subject.totalTopics} topics • ${subject.accuracy}% accuracy",
            fontSize = 12.sp,
            color = Color.Gray
        )
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { subject.completedTopics.toFloat() / subject.totalTopics.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = Color(0xFF006064),
            trackColor = Color(0xFFE0E0E0)
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { onViewProfile() },
            modifier = Modifier.fillMaxWidth(0.5f),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color(0xFF006064))
        ) {
            Text(text = "View Details", color = Color(0xFF006064), fontSize = 12.sp)
        }
    }
}

@Composable
fun HeaderSection(username: String, style: String, onLogout: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF006064), RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .padding(top = 24.dp, start = 20.dp, end = 20.dp, bottom = 60.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Welcome, $username!", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("We morph your learn 🚀", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                }
                IconButton(onClick = onLogout) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.White)
                }
            }

            Spacer(Modifier.height(24.dp))

            Surface(
                color = Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (style.contains("Read", true)) Icons.Default.MenuBook else Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Your Learning Style", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        Text(style, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }
        }
        Button(onClick = {

            navController?.navigate("notes") {
                popUpTo("home") { inclusive = true }
                // Optional: popUpTo("register") { inclusive = true }
            }
        }) {
            Text("notes")
        }
    }
}