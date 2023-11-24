package com.example.doodl.ui.screens

import android.content.Context
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
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.doodl.ui.RoundImageCardFeed
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
    val isFetchingPosts by feedViewModel.isFetchingPosts.observeAsState(false)
    // Fetch images once the composable is launched
    /*LaunchedEffect(feedViewModel) {
        feedViewModel.fetchNewestPostsPaginated()
    }*/

    // Use rememberSwipeRefreshState to manage the refresh state
    val refreshState = rememberSwipeRefreshState(isRefreshing = false)

    // Fetch images once the composable is launched or when refreshing
    LaunchedEffect(feedViewModel, refreshState.isRefreshing) {
        feedViewModel.fetchNewestPostsPaginated()
        feedViewModel.fetchUserLikedAPost()
        refreshState.isRefreshing = false
    }
    // Check if posts are being fetched and the list is currently empty
    val showCenteredLoadingIndicator = isFetchingPosts && newestPosts.isEmpty()

    if (showCenteredLoadingIndicator) {
        // Show centered loading indicator
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.tertiary, strokeWidth = 2.dp)
        }
    } else {
        // Regular feed view
        SwipeRefresh(
            state = refreshState,
            onRefresh = {
                refreshState.isRefreshing = true
            }
        ) {
            ImageFeed(newestPosts, userLikesAPost, postTags, feedViewModel, navBarHeight)
        }
    }
}


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
    }
    // Display a vertical list of images
    LazyColumn(
        modifier = Modifier
            .fillMaxHeight()
            .heightIn(max = maxFeedHeight.dp)
    ) {
        itemsIndexed(posts) { index, post ->
            val isLastItem = index == posts.lastIndex

            PostItem(post, userLikedPosts, postTags, feedViewModel, context)

            if (isLastItem) {
                // Trigger loading more posts when the last post is displayed
                LaunchedEffect(key1 = Unit) {
                    feedViewModel.fetchNewestPostsPaginated()
                }
                Spacer(modifier = Modifier.height(65.dp))
            }
        }
    }
}
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostItem(post: Post, userLikedPosts: List<String>, postTags: Map<String, List<String>>, feedViewModel: FeedViewModel, context: Context) {
    val isLiked = userLikedPosts.contains(post.postId)
    var applyColorFilter by remember { mutableStateOf(isLiked) }
    val isFollowing =
        feedViewModel.followStatusMap.observeAsState().value?.get(post.userId) ?: false

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
            // User profile and post information
            Row(verticalAlignment = Alignment.CenterVertically) {
                RoundImageCardFeed(
                    url = post.profilePicUrl ?: "",
                    Modifier
                        .size(48.dp)
                        .padding(4.dp)
                )
                Text(
                    text = post.username ?: "Anonymous",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.weight(1f))
                if (post.userId != feedViewModel.currentUserID) {
                    Button(
                        onClick = {
                            if (isFollowing) {
                                feedViewModel.unfollowUser(post.userId)
                            } else {
                                feedViewModel.followUser(post.userId)
                            }// Might need to add a cooldown similar to likes
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text(if (isFollowing) "Unfollow" else "Follow", color = Color.White)
                    }
                }
            }

            // Post image
            if (!post.imageUrl.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(model = post.imageUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.68f)
                        .padding(8.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
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

            // Like button and tags
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like button
                Image(
                    painter = painterResource(id = R.drawable.likeicon),
                    contentDescription = null,
                    colorFilter = if (!applyColorFilter) {
                        ColorFilter.tint(Color.LightGray)
                    } else {
                        ColorFilter.tint(Color.Red)
                    },
                    alignment = Alignment.TopEnd,
                    modifier = Modifier
                        .clickable {
                            // Like/unlike post logic
                            val currentTimeStamp = System.currentTimeMillis()
                            if (currentTimeStamp - feedViewModel.lastLikeTimestamp >= feedViewModel.likeCooldown) {
                                applyColorFilter = !applyColorFilter
                                if (applyColorFilter) {
                                    feedViewModel.likePost(post.postId)
                                    Toast.makeText(context, "You liked a post", Toast.LENGTH_SHORT)
                                        .show()
                                } else {
                                    feedViewModel.unlikePost(post.postId)
                                    Toast.makeText(
                                        context,
                                        "You disliked a post",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Please wait before liking again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                )
                Text(text = "like", modifier = Modifier.padding(start = 8.dp), color = Color.Black)

                Spacer(modifier = Modifier.width(8.dp))

                // We can place the download icon & logic here
            }

            // Displaying tags
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
}
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