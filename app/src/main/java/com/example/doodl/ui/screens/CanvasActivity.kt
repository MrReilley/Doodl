package com.example.doodl.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.doodl.ui.colorButton
import com.example.doodl.ui.drawCanvas
import com.example.doodl.ui.eraserButton
import com.example.doodl.util.generateBitmapFromPaths
import com.example.doodl.util.handleDrawingActivityTouchEvent
import com.example.doodl.viewmodel.CanvasViewModel
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.AlphaTile
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController

// Composable functions for UI of each screen
@Composable
fun CanvasScreen(
    navController: NavController,
    navBarHeight: Int,
    canvasViewModel: CanvasViewModel
) {
    BackHandler {
        // Do nothing, effectively disabling the back button
    }
    var selectedColor by remember { mutableStateOf(Color.Black) }

    CanvasActivity(navController, canvasViewModel, navBarHeight,  selectedColor) { newColor ->
        selectedColor = newColor
    }
}
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CanvasActivity(
    navController: NavController,
    canvasViewModel: CanvasViewModel,
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
                            canvasViewModel.currentBitmap.value = bitmap
                            navController.navigate("postInfo")
                        }
                    },
                    enabled = !isColorPickerVisible.value && paths.isNotEmpty()
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
                        colorButton(
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
                    eraserButton(selectedButtonId, {
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
                drawCanvas(paths, currentPath, selectedColor, brushSize)
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PostInfoScreen(
    navController: NavController,
    canvasViewModel: CanvasViewModel
) {
    val bitmap = canvasViewModel.currentBitmap.value
    val context = LocalContext.current
    val selectedTags = remember { mutableStateListOf<String>() }
    val tagOptions = listOf(
        "Abstract",
        "Animal",
        "Anime",
        "Cute",
        "Dramatic",
        "Funny",
        "Scary",
        "Landscape",
        "Mysterious",
        "Nostalgic",
        "Realism",
        "Surreal"
    )
    var isUploading by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go Back")
                    }
                },
                backgroundColor = MaterialTheme.colorScheme.primary
            )
        }
    ) {
            innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.tertiary)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Image Preview
            if (bitmap != null) {
                val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
                val previewWidth = 200.dp
                val previewHeight = previewWidth / aspectRatio
                Box(
                    modifier = Modifier
                        .border(2.dp, Color.LightGray)
                        .width(previewWidth)
                        .height(previewHeight)
                ) {
                    Image(bitmap = bitmap.asImageBitmap(), contentDescription = "Preview")
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.Center,
                    maxItemsInEachRow = 3
                ) {
                    tagOptions.forEach { tag ->
                        TagButton(tag, selectedTags)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (bitmap != null) {
                            isUploading = true
                            canvasViewModel.uploadDrawing(bitmap) { success ->
                                isUploading = false
                                if (success) {
                                    canvasViewModel.clearCurrentBitmap()
                                    navController.navigate("feed")
                                    val message = "Post successful"
                                    val duration = Toast.LENGTH_SHORT
                                    Toast.makeText(context, message, duration).show()
                                } else {
                                    val message = "Post failed"
                                    val duration = Toast.LENGTH_SHORT
                                    Toast.makeText(context, message, duration).show()
                                }
                            }
                        }
                    },
                    enabled = !isUploading
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(color = Color.Black, strokeWidth = 2.dp)
                    } else {
                        Text("Share", color = Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun TagButton(tag: String, selectedTags: MutableList<String>) {
    val isSelected = remember { mutableStateOf(false) }
    Button(
        onClick = {
            isSelected.value = !isSelected.value
            if (isSelected.value) {
                selectedTags.add(tag)
            } else {
                selectedTags.remove(tag)
            }
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected.value) Color.Green else Color.LightGray
        ),
        modifier = Modifier.padding(4.dp)
    ) {
        Text(tag, color = Color.Black)
    }
}