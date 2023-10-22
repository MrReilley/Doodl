package com.example.doodl.data

// Data class that defines the structure of a User object
data class DoodlUser(
    val userId: String, // Unique user ID
    val username: String, // User’s display name
    val profilePicPath: String?, //Path to profile picture in Firebase Storage.
    val userBio: String? //User's biography
)