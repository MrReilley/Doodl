package com.example.doodl.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.doodl.ui.screens.FeedsScreen

@Composable
fun BottomNavGraph(navController: NavHostController) {
    //val feedIntent = Intent(LocalContext.current, FeedActivity::class.java)
    //val canvasIntent = Intent(LocalContext.current, CanvasActivity::class.java)
    NavHost(
        navController = navController,
        startDestination = BottomNavBarScreen.Feed.route
    ) {
        composable(route = BottomNavBarScreen.Feed.route) {
            //FeedActivity()
            //LocalContext.current.startActivity(feedIntent)
            FeedsScreen()
        }
        composable(route = BottomNavBarScreen.Post.route) {
            PostCanvasScreen()
        }
    }
}



