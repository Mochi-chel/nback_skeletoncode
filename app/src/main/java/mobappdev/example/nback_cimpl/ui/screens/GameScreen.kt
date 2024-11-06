package mobappdev.example.nback_cimpl.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mobappdev.example.nback_cimpl.ui.screens.NBackGrid
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import mobappdev.example.nback_cimpl.ui.viewmodels.GameType
import mobappdev.example.nback_cimpl.ui.viewmodels.GameViewModel
import mobappdev.example.nback_cimpl.ui.viewmodels.MatchFeedback

@Composable
fun GameScreen(
    vm: GameViewModel,
    onGameEnd: () -> Unit
)
{

    LaunchedEffect(Unit) {
        vm.startGame()
    }

    val gameState by vm.gameState.collectAsState()
    val score by vm.score.collectAsState()
    val correctAnswers by vm.correctAnswers.collectAsState()
    val wrongAnswers by vm.wrongAnswers.collectAsState()
    val gridState by vm.gridState.collectAsState()

    val currentIndex = gameState.eventValue

    // Avsluta spelet-knapp
    Button(onClick = {
        onGameEnd()
    }) {
        Text("End Game")
    }


    Text(
        modifier = Modifier.padding(48.dp),
        text = "nBack: ${vm.nBack.collectAsState().value}",
        style = MaterialTheme.typography.bodyLarge
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Visa aktuell poäng
        Text(
            text = "Score: $score",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Visa antal korrekta och felaktiga svar
        Row {
            Text("Correct: $correctAnswers", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.width(16.dp))
            Text("Wrong: $wrongAnswers", style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            contentAlignment = Alignment.Center
        ) {
            NBackGrid(gridState = gridState, modifier = Modifier.fillMaxHeight(0.6f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (gameState.gameType) {
            GameType.Visual -> {
                Button(
                    onClick = { vm.checkMatch(currentIndex) },
                    modifier = Modifier.size(150.dp)
                ) {
                    Text("Visual Match")
                }
            }
            GameType.Audio -> {
                Button(
                    onClick = { vm.checkMatch(currentIndex) },
                    modifier = Modifier.size(150.dp)
                ) {
                    Text("Audio Match")
                }
            }
            GameType.AudioVisual -> {
                Button(
                    onClick = { vm.checkMatch(currentIndex) },
                    modifier = Modifier.size(150.dp)
                ) {
                    Text("Visual Match")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { vm.checkMatch(currentIndex) },
                    modifier = Modifier.size(150.dp)
                ) {
                    Text("Audio Match")
                }
            }
        }

 /*       Spacer(modifier = Modifier.height(16.dp))
// Retry Button
        Button(
            onClick = {
                vm.startGame()  // Restart the game by calling startGame from ViewModel
            },
            modifier = Modifier.size(150.dp)
        ) {
            Text("Retry")
        }
*/
    }
}


