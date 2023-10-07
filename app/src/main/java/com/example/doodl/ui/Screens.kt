package com.example.doodl.ui


//import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import com.example.doodl.util.generateBitmapFromPaths
import com.example.doodl.util.handleDrawingActivityTouchEvent
import com.example.doodl.viewmodel.CanvasViewModel

// Composable functions for UI of each screen

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CanvasActivity(
    viewModel: CanvasViewModel,
    selectedColor: Color,
    updateSelectedColor: (Color) -> Unit) {
    val paths = remember { mutableStateListOf<Pair<List<Offset>, Color>>() }
    val currentPath = remember { mutableStateListOf<Offset>() }
    var canvasSize by remember { mutableStateOf(IntSize(0, 0)) }

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
                handleDrawingActivityTouchEvent(event, currentPath, paths, selectedColor)
            }
    ) {
        // Draw the canvas using paths captured from touch event handler
        drawCanvas(paths, currentPath, selectedColor)
    }

    val canvasWidth = canvasSize.width
    val canvasHeight = canvasSize.height

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Button(
            onClick = {
                val bitmap = generateBitmapFromPaths(paths, canvasWidth, canvasHeight)
                viewModel.uploadDrawing(bitmap)
                viewModel.saveBitmapToInternalStorage(bitmap, context)
            },
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            Text("Upload/Save")
        }
    }

}
//-------------------------Canvas/Post Screen----------------------------

//-----------------------------Feed Screen-------------------------------
/*@Composable
fun FeedScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Magenta),
        contentAlignment = Alignment.Center
    ) {
        //Scrollable Lists
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(20){
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(70.dp))
            }
        }
    }
}*/

/*@Composable
fun doodlCard(){}*/


