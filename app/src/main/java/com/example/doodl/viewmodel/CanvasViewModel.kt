package com.example.doodl.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.doodl.data.Post
import com.example.doodl.data.repository.Repository
import com.google.firebase.auth.FirebaseAuth
import java.io.ByteArrayOutputStream
import java.util.UUID

// A ViewModel is a component used to store and manage UI-related data in a way that survives configuration changes
// (like screen rotations) and is independent of the UI components (e.g. Activities). They are designed to store and manage UI-related
// data and logic separately from the user interface. Acts as a bridge between the UI and the underlying data sources.

// This ViewModel will hold the drawing data and facilitate its upload to Firebase Storage,
// ensuring a seamless and responsive user experience to help keep track of users' drawings and manage the process of sending them to
// Firebase Storage without tying this logic directly into UI components, enhancing code organization and maintainability.

class CanvasViewModel(private val repository: Repository) : ViewModel() {

    fun uploadDrawing(bitmap: Bitmap) {
        try {
            val byteArray = bitmapToByteArray(bitmap)
            val uploadTask = repository.uploadByteArray(byteArray)
            uploadTask.addOnSuccessListener {
                // Get the image path
                val imagePath = it.storage.path

                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@addOnSuccessListener

                // Fetch username and then save post to Firestore
                repository.fetchUsername(userId).addOnSuccessListener { username ->
                    // Create a Post object
                    val postId = UUID.randomUUID().toString()
                    val timestamp = System.currentTimeMillis()
                    val newPost = Post(postId, userId, username ?: "Anonymous", timestamp, imagePath)

                    // Save the post to Firestore
                    repository.savePostToFirestore(newPost)
                        .addOnSuccessListener {
                            Log.d("CanvasViewModel", "Post saved successfully")
                        }.addOnFailureListener { exception ->
                            Log.e("CanvasViewModel", "Failed to save post: ${exception.message}")
                        }
                }.addOnFailureListener { exception ->
                    Log.e("CanvasViewModel", "Failed to fetch username: ${exception.message}")
                }

            }.addOnFailureListener { exception: Exception ->
                Log.e("CanvasViewModel", "Upload failed: ${exception.message}")
            }
        } catch (e: Exception) {
            Log.e("CanvasViewModel", "Bitmap to byte array conversion failed: ${e.message}")
        }
    }

    @Throws(Exception::class)
    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
            throw Exception("Failed to compress bitmap")
        }
        return stream.toByteArray()
    }

    fun saveBitmapToInternalStorage(bitmap: Bitmap, context: Context): String? {
        val filename = "${System.currentTimeMillis()}.png"
        return try {
            context.openFileOutput(filename, Context.MODE_PRIVATE).use { fileOutputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            }
            context.getFileStreamPath(filename).absolutePath
        } catch (e: Exception) {
            Log.e("CanvasViewModel", "Failed to save bitmap to internal storage: ${e.message}")
            return null
        }
    }
}

// Factory for creating CanvasViewModel instances with a Repository dependency
class CanvasViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
    // Creates a ViewModel of type T
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Check if modelClass is type CanvasViewModel
        if (modelClass == CanvasViewModel::class.java) {
            return CanvasViewModel(repository) as T // TODO uncheck cast
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
