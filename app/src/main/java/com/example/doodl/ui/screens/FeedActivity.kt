package com.example.doodl.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
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
    val userLikedPosts by feedViewModel.userLikedPosts.observeAsState(emptyList())

    // Fetch images once the composable is launched
    LaunchedEffect(feedViewModel) {
        feedViewModel.fetchNewestPosts()
        feedViewModel.fetchUserLikedPosts()
    }
    ImageFeed(newestPosts, userLikedPosts, feedViewModel)
}

@Composable
fun ImageFeed(posts: List<Post>, userLikedPosts: List<String>, feedViewModel: FeedViewModel) {
    // Obtain the context using LocalContext.current
    val context = LocalContext.current

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
                    .background(Color.LightGray)
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
                    }
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
                            }
                        )
                        Text(text = "likes", modifier = Modifier.padding(start = 8.dp))

                        Spacer(modifier = Modifier.width(8.dp))

                        Image(
                            painter = painterResource(id = R.drawable.downloadicon),
                            contentDescription = "Download",
                            modifier = Modifier.clickable {
                                val message = "This is a fake download lol"
                                val duration = Toast.LENGTH_SHORT // or Toast.LENGTH_LONG
                                // Display a toast message using the obtained context
                                Toast.makeText(context, message, duration).show()
                            }.size(26.dp)
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