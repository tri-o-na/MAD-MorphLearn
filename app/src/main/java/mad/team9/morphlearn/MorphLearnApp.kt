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
import androidx.compose.material.icons.filled.MenuBook
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
fun MorphLearnApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Only show bottom bar on these main screens
    val mainScreens = listOf("home", "profile", "library")
    val showBottomBar = currentRoute in mainScreens

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = Color.White) {
                    // Library Tab
                    NavigationBarItem(
                        selected = currentRoute == "library",
                        onClick = { /* navigate to library */ },
                        icon = { Icon(Icons.Default.MenuBook, "Library") },
                        label = { Text("Library") }
                    )
                    // Home Tab
                    NavigationBarItem(
                        selected = currentRoute == "home",
                        onClick = { navController.navigate("home") { launchSingleTop = true } },
                        icon = { Icon(Icons.Default.Home, "Home") },
                        label = { Text("Home") }
                    )
                    // Profile Tab
                    NavigationBarItem(
                        selected = currentRoute == "profile",
                        onClick = { navController.navigate("profile") { launchSingleTop = true } },
                        icon = { Icon(Icons.Default.Person, "Profile") },
                        label = { Text("Profile") }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (FirebaseAuth.getInstance().currentUser != null) "home" else "login",
            modifier = Modifier.padding(innerPadding) // This prevents content hiding under the bar
        ) {
            composable("login") { /* ... existing code */ }
            composable("register") { /* ... existing code */ }
            composable("onboarding_quiz") { /* ... existing code */ }

            composable("home") {
                val user = FirebaseAuth.getInstance().currentUser
                val displayName = user?.email?.substringBefore("@") ?: "Learner"
                HomeScreen(
                    username = displayName,
                    navController = navController,
                    modifier = Modifier
                )
            }

            composable("profile") {
                ProfileScreen()
            }
        }
    }
}