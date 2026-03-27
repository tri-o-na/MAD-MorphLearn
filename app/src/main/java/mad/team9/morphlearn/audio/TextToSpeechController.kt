package mad.team9.morphlearn.audio

interface TextToSpeechController {
    fun speak(text: String)
    fun stop()
    fun shutdown()
    fun isReady(): Boolean
}