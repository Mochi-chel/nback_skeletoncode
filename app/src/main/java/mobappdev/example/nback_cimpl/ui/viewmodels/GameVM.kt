package mobappdev.example.nback_cimpl.ui.viewmodels

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mobappdev.example.nback_cimpl.GameApplication
import mobappdev.example.nback_cimpl.NBackHelper
import mobappdev.example.nback_cimpl.data.UserPreferencesRepository
import mobappdev.example.nback_cimpl.ui.screens.TTSManager
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.MutableLiveData


/**
 * This is the GameViewModel.
 *
 * It is good practice to first make an interface, which acts as the blueprint
 * for your implementation. With this interface we can create fake versions
 * of the viewmodel, which we can use to test other parts of our app that depend on the VM.
 *
 * Our viewmodel itself has functions to start a game, to specify a gametype,
 * and to check if we are having a match
 *
 * Date: 25-08-2023
 * Version: Version 1.0
 * Author: Yeetivity
 *
 */


interface GameViewModel {
    val gameState: StateFlow<GameState>
    val score: StateFlow<Int>
    val highscore: StateFlow<Int>
    val nBack: StateFlow<Int>
    val gridState: StateFlow<List<Color>>
    val correctAnswers: StateFlow<Int>
    val wrongAnswers: StateFlow<Int>
    val gridSize: StateFlow<Int>


    fun setGameType(gameType: GameType)
    fun setNBack(newNBack: Int)
    suspend fun startGame()

    fun checkMatch(currentIndex: Int)
    fun checkMatch(currentIndex: Int, matchType: MatchType)
}

enum class MatchType {
    AUDIO, VISUAL
}




