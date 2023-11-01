package com.example.doodl.data

data class DoodlUser(
    val userId: String,
    val username: String,
    val profilePicPath: String?,
    val userBio: String?,
)

data class User(
    val profileImageResource: Int,
    val username: String,
    val description: String?
)