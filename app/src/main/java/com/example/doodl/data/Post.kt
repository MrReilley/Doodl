package com.example.doodl.data

data class Post(
    val postId: String,
    val userId: String,
    val timestamp: Long,
    val imagePath: String,
    val likes: Int = 0,
    val likedBy: List<String> = listOf()
)
