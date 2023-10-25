package com.example.doodl.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import java.util.regex.Pattern
import com.example.doodl.R

// Composable functions for reusable UI components

@Composable
fun drawCanvas(paths: List<Triple<List<Offset>, Color, Float>>,
               currentPath: List<Offset>,
               selectedColor: Color,
               brushSize: Float) {
    Canvas(Modifier.fillMaxSize()) {
        drawRect(color = Color.White, size = size)
        // Redraws previous canvas paths user has completed drawing to ensure all paths reapper if UI updates
        // For each path in paths, spilt it into offsets, Color
        paths.forEach { (offsets, color, pathBrushSize) ->
            val isEraser = (color == Color.White)
            val graphicalPath = Path().apply {
                // Sets start point to first offset in list
                moveTo(offsets.first().x, offsets.first().y)
                // For each offset in path, draw a line to the next point
                offsets.forEach { lineTo(it.x, it.y) }
            }
            drawPath(
                path = graphicalPath,
                color = color,
                style = Stroke(
                    width = pathBrushSize,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
        // Draws canvas path currently being drawn if there is an active path
        if (currentPath.isNotEmpty()) {
            val activeGraphicalPath = Path().apply {
                // Sets start point to first point in current path
                moveTo(currentPath.first().x, currentPath.first().y)
                // For each offset in currentPath, draw a line to the next point
                currentPath.forEach { lineTo(it.x, it.y) }
            }
            drawPath(
                path = activeGraphicalPath,
                color = selectedColor,
                style = Stroke(
                    width = brushSize,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}

@Composable
fun colorButton(selectedColor: Color,
                color: Color, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .background(color, CircleShape)
            .then(
                if (selectedColor == color) Modifier.border(
                    2.dp,
                    Color.White,
                    CircleShape
                ).background(Color.Gray.copy(alpha = 0.4f), CircleShape)
                else Modifier
            )
    ) {}
}

@Composable
fun eraserButton(
    selectedTool: Color,
    onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .background(Color.White, CircleShape)
            .then(
                if (selectedTool == Color.White) Modifier.border(
                    2.dp,
                    Color.White,
                    CircleShape
                ).background(Color.Gray.copy(alpha = 0.4f), CircleShape)
                else Modifier
            )
    ) {
        Icon(
            painter = painterResource(id = R.drawable.eraser),
            contentDescription = "Eraser",
            tint = Color.Gray
        )
    }
}

@Composable
fun BottomNavigationBar(navController: NavController,
                        modifier: Modifier = Modifier,
                        onHeightCalculated: (Int) -> Unit) {
    // Remember the last height of the BottomNavigation; initialized to -1 as a placeholder value
    var lastHeight by remember { mutableStateOf(-1)}
    BottomNavigation(
        modifier = modifier // Apply the passed modifier
            .onGloballyPositioned { layoutInfo ->
                // Check for height changes in BottomNavigation to prevent redundant calls to onHeightCalculated
                val newHeight = layoutInfo.size.height
                if (newHeight != lastHeight) {
                    lastHeight = newHeight
                    onHeightCalculated(newHeight)
                }
            },
        backgroundColor = MaterialTheme.colorScheme.primary,
    ) {
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Info, contentDescription = null) },
            label = { Text("Feed") },
            selected = navController.currentDestination?.route == "feed",
            onClick = { navController.navigate("feed") }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Canvas") },
            selected = navController.currentDestination?.route == "canvas",
            onClick = { navController.navigate("canvas") }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            label = { Text("Profile") },
            selected = navController.currentDestination?.route == "profile",
            onClick = { navController.navigate("profile") }
        )
    }
}

fun logout(navController: NavController) {
    FirebaseAuth.getInstance().signOut()
    navController.navigate("loginScreen") {
        popUpTo("loginScreen") { inclusive = true }
    }
}

fun isValidEmail(email: String): Boolean {
    val emailRegex = Pattern.compile(
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    )
    return emailRegex.matcher(email).matches()
}

fun containsLetterAndNumber(password: String): Boolean {
    val hasLetter = password.any { it.isLetter() }
    val hasDigit = password.any { it.isDigit() }
    return hasLetter && hasDigit
}