package com.example.doodl.util

import android.graphics.Bitmap
import android.view.MotionEvent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import android.graphics.Paint
import android.graphics.Canvas

// Reusable utility functions like date formatting, string manipulation, or other helper functions
// not tied to specific feature or component

fun handleDrawingActivityTouchEvent(event: MotionEvent,
                                    currentPath: MutableList<Offset>,
                                    paths: MutableList<Triple<List<Offset>, Color, Float>>,
                                    selectedColor: Color,
                                    brushSize: Float): Boolean {
    val touchAction = event.action
    val touchActionCoordinates = Offset(event.x, event.y)

    // Handles touch events for drawing paths
    // Path is a sequence of connected points represented by a list of Offsets (coordinates)
    return when (touchAction) {
        // If starting to draw or drawing, add to current path
        MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
            currentPath.add(touchActionCoordinates)
            true
        }
        // If touch is released, add current path to completed paths
        MotionEvent.ACTION_UP -> {
            if (currentPath.size == 1) {
                // If it's a single point (a dot), duplicate the point to make it a "path"
                currentPath.add(Offset(event.x + 1e-5f, event.y + 1e-5f)) // tiny offset to make it a line
            }
            paths.add(Triple(currentPath.toList(), selectedColor, brushSize))
            currentPath.clear()
            true
        }
        // Ignore other touch events
        else -> false
    }
}

fun generateBitmapFromPaths(paths: List<Triple<List<Offset>, Color, Float>>,
                            canvasWidth: Int,
                            canvasHeight: Int): Bitmap {
    // Create empty bitmap with given dimensions and color format
    val outputBitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)

    // Create canvas object for drawing on empty bitmap
    val canvasToDraw = Canvas(outputBitmap)

    // Create paint object for drawing white background
    val backgroundPaint = Paint()
    backgroundPaint.color = android.graphics.Color.WHITE

    // Draw white background onto canvas object
    canvasToDraw.drawRect(0f, 0f, canvasWidth.toFloat(), canvasHeight.toFloat(), backgroundPaint)

    // Create paint object for drawing paths
    val drawingPaint = Paint()
    drawingPaint.isAntiAlias = true
    drawingPaint.strokeCap = Paint.Cap.ROUND
    drawingPaint.strokeJoin = Paint.Join.ROUND

    // A path is a series of connected lines formed by multiple points
    for (path in paths) {
        // Get color and points from current path
        val (coordinates, pathColor, pathBrushSize) = path

        // Set paint object's color, stroke width, and style properties
        drawingPaint.color = android.graphics.Color.rgb(pathColor.red, pathColor.green, pathColor.blue)
        drawingPaint.strokeWidth = pathBrushSize
        drawingPaint.style = Paint.Style.STROKE

        // Loop through list of coordinates, starting from second point (i=1)
            // i.e. path = [point1, point2, point3, point4]
            // -> line1 = point1 to point2, line2 = point2 to point3, line3 = point3 to point4
        for (i in 1 until coordinates.size) {
            // Get coordinates of starting point of line
            val startX = coordinates[i-1].x
            val startY = coordinates[i-1].y

            // Get coordinates of ending point of line
            val endX = coordinates[i].x
            val endY = coordinates[i].y

            // Draw line from start point to end point on canvas
            canvasToDraw.drawLine(startX, startY, endX, endY, drawingPaint)
        }
    }
    return outputBitmap
}
object ValidationUtils {

    fun validateUsername(username: String): Pair<Boolean, String> {
        // Username should contain only letters and numbers and be no longer than 12 characters
        val isValid = username.matches(Regex("^[a-zA-Z0-9]{1,15}\$"))
        val message = if (isValid) "Valid username" else "Username should be 1-12 characters long and contain only letters and numbers."
        return Pair(isValid, message)
    }

    fun validateBio(bio: String): Pair<Boolean, String> {
        // Bio should be no more than 100 characters
        val isValid = bio.length <= 100
        val message = if (isValid) "Valid bio" else "Bio should not exceed 100 characters."
        return Pair(isValid, message)
    }

}
object ComposableStateUtil {
    fun resetEditableFields(
        currentUsername: String?,
        currentBio: String?,
        onUsernameReset: (String) -> Unit,
        onBioReset: (String) -> Unit
    ) {
        val newUsername = currentUsername ?: ""
        val newBio = currentBio ?: ""
        onUsernameReset(newUsername)
        onBioReset(newBio)
    }
}


