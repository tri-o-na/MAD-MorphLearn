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
    onBottomNavItemSelected: (String) -> Unit = {}
) {
    LaunchedEffect(Unit) {
        viewModel.fetchUserData()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        item {
            HeaderSection(
                username = username,
                style = viewModel.learningStyle,
                onLogout = {
                    FirebaseAuthManager.signOut()
                    navController?.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        item {
            StatsGrid(viewModel)
        }

        // 3. Dynamic Subject Streaks Section
        if (viewModel.subjectStreaks.isNotEmpty()) {
            item {
                SectionTitle("Subject Streaks 🔥")
            }
            items(viewModel.subjectStreaks.toList()) { streakPair ->
                SubjectStreakCard(streakPair.first, streakPair.second)
            }
        }

        // 2. Subject Progress Section (Latest 3)
        item { SectionTitle("Subject Progress") }
        if (viewModel.latestSubjectProgress.isNotEmpty()) {
            items(viewModel.latestSubjectProgress) { progress ->
                DetailedSubjectProgressCard(progress)
            }
        } else {
            item {
                Text(
                    "No quiz activity yet.",
                    modifier = Modifier.padding(horizontal = 20.dp),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

//        item { SectionTitle("General Progress") }
//        item {
//            LibrarySummaryCard(viewModel.totalMaterials, onBottomNavItemSelected)
//        }

        item { Spacer(Modifier.height(32.dp)) }
    }
}

@Composable
fun SubjectStreakCard(subjectTitle: String, streakCount: Int) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(subjectTitle, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF2D3436))
                Text("Daily Streak", fontSize = 12.sp, color = Color.Gray)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Whatshot, null, tint = Color(0xFFFF7043), modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(4.dp))
                Text(streakCount.toString(), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFF7043))
            }
        }
    }
}

//@Composable
//fun LibrarySummaryCard(totalMaterials: Int, onBottomNavItemSelected: (String) -> Unit) {
//    Card(
//        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
//        colors = CardDefaults.cardColors(containerColor = Color.White),
//        elevation = CardDefaults.cardElevation(2.dp)
//    ) {
//        Column(Modifier.padding(16.dp)) {
//            Text("Your Materials", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF006064))
//            Text(
//                "You currently have $totalMaterials items in your library.",
//                fontSize = 12.sp,
//                color = Color.Gray,
//                modifier = Modifier.padding(top = 4.dp)
//            )
//            Spacer(Modifier.height(12.dp))
//            Button(
//                onClick = { onBottomNavItemSelected("Library") },
//                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006064)),
//                shape = RoundedCornerShape(8.dp),
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Text("Open Library", color = Color.White)
//            }
//        }
//    }
//}

@Composable
fun DetailedSubjectProgressCard(progress: SubjectProgress) {
    Card(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = progress.subject,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFF2D3436)
            )
            Text(
                text = "${progress.completedTopics}/${progress.totalTopics} topics • ${progress.accuracy}% accuracy",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
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
                        val displayStyle = style.replace("_", "/")
                        Text(displayStyle, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun StatsGrid(viewModel: HomeViewModel) {
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .offset(y = (-40).dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(viewModel.completedQuizzes.toString(), "Completed", Icons.Default.CheckCircle, Color(0xFF006064), Modifier.weight(1f))
        StatCard(viewModel.totalMaterials.toString(), "Materials", Icons.Default.LibraryBooks, Color(0xFFA78BFA), Modifier.weight(1f))
        StatCard(viewModel.successRate, "Rate", Icons.Default.EmojiEvents, Color(0xFFA78BFA), Modifier.weight(1f))
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
        fontSize = 16.sp,
        color = Color(0xFF2D3436)
    )
}