package com.example.doodl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.doodl.ui.BottomNavigationBar
import com.example.doodl.ui.screens.CanvasScreen
import com.example.doodl.ui.screens.FeedScreen
import com.example.doodl.ui.theme.DoodlTheme


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
                            startDestination = "canvas",
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