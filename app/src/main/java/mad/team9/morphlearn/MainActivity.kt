package mad.team9.morphlearn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import mad.team9.morphlearn.stylebasedquiz.common.QuizResult
import mad.team9.morphlearn.stylebasedquiz.common.QuizResultRepository
import mad.team9.morphlearn.ui.theme.MorphLearnTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Force dynamicColor to false to use the branded colors defined in ui.theme
            MorphLearnTheme(dynamicColor = false) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MorphLearnApp(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
