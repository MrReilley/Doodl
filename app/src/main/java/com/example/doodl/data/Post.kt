package com.example.doodl.data

data class Post(
    val postId: String = "",
    val userId: String = "",
    val timestamp: Long = 0,
    val imagePath: String = "",
    val likes: Int = 0,
    val likedBy: List<String> = listOf(),
    val tags: List<String> = listOf(),
    var imageUrl: String? = null
)
