package mad.team9.morphlearn.ai

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.net.URLDecoder

// Update this kotlin file to be upload to DB instead of displaying the output of the generated notes
@Composable
fun AIGeneratedNotes (notes: String) {
    val decoded = URLDecoder.decode(notes, "UTF-8")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text (
            text= decoded,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}