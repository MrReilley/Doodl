package com.example.doodl

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun BottomNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = BottomNavBarScreen.Feed.route
    ) {
        composable(route = BottomNavBarScreen.Feed.route) {
            FeedScreen()
        }
        composable(route = BottomNavBarScreen.Post.route) {
            PostCanvasScreen()
        }
    }
}