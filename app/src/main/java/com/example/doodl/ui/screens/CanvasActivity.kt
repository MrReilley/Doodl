package com.example.doodl.ui.screens

import com.example.doodl.R
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import com.example.doodl.util.generateBitmapFromPaths
import com.example.doodl.util.handleDrawingActivityTouchEvent
import com.example.doodl.viewmodel.CanvasViewModel
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.doodl.ui.colorButton
import com.example.doodl.ui.drawCanvas

// Composable functions for UI of each screen

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CanvasActivity(viewModel: CanvasViewModel,
                   selectedColor: Color,
                   updateSelectedColor: (Color) -> Unit) {
    val paths = remember { mutableStateListOf<Triple<List<Offset>, Color, Float>>() }
    val currentPath = remember { mutableStateListOf<Offset>() }
    var canvasSize by remember { mutableStateOf(IntSize(0, 0)) }
    var brushSize by remember { mutableStateOf(5f) }

    // For accessing android system's resources if saving to local storage
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Updates canvas sized based on size of Box layout
            .onGloballyPositioned { coordinates ->
                canvasSize = coordinates.size
            }
            // Captures paths with touch event handler
            .pointerInteropFilter { event ->
                handleDrawingActivityTouchEvent(event, currentPath, paths, selectedColor, brushSize)
            }
    ) {
        // Draw the canvas using paths captured from touch event handler
        drawCanvas(paths, currentPath, selectedColor, brushSize)
    }

    val canvasWidth = canvasSize.width
    val canvasHeight = canvasSize.height

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .align(Alignment.TopCenter)
                .background(Color.DarkGray)
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                colorButton(selectedColor, Color.Black) { updateSelectedColor(Color.Black) }
                colorButton(selectedColor, Color.White) { updateSelectedColor(Color.White) }
                colorButton(selectedColor, Color.Red) { updateSelectedColor(Color.Red) }
                colorButton(selectedColor, Color.Green) { updateSelectedColor(Color.Green) }
                colorButton(selectedColor, Color.Blue) { updateSelectedColor(Color.Blue) }
                Button(
                    onClick = {
                        val bitmap = generateBitmapFromPaths(paths, canvasWidth, canvasHeight)
                        viewModel.uploadDrawing(bitmap)
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.uploadicon),
                        contentDescription = "Upload"
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Slider(
                    value = brushSize,
                    onValueChange = { brushSize = it },
                    valueRange = 5f..50f,
                    modifier = Modifier.weight(2f)
                )
                Button(
                    onClick = {
                        val bitmap = generateBitmapFromPaths(paths, canvasWidth, canvasHeight)
                        viewModel.saveBitmapToInternalStorage(bitmap, context)
                    },
                    modifier = Modifier.width(72.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.downloadicon),
                        contentDescription = "Download"
                    )
                }
            }
        }
    }
}


