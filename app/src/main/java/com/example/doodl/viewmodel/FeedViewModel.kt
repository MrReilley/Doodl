package com.example.doodl.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.doodl.data.Like
import com.example.doodl.data.Post
import com.example.doodl.data.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
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

    val newestPosts: LiveData<List<Post>> get() = _newestPosts
    val userImageUrls: LiveData<List<String>> get() = _userImageUrls
    val likesCountForPosts: LiveData<Map<String, Int>> get() = _likesCountForPosts
    val userLikedAPost: LiveData<List<String>> get() = _userLikedAPost
    val likedPosts: LiveData<List<Post>> get() = _likedPosts
    val postLikesCount: LiveData<Map<String, Int>> get() = _postLikesCount
    val postTags: LiveData<Map<String, List<String>>> get() = _postTags


    val userName = MutableLiveData<String>()
    val userBio = MutableLiveData<String?>()
    val profilePic = MutableLiveData<Bitmap?>()

    var lastLikeTimestamp = 0L // Timestamp of the last like action
    val likeCooldown = 1000L // Minimum cooldown period between likes in milliseconds

    // Function to fetch all images from Firebase storage and update `_images` LiveData.
    fun fetchUserImageUrls() {
        repository.fetchUserImages(userId, onSuccess = { imagePaths ->
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

    fun fetchUserDetails(userId: String) {
        repository.getUserDetails(userId).addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                userName.value = document.getString("username") ?: "Anonymous"
                userBio.value = document.getString("userBio")

                val profilePicPath = document.getString("profilePicPath")
                if (profilePicPath != null) {
                    viewModelScope.launch {
                        withContext(Dispatchers.IO) {
                            try {
                                // If profilePicPath is not null, attempt to download the profile picture
                                val bitmap = repository.downloadImage(profilePicPath).await()
                                profilePic.postValue(bitmap)
                            } catch (exception: Exception) {
                                Log.e("FeedViewModel", "Profile picture download failed: ${exception.message}")
                            }
                        }
                    }
                } else {
                    profilePic.value = null
                }
            } else {
                // Handle the case where the document doesn't exist.
            }
        }.addOnFailureListener {
            // Handle any errors here.
        }
    }
    fun updateUserProfile(username: String, userBio: String) {
        repository.updateUserDetails(userId, username, userBio).addOnSuccessListener {
            // Successfully updated user details
            this.userName.value = username
            this.userBio.value = userBio
            // You can also show some feedback to the user here
        }.addOnFailureListener { exception ->
            // Handle the error
            Log.e("FeedViewModel", "Error updating user profile: ${exception.message}")
        }
    }

    fun fetchNewestPosts() {
        viewModelScope.launch {
            try {
                val posts = repository.getNewestPosts().await()
                val updatedPosts = posts.map { post ->
                    val username = repository.getUserDetails(post.userId).await().getString("username") ?: "Anonymous"
                    fetchTagsForPost(post.postId)
                    post.copy(imageUrl = repository.getImageUrl(post.imagePath).await(), username = username)
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
                // Step 1: Fetch liked post IDs
                val likedPostIds = repository.getLikedPostsForUser(userId).await().documents.mapNotNull { it.getString("postId") }

                // Step 2: Fetch post data for each liked post ID
                val likedPostsData = likedPostIds.map { postId ->
                    repository.getPostData(postId).await().toObject(Post::class.java)
                }.filterNotNull()

                // Step 3: Fetch URLs for the images of these posts
                val likedPostsWithUrls = likedPostsData.map { post ->
                    post.copy(imageUrl = repository.getImageUrl(post.imagePath).await())
                }

                // Step 4: Update LiveData with the fetched posts
                _likedPosts.value = likedPostsWithUrls
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


