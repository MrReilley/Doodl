package com.example.doodl.ui

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
import com.example.doodl.util.handleDrawingActivityTouchEvent

// Composable functions for UI of each screen

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CanvasActivity(selectedColor: Color, updateSelectedColor: (Color) -> Unit) {
    val paths = remember { mutableStateListOf<Pair<List<Offset>, Color>>() }
    val currentPath = remember { mutableStateListOf<Offset>() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInteropFilter { event ->
                handleDrawingActivityTouchEvent(event, currentPath, paths, selectedColor)
            }
    ) {
        drawCanvas(paths, currentPath, selectedColor)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.DarkGray)
                .align(Alignment.TopCenter)
        ) {}

    }
}


