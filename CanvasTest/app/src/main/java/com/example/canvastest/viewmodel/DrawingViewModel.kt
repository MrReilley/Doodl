package com.example.canvastest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.launch

class DrawingViewModel : ViewModel() {
    val paths = mutableStateListOf<Pair<List<Offset>, Color>>()

    val currentPath = mutableStateListOf<Offset>()

    var selectedColor = Color.Black
    // TODO

}
