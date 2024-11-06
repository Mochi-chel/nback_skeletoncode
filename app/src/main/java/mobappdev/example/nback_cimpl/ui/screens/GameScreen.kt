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
import mobappdev.example.nback_cimpl.ui.viewmodels.GameType
import mobappdev.example.nback_cimpl.ui.viewmodels.GameViewModel
import mobappdev.example.nback_cimpl.ui.viewmodels.MatchFeedback

@Composable
fun GameScreen(
    vm: GameViewModel, // Din ViewModel för spelet
    onGameEnd: () -> Unit  // En callback för att gå tillbaka till hemskärmen efter spelet är klart
) {

    LaunchedEffect(Unit) {
        vm.startGame()
    }

    val gameState by vm.gameState.collectAsState()
    val score by vm.score.collectAsState()
    val gridState by vm.gridState.collectAsState()

    val currentIndex = gameState.eventValue

    // Avsluta spelet-knapp
    Button(onClick = {
        onGameEnd() // Navigera tillbaka till startsidan
    }) {
        Text("End Game")
    }
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            contentAlignment = Alignment.Center
        ) {
            NBackGrid(gridState = gridState, modifier = Modifier.fillMaxHeight(0.6f))  // Ge NBackGrid ett begränsat höjdutrymme
        }


        Spacer(modifier = Modifier.height(16.dp))

        when (gameState.gameType) {
            // För VisualGame, visa endast Visual Match-knappen
            GameType.Visual -> {
                Button(
                    onClick = { vm.checkMatch(currentIndex) },
                    modifier = Modifier
                        .size(150.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (gameState.matchFeedback) {
                            MatchFeedback.CORRECT -> Color.Green
                            MatchFeedback.WRONG -> Color.Red
                            else -> Color(0xFF2196F3)
                        },
                        contentColor = Color.White
                    )
                ) {
                    Text("Visual Match")
                }
            }
            // För AudioGame, visa endast Audio Match-knappen
            GameType.Audio -> {
                Button(
                    onClick = { vm.checkMatch(currentIndex) },
                    modifier = Modifier.size(150.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (gameState.matchFeedback) {
                            MatchFeedback.CORRECT -> Color.Green
                            MatchFeedback.WRONG -> Color.Red
                            else -> Color(0xFF2196F3)
                        },
                        contentColor = Color.White
                    )
                ) {
                    Text("Audio Match")
                }
            }
            // För AudioVisualGame, visa både Audio och Visual Match-knappar
            GameType.AudioVisual -> {
                // Visual Match-knapp
                Button(
                    onClick = { vm.checkMatch(currentIndex) },
                    modifier = Modifier.size(150.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (gameState.matchFeedback) {
                            MatchFeedback.CORRECT -> Color.Green
                            MatchFeedback.WRONG -> Color.Red
                            else -> Color(0xFF2196F3)
                        },
                        contentColor = Color.White
                    )
                ) {
                    Text("Visual Match")
                }
                Spacer(modifier = Modifier.height(8.dp))  // Avstånd mellan knapparna
                // Audio Match-knapp
                Button(
                    onClick = { vm.checkMatch(currentIndex) },
                    modifier = Modifier.size(150.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (gameState.matchFeedback) {
                            MatchFeedback.CORRECT -> Color.Green
                            MatchFeedback.WRONG -> Color.Red
                            else -> Color(0xFF2196F3)
                        },
                        contentColor = Color.White
                    )
                ) {
                    Text("Audio Match")
                }
            }


        }
    }
}
