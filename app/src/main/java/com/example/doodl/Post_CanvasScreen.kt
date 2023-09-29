package com.example.doodl

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.doodl.ui.CanvasActivity
import androidx.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun PostCanvasScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = androidx.compose.material3.MaterialTheme.colorScheme.background
    ) {
        MyCanvasApp()
    }
}

@Composable

fun MyCanvasApp() {
    var selectedColor by remember { mutableStateOf(Color.Black) }

    CanvasActivity(selectedColor) { newColor ->
        selectedColor = newColor
    }
}