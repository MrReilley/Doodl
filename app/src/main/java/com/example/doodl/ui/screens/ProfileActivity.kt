package com.example.doodl.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.doodl.R
import com.example.doodl.data.User
import com.example.doodl.data.Post
import com.example.doodl.data.repository.Repository
import com.example.doodl.ui.EditableTextField
import com.example.doodl.ui.ProfilePictureItem
import com.example.doodl.ui.ProfilePosts
import com.example.doodl.ui.RoundImageCard
import com.example.doodl.ui.logout
import com.example.doodl.viewmodel.FeedViewModel
import com.example.doodl.viewmodel.FeedViewModelFactory

private var lastUserProfile = mutableStateOf(User(R.drawable.likeicon, "userName", "This is for the bio/description box for the template section of the profile page :)."))

@Composable
fun ProfileScreen(userId: String, navController: NavController? = null) {
    BackHandler {
        // Do nothing, effectively disabling the back button
    }
    val repository = Repository()
    val feedViewModel:FeedViewModel = viewModel(factory = FeedViewModelFactory(userId, repository))
    // Fetch images once the composable is launched
    LaunchedEffect(feedViewModel) {
        feedViewModel.fetchUserImageUrls()
        feedViewModel.fetchUserDetails(userId)
        feedViewModel.fetchLikedPosts()
    }
    var profileImage by remember { mutableIntStateOf(lastUserProfile.value.profileImageResource) }
    var profUsername by remember { mutableStateOf(lastUserProfile.value.username) }
    var profDescription by remember { mutableStateOf(lastUserProfile.value.description) }

    // Observe images LiveData and pass it to the ImageFeed composable.
    val imageUrls = feedViewModel.userImageUrls.observeAsState(emptyList()).value

    val userName = feedViewModel.userName.observeAsState(initial = "Loading...").value
    val profilePicBitmap = feedViewModel.profilePic.observeAsState(null).value
    val userBioText = feedViewModel.userBio.observeAsState(null).value
    val likedPosts = feedViewModel.likedPosts.observeAsState(emptyList())



    Column(
        modifier = Modifier.fillMaxSize()
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.secondary)
        ){
                // feed card layout
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "")
                if (profUsername.length >= 14) {
                    Spacer(modifier = Modifier.width(110.dp))
                }else if(profUsername.length >= 20){
                    Spacer(modifier = Modifier.width(90.dp))
                }
                else{
                    Spacer(modifier = Modifier.width(140.dp))
                }
                val maxUsernameLength = 20
                Text(
                    text = userName,
                    text = if(profUsername.length > maxUsernameLength) {
                        profUsername.take(maxUsernameLength)
                    }else{
                        profUsername
                         },
                    fontWeight = FontWeight.Bold,
                    fontSize = if (profUsername.length >= 15) {
                        19.sp
                    }else if(profUsername.length >= 20){
                        16.sp
                    }else{
                        30.sp
                         },
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    color = Color.Black,
                )
                Spacer(modifier = Modifier.weight(0.9f))
                EditPopup(
                    profUsername,
                    profDescription,
                    profileImage,
                    selectedProfilePicture = remember { mutableIntStateOf(profileImage) },
                    onTextUpdated = { newUsername, newDescription, newProfilePicture ->
                        profUsername = newUsername
                        profDescription = newDescription
                        profileImage = newProfilePicture
                    }
                    )
                Spacer(modifier = Modifier.weight(0.15f))
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier
                        .clickable {
                            if (navController != null) {
                                logout(navController)
                            }
                        }
                )
                /*Text(
                    text = "logout",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.Blue,
                    modifier = Modifier
                        .clickable {
                            if (navController != null) {
                                logout(navController)
                            }
                        }
                        .fillMaxWidth()
                )*/
            }
            Spacer(modifier = Modifier.width(25.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                RoundImageCard(
                    image = profileImage,
                    Modifier
                        .size(125.dp)
                        .padding(4.dp)
                )
                Text(
                    text = userBioText ?: "No bio available.",
                    text = profDescription,
                    letterSpacing = 0.5.sp,
                    lineHeight = 20.sp,
                    softWrap = true,
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.width(25.dp))
            var selectedTabIndex by remember { mutableIntStateOf(0) }
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.background(Color.LightGray),
                containerColor = Color.Black,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    modifier = Modifier
                        .height(45.dp)
                ){
                    Image(
                        painter =  painterResource(id = R.drawable.ic_grid),
                        contentDescription = null
                    )
                }
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    modifier = Modifier
                        .height(45.dp)
                ){
                    Image(
                        painter =  painterResource(id = R.drawable.likeicon),
                        contentDescription = null
                    )
                }
            }
            when (selectedTabIndex) {
                0 -> {
                    Spacer(modifier = Modifier.width(20.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ProfileUsersPosts(imageUrls = imageUrls)
                    }
                }
                1 -> {
                    Spacer(modifier = Modifier.width(20.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ProfileLikedPosts(posts = likedPosts.value)
                    }
                }
            }
        }
    }
}

@Composable
fun EditPopup(oldUsername:String, oldDescription:String, oldImageResource: Int, selectedProfilePicture: MutableState<Int>, onTextUpdated: (newUsername: String, newDescription: String, newImageResource: Int) -> Unit) {
    var isEditable by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf(oldUsername) }
    var description by remember { mutableStateOf(oldDescription) }
    var profilePicture by remember { mutableIntStateOf(oldImageResource) }
    val profilePictures = listOf(
        R.drawable.downloadicon,
        R.drawable.eraser,
        R.drawable.ic_grid,
        R.drawable.likeicon
    )
    Column {
        /*ClickableText(
            text = AnnotatedString("edit"),
            onClick = {
                isEditable = true
            },
            style = TextStyle(
                color = Color.Blue,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(16.dp)
        )*/
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiaryContainer,
            modifier = Modifier
                .clickable {
                    isEditable = true
                }
        )
    }
    if (isEditable) {
        AlertDialog(
            onDismissRequest = {
                isEditable = false
            },
            title = {
                Text("Edit your profile")
            },
            confirmButton = {
                Button(
                    onClick = {
                        isEditable = false
                        onTextUpdated(username, description, profilePicture)
                        val updatedUserProfile = User(profilePicture, username, description)
                        lastUserProfile.value = updatedUserProfile
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text("Save")
                }
            },
            text = {
                Column {
                    Text(text = "Set your profile picture")
                    // List of selectable profile pictures
                    LazyRow {
                        items(profilePictures) { imageResource ->
                            ProfilePictureItem(
                                imageResource = imageResource,
                                isSelected = imageResource == selectedProfilePicture.value,
                                onProfilePictureSelected = {
                                    selectedProfilePicture.value = imageResource
                                    profilePicture = imageResource
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    EditableTextField("Username", username) { newText ->
                        username = newText
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    EditableTextField("Description", description) { newText ->
                        description = newText
                    }
                }
            }
        )
fun ProfileUsersPosts(imageUrls: List<String>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3)
    ) {
        itemsIndexed(imageUrls) { _, imageUrl ->
            Box(
                modifier = Modifier
                    .aspectRatio(1f) // Set the aspect ratio to make images square
                    .padding(8.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = imageUrl),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(1.1f) // Apply the scaling factor to individual images
                )
            }
        }
    }
}

@Composable
fun ProfileLikedPosts(posts: List<Post>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3)
    ) {
        items(posts) { post ->
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(8.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = post.imageUrl),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
