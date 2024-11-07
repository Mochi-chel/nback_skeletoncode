package mobappdev.example.nback_cimpl.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore by preferencesDataStore(name = "user_preferences")

object DataStoreManager {
    // Skapa en funktion som ger en DataStore-instans för användning
    fun getDataStore(context: Context) = context.dataStore
}

class UserPreferencesRepository(private val context: Context) {

    private val dataStore: DataStore<Preferences> = DataStoreManager.getDataStore(context) // Använd gemensam instans

    companion object {
        val HIGHSCORE = intPreferencesKey("highscore")
        val N_BACK_LEVEL = intPreferencesKey("n_back_level")
        val GRID_SIZE = intPreferencesKey("grid_size")
        val EVENTS_PER_ROUND = intPreferencesKey("events_per_round")
        val TIME_BETWEEN_EVENTS = intPreferencesKey("time_between_events")
        val NUM_SPOKEN_LETTERS = intPreferencesKey("num_spoken_letters")

        const val TAG = "UserPreferencesRepo"
    }

    fun getPreferenceValue(key: String): Flow<Int> {
        return when (key) {
            "n_back_level" -> dataStore.data
                .catch { emit(emptyPreferences()) }
                .map { preferences -> preferences[N_BACK_LEVEL] ?: 2 }
            "grid_size" -> dataStore.data
                .catch { emit(emptyPreferences()) }
                .map { preferences -> preferences[GRID_SIZE] ?: 3 }
            "events_per_round" -> dataStore.data
                .catch { emit(emptyPreferences()) }
                .map { preferences -> preferences[EVENTS_PER_ROUND] ?: 10 }
            "time_between_events" -> dataStore.data
                .catch { emit(emptyPreferences()) }
                .map { preferences -> preferences[TIME_BETWEEN_EVENTS] ?: 1000 }
            "num_spoken_letters" -> dataStore.data
                .catch { emit(emptyPreferences()) }
                .map { preferences -> preferences[NUM_SPOKEN_LETTERS] ?: 3 }
            else -> dataStore.data
                .catch { emit(emptyPreferences()) }
                .map { preferences -> preferences[N_BACK_LEVEL] ?: 2 }
        }
    }

    suspend fun savePreferenceValue(key: String, value: Int) {
        dataStore.edit { preferences ->
            when (key) {
                "n_back_level" -> preferences[N_BACK_LEVEL] = value
                "grid_size" -> preferences[GRID_SIZE] = value
                "events_per_round" -> preferences[EVENTS_PER_ROUND] = value
                "time_between_events" -> preferences[TIME_BETWEEN_EVENTS] = value
                "num_spoken_letters" -> preferences[NUM_SPOKEN_LETTERS] = value
            }
        }
    }

    val highscore: Flow<Int> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[HIGHSCORE] ?: 0
        }

    suspend fun saveHighScore(score: Int) {
        dataStore.edit { preferences ->
            preferences[HIGHSCORE] = score
        }
    }
}
