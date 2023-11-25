package com.example.doodl.data.repository
import android.net.Uri
import com.example.doodl.data.Follow
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
    }

    fun updateUserProfile(userId: String, newUsername: String, newBio: String, newProfilePicPath: String?): Task<Void> {
        val userDocumentRef = db.collection("users").document(userId)
        val updates = hashMapOf<String, Any>(
            "username" to newUsername,
            "userBio" to newBio
        )
        newProfilePicPath?.let { updates["profilePicPath"] = it }
        return userDocumentRef.update(updates)
    }

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
    }

    fun getUsername(userId: String): Task<String?> {
        return db.collection("users").document(userId).get().continueWith { task ->
            if (task.isSuccessful) {
                return@continueWith task.result?.getString("username")
            } else {
                throw task.exception ?: RuntimeException("Unknown error occurred")
            }
        }
    }
    fun getUserPostIds(userId: String): Task<List<String>> {
        // Create a TaskCompletionSource to manage the task manually
        val taskCompletionSource = TaskCompletionSource<List<String>>()

        // Query the 'posts' collection for documents where 'userId' matches the provided userId
        db.collection("posts")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                // Extract post IDs from the documents
                val postIds = documents.documents.mapNotNull { it.id }
                taskCompletionSource.setResult(postIds)
            }
            .addOnFailureListener { exception ->
                // Handle any errors that occur during the query
                taskCompletionSource.setException(exception)
            }

        // Return the Task from the TaskCompletionSource
        return taskCompletionSource.task
    }

    fun getProfileImages(userId: String): Task<List<String>> {
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
    fun getNewestPosts(startAfter: DocumentSnapshot? = null, limit: Long = 3): Task<List<Post>> {
        var query = db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit)

        startAfter?.let {
            query = query.startAfter(it)
        }

        return query.get().continueWith { task ->
            if (task.isSuccessful) {
                task.result?.documents?.mapNotNull { document ->
                    document.toObject(Post::class.java)?.apply {
                        snapshot = document // Set the snapshot for each post
                    }
                } ?: emptyList()
            } else {
                throw task.exception ?: RuntimeException("Error fetching posts")
            }
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
    }
    fun followUser(followerId: String, followeeId: String): Task<Void> {
        val follow = Follow(followerId, followeeId)
        return db.collection("follows").document().set(follow)
    }
    fun unfollowUser(followerId: String, followeeId: String): Task<Void> {
        return db.collection("follows")
            .whereEqualTo("followerId", followerId)
            .whereEqualTo("followeeId", followeeId)
            .get()
            .continueWithTask { task ->
                if (!task.isSuccessful || task.result?.isEmpty == true) {
                    throw task.exception ?: Exception("No follow relationship found")
                }
                val document = task.result!!.documents[0]
                db.collection("follows").document(document.id).delete()
            }
    }
    fun isFollowing(followerId: String, followeeId: String): Task<Boolean> {
        return db.collection("follows")
            .whereEqualTo("followerId", followerId)
            .whereEqualTo("followeeId", followeeId)
            .get()
            .continueWith { task ->
                task.isSuccessful && task.result?.documents?.isNotEmpty() == true
            }
    }
    fun deletePost(postId: String): Task<Void> {
        val postDocumentRef = db.collection("posts").document(postId)

        // Get the post document to retrieve the imagePath
        return postDocumentRef.get().continueWithTask { task ->
            val imagePath = task.result?.getString("imagePath")

            // Start a batch write
            val batch = db.batch()

            // Delete the post document
            batch.delete(postDocumentRef)

            // Delete likes, this is a simple approach. Might need to change for larger scale.
            return@continueWithTask db.collection("Likes").whereEqualTo("postId", postId).get()
                .continueWithTask { likesTask ->
                    for (document in likesTask.result) {
                        batch.delete(document.reference)
                    }
                    batch.commit()
                }.continueWithTask {
                    // Delete the image from Firebase Storage
                    if (imagePath != null) {
                        FirebaseStorage.getInstance().getReference(imagePath).delete()
                    } else {
                        Tasks.forException<Void>(Exception("Image path not found"))
                    }
                }
        }
    }
    fun deleteUserAccount(userId: String): Task<Void> {
        // Helper function to handle batch deletion of documents
        fun deleteDocuments(task: Task<QuerySnapshot>): Task<Void> {
            val batch = db.batch()
            for (document in task.result.documents) {
                batch.delete(document.reference)
            }
            return batch.commit()
        }

        // Special handling for the 'follows' collection
        fun deleteFollows(): Task<Void> {
            val followerTask = db.collection("follows")
                .whereEqualTo("followerId", userId)
                .get()
                .continueWithTask { deleteDocuments(it) }

            val followeeTask = db.collection("follows")
                .whereEqualTo("followeeId", userId)
                .get()
                .continueWithTask { deleteDocuments(it) }

            return Tasks.whenAll(followerTask, followeeTask)
        }

        // Delete user's posts and associated images
        val deletePostsTask = db.collection("posts").whereEqualTo("userId", userId).get()
            .continueWithTask { task ->
                val deleteImageTasks = mutableListOf<Task<Void>>()
                val postsBatch = db.batch()  // New WriteBatch instance

                for (post in task.result.documents) {
                    val imagePath = post.getString("imagePath")
                    imagePath?.let {
                        deleteImageTasks.add(storageReference.child(it).delete())
                    }
                    postsBatch.delete(post.reference)  // Use the new batch instance
                }

                // Combine image deletion tasks with post deletion
                Tasks.whenAll(deleteImageTasks).continueWithTask {
                    postsBatch.commit()  // Commit the batch operation for deleting posts
                }
            }

        // Delete likes
        val deleteLikesTask = db.collection("Likes").whereEqualTo("userId", userId).get()
            .continueWithTask { deleteDocuments(it) }

        // Delete profile pictures
        val deleteProfilePicsTask = storageReference.child("user/$userId/profilepic").listAll()
            .continueWithTask { listResult ->
                val deleteTasks = listResult.result.items.map { it.delete() }
                Tasks.whenAll(deleteTasks)
            }

        // Delete follows (both follower and followee)
        val deleteFollowsTask = deleteFollows()

        // Task to delete the user document from the 'users' collection
        val deleteUserDocumentTask = db.collection("users").document(userId).delete()

        // Combine all tasks and then delete the user from Firebase Authentication
        return Tasks.whenAll(deletePostsTask, deleteLikesTask, deleteFollowsTask, deleteProfilePicsTask, deleteUserDocumentTask)
            .continueWithTask {
                val user = FirebaseAuth.getInstance().currentUser
                user?.delete() ?: Tasks.forException(Exception("No authenticated user found"))
            }
    }



}


