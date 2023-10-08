package com.example.doodl.data

//data class User()
// Data class that defines the structure of a User object
// name, profile picture, password, bio

data class User(
    val userId: String,
    val username: String,
    val profilePicPath: String?
)