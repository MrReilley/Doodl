package com.example.doodl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.BottomNavigation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.doodl.ui.screens.CanvasActivity
import com.example.doodl.ui.screens.CanvasScreen
import com.example.doodl.ui.screens.FeedScreen
import com.example.doodl.ui.theme.DoodlTheme
import com.example.doodl.viewmodel.CanvasViewModel
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.BottomNavigationItem
import androidx.compose.ui.Alignment
import androidx.navigation.compose.*


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DoodlTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(Modifier.fillMaxSize()) {
                        val navController = rememberNavController()

                        // Navigation Graph
                        NavHost(
                            navController = navController,
                            startDestination = "feed",
                            //modifier = Modifier.matchParentSize()
                        ) {
                            composable("canvas") { CanvasScreen() }
                            composable("feed") { FeedScreen() }
                        }

                        // Bottom Navigation Bar
                        BottomNavigationBar(navController, Modifier.align(Alignment.BottomCenter))
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController, modifier: Modifier = Modifier) {
    BottomNavigation(
        modifier = modifier // Apply the passed modifier
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
    }
}



@Composable
fun MyApp(canvasViewModel: CanvasViewModel) {
    var selectedColor by remember { mutableStateOf(Color.Black) }

    CanvasActivity(canvasViewModel, selectedColor) { newColor ->
        selectedColor = newColor
    }
}
