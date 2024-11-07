package mobappdev.example.nback_cimpl

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import mobappdev.example.nback_cimpl.data.UserPreferencesRepository
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Card
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.collectAsState
import androidx.datastore.preferences.preferencesDataStore

class SettingsActivity : AppCompatActivity() {

    private lateinit var preferencesRepository: UserPreferencesRepository
    private val Context.dataStore by preferencesDataStore(name = "user_preferences")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Skapa en instans av UserPreferencesRepository
        preferencesRepository = UserPreferencesRepository(applicationContext.dataStore)

        // Ställ in Content för inställningsskärmen
        setContent {
            SettingsScreen(preferencesRepository)
        }
    }

    @Composable
    fun SettingsScreen(preferencesRepository: UserPreferencesRepository) {
        val context = LocalContext.current

        Column(modifier = Modifier.padding(16.dp)) {
            // Rubrik för inställningar
            Text("Settings", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)

            // Antal events per runda
            SettingSlider(
                label = "Number of Events in Round",
                valueKey = "events_per_round",
                preferencesRepository = preferencesRepository
            )

            // Tid mellan events
            SettingSlider(
                label = "Time Between Events (seconds)",
                valueKey = "time_between_events",
                preferencesRepository = preferencesRepository
            )

            // n-Back nivå
            SettingSlider(
                label = "n-Back Level",
                valueKey = "n_back_level",
                preferencesRepository = preferencesRepository
            )

            // Gridstorlek
            SettingSlider(
                label = "Grid Size (Visual Stimuli)",
                valueKey = "grid_size",
                preferencesRepository = preferencesRepository
            )

            // Antal tal som ska talas
            SettingSlider(
                label = "Number of Spoken Letters (Auditory Stimuli)",
                valueKey = "num_spoken_letters",
                preferencesRepository = preferencesRepository
            )

            // Spara inställningar-knapp
            Button(onClick = {
                // Använd lifecycleScope.launch för att köra suspend-funktionerna asynkront
                lifecycleScope.launch {
                    saveSettings(preferencesRepository, context)
                }
            }) {
                Text("Save Settings")
            }
        }
    }

    @Composable
    fun SettingSlider(
        label: String,
        valueKey: String,
        preferencesRepository: UserPreferencesRepository
    ) {
        // Hämta nuvarande värde från DataStore som Flow och omvandla till en stat
        val currentValue = when (valueKey) {
            "events_per_round" -> preferencesRepository.numEvents.collectAsState(initial = 10).value
            "time_between_events" -> preferencesRepository.timeBetweenEvents.collectAsState(initial = 1000).value
            "n_back_level" -> preferencesRepository.nBackLevel.collectAsState(initial = 2).value
            "grid_size" -> preferencesRepository.gridSize.collectAsState(initial = 3).value
            "num_spoken_letters" -> preferencesRepository.numSpokenLetters.collectAsState(initial = 1).value
            else -> 10 // Fallback-värde
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(label)

            // Slider för inställningar
            Slider(
                value = currentValue.toFloat(),
                onValueChange = { value ->
                    // Spara det nya värdet till DataStore
                    when (valueKey) {
                        "events_per_round" -> lifecycleScope.launch { preferencesRepository.saveNumEvents(value.toInt()) }
                        "time_between_events" -> lifecycleScope.launch { preferencesRepository.saveTimeBetweenEvents(value.toInt()) }
                        "n_back_level" -> lifecycleScope.launch { preferencesRepository.saveNBackLevel(value.toInt()) }
                        "grid_size" -> lifecycleScope.launch { preferencesRepository.saveGridSize(value.toInt()) }
                        "num_spoken_letters" -> lifecycleScope.launch { preferencesRepository.saveNumSpokenLetters(value.toInt()) }
                    }
                },
                valueRange = 1f..10f, // Här kan du justera värdeintervallet efter behov
                modifier = Modifier.padding(8.dp)
            )

            Text("Current Value: $currentValue")
        }
    }

    // Spara inställningar till DataStore
    private suspend fun saveSettings(preferencesRepository: UserPreferencesRepository, context: Context) {
        try {
            preferencesRepository.saveNumEvents(20)
            preferencesRepository.saveTimeBetweenEvents(10) // i sekunder
            preferencesRepository.saveNBackLevel(2)
            preferencesRepository.saveGridSize(3)
            preferencesRepository.saveNumSpokenLetters(1)

            Toast.makeText(context, "Settings saved!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error saving settings", Toast.LENGTH_SHORT).show()
        }
    }
}
