package com.example.doodl.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import coil.imageLoader
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.example.doodl.data.Like
import com.example.doodl.data.Post
import com.example.doodl.data.repository.Repository
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID


// A ViewModel is a component used to store and manage UI-related data in a way that survives configuration changes
// (like screen rotations) and is independent of the UI components (e.g. Activities). They are designed to store and manage UI-related
// data and logic separately from the user interface. Acts as a bridge between the UI and the underlying data sources.

class FeedViewModel(private val userId: String, private val repository: Repository) : ViewModel() {
    // LiveData to hold a list of Bitmap images from Firebase.
    private val _newestPosts = MutableLiveData<List<Post>>()
    private val _userImageUrls = MutableLiveData<List<String>>()
    private val _likesCountForPosts = MutableLiveData<Map<String, Int>>()
    private val _userLikedAPost = MutableLiveData<List<String>>()
    private val _likedPosts = MutableLiveData<List<Post>>()
    private val _postLikesCount = MutableLiveData<Map<String, Int>>()
    private val _postTags = MutableLiveData<Map<String, List<String>>>()
    private val _profileImages = MutableLiveData<List<String>>()

    val newestPosts: LiveData<List<Post>> get() = _newestPosts
    val userImageUrls: LiveData<List<String>> get() = _userImageUrls
    val likesCountForPosts: LiveData<Map<String, Int>> get() = _likesCountForPosts
    val userLikedAPost: LiveData<List<String>> get() = _userLikedAPost
    val likedPosts: LiveData<List<Post>> get() = _likedPosts
    val postLikesCount: LiveData<Map<String, Int>> get() = _postLikesCount
    val postTags: LiveData<Map<String, List<String>>> get() = _postTags
    val profileImages: LiveData<List<String>> = _profileImages


    var userName = MutableLiveData<String?>()
    var userBio = MutableLiveData<String?>()
    var profilePic = MutableLiveData<String?>()

    var lastLikeTimestamp = 0L // Timestamp of the last like action
    val likeCooldown = 1000L // Minimum cooldown period between likes in milliseconds

    // Function to fetch all images from Firebase storage and update `_images` LiveData.
    fun fetchUserImageUrls() {
        repository.getUserImages(userId, onSuccess = { imagePaths ->
            viewModelScope.launch {
                val urls = imagePaths.mapNotNull { path ->
                    withContext(Dispatchers.IO) {
                        try {
                            repository.getImageUrl(path).await()
                        } catch (exception: Exception) {
                            Log.e("FeedViewModel", "URL fetch failed: ${exception.message}")
                            null
                        }
                    }
                }
                _userImageUrls.value = urls
            }
        }, onFailure = { exception ->
            Log.e("FeedViewModel", "Fetching paths failed: ${exception.message}")
        })
    }
    fun fetchProfileImages() {
        viewModelScope.launch {
            try {
                val images = repository.fetchProfileImages(userId).await()
                _profileImages.value = images
            } catch (exception: Exception) {
                Log.e("FeedViewModel", "Error fetching profile images: ${exception.message}")
            }
        }
    }
    fun fetchUserDetails(userId: String) {
        viewModelScope.launch {
            try {
                val document = repository.getUserDetails(userId).await()
                if (document.exists()) {
                    userName.value = document.getString("username") ?: "Anonymous"
                    userBio.value = document.getString("userBio")

                    val profilePicPath = document.getString("profilePicPath")
                    if (!profilePicPath.isNullOrEmpty()) {
                        try {
                            // Fetch URL from the path
                            val profilePicUrl = repository.getProfilePicUrl(profilePicPath).await()
                            profilePic.value = profilePicUrl // Store the URL
                        } catch (exception: Exception) {
                            Log.e("FeedViewModel", "Profile picture URL fetch failed: ${exception.message}")
                            profilePic.value = null // Set to null if fetching URL fails
                        }
                    } else {
                        profilePic.value = null
                    }
                } else {
                    // Handle the case where the document doesn't exist.
                    profilePic.value = null
                }
            } catch (e: Exception) {
                Log.e("FeedViewModel", "Error fetching user details: ${e.message}")
                profilePic.value = null
            }
        }
    }//modified for profile showing profile pic

