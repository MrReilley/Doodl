package com.example.doodl.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.doodl.data.Repository
import com.example.doodl.viewmodel.CanvasViewModel
import com.example.doodl.viewmodel.CanvasViewModelFactory

@Composable
fun PostCanvasScreen() {
    /*Surface(
        modifier = Modifier.fillMaxSize(),
        color = androidx.compose.material3.MaterialTheme.colorScheme.background
    ) {
        MyCanvasApp()
    }*/
    val repository = Repository()
    //val factory = CanvasViewModelFactory(repository)
    // Retrieve or create a CanvasViewModel instance scoped to this Activity, using the specified factory for its creation
    // Allows instance to survives configuration changes like screen rotations
    // TODO: Instances still do not survive between configuration changes
    //val canvasViewModel = ViewModelProvider(MainActivity(), factory)[CanvasViewModel::class.java]
    val canvasViewModel: CanvasViewModel = viewModel(factory = CanvasViewModelFactory(repository))
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = androidx.compose.material3.MaterialTheme.colorScheme.background
        //color = MaterialTheme.colorScheme.background
    ) {
        var selectedColor by remember { mutableStateOf(Color.Black) }

        CanvasActivity(canvasViewModel, selectedColor) { newColor ->
            selectedColor = newColor
        }
    }
}

/*@Composable

fun MyCanvasApp() {
    var selectedColor by remember { mutableStateOf(Color.Black) }

    CanvasActivity(selectedColor) { newColor ->
        selectedColor = newColor
    }
}*/
/*@Composable
fun MyCanvasApp(canvasViewModel: CanvasViewModel) {

}*/

@Preview
@Composable
fun CanvasScreenPreview(){
    PostCanvasScreen()
}

