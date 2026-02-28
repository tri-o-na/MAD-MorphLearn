package mad.team9.morphlearn.ai

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun AIFloatingActionButton(
    navController: NavController
) {
    FloatingActionButton(
        onClick = { navController.navigate("upload")}
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Upload PDF to AI"
        )
    }
}