    fun fetchNewestPosts() {
        viewModelScope.launch {
            try {
                val posts = repository.getNewestPosts().await()
                val updatedPosts = posts.mapNotNull { post ->
                    val username = repository.getUserDetails(post.userId).await().getString("username") ?: "Anonymous"
                    val imageUrl = try {
                        repository.getImageUrl(post.imagePath).await()
                    } catch (exception: Exception) {
                        "" // Fallback to empty string if image URL fetching fails
                    }
                    val profilePicUrl = try {
                        post.profilePicPath?.let { repository.getProfilePicUrl(it).await() } ?: ""
                    } catch (exception: Exception) {
                        "" // Fallback to empty string if profile picture URL fetching fails
                    }
                    fetchTagsForPost(post.postId)
                    post.copy(imageUrl = imageUrl, username = username, profilePicUrl = profilePicUrl)
                }
                _newestPosts.value = updatedPosts
            } catch (exception: Exception) {
                Log.e("FeedViewModel", "Error fetching newest posts: ${exception.message}")
            }
        }
    }

    fun fetchLikedPosts() {
        viewModelScope.launch {
            try {
                // Fetch liked post IDs
                val likedPostIds = repository.getLikedPostsForUser(userId).await().documents.mapNotNull { it.getString("postId") }

                // Fetch post data for each liked post ID
                val likedPostsData = likedPostIds.mapNotNull { postId ->
                    val post = repository.getPostData(postId).await().toObject(Post::class.java)
                    try {
                        // Attempt to fetch the image URL
                        post?.copy(imageUrl = repository.getImageUrl(post.imagePath).await())
                    } catch (e: Exception) {
                        // If URL fetching fails, return the post without modifying imageUrl
                        Log.e("FeedViewModel", "Error fetching image URL for post $postId: ${e.message}")
                        post
                    }
                }

                // Update LiveData with the fetched posts
                _likedPosts.value = likedPostsData
            } catch (exception: Exception) {
                Log.e("FeedViewModel", "Error fetching liked posts: ${exception.message}")
            }
        }
    }


    fun likePost(postId: String) {
        val currentTimestamp = System.currentTimeMillis()
        if (currentTimestamp - lastLikeTimestamp < likeCooldown) {
            Log.d("FeedViewModel", "Like action is on cooldown.")
            return
        }

        lastLikeTimestamp = currentTimestamp

        repository.isPostLikedByUser(postId, userId).addOnSuccessListener { querySnapshot ->
            if (querySnapshot.isEmpty) { // if not liked yet
                val likeId = UUID.randomUUID().toString()
                val like = Like(likeId, userId, postId, System.currentTimeMillis())
                repository.addLike(like).addOnSuccessListener {
                    Log.d("FeedViewModel", "Successfully liked post")
                    // Optionally, update likes count or list of liked posts
                }.addOnFailureListener { exception ->
                    Log.e("FeedViewModel", "Error liking post: ${exception.message}")
                }
            }
        }.addOnFailureListener { exception ->
            Log.e("FeedViewModel", "Error checking if post is liked: ${exception.message}")
        }
        val currentLikes = _userLikedAPost.value.orEmpty().toMutableList()
        if (!currentLikes.contains(postId)) {
            currentLikes.add(postId)
            _userLikedAPost.value = currentLikes
        }
    }

    fun unlikePost(postId: String) {
        val currentTimestamp = System.currentTimeMillis()
        if (currentTimestamp - lastLikeTimestamp < likeCooldown) {
            Log.d("FeedViewModel", "Unlike action is on cooldown.")
            return
        }

        lastLikeTimestamp = currentTimestamp

        repository.isPostLikedByUser(postId, userId).addOnSuccessListener { querySnapshot ->
            val likeDocument = querySnapshot.documents.firstOrNull()
            likeDocument?.let {
                repository.removeLike(it.id).addOnSuccessListener {
                    Log.d("FeedViewModel", "Successfully unliked post")
                    // Optionally, update likes count or list of liked posts
                }.addOnFailureListener { exception ->
                    Log.e("FeedViewModel", "Error unliking post: ${exception.message}")
                }
            }
        }.addOnFailureListener { exception ->
            Log.e("FeedViewModel", "Error finding like for the post: ${exception.message}")
        }
        val currentLikes = _userLikedAPost.value.orEmpty().toMutableList()
        currentLikes.remove(postId)
        _userLikedAPost.value = currentLikes
    }
    fun fetchLikesCountForPost(postId: String) {
        repository.getLikesCountForPost(postId).addOnSuccessListener { querySnapshot ->
            val likesCount = querySnapshot.size()
            _postLikesCount.value = mapOf(postId to likesCount)
        }.addOnFailureListener { exception ->
            Log.e("FeedViewModel", "Error fetching likes count for post: ${exception.message}")
        }
    }
    fun fetchUserLikedAPost() {
        repository.getLikedPostsForUser(userId).addOnSuccessListener { querySnapshot ->
            val likedPostIds = querySnapshot.documents.mapNotNull { document ->
                document.getString("postId")
            }
            _userLikedAPost.value = likedPostIds
        }.addOnFailureListener { exception ->
            Log.e("FeedViewModel", "Error fetching user's liked posts: ${exception.message}")
        }
    }

