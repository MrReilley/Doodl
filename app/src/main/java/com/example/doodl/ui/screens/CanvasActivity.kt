package com.example.doodl.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.doodl.R
import com.example.doodl.data.Repository
import com.example.doodl.ui.colorButton
import com.example.doodl.ui.drawCanvas
import com.example.doodl.util.generateBitmapFromPaths
import com.example.doodl.util.handleDrawingActivityTouchEvent
import com.example.doodl.viewmodel.CanvasViewModel
import com.example.doodl.viewmodel.CanvasViewModelFactory

// Composable functions for UI of each screen
@Composable
fun CanvasScreen(navBarHeight: Int) {
    BackHandler {
        // Do nothing, effectively disabling the back button
    }
    val repository = Repository()
    val canvasViewModel: CanvasViewModel = viewModel(factory = CanvasViewModelFactory(repository))
    var selectedColor by remember { mutableStateOf(Color.Black) }

    CanvasActivity(canvasViewModel, navBarHeight,  selectedColor) { newColor ->
        selectedColor = newColor
    }
}
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CanvasActivity(viewModel: CanvasViewModel,
                   navBarHeight: Int,
                   selectedColor: Color,
                   updateSelectedColor: (Color) -> Unit) {
    val paths = remember { mutableStateListOf<Triple<List<Offset>, Color, Float>>() }
    val currentPath = remember { mutableStateListOf<Offset>() }
    var canvasSize by remember { mutableStateOf(IntSize(0, 0)) }
    var brushSize by remember { mutableFloatStateOf(5f) }

    // For accessing android system's resources if saving to local storage
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .wrapContentHeight()
                .background(Color.DarkGray)
                .padding(10.dp)
                .zIndex(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                colorButton(selectedColor, Color.Black) { updateSelectedColor(Color.Black) }
                colorButton(selectedColor, Color.Magenta) { updateSelectedColor(Color.Magenta) }
                colorButton(selectedColor, Color.Red) { updateSelectedColor(Color.Red) }
                colorButton(selectedColor, Color.Green) { updateSelectedColor(Color.Green) }
                colorButton(selectedColor, Color.Blue) { updateSelectedColor(Color.Blue) }
                Button(
                    onClick = {
                        // Prevents empty canvas from getting uploaded
                        if (paths.isNotEmpty()) {
                            val bitmap = generateBitmapFromPaths(paths, canvasSize.width, canvasSize.height)
                            viewModel.uploadDrawing(bitmap)
                            paths.clear()
                        }
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
                        // Prevents empty canvas from getting downloaded
                        if (paths.isNotEmpty()) {
                            val bitmap = generateBitmapFromPaths(paths, canvasSize.width, canvasSize.height)
                            viewModel.saveBitmapToInternalStorage(bitmap, context)
                        }
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
        Column(
            modifier = Modifier.fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    val height = coordinates.size.height - navBarHeight
                    val width = coordinates.size.width
                    canvasSize = IntSize(width, height)
                }
                // Captures paths with touch event handler
                .pointerInteropFilter { event ->
                    handleDrawingActivityTouchEvent(
                        event,
                        currentPath,
                        paths,
                        selectedColor,
                        brushSize
                    )
                }
        ) {
            // Draw the canvas using paths captured from touch event handler
            drawCanvas(paths, currentPath, selectedColor, brushSize)
        }
    }
}