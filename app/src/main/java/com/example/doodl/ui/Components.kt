package com.example.doodl.ui

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
fun drawCanvas(
    paths: List<Triple<List<Offset>, Color, Float>>,
    currentPath: List<Offset>,
    selectedColor: Color,
    brushSize: Float
) {
    Canvas(Modifier.fillMaxSize()) {
        drawRect(color = Color.White, size = size)
        // Redraws previous canvas paths user has completed drawing to ensure all paths reapper if UI updates
        // For each path in paths, spilt it into offsets, Color
        paths.forEach { (offsets, color, pathBrushSize) ->
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun colorButton(
    selectedButtonId: String,
    buttonId: String,
    buttonColor: Color,
    onClick: () -> Unit,
    currentColorButton: MutableState<Color?>,
    isColorPickerVisible: MutableState<Boolean>,
    updateSelectedButtonId: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
                color = buttonColor,
                shape = CircleShape
            )
            .then(
                when {
                    // Color picker is not visible, and this button is selected
                    (!isColorPickerVisible.value && selectedButtonId == buttonId) -> {
                        Modifier.border(2.dp, Color.White, CircleShape)
                    }
                    // Color picker is visible and this button is selected
                    (isColorPickerVisible.value && selectedButtonId == buttonId) -> {
                        Modifier.border(2.dp, Color.White, CircleShape)
                    }
                    // Color picker is visible and this button is not selected
                    (isColorPickerVisible.value && selectedButtonId != buttonId) -> {
                        Modifier.background(Color.Gray.copy(alpha = 0.4f), CircleShape)
                    } else -> {
                        Modifier
                    }
                }
            )
            .combinedClickable(
                onClick = {
                    // If color picker is not visible OR color picker is visible and this button is the selected one
                    if (!isColorPickerVisible.value || (isColorPickerVisible.value && selectedButtonId == buttonId)) {
                        onClick()
                    }
                },
                onLongClick = {
                    // If color picker is not visible OR color picker is visible and this button is the selected one
                    if (!isColorPickerVisible.value || (isColorPickerVisible.value && selectedButtonId == buttonId)) {
                        currentColorButton.value = buttonColor
                        isColorPickerVisible.value = true
                        updateSelectedButtonId(buttonId)
                    }
                }
            )
    ) {}
}

@Composable
fun eraserButton(
    selectedButtonId: String,
    onClick: () -> Unit,
    isColorPickerVisible: MutableState<Boolean>
) {
    val isEraserActive = selectedButtonId == "eraserButton"
    IconButton(
        onClick = {
            if (!isColorPickerVisible.value) {
                onClick()
            }
        },
        modifier = Modifier
            .background(Color.White, CircleShape)
            .then(
                when {
                    // Eraser is active, and color picker is not visible
                    (isEraserActive && !isColorPickerVisible.value) -> {
                        Modifier.border(2.dp, Color.White, CircleShape)
                    }
                    // Color picker is visible
                    isColorPickerVisible.value -> {
                        Modifier.background(Color.Gray.copy(alpha = 0.4f), CircleShape)
                    } else -> {
                        Modifier
                    }
                }
            )
    ) {
        Icon(
            painter = painterResource(id = R.drawable.eraser),
            contentDescription = "Eraser"
        )
    }
}

@Composable
fun BottomNavigationBar(navController: NavController,
                        modifier: Modifier = Modifier,
                        onHeightCalculated: (Int) -> Unit) {
    // Remember the last height of the BottomNavigation; initialized to -1 as a placeholder value
    var lastHeight by remember { mutableStateOf(-1)}
    val currentRoute = navController.currentDestination?.route
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
            icon = {
                val icon = if (currentRoute == "feed") Icons.Default.Home else Icons.Outlined.Home
                Icon(icon, contentDescription = null)
            },
            label = { Text("Feed") },
            selected = navController.currentDestination?.route == "feed",
            onClick = { navController.navigate("feed") }
        )
        BottomNavigationItem(
            icon = {
                val icon = if (currentRoute == "canvas") Icons.Default.Brush else Icons.Outlined.Brush
                Icon(icon, contentDescription = null)
            },
            label = { Text("Canvas") },
            selected = navController.currentDestination?.route == "canvas",
            onClick = { navController.navigate("canvas") }
        )
        BottomNavigationItem(
            icon = {
                val icon = if (currentRoute == "profile") Icons.Default.Person else Icons.Outlined.Person
                Icon(icon, contentDescription = null)
            },
            label = { Text("Profile") },
            selected = navController.currentDestination?.route == "profile",
            onClick = { navController.navigate("profile") }
        )
    }
}

fun logout(navController: NavController) {
    FirebaseAuth.getInstance().signOut()
    navController.navigate("loginScreen") {
        // This will clear everything on the back stack up to, but not including, "loginScreen".
        popUpTo("loginScreen") { inclusive = false }
        // This will clear any existing tasks so that the user cannot go back to the previous screen after logging out.
        launchSingleTop = true
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