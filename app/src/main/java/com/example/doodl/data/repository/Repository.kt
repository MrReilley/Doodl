package com.example.doodl.data.repository
import android.net.Uri
import com.example.doodl.data.Like
import com.example.doodl.data.Post
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask


// Code that interacts with Firebase Storage
// API for data access for rest of application
// Data operations, abstracting origin of data
class Repository {

    private val storageReference = FirebaseStorage.getInstance().reference
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Function used in CanvasViewModel to upload byte array representing an image to Firebase Storage
    fun uploadByteArray(byteArray: ByteArray): UploadTask {
        // Generate a unique file reference in Firebase Storage using the userId and current timestamp
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User must be logged in to upload")
        val fileRef = storageReference.child("user/$userId/posts/${System.currentTimeMillis()}.png")
        // Upload byte array to Firebase Storage reference
        return fileRef.putBytes(byteArray)
    }
    fun uploadProfileImage(userId: String, imageByteArray: ByteArray): Task<Uri> {
        val fileRef = storageReference.child("user/$userId/profilepic/${System.currentTimeMillis()}.png")
        return fileRef.putBytes(imageByteArray).continueWithTask { task ->
            if (!task.isSuccessful) {
                throw task.exception ?: Exception("Failed to upload profile image")
            }
            fileRef.downloadUrl
        }
    }//new for profileactivity

    fun updateUserProfile(userId: String, newUsername: String, newBio: String, newProfilePicPath: String): Task<Void> {
        val userDocumentRef = db.collection("users").document(userId)
        return userDocumentRef.update(mapOf(
            "username" to newUsername,
            "userBio" to newBio,
            "profilePicPath" to newProfilePicPath
        ))
    }//new for profileactivity

    fun updateUserPostsUsername(userId: String, newUsername: String, newProfilePicPath: String): Task<Void> {
        // This is a simple single document update might need to make this more efficient
        val postsQuery = db.collection("posts").whereEqualTo("userId", userId)
        return postsQuery.get().continueWithTask { task ->
            if (!task.isSuccessful) {
                throw task.exception ?: Exception("Failed to fetch user posts")
            }
            val batch = db.batch()
            for (document in task.result!!) {
                val postRef = document.reference
                batch.update(postRef, "username", newUsername)
                batch.update(postRef, "profilePicPath", newProfilePicPath)
            }
            batch.commit()
        }
    }//new for profileactivity

    fun getUsername(userId: String): Task<String?> {
        return db.collection("users").document(userId).get().continueWith { task ->
            if (task.isSuccessful) {
                return@continueWith task.result?.getString("username")
            } else {
                throw task.exception ?: RuntimeException("Unknown error occurred")
            }
        }
    }

    fun getUserImages(userId: String, onSuccess: (List<String>) -> Unit, onFailure: (Exception) -> Unit) {
        // Reference to the logged-in user's images directory in Firebase storage
        val userImagesRef = storageReference.child("user/$userId/posts")

        // Retrieve all file references in the user's posts directory
        userImagesRef.listAll().addOnSuccessListener { listResult ->
            // Map the results to their paths and trigger the onSuccess callback
            val imagePaths = listResult.items.map { it.path }
            onSuccess(imagePaths.reversed()) // .reversed will reverse the list so newest images come first
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }
    }

    fun getUserDetails(userId: String): Task<DocumentSnapshot> {
        return db.collection("users").document(userId).get()
    }

    fun getProfilePicUrl(profilePicPath: String): Task<String> {
        val fileRef = storageReference.child(profilePicPath)

        return fileRef.downloadUrl.continueWith { task ->
            if (task.isSuccessful) {
                task.result?.toString() ?: ""
            } else {
                throw task.exception ?: RuntimeException("Error fetching profile pic URL")
            }
        }
    }
    fun getProfilePicPath(userId: String): Task<String?> {
        return db.collection("users").document(userId).get()
            .continueWith { task ->
                if (task.isSuccessful) {
                    task.result?.getString("profilePicPath")
                } else {
                    throw task.exception ?: RuntimeException("Error fetching profile pic path")
                }
            }
    }

    fun savePostToFirestore(post: Post): Task<Void> {
        return db.collection("posts").document(post.postId).set(post)
    }
    fun getNewestPosts(): Task<List<Post>> {
        // Return the Task from Firebase directly, ordered by timestamp in descending order
        return db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .continueWith {
                it.result?.toObjects(Post::class.java) ?: emptyList()
            }
    }

    fun getImageUrl(imagePath: String): Task<String> {
        val storageRef = FirebaseStorage.getInstance().getReference(imagePath)
        return storageRef.downloadUrl.continueWith { task ->
            if (task.isSuccessful) {
                return@continueWith task.result?.toString() ?: ""
            } else {
                throw task.exception ?: RuntimeException("Unknown error occurred")
            }
        }
    }
    fun addLike(like: Like): Task<Void> {
        return db.collection("Likes").document(like.likeId).set(like)
    }
    fun removeLike(likeId: String): Task<Void> {
        return db.collection("Likes").document(likeId).delete()
    }
    fun isPostLikedByUser(postId: String, userId: String): Task<QuerySnapshot> {
        return db.collection("Likes")
            .whereEqualTo("postId", postId)
            .whereEqualTo("userId", userId)
            .get()
    }
    fun getLikedPostsForUser(userId: String): Task<QuerySnapshot> {
        return db.collection("Likes").whereEqualTo("userId", userId).get()
    }
    fun getLikesCountForPost(postId: String): Task<QuerySnapshot> {
        return db.collection("Likes").whereEqualTo("postId", postId).get()
    }
    fun getPostData(postId: String): Task<DocumentSnapshot> {
        return db.collection("posts").document(postId).get()
    }

    fun getTagsForPost(postId: String): Task<List<String>> {
        return db.collection("posts").document(postId).get()
            .continueWith { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && document.exists()) {
                        val tags = document.get("tags") as? List<String> ?: emptyList()
                        return@continueWith tags
                    } else {
                        throw RuntimeException("Document does not exist")
                    }
                } else {
                    throw task.exception ?: RuntimeException("Unknown error occurred")
                }
            }
    }


}


