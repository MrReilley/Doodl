package com.example.doodl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModelProvider
import com.example.doodl.data.Repository
import com.example.doodl.ui.CanvasActivity
import com.example.doodl.ui.theme.DoodlTheme
import com.example.doodl.viewmodel.CanvasViewModel
import com.example.doodl.viewmodel.CanvasViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = Repository()
        val factory = CanvasViewModelFactory(repository)
        // Retrieve or create a CanvasViewModel instance scoped to this Activity, using the specified factory for its creation
        // Allows instance to survives configuration changes like screen rotations
        // TODO: Instances still do not survive between configuration changes
        val canvasViewModel = ViewModelProvider(this, factory)[CanvasViewModel::class.java]
        setContent {
            DoodlTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyApp(canvasViewModel)
                }
            }
        }
    }
}

@Composable
fun MyApp(canvasViewModel: CanvasViewModel) {
    var selectedColor by remember { mutableStateOf(Color.Black) }

    CanvasActivity(canvasViewModel, selectedColor) { newColor ->
        selectedColor = newColor
    }
}