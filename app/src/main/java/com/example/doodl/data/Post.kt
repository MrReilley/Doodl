package com.example.doodl.data

data class Post(
    val postId: String = "",
    val userId: String = "",
    val username: String? = null,
    val timestamp: Long = 0,
    val imagePath: String = "",
    val likes: Int = 0,
    val likedBy: List<String> = listOf(),
    val tags: List<String> = listOf(),
    var imageUrl: String? = null,
    val profilePicPath: String? = null,
    var profilePicUrl: String? = null
)