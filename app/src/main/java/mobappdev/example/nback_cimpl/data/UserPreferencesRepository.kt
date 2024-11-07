package mobappdev.example.nback_cimpl.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * This repository provides a way to interact with the DataStore api,
 * with this API you can save key:value pairs
 *
 * Currently this file contains only one thing: getting the highscore as a flow
 * and writing to the highscore preference.
 * (a flow is like a waterpipe; if you put something different in the start,
 * the end automatically updates as long as the pipe is open)
 *
 * Date: 25-08-2023
 * Version: Skeleton code version 1.0
 * Author: Yeetivity
 *
 */

class UserPreferencesRepository (
    private val dataStore: DataStore<Preferences>

){
    private companion object {
        val HIGHSCORE = intPreferencesKey("highscore")
        val NUM_EVENTS = intPreferencesKey("num_events")  // Nyckel för att spara antalet händelser
        val TIME_BETWEEN_EVENTS = intPreferencesKey("time_between_events") // Nyckel för tid mellan händelser
        val N_BACK_LEVEL = intPreferencesKey("n_back_level")  // Nyckel för att spara n-back nivå
        val GRID_SIZE = intPreferencesKey("grid_size") // Nyckel för att spara gridstorlek
        val NUM_SPOKEN_LETTERS = intPreferencesKey("num_spoken_letters") // Nyckel för att spara antalet talade bokstäver
        const val TAG = "UserPreferencesRepo"
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

    // Lägg till metoder för att hämta inställningar
    val numEvents: Flow<Int> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences ->
            preferences[NUM_EVENTS] ?: 10 // Default: 10 händelser
        }

    val timeBetweenEvents: Flow<Int> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences ->
            preferences[TIME_BETWEEN_EVENTS] ?: 1000 // Default: 1000 ms (1 sekund)
        }

    val nBackLevel: Flow<Int> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences ->
            preferences[N_BACK_LEVEL] ?: 2 // Default: 2-back
        }

    val gridSize: Flow<Int> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences ->
            preferences[GRID_SIZE] ?: 3 // Default: 3x3 grid
        }

    val numSpokenLetters: Flow<Int> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences ->
            preferences[NUM_SPOKEN_LETTERS] ?: 1 // Default: 1 bokstav
        }

    suspend fun saveHighScore(score: Int) {
        dataStore.edit { preferences ->
            preferences[HIGHSCORE] = score
        }
    }
    suspend fun saveNumEvents(numEvents: Int) {
        dataStore.edit { preferences ->
            preferences[NUM_EVENTS] = numEvents
        }
    }

    suspend fun saveTimeBetweenEvents(time: Int) {
        dataStore.edit { preferences ->
            preferences[TIME_BETWEEN_EVENTS] = time
        }
    }

    suspend fun saveNBackLevel(level: Int) {
        dataStore.edit { preferences ->
            preferences[N_BACK_LEVEL] = level
        }
    }

    suspend fun saveGridSize(size: Int) {
        dataStore.edit { preferences ->
            preferences[GRID_SIZE] = size
        }
    }

    suspend fun saveNumSpokenLetters(numLetters: Int) {
        dataStore.edit { preferences ->
            preferences[NUM_SPOKEN_LETTERS] = numLetters
        }
    }
}