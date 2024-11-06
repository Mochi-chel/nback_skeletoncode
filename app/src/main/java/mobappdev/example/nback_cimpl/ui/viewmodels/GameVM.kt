package mobappdev.example.nback_cimpl.ui.viewmodels

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
    val nBack: Int
    val gridState: StateFlow<List<Color>>

    fun setGameType(gameType: GameType)
    fun startGame()

    fun checkMatch(currentIndex: Int)
}





class GameVM(
    private val userPreferencesRepository: UserPreferencesRepository
): GameViewModel, ViewModel() {
    private val _gameState = MutableStateFlow(GameState())
    override val gameState: StateFlow<GameState>
        get() = _gameState.asStateFlow()

    private val _score = MutableStateFlow(0)
    override val score: StateFlow<Int>
        get() = _score

    private val _highscore = MutableStateFlow(0)
    override val highscore: StateFlow<Int>
        get() = _highscore

    // nBack is currently hardcoded
    override val nBack: Int = 2

    private var job: Job? = null  // coroutine job for the game event
    private val eventInterval: Long = 2000L  // 2000 ms (2s)

    private val nBackHelper = NBackHelper()  // Helper that generate the event array
    private var currentIndex = 0
    private var events = emptyArray<Int>()  // Array with all events

    private val _matchFeedback = MutableStateFlow("")  // Håller feedback om rätt/fel
    val matchFeedback: StateFlow<String> = _matchFeedback.asStateFlow()

    private val _gridState = MutableStateFlow(List(9) { Color.White })
    override val gridState: StateFlow<List<Color>> = _gridState

    // Funktion för att uppdatera färgerna baserat på spelets logik
    private fun updateGridColors(value: Int) {
        _gridState.value = _gridState.value.mapIndexed { index, color ->
            if (index == value) Color.Blue else Color.White // Byt färg på den valda rutan
        }
    }

    // Uppdatera färger för en viss cell
    fun updateGridState(index: Int, color: Color) {
        _gridState.value = _gridState.value.toMutableList().also { it[index] = color }
    }

    override fun checkMatch(currentIndex: Int) {
        val stimulus = gameState.value.eventValue // Den aktuella stimulansen
        val previousStimulus = events.getOrNull(currentIndex - nBack) // Stimulus som visades n steg tidigare

        // En gemensam funktion för att jämföra stimuli oavsett speltyp
        val isMatch = stimulus == previousStimulus

        // Om stimulanserna matchar
        if (isMatch) {
            _gameState.value = _gameState.value.copy(matchFeedback = MatchFeedback.CORRECT) // Ge rätt feedback
            _score.value += 1 // Öka poängen
        } else {
            _gameState.value = _gameState.value.copy(matchFeedback = MatchFeedback.WRONG) // Ge fel feedback
        }
    }
/*override fun checkMatch() {
    *
   * Todo: This function should check if there is a match when the user presses a match button
     * Make sure the user can only register a match once for each event.

}*/


override fun setGameType(gameType: GameType) {
    // update the gametype in the gamestate
    _gameState.value = _gameState.value.copy(gameType = gameType)
}



override fun startGame() {
    job?.cancel()  // Cancel any existing game loop

    // Get the events from our C-model (returns IntArray, so we need to convert to Array<Int>)
    events = nBackHelper.generateNBackString(10, 9, 30, nBack).toList().toTypedArray()  // Todo Higher Grade: currently the size etc. are hardcoded, make these based on user input
    Log.d("GameVM", "The following sequence was generated: ${events.contentToString()}")

    job = viewModelScope.launch {
        when (gameState.value.gameType) {
            GameType.Audio -> runAudioGame()
            GameType.AudioVisual -> runAudioVisualGame()
            GameType.Visual -> runVisualGame(events)
        }
        // Todo: update the highscore
    }
}
private suspend fun runAudioGame() {
    for (value in events) {
        updateGridColors(value)
        _gameState.value = _gameState.value.copy(eventValue = value)
        delay(500)
        resetGridColors()
        delay(500)
    }
}

private suspend fun runVisualGame(events: Array<Int>){
    for (value in events) {
        updateGridColors(value)
        _gameState.value = _gameState.value.copy(eventValue = value)
        delay(500)
        resetGridColors()
        delay(500)
    }
}

private suspend fun runAudioVisualGame(){
    for (value in events) {
        updateGridColors(value)
        _gameState.value = _gameState.value.copy(eventValue = value)
        delay(500)
        resetGridColors()
        delay(500)
    }
}

private fun resetGridColors() {
    _gridState.value = _gridState.value.map { Color.White }
}

companion object {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val application = (this[APPLICATION_KEY] as GameApplication)
            GameVM(application.userPreferencesRespository)
        }
    }
}

init {
    // Code that runs during creation of the vm
    viewModelScope.launch {
        userPreferencesRepository.highscore.collect {
            _highscore.value = it
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
val matchFeedback: MatchFeedback = MatchFeedback.NONE
)

enum class MatchFeedback {
NONE, CORRECT, WRONG
}

class FakeVM: GameViewModel{
override val gameState: StateFlow<GameState>
    get() = MutableStateFlow(GameState()).asStateFlow()
override val score: StateFlow<Int>
    get() = MutableStateFlow(2).asStateFlow()
override val highscore: StateFlow<Int>
    get() = MutableStateFlow(42).asStateFlow()
override val nBack: Int
    get() = 2
override val gridState: StateFlow<List<Color>>
    get() = TODO("Not yet implemented")

override fun setGameType(gameType: GameType) {
}

override fun startGame() {

}

override fun checkMatch(currentIndex: Int) {
}
}