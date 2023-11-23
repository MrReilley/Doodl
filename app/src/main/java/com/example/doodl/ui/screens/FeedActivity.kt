package com.example.doodl.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.AlertDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.doodl.viewmodel.FeedViewModel
import com.example.doodl.viewmodel.FeedViewModelFactory
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

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

    // Use rememberSwipeRefreshState to manage the refresh state
    val refreshState = rememberSwipeRefreshState(isRefreshing = false)

    // Fetch images once the composable is launched or when refreshing
    LaunchedEffect(feedViewModel, refreshState.isRefreshing) {
        feedViewModel.fetchNewestPosts()
        feedViewModel.fetchUserLikedAPost()
        refreshState.isRefreshing = false
    }

    SwipeRefresh(
        state = refreshState,
        onRefresh = {
            refreshState.isRefreshing = true
        }
    ) {
        ImageFeed(newestPosts, userLikesAPost, postTags, feedViewModel, navBarHeight)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImageFeed(posts: List<Post>, userLikedPosts: List<String>, postTags: Map<String, List<String>>, feedViewModel: FeedViewModel, navBarHeight: Int) {
    // Obtain the context using LocalContext.current
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp
    val maxFeedHeight = screenHeightDp - navBarHeight
    var showFilterDialog by remember { mutableStateOf(false) }

    // Display a vertical list of images, filling the available space
    Column {
        if (showFilterDialog) {
            FilterDialog(
                tags = postTags.values.flatten().distinct(),
                selectedTags = mutableListOf(),
                onFilterSelected = { selectedTags ->
                    // Handle the selected tags here
                    // You may want to filter the posts based on the selected tags
                    showFilterDialog = false
                },
                onDismissRequest = { showFilterDialog = false }
            )
        }
        IconButton(
            onClick = { showFilterDialog = true },
            modifier = Modifier
                .align(Alignment.End)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Filter, contentDescription = "Filter")
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .heightIn(max = maxFeedHeight.dp)
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
                            RoundImageCard(
                                image = R.drawable.profpic8,// needs replacing for user's pp
                                Modifier
                                    .size(48.dp)
                                    .padding(4.dp)
                            )
                            Text(text = post.username ?: "Anonymous", fontWeight = FontWeight.Bold, color=Color.Black)
                        }
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

}

/*@Composable
fun FilterDialog(
    tags: List<String>,
    selectedTags: Set<String>,
    onFilterSelected: (Set<String>) -> Unit,
    onDismissRequest: () -> Unit
) {
    var selectedTagsState by remember { mutableStateOf(selectedTags.toMutableSet()) }

    AlertDialog(
        onDismissRequest = {
            // Save the selected tags when the dialog is dismissed
            onFilterSelected(selectedTagsState)
            onDismissRequest()
        },
        title = {
            Text("Filter by Tags")
        },
        buttons = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = {
                    // Reset selected tags to an empty set
                    selectedTagsState = mutableSetOf()
                }) {
                    Text("Clear All")
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = {
                    // Save the selected tags and dismiss the dialog
                    onFilterSelected(selectedTagsState)
                    onDismissRequest()
                }) {
                    Text("Apply")
                }
            }
        },
        text = {
            // Display checkboxes for each tag
            tags.forEach { tag ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Checkbox(
                        checked = selectedTagsState.contains(tag),
                        onCheckedChange = {
                            // Update the selected tags when the checkbox state changes
                            if (it) {
                                selectedTagsState.add(tag)
                            } else {
                                selectedTagsState.remove(tag)
                            }
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(tag)
                }
            }
        }
    )
}*/
@Composable
fun FilterDialog(
    tags: List<String>,
    selectedTags: MutableList<String>,
    onFilterSelected: (List<String>) -> Unit,
    onDismissRequest: () -> Unit
) {
    var selectedTagsState by remember { mutableStateOf(selectedTags.toMutableList()) }

    AlertDialog(
        onDismissRequest = {
            // Save the selected tags when the dialog is dismissed
            onFilterSelected(selectedTagsState)
            onDismissRequest()
        },
        title = {
            Text("Filter by Tags")
        },
        buttons = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = {
                    // Reset selected tags to an empty list
                    selectedTagsState = mutableListOf()
                }) {
                    Text("Clear All")
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = {
                    // Save the selected tags and dismiss the dialog
                    onFilterSelected(selectedTagsState)
                    onDismissRequest()
                }) {
                    Text("Apply")
                }
            }
        },
        text = {
            // Display checkboxes for each tag
            tags.forEach { tag ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    TagButton(tag, selectedTagsState)
                }
            }
        }
    )
}