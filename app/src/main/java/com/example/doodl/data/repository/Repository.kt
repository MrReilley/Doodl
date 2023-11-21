package com.example.doodl.data.repository
import android.net.Uri
import com.example.doodl.data.Like
import com.example.doodl.data.Post
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlin.random.Random


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
    fun uploadProfileImage(userId: String, imageByteArray: ByteArray): Task<String> {
        fun getRandomNumber() = Random.nextInt(1000, 9999)

        // Create a TaskCompletionSource
        val taskCompletionSource = TaskCompletionSource<String>()

        // Generate filename with random number
        val randomNumber = getRandomNumber()
        val fileName = "profilepic$randomNumber.png"
        val filePath = "user/$userId/profilepic/$fileName"
        val fileRef = storageReference.child(filePath)

        // Upload the file
        fileRef.putBytes(imageByteArray).addOnSuccessListener {
            // On success, set the result as the file storage path
            taskCompletionSource.setResult(filePath)
        }.addOnFailureListener { exception ->
            // On failure, set the exception
            taskCompletionSource.setException(exception)
        }

        // Return the Task from the TaskCompletionSource
        return taskCompletionSource.task
    }//new for profileactivity

    fun updateUserProfile(userId: String, newUsername: String, newBio: String, newProfilePicPath: String?): Task<Void> {
        val userDocumentRef = db.collection("users").document(userId)
        val updates = hashMapOf<String, Any>(
            "username" to newUsername,
            "userBio" to newBio
        )
        newProfilePicPath?.let { updates["profilePicPath"] = it }
        return userDocumentRef.update(updates)
    }//new for profileactivity

    fun updateUserPostsUsername(userId: String, newUsername: String, newProfilePicPath: String?): Task<Void> {
        val postsQuery = db.collection("posts").whereEqualTo("userId", userId)
        return postsQuery.get().continueWithTask { task ->
            if (!task.isSuccessful) {
                throw task.exception ?: Exception("Failed to fetch user posts")
            }
            val batch = db.batch()
            for (document in task.result!!) {
                val postRef = document.reference
                batch.update(postRef, "username", newUsername)
                newProfilePicPath?.let { batch.update(postRef, "profilePicPath", it) }
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
    fun fetchProfileImages(userId: String): Task<List<String>> {
        val userImagesRef = storageReference.child("user/$userId/profilepic")

        val taskCompletionSource = TaskCompletionSource<List<String>>()
        userImagesRef.listAll().addOnSuccessListener { listResult ->
            val tasks = listResult.items.map { it.downloadUrl }
            Tasks.whenAllSuccess<Uri>(tasks).addOnSuccessListener { uris ->
                val urlStrings = uris.map { it.toString() } // Convert URIs to Strings
                taskCompletionSource.setResult(urlStrings)
            }
        }.addOnFailureListener { exception ->
            taskCompletionSource.setException(exception)
        }
        return taskCompletionSource.task
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
    fun isUsernameAvailable(username: String): Task<Boolean> {
        return db.collection("users")
            .whereEqualTo("username", username)
            .limit(1) // Limit to checking just one document
            .get()
            .continueWith { task ->
                if (!task.isSuccessful) {
                    throw task.exception ?: Exception("Failed to check username availability")
                }
                val querySnapshot = task.result
                querySnapshot?.isEmpty ?: true // Username is available if no documents are found
            }
    }//new for profileactivity

}


