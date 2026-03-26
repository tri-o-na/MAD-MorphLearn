package mad.team9.morphlearn.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class AndroidTextToSpeechController(
    context: Context
) : TextToSpeechController, TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = TextToSpeech(context, this)
    private var ready: Boolean = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale.US)
            ready = result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED
        } else {
            ready = false
        }
    }

    override fun speak(text: String) {
        if (!ready || text.isBlank()) return

        textToSpeech?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "morphlearn_tts"
        )
    }

    override fun stop() {
        textToSpeech?.stop()
    }

    override fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        ready = false
    }

    override fun isReady(): Boolean {
        return ready
    }
}