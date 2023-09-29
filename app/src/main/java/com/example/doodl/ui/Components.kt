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

// Composable functions for reusable UI components

@Composable
fun drawCanvas(paths: List<Pair<List<Offset>, Color>>, currentPath: List<Offset>, selectedColor: Color) {
    val drawStroke = Stroke(width = 5f, cap = StrokeCap.Round)

    Canvas(Modifier.fillMaxSize()) {
        // Draw each completed path
        paths.forEach { (path, color) ->
            val p = Path().apply {
                moveTo(path.first().x, path.first().y)
                path.forEach { lineTo(it.x, it.y) }
            }
            drawPath(path = p, color = color, style = drawStroke)
        }

        // Draw the current path if it exists
        if (currentPath.isNotEmpty()) {
            val p = Path().apply {
                moveTo(currentPath.first().x, currentPath.first().y)
                currentPath.forEach { lineTo(it.x, it.y) }
            }
            drawPath(path = p, color = selectedColor, style = drawStroke)
        }
    }
}
