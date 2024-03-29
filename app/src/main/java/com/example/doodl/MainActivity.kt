package com.example.doodl

import android.os.Bundle
import android.util.Log
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.doodl.data.repository.Repository
import com.example.doodl.ui.BlackScreenWithLoadingIndicator
import com.example.doodl.ui.BottomNavigationBar
import com.example.doodl.ui.screens.CanvasScreen
import com.example.doodl.ui.screens.FeedScreen
import com.example.doodl.ui.screens.LoginScreen
import com.example.doodl.ui.screens.PasswordResetScreen
import com.example.doodl.ui.screens.PostInfoScreen
import com.example.doodl.ui.screens.ProfileScreen
import com.example.doodl.ui.screens.RegistrationScreen
import com.example.doodl.ui.theme.DoodlTheme
import com.example.doodl.viewmodel.CanvasViewModel
import com.example.doodl.viewmodel.CanvasViewModelFactory
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userLoggedIn = if (FirebaseAuth.getInstance().currentUser != null) {
            Log.d("MainActivity", "User is logged in, navigating to feed.")
            "feed"
        } else {
            Log.d("MainActivity", "User is not logged in, navigating to loginScreen.")
            "loginScreen"
        }

        setContent {
            DoodlTheme(
                darkTheme = true,
                dynamicColor = false
            ) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val repository = Repository()
                    val canvasViewModel: CanvasViewModel = viewModel(factory = CanvasViewModelFactory(repository))
                    val navController = rememberNavController()
                    var navBarHeight by remember { mutableIntStateOf(0) }

                    // Retrieve the current back stack entry
                    val backStackEntry by navController.currentBackStackEntryAsState()
                    // Retrieve the current route
                    val currentRoute = backStackEntry?.destination?.route

                    Log.d("MainActivity", "Current route: $currentRoute")

                    Box(Modifier.fillMaxSize()) {
                        NavHost(
                            navController = navController,
                            startDestination = userLoggedIn,
                        ) {
                            composable("loginScreen") {
                                Log.d("MainActivity", "Navigating to loginScreen")
                                LoginScreen(navController, this@MainActivity) }
                            composable("passwordResetScreen") {
                                Log.d("MainActivity", "Navigating to passwordResetScreen")
                                PasswordResetScreen(navController, this@MainActivity) }
                            composable("registrationScreen") {
                                Log.d("MainActivity", "Navigating to registrationScreen")
                                RegistrationScreen(navController, this@MainActivity) }
                            composable("canvas") {
                                Log.d("MainActivity", "Navigating to canvas")
                                CanvasScreen(navController, navBarHeight, canvasViewModel) }
                            composable("postInfo") { PostInfoScreen(navController, canvasViewModel) }
                            composable("deleteAccountLoading") { BlackScreenWithLoadingIndicator(navController) }
                            composable("feed") {
                                Log.d("MainActivity", "Navigating to feed")
                                val userId = FirebaseAuth.getInstance().currentUser?.uid
                                    ?: throw IllegalStateException("User must be logged in to access the feed.")
                                FeedScreen(userId, navBarHeight)
                            }
                            composable("profile") {
                                val currentUser = FirebaseAuth.getInstance().currentUser
                                if (currentUser == null) {
                                    navController.navigate("loginScreen")
                                } else {
                                    ProfileScreen(currentUser.uid, navController, navBarHeight = navBarHeight)
                                }
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
