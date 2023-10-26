package com.example.doodl.data.repository
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask


// Code that interacts with Firebase Storage
// API for data access for rest of application
// Data operations, abstracting origin of data
class Repository {

    //Firebase Storage
    private val storageReference = FirebaseStorage.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

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

    fun fetchAllImages(onSuccess: (List<String>) -> Unit, onFailure: (Exception) -> Unit) {
        // Reference to your 'feed' directory in Firebase storage
        val feedRef = storageReference.child("feed")

        // Retrieve all file references in 'feed' directory
        feedRef.listAll().addOnSuccessListener { listResult ->
            // Map the results to their paths and trigger the onSuccess callback
            val imagePaths = listResult.items.map { it.path }
            // Invoke onSuccess callback with the list of image paths
            onSuccess(imagePaths.reversed())//.reversed will reverse
        }.addOnFailureListener { exception ->
            // Trigger onFailure callback on error
            onFailure(exception)
        }
    }

}


