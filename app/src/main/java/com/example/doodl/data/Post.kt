package com.example.doodl.data

// Data class that defines the structure of a User object
data class Post(
    val postId: String, //Unique post ID
    val userId: String, // ID of the user who created the post
    val timestamp: Long, // When the post was created
    val imagePath: String, // Path to image in Firebase Storage
    val likes: Int = 0, // Number of likes
    val likedBy: List<String> = listOf(), /* Map or array of user IDs who have liked the post
    (to prevent duplicate likes from a user)*/
    val tags: List<String> = listOf() // Allowing multiple tags per post
)
