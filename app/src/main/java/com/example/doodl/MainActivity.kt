package com.example.doodl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.doodl.ui.BottomNavigationBar
import com.example.doodl.ui.screens.CanvasScreen
import com.example.doodl.ui.screens.FeedScreen
import com.example.doodl.ui.screens.LoginScreen
import com.example.doodl.ui.screens.ProfileScreen
import com.example.doodl.ui.screens.RegistrationScreen
import com.example.doodl.ui.theme.DoodlTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userLoggedIn = if (FirebaseAuth.getInstance().currentUser != null) {
            "feed"
        } else {
            "loginScreen"
        }
        setContent {
            DoodlTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    var navBarHeight by remember { mutableIntStateOf(0) }

                    // Retrieve the current back stack entry
                    val backStackEntry by navController.currentBackStackEntryAsState()
                    // Retrieve the current route
                    val currentRoute = backStackEntry?.destination?.route

                    Box(Modifier.fillMaxSize()) {
                        NavHost(
                            navController = navController,
                            startDestination = userLoggedIn,
                        ) {
                            composable("loginScreen") { LoginScreen(navController, this@MainActivity) }
                            composable("registrationScreen") { RegistrationScreen(navController, this@MainActivity) }
                            composable("canvas") { CanvasScreen(navBarHeight) }
                            composable("feed") {
                                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: throw IllegalStateException("User must be logged in to access the feed.")
                                FeedScreen(userId)
                            }
                            composable("profile") {
                                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: throw IllegalStateException("User must be logged in to access the profile.")
                                ProfileScreen(userId,navController)
                            }
                        }

                        // Bottom Navigation Bar
                        // Only display it if currentRoute is either "canvas" ,"feed", or "profile"
                        if(currentRoute in listOf("canvas", "feed", "profile")) {
                            BottomNavigationBar(navController, Modifier.align(Alignment.BottomCenter)) { height ->
                                navBarHeight = height
                            }
                        }
                    }
                }
            }
        }
    }
}
