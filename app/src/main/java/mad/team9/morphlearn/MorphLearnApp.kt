package mad.team9.morphlearn

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import mad.team9.morphlearn.home.HomeScreen
import mad.team9.morphlearn.login.LoginScreen
import mad.team9.morphlearn.profile.ProfileScreen
import mad.team9.morphlearn.login.RegisterScreen
import mad.team9.morphlearn.notes.NotesScreen
import mad.team9.morphlearn.notes.NoteDetailsScreen
import mad.team9.morphlearn.stylebasedquiz.QuizPlayScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FirebaseFirestore
import mad.team9.morphlearn.ai.AIFloatingActionButton
import mad.team9.morphlearn.ai.AIGeneratedNotes
import mad.team9.morphlearn.ai.AINotesRepository
import mad.team9.morphlearn.ai.AINotesViewModel
import java.net.URLEncoder
import mad.team9.morphlearn.onboardingQuiz.OnboardingQuizScreen
import mad.team9.morphlearn.login.FirebaseAuthManager

@Composable
fun MorphLearnApp(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val route = currentDestination?.route

    val firestore = FirebaseFirestore.getInstance()
    val repository = remember { AINotesRepository(firestore) }

    // Create Ai viewmodel to use for AI Upload PDF page and AI Generated Notes page
    val aiNotesViewModel: AINotesViewModel = viewModel(
        factory = object : ViewModelProvider.Factory{
            override fun<T: ViewModel> create(modelClass: Class<T>): T{
                return AINotesViewModel(repository) as T
            }
        }
    )

    // Define which screens should show navigation elements
    val bottomBarRoutes = listOf("home", "profile", "library")
    val shouldShowBottomBar = route in bottomBarRoutes
    val showAIFAB = route == "home"

    // Track the current user and their quiz status
    val currentUser = remember { FirebaseAuth.getInstance().currentUser }
    var isQuizComplete by remember { mutableStateOf<Boolean?>(null) }

    // Run the check when the app launches
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            isQuizComplete = FirebaseAuthManager.isLearningStyleSet()
        } else {
            isQuizComplete = false
        }
    }

    // Decide where to send them
    val startDestination = when {
        currentUser == null -> "login"
        isQuizComplete == null -> "loading" // Temporary state while fetching data
        isQuizComplete == true -> "home"
        else -> "onboarding_quiz"
    }

    if (isQuizComplete == null && currentUser != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                floatingActionButton = {
                    if (showAIFAB) {
                        AIFloatingActionButton(navController, aiNotesViewModel)
                    }
                },
                bottomBar = {
                    if (shouldShowBottomBar) {
                        NavigationBar(
                            containerColor = Color.White,
                            tonalElevation = 8.dp
                        ) {
                            // Library Tab
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        Icons.Default.LibraryBooks,
                                        contentDescription = "Library"
                                    )
                                },
                                label = { Text("Library") },
                                selected = currentDestination?.hierarchy?.any { it.route == "notes" } == true,
                                onClick = {
                                    navController.navigate("notes") {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )

                            // Home Tab
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                label = { Text("Home") },
                                selected = currentDestination?.hierarchy?.any { it.route == "home" } == true,
                                onClick = {
                                    navController.navigate("home") {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )

                            // Profile Tab
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = "Profile"
                                    )
                                },
                                label = { Text("Profile") },
                                selected = currentDestination?.hierarchy?.any { it.route == "profile" } == true,
                                onClick = {
                                    navController.navigate("profile") {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = modifier.padding(innerPadding)
                ) {
                    composable("login") {
                        val scope = rememberCoroutineScope()
                        LoginScreen(
                            onLoginSuccess = {
                                scope.launch {
                                    val complete = FirebaseAuthManager.isLearningStyleSet()
                                    if (complete) {
                                        navController.navigate("home") {
                                            popUpTo("login") {
                                                inclusive = true
                                            }
                                        }
                                    } else {
                                        navController.navigate("onboarding_quiz") {
                                            popUpTo("login") {
                                                inclusive = true
                                            }
                                        }
                                    }
                                }
                            },
                            onNavigateToRegister = { navController.navigate("register") }
                        )
                    }

                    composable("register") {
                        RegisterScreen(
                            onRegisterSuccess = {
                                navController.navigate("onboarding_quiz") {
                                    popUpTo("register") { inclusive = true }
                                }
                            },
                            onBackToLogin = { navController.popBackStack() }
                        )
                    }

                    composable("onboarding_quiz") {
                        OnboardingQuizScreen(
                            onQuizComplete = {
                                navController.navigate("home") {
                                    popUpTo("onboarding_quiz") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("home") {
                        val user = FirebaseAuth.getInstance().currentUser
                        val displayName = user?.email?.substringBefore("@") ?: "Learner"

                        HomeScreen(
                            username = displayName,
                            navController = navController,
                            onBottomNavItemSelected = { selectedRoute ->
                                navController.navigate(selectedRoute.lowercase()) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                    composable("notes") {
                        NotesScreen(
                            navController = navController,
                            onOpenTopic = { materialId ->
                                navController.navigate("noteDetails/$materialId")
                            }
                        )
                    }
                    composable(
                        route = "noteDetails/{materialId}",
                        arguments = listOf(navArgument("materialId") { type = NavType.StringType })
                    ) {
                        val materialId = it.arguments?.getString("materialId") ?: ""

                        NoteDetailsScreen(
                            materialId = materialId,
                            onBack = { navController.popBackStack() },
                            onTakeQuiz = { quizId, topicTitle ->
                                val encQuizId = URLEncoder.encode(quizId, "UTF-8")
                                val encTopic = URLEncoder.encode(topicTitle, "UTF-8")
                                navController.navigate("quizPlay/$encQuizId/$encTopic")
                            }
                        )
                    }
                    composable(
                        route = "quizPlay/{quizId}/{topic}",
                        arguments = listOf(
                            navArgument("quizId") { type = NavType.StringType },
                            navArgument("topic") { type = NavType.StringType }
                        )
                    ) {
                        val quizId = it.arguments?.getString("quizId") ?: ""
                        val topic = it.arguments?.getString("topic") ?: ""
                        QuizPlayScreen(
                            quizId = quizId,
                            topic = topic,
                            onDone = {
                                navController.navigate("notes") {
                                    popUpTo("notes") { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                    composable("profile") {
                        ProfileScreen()
                    }

                    composable(
                        route = "ai-response-PDF",
                    ) {
                        AIGeneratedNotes(navController, aiNotesViewModel)
                    }

                    composable("library") {
                        Text("Library Screen coming soon", modifier = Modifier.padding(16.dp))
                    }
                }
            }

            if (aiNotesViewModel.isLoading) {
                BackHandler(true) { }
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Black.copy(alpha = 0.6f)) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ){
                        CircularProgressIndicator(color = Color.White, strokeWidth = 4.dp)
                        Spacer(modifier= Modifier.height(16.dp))
                        Text(
                            text="AI is analyzing your PDF...",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "This may take a minute",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}