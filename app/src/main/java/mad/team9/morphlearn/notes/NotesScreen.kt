package mad.team9.morphlearn.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NotesScreen(
    viewModel: NotesViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onOpenTopic: (materialId: String) -> Unit
) {
    val materials by viewModel.materials.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadMaterials() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Topics", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        if (error != null) {
            Text(
                text = "Error: $error",
                color = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(12.dp))
        }

        if (materials.isEmpty() && error == null) {
            Text(
                text = "No topics found yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(materials) { m ->
                Button(
                    onClick = { onOpenTopic(m.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(m.title.ifBlank { "Untitled Topic" })
                }
            }
        }
    }
}