package mad.team9.morphlearn.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import mad.team9.morphlearn.login.FirebaseAuthManager
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.filled.ArrowDropDown
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val chartColors = listOf(
    Color(0xFF006064), // Teal
    Color(0xFFA78BFA), // Purple
    Color(0xFFFFCC80), // Orange
    Color(0xFFF06292), // Pink
    Color(0xFF4DB6AC), // Light Teal
    Color(0xFF7986CB), // Indigo
    Color(0xFF9CCC65)  // Light Green
)

@Composable
fun HomeScreen(
    username: String,
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    viewModel: HomeViewModel = viewModel(),
    onBottomNavItemSelected: (String) -> Unit = {}
) {
    LaunchedEffect(Unit) {
        viewModel.fetchUserData()
        viewModel.fetchChartData()
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

        // Subject Streaks Section
        if (viewModel.subjectStreaks.isNotEmpty()) {
            item {
                SectionTitle("Subject Streaks 🔥")
            }
            items(viewModel.subjectStreaks.toList()) { streakPair ->
                SubjectStreakCard(streakPair.first, streakPair.second)
            }
        }

        // Subject Progress Section (Latest 3)
        item { SectionTitle("Recent Topic Progress") }
        if (viewModel.latestSubjectProgress.isNotEmpty()) {
            items(viewModel.latestSubjectProgress) { progress ->
                DetailedSubjectProgressCard(progress)
            }
        } else {
            item {
                Text(
                    "No quiz activity yet.",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        // Subject Mastery Radar Chart
        item { SectionTitle("Subject Mastery (Radar)") }
        item {
            RadarChartCard(viewModel.radarDataList)
        }

        // Topic Trends Line Chart
        item {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionTitle("Topic Trends", Modifier.padding(0.dp))
                SubjectDropdown(viewModel)
            }
        }
        item {
            LineChartCard(viewModel.topicTrends)
        }

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
                text = "${progress.accuracy}% accuracy",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun SubjectDropdown(viewModel: HomeViewModel) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { expanded = true }) {
            Text(viewModel.selectedSubjectForLineChart ?: "Select Subject", color = Color(0xFF006064))
            Icon(Icons.Default.ArrowDropDown, null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            viewModel.radarDataList.forEach { data ->
                DropdownMenuItem(
                    text = { Text(data.subjectName) },
                    onClick = {
                        viewModel.onSubjectSelected(data.subjectName)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun RadarChartCard(data: List<RadarData>) {
    Card(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp).fillMaxWidth().height(250.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        if (data.size < 3) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    if (data.isEmpty()) "Take more quizzes to see analysis" else "Add more subjects to see radar",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        } else {
            Column(Modifier.padding(16.dp)) {
                Canvas(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    val centerX = size.width / 2
                    val centerY = size.height / 2
                    val radius = size.minDimension / 2.5f
                    val numPoints = data.size
                    val angleStep = (2 * PI / numPoints).toFloat()

                    // Draw Background Circles
                    for (i in 1..4) {
                        drawCircle(
                            color = Color.LightGray.copy(alpha = 0.5f),
                            radius = radius * (i / 4f),
                            center = center,
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }

                    // Draw Axes with Subject Colors
                    data.forEachIndexed { index, _ ->
                        val color = chartColors[index % chartColors.size]
                        val angle = index * angleStep - (PI / 2).toFloat()
                        val lineEndX = centerX + radius * cos(angle)
                        val lineEndY = centerY + radius * sin(angle)
                        drawLine(
                            color = color.copy(alpha = 0.3f),
                            start = center,
                            end = Offset(lineEndX, lineEndY),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Draw Data Polygon Segments (Shaded Triangles)
                    data.forEachIndexed { index, _ ->
                        val color = chartColors[index % chartColors.size]
                        val nextIndex = (index + 1) % numPoints
                        
                        val angle1 = index * angleStep - (PI / 2).toFloat()
                        val valueRadius1 = (data[index].averageAccuracy / 100f) * radius
                        val x1 = centerX + valueRadius1 * cos(angle1)
                        val y1 = centerY + valueRadius1 * sin(angle1)
                        
                        val angle2 = nextIndex * angleStep - (PI / 2).toFloat()
                        val valueRadius2 = (data[nextIndex].averageAccuracy / 100f) * radius
                        val x2 = centerX + valueRadius2 * cos(angle2)
                        val y2 = centerY + valueRadius2 * sin(angle2)
                        
                        val segmentPath = Path().apply {
                            moveTo(centerX, centerY)
                            lineTo(x1, y1)
                            lineTo(x2, y2)
                            close()
                        }
                        
                        // Fill the triangular segment with the subject's color
                        drawPath(
                            path = segmentPath,
                            color = color.copy(alpha = 0.35f),
                            style = Fill
                        )
                        
                        // Draw the outer border line for this segment
                        drawLine(
                            color = Color.DarkGray.copy(alpha = 0.4f),
                            start = Offset(x1, y1),
                            end = Offset(x2, y2),
                            strokeWidth = 1.5.dp.toPx()
                        )
                    }

                    // Draw points with specific colors
                    data.forEachIndexed { index, radarData ->
                        val color = chartColors[index % chartColors.size]
                        val angle = index * angleStep - (PI / 2).toFloat()
                        val valueRadius = (radarData.averageAccuracy / 100f) * radius
                        val x = centerX + valueRadius * cos(angle)
                        val y = centerY + valueRadius * sin(angle)
                        
                        drawCircle(color, 5.dp.toPx(), Offset(x, y))
                        drawCircle(Color.White, 2.5.dp.toPx(), Offset(x, y))
                    }
                }
                
                Spacer(Modifier.height(8.dp))

                // Legend with matching colors
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    data.forEachIndexed { index, item ->
                        val color = chartColors[index % chartColors.size]
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)) {
                            Box(Modifier.size(8.dp).background(color, RoundedCornerShape(2.dp)))
                            Spacer(Modifier.width(4.dp))
                            Text(item.subjectName, fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LineChartCard(trends: List<TopicTrend>) {
    Card(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp).fillMaxWidth().height(200.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        if (trends.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No data for this subject", color = Color.Gray, fontSize = 12.sp)
            }
        } else {
            Column(Modifier.padding(16.dp)) {
                Canvas(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    val width = size.width
                    val height = size.height
                    
                    // Draw Grid
                    for (i in 0..4) {
                        val y = height - (i * height / 4)
                        drawLine(Color.LightGray.copy(0.3f), Offset(0f, y), Offset(width, y), 1.dp.toPx())
                    }

                    trends.forEach { trend ->
                        if (trend.points.size > 1) {
                            val path = Path()
                            trend.points.forEachIndexed { index, point ->
                                val x = (index.toFloat() / (trend.points.size - 1)) * width
                                val y = height - (point.score / 100f * height)
                                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            }
                            drawPath(path, trend.color, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
                            
                            // Draw points
                            trend.points.forEachIndexed { index, point ->
                                val x = (index.toFloat() / (trend.points.size - 1)) * width
                                val y = height - (point.score / 100f * height)
                                drawCircle(trend.color, 4.dp.toPx(), Offset(x, y))
                                drawCircle(Color.White, 2.dp.toPx(), Offset(x, y))
                            }
                        } else if (trend.points.size == 1) {
                             // Single point case
                             val point = trend.points[0]
                             val x = width / 2
                             val y = height - (point.score / 100f * height)
                             drawCircle(trend.color, 4.dp.toPx(), Offset(x, y))
                        }
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                // Legend
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    trends.forEach { trend ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(4.dp)) {
                            Box(Modifier.size(8.dp).background(trend.color, RoundedCornerShape(2.dp)))
                            Spacer(Modifier.width(4.dp))
                            Text(trend.topicName, fontSize = 10.sp, color = Color.Gray, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        modifier = modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp),
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = Color(0xFF2D3436)
    )
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
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = Color.White)
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
                        if (style.contains("Read", true)) Icons.AutoMirrored.Filled.MenuBook else Icons.Default.TouchApp,
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
        StatCard(viewModel.totalMaterials.toString(), "Materials", Icons.AutoMirrored.Filled.LibraryBooks, Color(0xFFA78BFA), Modifier.weight(1f))
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
