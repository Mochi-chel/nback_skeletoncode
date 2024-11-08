package mobappdev.example.nback_cimpl.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import mobappdev.example.nback_cimpl.R
import mobappdev.example.nback_cimpl.ui.viewmodels.FakeVM
import mobappdev.example.nback_cimpl.ui.viewmodels.GameType
import mobappdev.example.nback_cimpl.ui.viewmodels.GameViewModel

/**
 * This is the Home screen composable
 *
 * Currently this screen shows the saved highscore
 * It also contains a button which can be used to show that the C-integration works
 * Furthermore it contains two buttons that you can use to start a game
 *
 * Date: 25-08-2023
 * Version: Version 1.0
 * Author: Yeetivity
 *
 */

@Composable
fun HomeScreen(
    vm: GameViewModel,
    onStartGame: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val highscore by vm.highscore.collectAsState()  // Highscore is its own StateFlow
    val gameState by vm.gameState.collectAsState()
    val nBack by vm.nBack.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = onOpenSettings) {
                Text("Open Settings")
            }
            Text(
                modifier = Modifier.padding(32.dp),
                text = "High Score  $highscore",
                style = MaterialTheme.typography.headlineLarge
            )

            Text(
                modifier = Modifier.padding(16.dp),
                text = "nBack: ${vm.nBack.collectAsState().value}",
                style = MaterialTheme.typography.bodyLarge
            )


            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                // Minus Button
                Button(
                    onClick = {
                        val newValue = (nBack - 1).coerceIn(1, 5)
                        vm.setNBack(newValue)
                    },
                    modifier = Modifier.padding(end = 8.dp) // Adds some space between buttons
                ) {
                    Text(text = "-")
                }

                // Plus Button
                Button(
                    onClick = {
                        val newValue = (nBack + 1).coerceIn(1, 5)
                        vm.setNBack(newValue)
                    },
                    modifier = Modifier.padding(start = 8.dp) // Adds some space between buttons
                ) {
                    Text(text = "+")
                }
            }

            // Todo: You'll probably want to change this "BOX" part of the composable
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (gameState.eventValue != -1) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = "Current eventValue is: ${gameState.eventValue}",
                            textAlign = TextAlign.Center
                        )
                    }
                    Button(onClick = onStartGame) {//onClick = vm::startGame
                        Text(text = "Start Game")
                    }
                }
            }


            Text(
                modifier = Modifier.padding(16.dp),
                text = "Select Game Type".uppercase(),
                style = MaterialTheme.typography.displaySmall
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Button for selecting Audio-Visual Game
                Button(
                    onClick = {
                        vm.setGameType(GameType.AudioVisual) // Set the game type to AudioVisual
                        scope.launch {
                            snackBarHostState.showSnackbar(
                                message = "Audio-Visual Game Selected"
                            )
                        }
                    }) {
                    Text(text = "Audio-Visual Game")
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = {
                    vm.setGameType(GameType.Audio)// Todo: change this button behaviour
                    scope.launch {
                        snackBarHostState.showSnackbar(
                            message = "Audio Game Selected"
                        )
                    }
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.sound_on),
                        contentDescription = "Sound",
                        modifier = Modifier
                            .height(48.dp)
                            .aspectRatio(3f / 2f)
                    )
                }
                Button(
                    onClick = {
                        vm.setGameType(GameType.Visual)// Todo: change this button behaviour
                        scope.launch {
                            snackBarHostState.showSnackbar(
                                message = "Visual Game Selected",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }) {
                    Icon(
                        painter = painterResource(id = R.drawable.visual),
                        contentDescription = "Visual",
                        modifier = Modifier
                            .height(48.dp)
                            .aspectRatio(3f / 2f)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    // Since I am injecting a VM into my homescreen that depends on Application context, the preview doesn't work.
    Surface(){
        HomeScreen(
            FakeVM(
                correctAnswers = TODO(),
                wrongAnswers = TODO()
            ),
            onStartGame = TODO(),
            onOpenSettings = TODO()
        )
    }
}