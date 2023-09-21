package com.example.canvastest

import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.tooling.preview.Preview
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
        Greeting("Android")
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

    val drawStroke = Stroke(width = 5f, cap = StrokeCap.Round)

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





