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
import androidx.compose.material3.Switch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.collectAsState
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first


class SettingsActivity : AppCompatActivity() {
    private lateinit var preferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Skapa en instans av UserPreferencesRepository
        preferencesRepository = UserPreferencesRepository(applicationContext)

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
                preferencesRepository = preferencesRepository,
                valueRange = 10f..24f
            )

            // Tid mellan events
            SettingSlider(
                label = "Time Between Events (seconds)",
                valueKey = "time_between_events",
                preferencesRepository = preferencesRepository,
                valueRange = 500f..2500f
            )

            // n-Back nivå
            SettingSlider(
                label = "n-Back Level",
                valueKey = "n_back_level",
                preferencesRepository = preferencesRepository,
                valueRange = 1f..10f
            )

            // Antal tal som ska talas
            SettingSlider(
                label = "Number of Spoken Letters (Auditory Stimuli)",
                valueKey = "num_spoken_letters",
                preferencesRepository = preferencesRepository,
                valueRange = 1f..5f
            )

            // Gridstorlek
            GridSizeSwitch(preferencesRepository)

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
    fun GridSizeSwitch(preferencesRepository: UserPreferencesRepository) {
        val currentValue = preferencesRepository.getPreferenceValue("grid_size").collectAsState(initial = 3).value
        val isFiveByFive = currentValue == 5

        Column {
            Text("Grid Size (Visual Stimuli): ${if (isFiveByFive) "5x5" else "3x3"}")

            Switch(
                checked = isFiveByFive,
                onCheckedChange = { isChecked ->
                    val newSize = if (isChecked) 5 else 3
                    lifecycleScope.launch {
                        preferencesRepository.savePreferenceValue("grid_size", newSize)
                    }
                }
            )
        }
    }


    @Composable
    fun SettingSlider(
        label: String,
        valueKey: String,
        preferencesRepository: UserPreferencesRepository,
        valueRange: ClosedFloatingPointRange<Float>,  // Lägg till detta parameter för att ange intervall
        isInteger: Boolean = false
    ) {
        val currentValue = preferencesRepository.getPreferenceValue(valueKey).collectAsState(initial = 10).value

        Column(modifier = Modifier.padding(16.dp)) {
            Text(label)

            Slider(
                value = currentValue.toFloat(),
                onValueChange = { value ->
                    lifecycleScope.launch {
                        // Konvertera till Int om det är en Integer slider
                        val finalValue = if (isInteger) value.toInt() else value.toLong().toInt() // Om Long, spara som Int
                        preferencesRepository.savePreferenceValue(valueKey, finalValue)
                    }
                },
                valueRange = valueRange,
                modifier = Modifier.padding(8.dp)
            )

            Text("Current Value: $currentValue")
        }
    }

    // Spara inställningar till DataStore
    private suspend fun saveSettings(preferencesRepository: UserPreferencesRepository, context: Context) {
        try {
            // Hämta de aktuella värdena från UI genom att samla in flödena
            val numEvents = preferencesRepository.getPreferenceValue("events_per_round").first() // Samla in värdet från flödet
            val timeBetweenEvents = preferencesRepository.getPreferenceValue("time_between_events").first()
            val nBackLevel = preferencesRepository.getPreferenceValue("n_back_level").first()
            val gridSize = preferencesRepository.getPreferenceValue("grid_size").first()
            val numSpokenLetters = preferencesRepository.getPreferenceValue("num_spoken_letters").first()

            // Spara de hämtade värdena
            preferencesRepository.savePreferenceValue("events_per_round", numEvents)
            preferencesRepository.savePreferenceValue("time_between_events", timeBetweenEvents)
            preferencesRepository.savePreferenceValue("n_back_level", nBackLevel)
            preferencesRepository.savePreferenceValue("grid_size", gridSize)
            preferencesRepository.savePreferenceValue("num_spoken_letters", numSpokenLetters)

            println("n-Back Level saved: $nBackLevel")
            println("Grid Size saved: $gridSize")

            // Visa en toast om att inställningarna har sparats
            Toast.makeText(context, "Settings saved!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error saving settings", Toast.LENGTH_SHORT).show()
        }
    }


}