class GameVM(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val context: Context
): GameViewModel, ViewModel() {

    private var ttsManager: TTSManager? = null  // Byt till din TTSManager

    init {
        // Initiera TTS Manager
        ttsManager = TTSManager(context)
    }

    private var audioIndex = 0
    private var visualIndex = 0

    private val _gameState = MutableStateFlow(GameState())
    override val gameState: StateFlow<GameState>
        get() = _gameState.asStateFlow()

    private val _score = MutableStateFlow(0)
    override val score: StateFlow<Int>
        get() = _score

    private val _highscore = MutableStateFlow(0)
    override val highscore: StateFlow<Int>
        get() = _highscore

    private val _nBack = MutableStateFlow<Int>(2)
    override val nBack: StateFlow<Int> = _nBack.asStateFlow()

    private val _gridSize = MutableStateFlow(3) // Default grid_size
    override val gridSize: StateFlow<Int> get() = _gridSize

    private val _eventsPerRound = MutableStateFlow(10) // Default events_per_round
    val eventsPerRound: StateFlow<Int> get() = _eventsPerRound

    private val _timeBetweenEvents = MutableStateFlow(2000L) // Default time_between_events
    val timeBetweenEvents: StateFlow<Long> get() = _timeBetweenEvents

    private val _numSpokenLetters = MutableStateFlow(3) // Default num_spoken_letters
    val numSpokenLetters: StateFlow<Int> get() = _numSpokenLetters


    private var job: Job? = null
    private val eventInterval: Long = 2000L

    private val nBackHelper = NBackHelper()
    private var currentIndex = 0
    private var events = emptyArray<Int>()
    private val _matchFeedback = MutableStateFlow("")
    val matchFeedback: StateFlow<String> = _matchFeedback.asStateFlow()

    private val _gridState = MutableStateFlow(List(_gridSize.value * _gridSize.value) { Color.White })
    override val gridState: StateFlow<List<Color>> = _gridState

    // Statistiken för korrekta och felaktiga svar
    private val _correctAnswers = MutableStateFlow(0)
    override val correctAnswers: StateFlow<Int> = _correctAnswers.asStateFlow()

    private val _wrongAnswers = MutableStateFlow(0)
    override val wrongAnswers: StateFlow<Int> = _wrongAnswers.asStateFlow()

    //Deklarerar för det som behöver i audioVisual
    private var visualEvents = emptyArray<Int>()
    private var audioEvents = emptyArray<Int>()

    private var hasCheckedCurrentStimulus = false



    fun updateGridState(index: Int, color: Color) {
        _gridState.value = _gridState.value.toMutableList().also { it[index] = color }
    }


    private fun updateGridColors(value: Int) {
        val adjustedValue = value - 1

        if (adjustedValue in 0 until _gridState.value.size) {
            _gridState.value = _gridState.value.mapIndexed { index, color ->
                if (index == adjustedValue) Color.Blue else Color.White
            }
        } else {
            Log.d("GameVM", "Ogiltigt index: $adjustedValue")
        }
    }

    override fun checkMatch(currentIndex: Int) {
        if (hasCheckedCurrentStimulus) {
            Log.d("GameVM", "Index $currentIndex är för lågt för en $nBack-back-jämförelse. \nHAS CHECKED")
            return
        }
        val stimulus = events[currentIndex]
        val previousStimulus = events.getOrNull(currentIndex - nBack.value)

        Log.d("GameVM", "Checking match at index $currentIndex: stimulus = $stimulus, previousStimulus = $previousStimulus")

        val isMatch = stimulus == previousStimulus

        if (isMatch) {
            _score.value += 1
            _correctAnswers.value += 1
            Log.d("GameVM", "Correct match! Score: ${_score.value}, Correct answers: ${_correctAnswers.value}")
        } else {
            _wrongAnswers.value += 1
            Log.d("GameVM", "Wrong match! Wrong answers: ${_wrongAnswers.value}")
        }
        hasCheckedCurrentStimulus = true
    }

    override fun checkMatch(currentIndex: Int, matchType: MatchType) {
        if (hasCheckedCurrentStimulus) {
            Log.d("GameVM", "Index $currentIndex är för lågt för en $nBack-back-jämförelse. \nHAS CHECKED")
            return
        }
        if(visualEvents.isEmpty())
        {
            Log.d("GameVM", "THE VISUALEVENTS IS EMPTY!!!!!")
        }

        Log.d("GameVM", "currentIndex: $currentIndex och visual event size är: $visualEvents.size ")
        // Ensure the index is large enough to check a match
        if (currentIndex >= nBack.value) {
            when (matchType) {
                MatchType.VISUAL -> {
                    if (visualEvents.isNotEmpty()) {
                        val visualStimulus = visualEvents[currentIndex]
                        val previousVisualStimulus = visualEvents.getOrNull(currentIndex - nBack.value)
                        Log.d("GameVM", "Checking match at index $currentIndex: stimulus = $visualStimulus, previousStimulus = $previousVisualStimulus")
                        val isVisualMatch = visualStimulus == previousVisualStimulus
                        processMatch(isVisualMatch)
                    } else {
                        Log.d("GameVM", "Visual events list is empty or index out of bounds.")
                    }
                }
                MatchType.AUDIO -> {
                    if (audioEvents.isNotEmpty() && currentIndex < audioEvents.size) {
                        val audioStimulus = audioEvents[currentIndex]
                        val previousAudioStimulus = audioEvents.getOrNull(currentIndex - nBack.value)
                        Log.d("GameVM", "Checking match at index $currentIndex: stimulus = $audioStimulus, previousStimulus = $previousAudioStimulus")
                        val isAudioMatch = audioStimulus == previousAudioStimulus
                        processMatch(isAudioMatch)
                    } else {
                        Log.d("GameVM", "Audio events list is empty or index out of bounds.")
                    }
                }
            }
        } else {
            Log.d("GameVM", "Index $currentIndex is too small for a ${nBack.value} -back comparison.")
        }
    }


    private fun processMatch(isMatch: Boolean) {
        if (isMatch) {
            _score.value += 1
            Log.d("GameVM", "Correct match! Score: ${_score.value}, Correct answers: ${_correctAnswers.value}")
            _correctAnswers.value += 1
        } else {
            _wrongAnswers.value += 1
            Log.d("GameVM", "Wrong match! Wrong answers: ${_wrongAnswers.value}")
        }
        hasCheckedCurrentStimulus = true
    }

    fun resetCheckFlag() {
        hasCheckedCurrentStimulus = false
    }

    override fun setNBack(newNBack: Int) {
        _nBack.value = newNBack
    }

    override fun setGameType(gameType: GameType) {
        _gameState.value = _gameState.value.copy(gameType = gameType)
    }

    override suspend fun startGame() {
        job?.cancel()
        _score.value = 0
        _correctAnswers.value = 0
        _wrongAnswers.value = 0

        events = nBackHelper.generateNBackString(eventsPerRound.value, gridSize.value*gridSize.value, 30, nBack.value).toList().toTypedArray()
        Log.d("GameVM", "Generated events: ${events.joinToString()}")
        Log.d("GameVM", "nBack value: ${nBack.value}")


        if (gameState.value.gameType == GameType.AudioVisual) {
            audioEvents = nBackHelper.generateNBackString(eventsPerRound.value, gridSize.value*gridSize.value, 30, nBack.value).toList().toTypedArray()
            delay(500)
            visualEvents = nBackHelper.generateNBackString(eventsPerRound.value, gridSize.value*gridSize.value, 30, nBack.value).toList().toTypedArray()

            Log.d("GameVM", "Generated audio events: ${audioEvents.joinToString()}")
            Log.d("GameVM", "Generated visual events: ${visualEvents.joinToString()}")
        }

        job = viewModelScope.launch {
            when (gameState.value.gameType) {
                GameType.Audio -> runAudioGame()
                GameType.AudioVisual -> runAudioVisualGame(audioEvents, visualEvents)
                GameType.Visual -> runVisualGame(events)
            }
        }
    }

    private fun numberToLetter(number: Int, numLetters: Int): String {
        val letters = StringBuilder()
        for (i in 0 until numLetters) {
            val letter = 'A' + ((number + i - 1) % 26) // Loopar tillbaka efter Z
            letters.append(letter)
        }
        return letters.toString()

    }

    private suspend fun runVisualGame(events: Array<Int>){

        for (index in events.indices) {
            resetCheckFlag()
            updateGridColors(events[index])
            _gameState.value = _gameState.value.copy(eventValue = index)
            delay(timeBetweenEvents.value)
            resetGridColors()
            delay(timeBetweenEvents.value)
        }
    }

    private suspend fun runAudioGame() {
        for (index in events.indices) {
            resetCheckFlag()
            updateGridColors(events[index])
            _gameState.value = _gameState.value.copy(eventValue = index)

            val stimulus = events[index]
            val letter = numberToLetter(stimulus, _numSpokenLetters.value)
            ttsManager?.speak("$letter")
            delay(timeBetweenEvents.value)
            resetGridColors()
            delay(timeBetweenEvents.value)
        }
    }

    private suspend fun runAudioVisualGame(audioEvents: Array<Int>, visualEvents: Array<Int>){
        for (index in audioEvents.indices) {
            resetCheckFlag()
            val audioStimulus = audioEvents[index]
            val visualStimulus = visualEvents[index]


            val letter = numberToLetter(audioStimulus, _numSpokenLetters.value)
            ttsManager?.speak("$letter")
            updateGridColors(visualStimulus)
            _gameState.value = _gameState.value.copy(eventValue = index)
            delay(timeBetweenEvents.value)
            resetGridColors()
            delay(timeBetweenEvents.value)
        }
    }

    fun stopTTS() {
        ttsManager?.stop()  // Se till att stoppa TTS när det inte behövs
    }

    private fun resetGridColors() {
        _gridState.value = _gridState.value.map { Color.White }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GameApplication)
                GameVM(application.userPreferencesRespository, application.applicationContext)
            }
        }
    }

    init {
        viewModelScope.launch {
            userPreferencesRepository.highscore.collect {
                _highscore.value = it
            }

        }
        viewModelScope.launch {
            userPreferencesRepository.getPreferenceValue("n_back_level").collect { savedNBackLevel ->
                _nBack.value = savedNBackLevel
            }
        }
        viewModelScope.launch {
            // Hämta och uppdatera grid_size
            userPreferencesRepository.getPreferenceValue("grid_size").collect { savedGridSize ->
                _gridSize.value = savedGridSize
                // Update gridState size to match new gridSize
                _gridState.value = List(savedGridSize * savedGridSize) { Color.White }
            }
        }
        viewModelScope.launch {
            // Hämta och uppdatera events_per_round
            userPreferencesRepository.getPreferenceValue("events_per_round").collect { savedEventsPerRound ->
                _eventsPerRound.value = savedEventsPerRound
            }
        }
        viewModelScope.launch {
            // Hämta och uppdatera time_between_events
            userPreferencesRepository.getPreferenceValue("time_between_events").collect { savedTimeBetweenEvents ->
                _timeBetweenEvents.value = savedTimeBetweenEvents.toLong()
            }
        }
        viewModelScope.launch {
            // Hämta och uppdatera num_spoken_letters
            userPreferencesRepository.getPreferenceValue("num_spoken_letters").collect { savedNumSpokenLetters ->
                _numSpokenLetters.value = savedNumSpokenLetters
            }
        }

    }
}


