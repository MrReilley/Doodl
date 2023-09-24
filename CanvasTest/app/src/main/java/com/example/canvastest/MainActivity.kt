package com.example.canvastest

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.canvastest.ui.theme.CanvasTestTheme
import kotlinx.coroutines.flow.*


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CanvasTestTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyApp()
                    ElevatedButtonExample {
                        // Handle button click here
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CanvasTestTheme {
        ElevatedButtonExample {

        }
    }
}

@Composable
fun MyApp() {
    DrawingActivity()
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DrawingActivity() {
    // Remembered state list holding each completed path (list of Offset)
    val paths = remember { mutableStateListOf<List<Offset>>() }

    // Remembered state list for the current drawing path (list of Offset)
    val currentPath = remember { mutableStateListOf<Offset>() }

    val drawStroke = Stroke(width = 16f, cap = StrokeCap.Round)

    // Create a box that fills the available space
    Box(
        modifier = Modifier
            .fillMaxSize()
            // Capture pointer (touch) input events
            .pointerInteropFilter { event ->
                // Extract action and position from the event
                val action = event.action
                val offset = Offset(event.x, event.y)

                // Handle different touch events
                when (action) {
                    // If starting to draw or drawing, add to current path
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                        currentPath.add(offset)
                        true
                    }
                    // If touch is released, add current path to completed paths
                    MotionEvent.ACTION_UP -> {
                        paths.add(currentPath.toList())
                        currentPath.clear()
                        true
                    }
                    // Ignore other touch events
                    else -> false
                }
            }
    ) {
        // Draw on a canvas that fills the available space
        Canvas(Modifier.fillMaxSize()) {
            // Draw each completed path
            paths.forEach { path ->
                // Convert list of points into drawable path
                val p = Path().apply {
                    moveTo(path.first().x, path.first().y)
                    path.forEach { lineTo(it.x, it.y) }
                }
                drawPath(path = p, color = Color.Black, style = drawStroke)
            }

            // Draw the current path if it exists
            if (currentPath.isNotEmpty()) {
                // Convert current path into drawable path
                val p = Path().apply {
                    moveTo(currentPath.first().x, currentPath.first().y)
                    currentPath.forEach { lineTo(it.x, it.y) }
                }
                drawPath(path = p, color = Color.Black, style = drawStroke)
            }
        }
    }
}
@Composable
fun ElevatedButtonExample(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize() // Makes the box fill the entire screen
            .padding(16.dp), // Adds padding around the button
        contentAlignment = Alignment.BottomEnd // Aligns the content to the bottom end
    ) {
        ElevatedButton(
            onClick = { onClick() },
            modifier = Modifier
                .width(100.dp) // Sets a specific width
                .height(48.dp), //Sets a specific height
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = Color.Red // Sets the background color of the button
            )
        ) {
            Text("Save",
                color = Color.White // Sets the text color of the button
                )
        }
    }

}






