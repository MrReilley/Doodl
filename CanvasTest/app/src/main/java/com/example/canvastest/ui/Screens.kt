package com.example.canvastest.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.canvastest.util.handleTouchEvent

// Composable functions for each screen

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DrawingActivity(selectedColor: Color, updateSelectedColor: (Color) -> Unit) {
    val paths = remember { mutableStateListOf<Pair<List<Offset>, Color>>() }
    val currentPath = remember { mutableStateListOf<Offset>() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInteropFilter { event ->
                handleTouchEvent(event, currentPath, paths, selectedColor)
            }
    ) {
        drawCanvas(paths, currentPath, selectedColor)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.DarkGray)
                .align(Alignment.TopCenter)
        ) {
            Row (modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                colorButton(selectedColor, Color.Black) { updateSelectedColor(Color.Black) }
                colorButton(selectedColor, Color.White) { updateSelectedColor(Color.White) }
                colorButton(selectedColor, Color.Red) { updateSelectedColor(Color.Red) }
                colorButton(selectedColor, Color.Green) { updateSelectedColor(Color.Green) }
                colorButton(selectedColor, Color.Blue) { updateSelectedColor(Color.Blue) }
                Button(onClick = { /*TODO*/ }) {
                    Text(text = "Done", fontSize = 10.sp)
                }
            }
        }
    }
}
