package com.example.doodl.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.doodl.util.COMMON_STROKE_WIDTH

// Composable functions for reusable UI components

@Composable
fun drawCanvas(paths: List<Pair<List<Offset>, Color>>,
               currentPath: List<Offset>,
               selectedColor: Color) {
    val drawStroke = Stroke(width = COMMON_STROKE_WIDTH, cap = StrokeCap.Round)

    Canvas(Modifier.fillMaxSize()) {
        // Redraws previous canvas paths user has completed drawing to ensure all paths reapper if UI updates
        // For each path in paths, spilt it into offsets, Color
        paths.forEach { (offsets, color) ->
            val graphicalPath = Path().apply {
                // Sets start point to first offset in list
                moveTo(offsets.first().x, offsets.first().y)
                // For each offset in path, draw a line to the next point
                offsets.forEach { lineTo(it.x, it.y) }
            }
            drawPath(path = graphicalPath, color = color, style = drawStroke)
        }
        // Draws canvas path currently being drawn if there is an active path
        if (currentPath.isNotEmpty()) {
            val activeGraphicalPath = Path().apply {
                // Sets start point to first point in current path
                moveTo(currentPath.first().x, currentPath.first().y)
                // For each offset in currentPath, draw a line to the next point
                currentPath.forEach { lineTo(it.x, it.y) }
            }
            drawPath(path = activeGraphicalPath, color = selectedColor, style = drawStroke)
        }
    }
}

@Composable
fun colorButton(selectedColor: Color, color: Color, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .background(color, CircleShape)
            .then(
                if (selectedColor == color) Modifier.border(
                    2.dp,
                    Color.White,
                    CircleShape
                )
                else Modifier
            )
    ) {}
}