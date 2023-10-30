package com.example.doodl.data.repository
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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

    fun downloadImage(imagePath: String): Task<Bitmap> {
        // Get a reference to the image file at the specified path in Firebase Storage
        val imageRef = storageReference.child(imagePath)
        // Define a maximum size for the image to be downloaded (5MB here)
        val maxSize: Long = 1024 * 1024 * 5
        // Request the byte data of the image and, once it's available, process it
        return imageRef.getBytes(maxSize).continueWith { task ->
            // Check if task is unsuccessful and throw an exception if it is
            if (!task.isSuccessful) {
                throw task.exception ?: Exception("Unknown error")
            }
            // Convert the fetched byte data into a Bitmap
            val bytes = task.result
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            bitmap// Return the constructed Bitmap
        }
    }
    fun fetchUsername(userId: String): Task<String?> {
        return db.collection("users").document(userId).get().continueWith { task ->
            if (task.isSuccessful) {
                return@continueWith task.result?.getString("username")
            } else {
                throw task.exception ?: RuntimeException("Unknown error occurred")
            }
        }
    }

    fun fetchUserImages(userId: String, onSuccess: (List<String>) -> Unit, onFailure: (Exception) -> Unit) {
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

    fun updateUserDetails(userId: String, username: String, userBio: String): Task<Void> {
        val userDocument = db.collection("users").document(userId)
        // Using a map to specify only the fields you want to update
        val updates = hashMapOf(
            "username" to username,
            "userBio" to userBio
        )
        return userDocument.update(updates as Map<String, Any>)
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


