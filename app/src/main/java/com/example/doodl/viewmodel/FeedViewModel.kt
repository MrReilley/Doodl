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
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
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
    // LiveData
    private val _newestPosts = MutableLiveData<List<Post>>()
    private val _userPosts = MutableLiveData<List<Post>>()
    private val _likesCountForPosts = MutableLiveData<Map<String, Int>>()
    private val _userLikedAPost = MutableLiveData<List<String>>()
    private val _likedPosts = MutableLiveData<List<Post>>()
    private val _postLikesCount = MutableLiveData<Map<String, Int>>()
    private val _postTags = MutableLiveData<Map<String, List<String>>>()
    private val _profileImages = MutableLiveData<List<String>>()
    private var lastVisiblePost: DocumentSnapshot? = null
    private val _isFetchingPosts = MutableLiveData<Boolean>(false)
    private val _isFetchingUserPosts = MutableLiveData<Boolean>(false)
    private val _isFetchingLikedPosts = MutableLiveData<Boolean>(false)
    //private val _isFollowingUser = MutableLiveData<Boolean>()
    private val _followStatusMap = MutableLiveData<Map<String, Boolean>>().apply { value = emptyMap() }

    val newestPosts: LiveData<List<Post>> get() = _newestPosts
    val userPosts: LiveData<List<Post>> get() = _userPosts
    val likesCountForPosts: LiveData<Map<String, Int>> get() = _likesCountForPosts
    val userLikedAPost: LiveData<List<String>> get() = _userLikedAPost
    val likedPosts: LiveData<List<Post>> get() = _likedPosts
    val postLikesCount: LiveData<Map<String, Int>> get() = _postLikesCount
    val postTags: LiveData<Map<String, List<String>>> get() = _postTags
    val profileImages: LiveData<List<String>> = _profileImages
    val isFetchingPosts: LiveData<Boolean> = _isFetchingPosts
    val isFetchingUserPosts: LiveData<Boolean> = _isFetchingUserPosts
    val isFetchingLikedPosts: LiveData<Boolean> = _isFetchingLikedPosts
    //val isFollowingUser: LiveData<Boolean> = _isFollowingUser
    val followStatusMap: LiveData<Map<String, Boolean>> = _followStatusMap


    var userName = MutableLiveData<String?>()
    var userBio = MutableLiveData<String?>()
    var profilePic = MutableLiveData<String?>()

    var lastLikeTimestamp = 0L // Timestamp of the last like action
    val likeCooldown = 1000L // Minimum cooldown period between likes in milliseconds

    val currentUserID: String
        get() = userId

    // Function to fetch all images from Firebase storage and update `_images` LiveData.
    fun fetchUserPosts() {
        _isFetchingUserPosts.value = true
        viewModelScope.launch {
            try {
                // Fetch the list of post IDs created by the user
                val userPostIds = repository.getUserPostIds(userId).await()

                // Fetch post data for each post ID
                val userPostsData = userPostIds.mapNotNull { postId ->
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
                _userPosts.value = userPostsData
            } catch (exception: Exception) {
                Log.e("FeedViewModel", "Error fetching user's posts: ${exception.message}")
            }
            _isFetchingUserPosts.value = false
        }
    }
    fun fetchProfileImages() {
        viewModelScope.launch {
            try {
                val images = repository.getProfileImages(userId).await()
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

    fun fetchNewestPostsPaginated() {
        if (_isFetchingPosts.value == true) return

        _isFetchingPosts.value = true
        viewModelScope.launch {
            try {
                val posts = repository.getNewestPosts(lastVisiblePost).await()
                val updatedPosts = posts.mapNotNull { post ->
                    try {
                        // Fetch additional details for each post
                        val username = repository.getUserDetails(post.userId).await().getString("username") ?: "Anonymous"
                        val imageUrl = try {
                            repository.getImageUrl(post.imagePath).await()
                        } catch (e: Exception) {
                            "" // Fallback to empty string if image URL fetching fails
                        }
                        val profilePicUrl = try {
                            post.profilePicPath?.let { repository.getProfilePicUrl(it).await() } ?: ""
                        } catch (e: Exception) {
                            "" // Fallback to empty string if profile picture URL fetching fails
                        }
                        // Return the updated post
                        fetchTagsForPost(post.postId)
                        post.copy(username = username, imageUrl = imageUrl, profilePicUrl = profilePicUrl)
                    } catch (e: Exception) {
                        null // Exclude the post if any error occurs
                    }
                }
                if (updatedPosts.isNotEmpty()) {
                    lastVisiblePost = posts.last().snapshot
                    val currentPosts = _newestPosts.value.orEmpty()
                    _newestPosts.value = currentPosts + updatedPosts
                    // Check the follow status for each unique user in these posts
                    val uniqueUserIds = updatedPosts.map { it.userId }.distinct()
                    uniqueUserIds.forEach { userId ->
                        if (userId != this@FeedViewModel.userId) {
                            checkIfFollowing(userId)
                        }
                    }
                }
                _isFetchingPosts.value = false
            } catch (exception: Exception) {
                Log.e("FeedViewModel", "Error fetching newest posts: ${exception.message}")
                _isFetchingPosts.value = false
            }
        }
    }

    fun fetchLikedPosts() {
        _isFetchingLikedPosts.value = true
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
            _isFetchingLikedPosts.value = false
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
    fun followUser(followeeId: String) {
        viewModelScope.launch {
            try {
                repository.followUser(userId, followeeId).await()
                updateFollowStatus(followeeId, true) // Update the follow status for this specific user
            } catch (e: Exception) {
                Log.e("FeedViewModel", "Error following user: ${e.message}")
            }
        }
    }
    fun unfollowUser(followeeId: String) {
        viewModelScope.launch {
            try {
                repository.unfollowUser(userId, followeeId).await()
                updateFollowStatus(followeeId, false) // Update the follow status for this specific user
            } catch (e: Exception) {
                Log.e("FeedViewModel", "Error unfollowing user: ${e.message}")
            }
        }
    }
    fun updateFollowStatus(followeeId: String, isFollowing: Boolean) {
        _followStatusMap.value = _followStatusMap.value.orEmpty() + (followeeId to isFollowing)
    }

    fun checkIfFollowing(followeeId: String) {
        viewModelScope.launch {
            try {
                val isFollowing = repository.isFollowing(userId, followeeId).await()
                updateFollowStatus(followeeId, isFollowing)
            } catch (e: Exception) {
                Log.e("FeedViewModel", "Error checking following status: ${e.message}")
            }
        }
    }
    fun deletePost(postId: String) {
        viewModelScope.launch {
            try {
                repository.deletePost(postId).await()
                // Remove the post from the list and update LiveData
                val updatedPosts = _newestPosts.value?.filterNot { it.postId == postId }
                _newestPosts.value = updatedPosts
            } catch (e: Exception) {
                Log.e("FeedViewModel", "Error deleting post: ${e.message}")
            }
        }
    }
    fun deleteAccount(userId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteUserAccount(userId).await()
                onComplete() // Call the completion lambda after successful account deletion
            } catch (e: Exception) {
                Log.e("FeedViewModel", "Error deleting account: ${e.message}")
            }
        }
    }
    fun reAuthenticateUser(password: String, onResult: (Boolean) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        val credential = EmailAuthProvider.getCredential(user!!.email!!, password)

        user.reauthenticate(credential)
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful)
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


