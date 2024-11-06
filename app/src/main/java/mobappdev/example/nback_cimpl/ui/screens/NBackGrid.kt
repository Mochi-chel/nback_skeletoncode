package mobappdev.example.nback_cimpl.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun NBackGrid(gridState: List<Color>, modifier: Modifier = Modifier) {
    val boxSize = 100.dp // Storleken på varje ruta
    val spacing = 8.dp // Avstånd mellan rutorna

    // Omge den med en Box för att centrera
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center // Centrera innehållet i Box
    ) {
        Column(
            modifier = Modifier.wrapContentSize(), // Se till att Column tar så lite plats som möjligt
            verticalArrangement = Arrangement.spacedBy(spacing) // Jämnt vertikalt avstånd mellan rader
        ) {
            // Skapa 3 rader
            for (row in 0 until 3) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing) // Jämnt horisontellt avstånd mellan kolumner
                ) {
                    // Skapa 3 kolumner
                    for (col in 0 until 3) {
                        val index = row * 3 + col
                        Box(
                            modifier = Modifier
                                .size(boxSize)
                                .background(gridState[index])
                                .border(1.dp, Color.Black) // Tydlig avgränsning mellan rutorna
                        )
                    }
                }
            }
        }
    }
}
