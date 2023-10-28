package com.example.doodl.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.doodl.data.repository.Repository
import com.example.doodl.ui.ColorButton
import com.example.doodl.ui.DrawCanvas
import com.example.doodl.ui.EraserButton
import com.example.doodl.util.generateBitmapFromPaths
import com.example.doodl.util.handleDrawingActivityTouchEvent
import com.example.doodl.viewmodel.CanvasViewModel
import com.example.doodl.viewmodel.CanvasViewModelFactory
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.AlphaTile
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController

// Composable functions for UI of each screen
@Composable
fun CanvasScreen(
    navBarHeight: Int
) {
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
fun CanvasActivity(
    viewModel: CanvasViewModel,
    navBarHeight: Int,
    selectedColor: Color,
    updateSelectedColor: (Color) -> Unit
) {
    val paths = remember { mutableStateListOf<Triple<List<Offset>, Color, Float>>() }
    val currentPath = remember { mutableStateListOf<Offset>() }
    var canvasSize by remember { mutableStateOf(IntSize(0, 0)) }
    var brushSize by remember { mutableFloatStateOf(5f) }
    val redoPaths = remember { mutableStateListOf<Triple<List<Offset>, Color, Float>>() }

    var currentColorButton = remember { mutableStateOf<Color?>(null) }
    var button1Color by remember { mutableStateOf(Color.Black) }
    var button2Color by remember { mutableStateOf(Color.Magenta) }
    var button3Color by remember { mutableStateOf(Color.Red) }
    var button4Color by remember { mutableStateOf(Color.Green) }
    var button5Color by remember { mutableStateOf(Color.Blue) }

    val colorToButtonMap = mapOf(
        button1Color to "button1Color",
        button2Color to "button2Color",
        button3Color to "button3Color",
        button4Color to "button4Color",
        button5Color to "button5Color"
    )

    var selectedButtonId by remember { mutableStateOf("") }
    val updateSelectedButtonId: (String) -> Unit = { newId ->
        selectedButtonId = newId
    }

    val controller = rememberColorPickerController()
    controller.setWheelColor(Color.DarkGray)
    var colorHexCode by remember { mutableStateOf("#FFFFFF") }
    var isColorPickerVisible = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Toolbar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(Color.DarkGray)
                .padding(10.dp)
                .zIndex(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconButton(onClick = {
                    if (!isColorPickerVisible.value && paths.isNotEmpty()) {
                        val lastPath = paths.removeLast()
                        redoPaths.add(lastPath)
                    }
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Undo")
                }
                IconButton(onClick = {
                    if (!isColorPickerVisible.value && redoPaths.isNotEmpty()) {
                        val lastRedoPath = redoPaths.removeLast()
                        paths.add(lastRedoPath)
                    }
                }) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Redo")
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        if (paths.isNotEmpty()) {
                            val bitmap = generateBitmapFromPaths(paths, canvasSize.width, canvasSize.height)
                            viewModel.uploadDrawing(bitmap)
                            paths.clear()
                        }
                    },
                    enabled = !isColorPickerVisible.value
                ) {
                    Text("Next", color = Color.Black)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val buttons = listOf(
                        Pair("button1Color", button1Color),
                        Pair("button2Color", button2Color),
                        Pair("button3Color", button3Color),
                        Pair("button4Color", button4Color),
                        Pair("button5Color", button5Color)
                    )

                    buttons.forEach { (buttonId, buttonColor) ->
                        ColorButton(
                            selectedButtonId = selectedButtonId,
                            buttonId = buttonId,
                            buttonColor = buttonColor,
                            onClick = {
                                updateSelectedColor(buttonColor)
                                updateSelectedButtonId(buttonId)
                            },
                            currentColorButton = currentColorButton,
                            isColorPickerVisible = isColorPickerVisible,
                            updateSelectedButtonId = updateSelectedButtonId
                        )
                    }
                    EraserButton(selectedButtonId, {
                        updateSelectedColor(Color.White)
                        updateSelectedButtonId("eraserButton")
                    }, isColorPickerVisible)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Slider(
                    value = brushSize,
                    onValueChange = { brushSize = it },
                    valueRange = 5f..100f
                )
            }
        }
        // Canvas
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        val height = coordinates.size.height - navBarHeight
                        val width = coordinates.size.width
                        canvasSize = IntSize(width, height)
                    }
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
                DrawCanvas(paths, currentPath, selectedColor, brushSize)
            }
            // Color Picker
            if (isColorPickerVisible.value) {
               Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .background(Color.DarkGray)
                        .zIndex(1f)
                        .padding(bottom = navBarHeight.dp, start = 10.dp, end = 10.dp, top = 10.dp),
                   horizontalAlignment = Alignment.CenterHorizontally,
                   verticalArrangement = Arrangement.Top
               ) {
                   HsvColorPicker(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        controller = controller,
                        initialColor = currentColorButton.value ?: Color.White,
                        onColorChanged = { colorEnvelope: ColorEnvelope ->
                            val newColor: Color = colorEnvelope.color
                            colorHexCode = colorEnvelope.hexCode
                            if (currentColorButton.value != null) {
                                updateSelectedColor(newColor)
                                val buttonToUpdate = colorToButtonMap[currentColorButton.value]
                                when (buttonToUpdate) {
                                    "button1Color" -> button1Color = newColor
                                    "button2Color" -> button2Color = newColor
                                    "button3Color" -> button3Color = newColor
                                    "button4Color" -> button4Color = newColor
                                    "button5Color" -> button5Color = newColor
                                }
                            }
                        }
                   )
                   Spacer(modifier = Modifier.height(10.dp))
                   AlphaSlider(
                       initialColor = currentColorButton.value ?: Color.White,
                       wheelColor = Color.DarkGray,
                       modifier = Modifier
                           .fillMaxWidth()
                           .padding(10.dp)
                           .height(35.dp),
                       controller = controller,
                   )
                   BrightnessSlider(
                       initialColor = currentColorButton.value ?: Color.White,
                       wheelColor = Color.DarkGray,
                       modifier = Modifier
                           .fillMaxWidth()
                           .padding(10.dp)
                           .height(35.dp),
                       controller = controller,
                   )
                   Spacer(modifier = Modifier.height(10.dp))
                   Text("#$colorHexCode", color = selectedColor)
                   AlphaTile(
                       modifier = Modifier
                           .size(80.dp)
                           .clip(RoundedCornerShape(6.dp)),
                       controller = controller
                   )
                   Spacer(modifier = Modifier.height(10.dp))
                   Button(onClick = {
                       isColorPickerVisible.value = false
                       currentColorButton.value = null
                   }) {
                       Text("Confirm",  color = Color.Black)
                   }
               }
            }
        }
    }
}