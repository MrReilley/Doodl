package com.example.doodl.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.doodl.R
import com.example.doodl.data.Post
import com.example.doodl.data.repository.Repository
import com.example.doodl.ui.RoundImageCard
import com.example.doodl.ui.RoundImageCardFeed
import com.example.doodl.viewmodel.FeedViewModel
import com.example.doodl.viewmodel.FeedViewModelFactory

@Composable
fun FeedScreen(userId: String, navBarHeight: Int) {
    BackHandler {
        // Do nothing, effectively disabling the back button
    }
    val repository = Repository()
    val feedViewModel:FeedViewModel = viewModel(factory = FeedViewModelFactory(userId, repository))
    val newestPosts by feedViewModel.newestPosts.observeAsState(emptyList())
    val userLikesAPost by feedViewModel.userLikedAPost.observeAsState(emptyList())
    val postTags by feedViewModel.postTags.observeAsState(emptyMap())
    // Fetch images once the composable is launched
    LaunchedEffect(feedViewModel) {
        feedViewModel.fetchNewestPosts()
        feedViewModel.fetchUserLikedAPost()
    }
    ImageFeed(newestPosts, userLikesAPost, postTags, feedViewModel, navBarHeight)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImageFeed(posts: List<Post>, userLikedPosts: List<String>, postTags: Map<String, List<String>>, feedViewModel: FeedViewModel, navBarHeight: Int) {
    // Obtain the context using LocalContext.current
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp
    val maxFeedHeight = screenHeightDp - navBarHeight
    // Display a vertical list of images, filling the available space
    LazyColumn(
        modifier = Modifier.fillMaxHeight().heightIn(max = maxFeedHeight.dp)
    ) {
        itemsIndexed(posts) {index, post ->
            val isLastItem = index == posts.size - 1

            // For each image, create an Image composable
            val isLiked = userLikedPosts.contains(post.postId)
            var applyColorFilter by remember { mutableStateOf(isLiked) }
            // background of feed
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(Color.White)
                ) {
                    // feed card layout
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RoundImageCardFeed(
                            url = post.profilePicUrl ?: "", // Pass the profilePicUrl here
                            Modifier
                                .size(48.dp)
                                .padding(4.dp)
                        )
                        Text(text = post.username ?: "Anonymous", fontWeight = FontWeight.Bold, color=Color.Black)
                    }
                    if (!post.imageUrl.isNullOrEmpty()) {
                        Image(
                            // Convert Bitmap to a format Image composable understands and renders it
                            painter = rememberAsyncImagePainter(model = post.imageUrl), // Use Coil to load image from URL
                            contentDescription = null,
                            // Style modifiers to control the layout and appearance of the image
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.68f)
                                .padding(8.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Display alternative content for empty imageUrl
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.68f)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No Image Available", color = Color.Black)
                        }
                    }
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
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
                        Text(text = "likes", modifier = Modifier.padding(start = 8.dp), color = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        /*Image(
                            painter = painterResource(id = R.drawable.downloadicon),
                            contentDescription = "Download",
                            modifier = Modifier.clickable {
                                val message = "This is a fake download lol"
                                val duration = Toast.LENGTH_SHORT // or Toast.LENGTH_LONG
                                // Display a toast message using the obtained context
                                Toast.makeText(context, message, duration).show()
                            }.size(26.dp)
                        )*/
                    }
                    val tagsForThisPost = postTags[post.postId] ?: emptyList()
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalArrangement = Arrangement.Center
                    ) {
                        tagsForThisPost.forEach { tag ->
                            Text(
                                text = "#$tag",
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.Gray.copy(alpha = 0.2f))
                                    .padding(start = 4.dp, end = 4.dp),
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
            if (isLastItem) {
                Spacer(modifier = Modifier.padding(bottom = 65.dp))
            }
        }
    }
}