package com.example.doodl_test

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavBarScreen(val route: String, val title: String, val icon: ImageVector){
    object Feed : BottomNavBarScreen(
        route = "feed",
        title = "feed",
        icon = Icons.Default.Home
    )

    object Post: BottomNavBarScreen(
        route = "post",
        title = "post",
        icon = Icons.Default.Person
    )
}
