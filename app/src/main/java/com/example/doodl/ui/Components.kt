package com.example.doodl.ui



import android.graphics.DashPathEffect
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.doodl.R
import com.example.doodl.ui.screens.BrushType
import com.example.doodl.ui.screens.Quadruple
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import java.util.regex.Pattern

// Composable functions for reusable UI components

@Composable
fun drawCanvas(
    paths: List<Quadruple<List<Offset>, Color, Float, BrushType>>,
    currentPath: List<Offset>,
    selectedColor: Color,
    brushSize: Float,
    selectedBrushType: BrushType
) {
    Canvas(Modifier.fillMaxSize()) {
        drawRect(color = Color.White, size = size)
        // Redraws previous canvas paths user has completed drawing to ensure all paths reappear if UI updates
        // For each path in paths, spilt it into offsets, Color
        paths.forEach { (offsets, color, pathBrushSize, brushType) ->
            val graphicalPath = Path().apply {
                // Sets start point to first offset in list
                moveTo(offsets.first().x, offsets.first().y)
                // For each offset in path, draw a line to the next point
                offsets.forEach { lineTo(it.x, it.y) }
            }

            // Configure paint style based on brushType
            when (brushType) {
                BrushType.NORMAL -> {
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

                BrushType.DASHED -> {
                    val dotLength = pathBrushSize * 0.2f // Small length for dot
                    val gapLength = pathBrushSize * 2// Gap equal to brush size
                    drawPath(
                        path = graphicalPath,
                        color = color,
                        style = Stroke(
                            width = pathBrushSize,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                            pathEffect = PathEffect.dashPathEffect(
                                floatArrayOf(
                                    dotLength,
                                    gapLength
                                )
                            )
                        )
                    )
                }

                BrushType.SQUARE -> {
                    drawPath(
                        path = graphicalPath,
                        color = color,
                        style = Stroke(
                            width = pathBrushSize,
                            cap = StrokeCap.Square,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

        }
        // Draws canvas path currently being drawn if there is an active path
        if (currentPath.isNotEmpty()) {
            val activeGraphicalPath = Path().apply {
                // Sets start point to first point in current path
                moveTo(currentPath.first().x, currentPath.first().y)
                // For each offset in currentPath, draw a line to the next point
                currentPath.forEach { lineTo(it.x, it.y) }
            }

            val activePathStyle = when (selectedBrushType) {
                BrushType.NORMAL -> Stroke(
                    width = brushSize,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )

                BrushType.DASHED -> {
                    val dotLength = brushSize * 0.2f // Length of the dot, significantly shorter than the brush size
                    val gapLength = brushSize * 2 // Gap length, adjusted to ensure the total of dot and gap equals brush size
                    Stroke(
                        width = brushSize,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dotLength, gapLength))
                    )
                }

                BrushType.SQUARE -> Stroke(
                    width = brushSize,
                    cap = StrokeCap.Square,
                    join = StrokeJoin.Bevel
                )
                // Add other brush types here if needed
            }

            drawPath(
                path = activeGraphicalPath,
                color = selectedColor,
                style = activePathStyle
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
                    }

                    else -> {
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
                    }

                    else -> {
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
                        onHeightCalculated: (Int) -> Unit){
    // Remember the last height of the BottomNavigation; initialized to -1 as a placeholder value
    var lastHeight by remember { mutableIntStateOf(-1) }
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
            onClick = {
                if (currentRoute != "feed") {
                    // Only navigate if the current route is different from the selected route
                    navController.navigate("feed")
                }
            }
        )
        BottomNavigationItem(
            icon = {
                val icon = if (currentRoute == "canvas") Icons.Default.Brush else Icons.Outlined.Brush
                Icon(icon, contentDescription = null)
            },
            label = { Text("Canvas") },
            selected = navController.currentDestination?.route == "canvas",
            onClick = {
                if (currentRoute != "canvas") {
                    // Only navigate if the current route is different from the selected route
                    navController.navigate("canvas")
                }
            }
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

@Composable
fun RoundImageCard(
    image: Int,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    Card(
        shape = CircleShape,
        modifier = modifier
    ) {
        Image(
            painter = painterResource(id = image),
            contentDescription = null,
            contentScale = contentScale,
            modifier = Modifier
                .fillMaxSize()
                .size(24.dp)
                .align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun RoundImageCardFeed(
    url: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    if(url.isBlank()) {
        // We can have a default image here instead of text
        Card(shape = CircleShape, modifier = modifier) {
            Text(
                "No Image",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            )
        }
    } else {
        Card(
            shape = CircleShape,
            modifier = modifier
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = url),
                contentDescription = null,
                contentScale = contentScale,
                modifier = Modifier
                    .fillMaxSize()
                    .size(24.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}


@Composable
fun EditableTextField(label: String, text: String, onTextChanged: (String) -> Unit) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val isFocused = remember { mutableStateOf(false) }
    Column {
        OutlinedTextField(
            value = text,
            onValueChange = { onTextChanged(it) },
            label = { Text(text = label, color = Color.White)},
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                }
            ),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                unfocusedBorderColor = Color.White,
                focusedLabelColor = MaterialTheme.colorScheme.tertiary,
                unfocusedLabelColor = Color.White,
                cursorColor = MaterialTheme.colorScheme.tertiary,
                textColor = if (isFocused.value) MaterialTheme.colorScheme.tertiary else Color.White
            ),
            modifier = Modifier
                .onFocusChanged { focusState ->
                    isFocused.value = focusState.isFocused
                }
        )
    }
}
@Composable
fun ConfirmationDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            containerColor = Color.Black,
            modifier = Modifier.border(2.3.dp, Color.White, RoundedCornerShape(30.dp)),
            onDismissRequest = { onDismiss() },
            title = { Text(title, color = Color.White) },
            text = { Text(message, color = Color.White) },
            confirmButton = {
                Button(onClick = onConfirm) {
                    Text("Yes", color = Color.White)
                }
            },
            dismissButton = {
                Button(onClick = onCancel) {
                    Text("No", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun ReAuthenticateDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onReAuthenticate: (String) -> Unit,
    errorMessage: String
) {
    if (showDialog) {
        var password by remember { mutableStateOf("") }

        AlertDialog(
            containerColor = Color.Black,
            modifier = Modifier.border(2.3.dp, Color.White, RoundedCornerShape(30.dp)),
            onDismissRequest = { onDismiss() },
            title = { Text("Re-authenticate", color = Color.White) },
            text = {
                Column {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Enter your password") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                            unfocusedBorderColor = Color.White,
                            focusedLabelColor = MaterialTheme.colorScheme.tertiary,
                            unfocusedLabelColor = Color.White,
                            cursorColor = MaterialTheme.colorScheme.tertiary,
                            textColor = MaterialTheme.colorScheme.tertiary
                        )
                    )
                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = Color.Red,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { onReAuthenticate(password) }) {
                    Text("Confirm", color = Color.White)
                }
            },
            dismissButton = {
                Button(onClick = { onDismiss() }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}
@Composable
fun BlackScreenWithLoadingIndicator(navController: NavController) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.tertiary,
            strokeWidth = 2.dp
        )

        // LaunchedEffect for delayed navigation
        LaunchedEffect(Unit) {
            delay(2000L)
            navController.navigate("loginScreen") {
                popUpTo("loginScreen") { inclusive = false }
                launchSingleTop = true
            }
        }
    }
}
