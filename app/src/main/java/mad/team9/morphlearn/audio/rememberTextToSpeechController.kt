package mad.team9.morphlearn.audio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberTextToSpeechController(): TextToSpeechController {
    val context = LocalContext.current
    val controller = remember { AndroidTextToSpeechController(context) }

    DisposableEffect(Unit) {
        onDispose {
            controller.shutdown()
        }
    }

    return controller
}