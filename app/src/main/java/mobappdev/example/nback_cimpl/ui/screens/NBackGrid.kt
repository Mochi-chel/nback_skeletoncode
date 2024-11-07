package mobappdev.example.nback_cimpl.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun NBackGridSelector(gridState: List<Color>, gridSize: Int, modifier: Modifier = Modifier) {
    when (gridSize) {
        3 -> NBackGrid3x3(gridState, modifier)
        5 -> NBackGrid5x5(gridState, modifier)
        else -> {
            // Om gridSize inte är 3 eller 5, visa ett tomt eller ett fallback-grid
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text("Unsupported grid size")
            }
        }
    }
}


@Composable
fun NBackGrid3x3(gridState: List<Color>, modifier: Modifier = Modifier) {
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

@Composable
fun NBackGrid5x5(gridState: List<Color>, modifier: Modifier = Modifier) {
    val boxSize = 60.dp // Mindre storlek på varje ruta för 5x5
    val spacing = 6.dp // Mindre avstånd mellan rutorna för 5x5

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.wrapContentSize(),
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            for (row in 0 until 5) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    for (col in 0 until 5) {
                        val index = row * 5 + col
                        Box(
                            modifier = Modifier
                                .size(boxSize)
                                .background(gridState.getOrNull(index) ?: Color.Gray)
                                .border(1.dp, Color.Black)
                        )
                    }
                }
            }
        }
    }
}