// Class with the different game types
enum class GameType{
Audio,
Visual,
AudioVisual
}

data class GameState(
// You can use this state to push values from the VM to your UI.
val gameType: GameType = GameType.Visual,  // Type of the game
val eventValue: Int = -1,  // The value of the array string
)

enum class MatchFeedback {
NONE, CORRECT, WRONG
}

class FakeVM(
    override val correctAnswers: StateFlow<Int>,
    override val wrongAnswers: StateFlow<Int>
) : GameViewModel{
override val gameState: StateFlow<GameState>
    get() = MutableStateFlow(GameState()).asStateFlow()
override val score: StateFlow<Int>
    get() = MutableStateFlow(2).asStateFlow()
override val highscore: StateFlow<Int>
    get() = MutableStateFlow(42).asStateFlow()
override val nBack = TODO()
    override val gridState: StateFlow<List<Color>>
    get() = TODO("Not yet implemented")
    override val gridSize: StateFlow<Int>
        get() = TODO("Not yet implemented")

    override fun setGameType(gameType: GameType) {
}

    override fun setNBack(newNBack: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun startGame() {

}

override fun checkMatch(currentIndex: Int) {
}

    override fun checkMatch(currentIndex: Int, matchType: MatchType) {
        TODO("Not yet implemented")
    }
}