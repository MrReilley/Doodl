package com.example.canvastest.util

import android.view.MotionEvent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

// Reusable utility functions like date formatting, string manipulation, or other helper functions not tied to specific feature or component

fun handleTouchEvent(event: MotionEvent, currentPath: MutableList<Offset>, paths: MutableList<Pair<List<Offset>, Color>>, selectedColor: Color): Boolean {
    val action = event.action
    val offset = Offset(event.x, event.y)

    return when (action) {
        // If starting to draw or drawing, add to current path
        MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
            currentPath.add(offset)
            true
        }
        // If touch is released, add current path to completed paths
        MotionEvent.ACTION_UP -> {
            paths.add(Pair(currentPath.toList(), selectedColor))
            currentPath.clear()
            true
        }
        // Ignore other touch events
        else -> false
    }
}

