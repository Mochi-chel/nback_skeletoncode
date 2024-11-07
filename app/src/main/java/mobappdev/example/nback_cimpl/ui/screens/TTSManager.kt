package mobappdev.example.nback_cimpl.ui.screens
import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import java.util.Locale

class TTSManager(private val context: Context) {

    private var textToSpeech: TextToSpeech? = null

    init {
        // Initiera TextToSpeech när TTSManager skapas
        textToSpeech = TextToSpeech(context, OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Sätt språk till standard
                val langResult = textToSpeech?.setLanguage(Locale.getDefault())
                if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTSManager", "Språk stöds inte eller saknas.")
                }
            } else {
                Log.e("TTSManager", "TextToSpeech initiering misslyckades.")
            }
        })
    }

    // Funktion för att tala text
    fun speak(text: String) {
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    // Funktion för att stoppa TextToSpeech när det inte används
    fun stop() {
        textToSpeech?.stop()
    }

    // Stäng TTS när objektet tas bort
    fun shutdown() {
        textToSpeech?.shutdown()
    }
}
