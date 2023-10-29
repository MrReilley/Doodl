package com.example.doodl.ui.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.doodl.R
import com.example.doodl.data.Post
import com.example.doodl.data.repository.Repository
import com.example.doodl.viewmodel.FeedViewModel
import com.example.doodl.viewmodel.FeedViewModelFactory

@Composable
fun FeedScreen(userId: String) {
    BackHandler {
        // Do nothing, effectively disabling the back button
    }
    val repository = Repository()
    val feedViewModel:FeedViewModel = viewModel(factory = FeedViewModelFactory(userId, repository))
    val newestPosts by feedViewModel.newestPosts.observeAsState(emptyList())
    val userLikesAPost by feedViewModel.userLikedAPost.observeAsState(emptyList())

    // Fetch images once the composable is launched
    LaunchedEffect(feedViewModel) {
        feedViewModel.fetchNewestPosts()
        feedViewModel.fetchUserLikedAPost()
    }
    ImageFeed(newestPosts, userLikesAPost, feedViewModel)
    // Observe images LiveData and pass it to the ImageFeed composable.
    val images = feedViewModel.liveImages.observeAsState(emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.secondary)
    ) {
        ImageFeed(images.value){
            feedViewModel.fetchImages()
        }
    }
}

@Composable
fun ImageFeed(posts: List<Post>, userLikedPosts: List<String>, feedViewModel: FeedViewModel) {
    // Obtain the context using LocalContext.current
    val context = LocalContext.current
    val lazyListState = rememberLazyListState()
    /*Box(
        modifier = Modifier.fillMaxSize()
    ) {

    // Display a vertical list of images.
    LazyColumn {
        items(posts) { post ->
            // For each image, create an Image composable
            val isLiked = userLikedPosts.contains(post.postId)
            var applyColorFilter by remember { mutableStateOf(isLiked) }
            // background of feed
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.secondary)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(Color.White)
                ) {
                    // feed card layout
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RoundImageCard(
                            image = R.drawable.likeicon,
                            Modifier
                                .size(48.dp)
                                .padding(4.dp)
                        )
                        Text(text = post.username ?: "Anonymous", fontWeight = FontWeight.Bold)
                        androidx.compose.material3.Text(text = "userName", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    androidx.compose.material3.Divider(
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                    )
                    Image(
                        // Convert Bitmap to a format Image composable understands and renders it
                        painter = rememberAsyncImagePainter(model = post.imageUrl), // Use Coil to load image from URL
                        contentDescription = null,
                        // Style modifiers to control the layout and appearance of the image
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.68f) // changed aspect ratio (Old A.R: 1)
                            .padding(8.dp),
                        contentScale = ContentScale.Crop
                    )
                    androidx.compose.material3.Divider(
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                    )
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        //var applyColorFilter by remember { mutableStateOf(false) }

                        Image(
                            painter = painterResource(id = R.drawable.likeicon),
                            contentDescription = null,
                            colorFilter = if (!applyColorFilter) {
                                ColorFilter.tint(Color.LightGray)
                            } else {
                                ColorFilter.tint(Color.Red)
                            },
                            alignment = Alignment.TopEnd,
                            modifier = Modifier.clickable {
                                // Toggle the applyColorFilter when the image is clicked
                                val currentTimeStamp = System.currentTimeMillis()
                                if (currentTimeStamp - feedViewModel.lastLikeTimestamp >= feedViewModel.likeCooldown) {
                                    applyColorFilter = !applyColorFilter
                                    if(applyColorFilter){
                                        feedViewModel.likePost(post.postId)
                                        val message = "You liked a post"
                                        val duration = Toast.LENGTH_SHORT // or Toast.LENGTH_LONG
                                        // Display a toast message using the obtained context
                                        Toast.makeText(context, message, duration).show()
                                    }else{
                                        feedViewModel.unlikePost(post.postId)
                                        val message = "You disliked a post"
                                        val duration = Toast.LENGTH_SHORT // or Toast.LENGTH_LONG
                                        // Display a toast message using the obtained context
                                        Toast.makeText(context, message, duration).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Please wait before liking again.", Toast.LENGTH_SHORT).show()
                                }

                            }
                        )
                        androidx.compose.material3.Text(text = "likes", modifier = Modifier.padding(start = 8.dp), color = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Image(
                            painter = painterResource(id = R.drawable.downloadicon),
                            contentDescription = "Download",
                            modifier = Modifier
                                .clickable {
                                    val message = "This is a fake download lol"
                                    val duration = Toast.LENGTH_SHORT
                                    Toast
                                        .makeText(context, message, duration)
                                        .show()
                                }
                                .size(26.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun RoundImageCard(
    image: Int, modifier: Modifier = Modifier
        .padding(8.dp)
        .size(64.dp)
) {
    Card(shape = CircleShape, modifier = modifier) {
        Image(
            painter = painterResource(id = image),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
    }
}