package com.example.doodl.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.doodl.data.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


// A ViewModel is a component used to store and manage UI-related data in a way that survives configuration changes
// (like screen rotations) and is independent of the UI components (e.g. Activities). They are designed to store and manage UI-related
// data and logic separately from the user interface. Acts as a bridge between the UI and the underlying data sources.

class FeedViewModel(private val repository: Repository) : ViewModel() {
    // LiveData to hold a list of Bitmap images from Firebase.
    private val _images = MutableLiveData<List<Bitmap>>()
    val images: LiveData<List<Bitmap>> = _images

    // Function to fetch all images from Firebase storage and update `_images` LiveData.
    fun fetchImages() {
        repository.fetchAllImages(onSuccess = { imagePaths ->
            viewModelScope.launch {
                val bitmaps = imagePaths.mapNotNull { path ->
                    // Launch a coroutine in IO dispatcher (optimized for I/O tasks)
                    withContext(Dispatchers.IO) {
                        try {
                            // Attempt to download the image and await its result
                            repository.downloadImage(path).await()
                        } catch (exception: Exception) {
                            // Log any errors during the download
                            Log.e("FeedViewModel", "Download failed: ${exception.message}")
                            null// Return null in case of failure
                        }
                    }
                }
                // Update _images LiveData with the fetched Bitmaps
                _images.value = bitmaps
            }
        }, onFailure = { exception ->
            Log.e("FeedViewModel", "Fetching paths failed: ${exception.message}")
        })
    }

    fun likePost(imageFileName: String, userId: String) {
        repository.likePost(imageFileName, userId)
            .addOnSuccessListener {
                Log.d("FeedViewModel", "Post liked successfully")
            }
            .addOnFailureListener { e ->
                Log.e("FeedViewModel", "Failed to like post", e)
            }
    }



}

// Factory for creating FeedViewModel instances with a Repository dependency
class FeedViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Check if modelClass is FeedViewModel
        if (modelClass.isAssignableFrom(FeedViewModel::class.java)) {
            // Return an instance of FeedViewModel, casting it to T
            return FeedViewModel(repository) as T
        }
        // If modelClass isn’t FeedViewModel, throw an exception
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

