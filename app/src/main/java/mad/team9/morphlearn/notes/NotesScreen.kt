package mad.team9.morphlearn.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@Composable
fun NotesScreen(
    viewModel: NotesViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val materials by viewModel.materials.collectAsState()
    val error by viewModel.error.collectAsState()
    val testMode = true

    val displayMaterials = if (testMode) {
        listOf(
            Material(
                id = "demo1",
                title = "Demo Note: Operating Systems",
                generatedNotes = """
Key Concepts:
- Process vs Thread
- Context switching overhead
- Scheduling: FCFS, SJF, RR

Example (Round Robin):
- Time quantum = 4ms
- P1 runs 4ms → preempt
- P2 runs 4ms → preempt

Summary:
This should show as headings + bullets, and the card should be collapsible.
            """.trimIndent()
            )
        )
    } else {
        materials
    }

    LaunchedEffect(Unit) { viewModel.loadMaterials() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Generated Notes", style = MaterialTheme.typography.titleLarge)
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
                text = "No notes found yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(displayMaterials) { m ->
                NotesCard(
                    title = m.title,
                    generatedNotes = m.generatedNotes
                )
            }
        }
    }
}

@Composable
private fun NotesCard(
    title: String,
    generatedNotes: String
) {
    var expanded by remember { mutableStateOf(false) }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.ifBlank { "Untitled" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }
            }

            if (expanded) {
                Spacer(Modifier.height(10.dp))

                if (generatedNotes.isBlank()) {
                    Text(
                        text = "(No generated notes yet)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    NotesBody(text = generatedNotes)
                }
            }
        }
    }
}

@Composable
private fun NotesBody(text: String) {
    val lines = text
        .replace("\r\n", "\n")
        .trim()
        .lines()

    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 420.dp)
            .verticalScroll(scroll)
    ) {
        lines.forEach { raw ->
            val line = raw.trim()

            if (line.isBlank()) {
                Spacer(Modifier.height(8.dp))
                return@forEach
            }

            val isHeading =
                line.endsWith(":") ||
                        (Regex("""^\d+[\.\)]\s+\S+""").containsMatchIn(line) && line.length <= 70)

            when {
                isHeading -> {
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(6.dp))
                }

                line.startsWith("- ") || line.startsWith("• ") -> {
                    Bullet(content = line.drop(2).trim())
                }

                line.length < 140 -> Bullet(content = line)

                else -> {
                    Text(text = line, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun Bullet(content: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("•", style = MaterialTheme.typography.bodyMedium)
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(Modifier.height(6.dp))
}