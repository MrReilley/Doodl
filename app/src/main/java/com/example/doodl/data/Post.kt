package com.example.doodl.data

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude

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
    var profilePicUrl: String? = null,
    @get:Exclude var snapshot: DocumentSnapshot? = null // Excludes this field from Firestore document
)