    fun fetchTagsForPost(postId: String) {
        viewModelScope.launch {
            try {
                val tags = repository.getTagsForPost(postId).await()
                val currentTags = _postTags.value?.toMutableMap() ?: mutableMapOf()
                currentTags[postId] = tags
                _postTags.value = currentTags.toMap()
            } catch (exception: Exception) {
                Log.e("FeedViewModel", "Error fetching tags for post: ${exception.message}")
            }
        }
    }

    fun updateProfile(newUsername: String, newBio: String, imageByteArray: ByteArray?) {
        Log.d("FeedViewModel", "Updating profile - started")
        viewModelScope.launch {
            try {
                val updateProfilePic = imageByteArray != null
                val newProfilePicPath = if (updateProfilePic) {
                    // Upload new image and get the storage path
                    repository.uploadProfileImage(userId, imageByteArray!!).await()
                } else {
                    // Keep the current profile picture path
                    profilePic.value ?: ""
                }

                // Fetch the download URL only if a new image has been uploaded
                val newProfilePicUrl = if (updateProfilePic) {
                    repository.getImageUrl(newProfilePicPath).await()
                } else {
                    profilePic.value ?: ""
                }

                // Update user's profile in Firestore
                repository.updateUserProfile(userId, newUsername, newBio, newProfilePicPath).await()

                // Update user's posts with new username and optionally new profile picture URL
                repository.updateUserPostsUsername(userId, newUsername, newProfilePicPath).await()

                // Update LiveData
                userName.value = newUsername
                userBio.value = newBio
                profilePic.value = newProfilePicUrl

                // Notify UI of successful update
                Log.d("FeedViewModel", "Updating profile - success")
            } catch (exception: Exception) {
                Log.e("FeedViewModel", "Error updating user information: ${exception.message}")
            }
        }
    } //new
    fun updateProfileWithImageUrl(newUsername: String, newBio: String, imageUrl: String) {
        Log.d("FeedViewModel", "Updating profile with image URL - started")
        viewModelScope.launch {
            try {
                // Extract the path from the URL
                val imagePath = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl).path

                // Update user's profile in Firestore
                repository.updateUserProfile(userId, newUsername, newBio, imagePath).await()

                // Update user's posts with new username and new profile picture URL
                repository.updateUserPostsUsername(userId, newUsername, imagePath).await()

                // Update LiveData
                userName.value = newUsername
                userBio.value = newBio
                profilePic.value = imageUrl // Store the full URL
                // Notify UI of successful update
                Log.d("FeedViewModel", "Updating profile with image URL - success")
            } catch (exception: Exception) {
                Log.e("FeedViewModel", "Error updating user information with image URL: ${exception.message}")
            }
        }
    }

    fun checkUsernameAvailability(username: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val isAvailable = repository.isUsernameAvailable(username).await()
                onResult(isAvailable)
            } catch (exception: Exception) {
                Log.e("FeedViewModel", "Error checking username availability: ${exception.message}")
                onResult(false) // Assume unavailable on error
            }
        }
    }//new
    suspend fun processImage(uri: Uri, context: Context): ByteArray? {
        return withContext(Dispatchers.IO) {
            // Set target size for the resized image to 400x400 pixels
            val targetSize = 400

            // Use Coil to load the image as a Bitmap, resize, and compress as PNG
            context.imageLoader.execute(ImageRequest.Builder(context)
                .data(uri)
                .size(targetSize)
                .apply {
                    // Additional logic to maintain aspect ratio and crop if necessary
                    transformations(CircleCropTransformation())
                }
                .build()).drawable?.toBitmap()?.let { bitmap ->
                // Compress the Bitmap to PNG and convert to ByteArray
                ByteArrayOutputStream().apply {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, this)
                }.toByteArray()
            }
        }
    }
    fun onImageSelected(uri: Uri, context: Context) {
        viewModelScope.launch {
            val processedImageBytes = processImage(uri, context)
            processedImageBytes?.let { imageBytes ->
                updateProfile(
                    newUsername = userName.value ?: "",
                    newBio = userBio.value ?: "",
                    imageByteArray = imageBytes
                )
            }
        }
    }

}


// Factory for creating FeedViewModel instances with a Repository dependency
class FeedViewModelFactory(private val userId: String, private val repository: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Check if modelClass is FeedViewModel
        if (modelClass.isAssignableFrom(FeedViewModel::class.java)) {
            // Return an instance of FeedViewModel, casting it to T
            return FeedViewModel(userId, repository) as T
        }
        // If modelClass isn’t FeedViewModel, throw an exception
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


