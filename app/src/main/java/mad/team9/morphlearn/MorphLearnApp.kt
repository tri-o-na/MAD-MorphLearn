package mad.team9.morphlearn

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import mad.team9.morphlearn.onboardingQuiz.OnboardingQuizScreen

@Composable
fun MorphLearnApp(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Define which screens should show the bottom navigation bar
    val bottomBarRoutes = listOf("home", "profile", "library")
    val shouldShowBottomBar = currentDestination?.route in bottomBarRoutes

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    // Library Tab
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.LibraryBooks, contentDescription = "Library") },
                        label = { Text("Library") },
                        selected = currentDestination?.hierarchy?.any { it.route == "library" } == true,
                        onClick = {
                            // Navigation logic to prevent stack buildup
                            navController.navigate("library") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
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
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )

                    // Profile Tab
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile") },
                        selected = currentDestination?.hierarchy?.any { it.route == "profile" } == true,
                        onClick = {
                            navController.navigate("profile") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
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
            // Decide start destination based on current Firebase auth state
            startDestination = if (FirebaseAuth.getInstance().currentUser != null) "home" else "login",
            modifier = modifier.padding(innerPadding) // This padding prevents content from being hidden by the bottom bar
        ) {
            composable("login") {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
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
                    // Passing the navigation list and callback here fixes the HomeScreen errors
                    bottomNavItems = listOf("Library", "Home", "Profile"),
                    onBottomNavItemSelected = { route ->
                        navController.navigate(route.lowercase()) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable("profile") {
                ProfileScreen()
            }

            composable("library") {
                // Placeholder for Library Screen
                Text("Library Screen coming soon", modifier = Modifier.padding(innerPadding))
            }
        }
    }
}