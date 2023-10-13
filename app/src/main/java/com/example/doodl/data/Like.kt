package com.example.doodl.data

data class Like(
    val likeId: String, // Unique like ID
    val userId: String, // ID of the user who liked
    val postId: String, // ID of the post that was liked
    val timestamp: Long // When the like occurred
)
