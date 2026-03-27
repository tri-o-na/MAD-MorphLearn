package mad.team9.morphlearn.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import mad.team9.morphlearn.ui.theme.*

private fun getQuizTypeLabel(learningStyle: String?): String {
    return when (learningStyle?.trim()?.uppercase()) {
        "READ_WRITE" -> "Fill in the blank"
        "VISUAL" -> "Flashcard"
        "AUDITORY" -> "Text to speech"
        "KINESTHETIC" -> "Drag and drop"
        else -> "Standard quiz"
    }
}

@Composable
fun NotesScreen(
    navController: NavController,
    viewModel: NotesViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onOpenTopic: (materialId: String) -> Unit
) {
    val groupedMaterials by viewModel.groupedMaterials.collectAsState()
    val error by viewModel.error.collectAsState()
    val learningStyle by viewModel.learningStyle.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadMaterials()
        viewModel.loadLearningStyle()
    }

    val expandedSubjects = remember { mutableStateMapOf<String, Boolean>() }
    val quizTypeLabel = getQuizTypeLabel(learningStyle)

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val items = listOf("notes", "home", "profile")

                items.forEach { screen ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == screen } == true
                    NavigationBarItem(
                        icon = {
                            when (screen) {
                                "home" -> Icon(Icons.Default.Home, contentDescription = null)
                                "notes" -> Icon(Icons.Default.LibraryBooks, contentDescription = null)
                                "profile" -> Icon(Icons.Default.Person, contentDescription = null)
                            }
                        },
                        label = {
                            Text(
                                if (screen == "notes") "Library"
                                else screen.replaceFirstChar { it.uppercase() }
                            )
                        },
                        selected = isSelected,
                        onClick = {
                            if (!isSelected) {
                                navController.navigate(screen) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MorphTeal,
                            selectedTextColor = MorphTeal,
                            indicatorColor = TrackTeal
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGray)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MorphTeal,
                        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                    )
                    .padding(start = 20.dp, top = 24.dp, end = 20.dp, bottom = 22.dp)
            ) {
                Text(
                    text = "My Library",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            if (error != null) {
                Text(
                    text = "Error: $error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                )
                return@Column
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 16.dp,
                    bottom = innerPadding.calculateBottomPadding() + 20.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(groupedMaterials, key = { it.subjectName }) { group ->
                    val isExpanded = expandedSubjects[group.subjectName] ?: true

                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
                            containerColor = Color.White,
                            contentColor = TextDark
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        expandedSubjects[group.subjectName] = !isExpanded
                                    },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${group.subjectName} (${group.materials.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )

                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                                    tint = MorphTeal
                                )
                            }

                            if (isExpanded) {
                                group.materials.forEach { material ->
                                    ElevatedButton(
                                        onClick = { onOpenTopic(material.id) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .defaultMinSize(minHeight = 88.dp)
                                            .border(
                                                width = 1.dp,
                                                color = Color(0xFFE3E8EA),
                                                shape = RoundedCornerShape(16.dp)
                                            ),
                                        colors = ButtonDefaults.elevatedButtonColors(
                                            containerColor = Color.White,
                                            contentColor = TextDark
                                        ),
                                        shape = RoundedCornerShape(16.dp),
                                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = material.title.ifBlank { "Untitled Topic" },
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 15.sp,
                                                color = TextDark
                                            )

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.Start
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .background(
                                                            color = TrackTeal,
                                                            shape = RoundedCornerShape(50)
                                                        )
                                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = quizTypeLabel,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = MorphTeal
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}