package com.example.doodl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.doodl.ui.screens.LoginScreen
import com.example.doodl.ui.screens.RegistrationScreen
import com.example.doodl.ui.theme.DoodlTheme


class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DoodlTheme {
                Surface {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "loginScreen") {
                        composable("loginScreen") { LoginScreen(navController) }
                        composable("registrationScreen") { RegistrationScreen(navController) }
                    }
                }
            }
        }
    }
}