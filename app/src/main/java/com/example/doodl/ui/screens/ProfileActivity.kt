package com.example.doodl.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.tv.material3.ExperimentalTvMaterial3Api
import coil.compose.rememberAsyncImagePainter
import com.example.doodl.R
import com.example.doodl.data.Post
import com.example.doodl.data.User
import com.example.doodl.data.repository.Repository
import com.example.doodl.ui.EditableTextField
import com.example.doodl.ui.ProfilePictureItem
import com.example.doodl.ui.RoundImageCard
import com.example.doodl.ui.logout
import com.example.doodl.viewmodel.FeedViewModel
import com.example.doodl.viewmodel.FeedViewModelFactory

private var lastUserProfile = mutableStateOf(User(R.drawable.profpic8, "userName", "This is for the bio/description box for the template section of the profile page :)."))

@Composable
fun ProfileScreen(userId: String, navController: NavController? = null, navBarHeight: Int) {
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
    //var userName by remember { mutableStateOf(lastUserProfile.value.username) }
    //var userBioText by remember { mutableStateOf(lastUserProfile.value.description) }

    // Observe images LiveData and pass it to the ImageFeed composable.
    val imageUrls = feedViewModel.userImageUrls.observeAsState(emptyList()).value

    var userName = feedViewModel.userName.observeAsState(null).value
    val profilePicBitmap = feedViewModel.profilePic.observeAsState(null).value
    var userBioText = feedViewModel.userBio.observeAsState(null).value
    val likedPosts = feedViewModel.likedPosts.observeAsState(emptyList())
    val postTags by feedViewModel.postTags.observeAsState(emptyMap())

    val onNameUpdated: (String) -> Unit = { newName ->
        userName = newName
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ){
                // feed card layout
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "")
                Spacer(modifier = Modifier.width(15.dp))
                /*
                if (userName.length >= 14) {
                    Spacer(modifier = Modifier.width(110.dp))
                }else if(userName.length >= 20){
                    Spacer(modifier = Modifier.width(90.dp))
                }
                else{
                    Spacer(modifier = Modifier.width(140.dp))
                }*/

                userName?.let {
                    Text(
                        text = it,
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                }
                //Spacer(modifier = Modifier.weight(0.05f))
                Spacer(modifier = Modifier.width(155.dp))
                userName?.let {
                    EditPopup(
                        it,
                        userBioText,
                        profileImage,
                        selectedProfilePicture = remember { mutableIntStateOf(profileImage) },
                        onTextUpdated = { newUsername, newDescription, newProfilePicture ->
                            userName = newUsername
                            userBioText = newDescription
                            profileImage = newProfilePicture
                        },
                        onNameUpdated = onNameUpdated // Pass the lambda function
                    )
                }
                //Spacer(modifier = Modifier.weight(0.002f))
                Spacer(modifier = Modifier.width(15.dp))
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable {
                            if (navController != null) {
                                logout(navController)
                            }
                        }
                )
            }
            Spacer(modifier = Modifier.width(25.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                RoundImageCard(
                    image = profileImage,
                    Modifier
                        .size(115.dp)
                        .padding(5.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = userBioText ?: "No bio available.",
                    letterSpacing = 0.5.sp,
                    lineHeight = 20.sp,
                    softWrap = true,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(25.dp))
            var selectedTabIndex by remember { mutableIntStateOf(0) }
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer),
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
                        ProfileUsersPosts(imageUrls = imageUrls, navBarHeight = navBarHeight, postTags )
                    }
                }
                1 -> {
                    Spacer(modifier = Modifier.width(20.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ProfileLikedPosts(posts = likedPosts.value, navBarHeight, postTags)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun EditPopup(
    oldUsername: String,
    oldDescription: String?,
    oldImageResource: Int,
    selectedProfilePicture: MutableState<Int>,
    onTextUpdated: (newUsername: String, newDescription: String?, newImageResource: Int) -> Unit,
    onNameUpdated: (newName: String) -> Unit // Add a new callback for name update
) {
    var isEditable by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(oldUsername) } // Use editedName
    var description: String? by remember { mutableStateOf(oldDescription) }
    var profilePicture by remember { mutableIntStateOf(oldImageResource) }
    val profilePictures = listOf(
        R.drawable.profpic1,
        R.drawable.profpic2,
        R.drawable.profpic3,
        R.drawable.profpic8,
        R.drawable.profpic4,
        R.drawable.profpic5,
        R.drawable.profpic6,
        R.drawable.profpic7,
        R.drawable.profpic9,
        R.drawable.profpic10
    )

    Column {
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clickable {
                    isEditable = true
                }
        )

        if (isEditable) {
            AlertDialog(
                containerColor = Color.Black,
                modifier = Modifier.border(2.3.dp, Color.White, RoundedCornerShape(30.dp)),
                onDismissRequest = {
                    isEditable = false
                },
                title = {
                    Text("Edit your profile", color = Color.White)
                },
                confirmButton = {
                    Button(
                        onClick = {
                            isEditable = false
                            // Pass the editedName to the onNameUpdated callback
                            onNameUpdated(editedName)
                            onTextUpdated(editedName, description, profilePicture)
                            val updatedUserProfile = User(profilePicture, editedName, description)
                            lastUserProfile.value = updatedUserProfile
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Save", color = Color.Black)
                    }
                },
                text = {
                    Column {
                        Text(text = "Set your profile picture", color = Color.White)
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
                        // Use editedName for the name input field
                        EditableTextField("Username", editedName) { newText ->
                            editedName = newText // Update the editedName
                        }
                        Spacer(modifier = Modifier.height(20.dp))

                        var nDescription: String = description ?: ""
                        EditableTextField("Description", nDescription) { newText ->
                            nDescription = newText
                            description = nDescription
                        }
                    }
                }
            )
        }
    }
}

        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
fun ProfileUsersPosts(imageUrls: List<String>, navBarHeight: Int, postTags: Map<String, List<String>>) {
            //data class for the userPosts needs to be a thing just like the likedPosts
            val configuration = LocalConfiguration.current
            val maxScreenHeightDp = configuration.screenHeightDp.dp
            val maxScreenHeight = with(LocalDensity.current) { maxScreenHeightDp.toPx() }
            val availableHeight = maxScreenHeight - navBarHeight

            var isClicked by remember { mutableStateOf(false) }
            var clickedPost by remember { mutableStateOf<String?>(null) } // Store the clicked post
            Box(
                modifier = Modifier.fillMaxSize()
            ){
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.height(availableHeight.dp)
                ) {
                    itemsIndexed(imageUrls) { index, imageUrl ->
                        val itemsPerRow = 3
                        val rowNumber = index / itemsPerRow
                        val isLastRow = rowNumber == (imageUrls.size - 1) / itemsPerRow
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .padding(8.dp)
                                .clickable {
                                    clickedPost = imageUrl
                                    isClicked = true
                                }
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
                        if (isLastRow) {
                            Spacer(modifier = Modifier.padding(bottom = 195.dp))
                        }
                    }
                }
                if (isClicked && clickedPost != null) {
                    AlertDialog(
                        modifier = Modifier.border(2.3.dp, Color.White),
                        onDismissRequest = {
                            isClicked = false
                            clickedPost = null
                        }
                    ) {
                        clickedPost?.let { post ->
                            Box(
                                modifier = Modifier
                                    .width(550.dp)
                                    .height(600.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.White)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    ) {
                                        RoundImageCard(
                                            image = R.drawable.profpic8, // Replace with the user's profile pic
                                            modifier = Modifier
                                                .size(48.dp)
                                                .padding(4.dp)
                                        )
                                        Text(
                                            text = "Anonymous", //text = post.username ?: "Anonymous"
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    }

                                    Image(
                                        painter = rememberAsyncImagePainter(model = imageUrls),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(0.68f)
                                            .padding(bottom = 8.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                    /*val tagsForThisPost = postTags[imageUrls] ?: emptyList()
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Start,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        tagsForThisPost.forEach { tag ->
                                            Text(
                                                text = "#$tag",
                                                modifier = Modifier
                                                    .padding(2.dp)
                                                    .clip(RoundedCornerShape(2.dp))
                                                    .background(Color.Gray.copy(alpha = 0.2f))
                                                    .padding(start = 2.dp, end = 2.dp),
                                                color = Color.Gray
                                            )
                                        }
                                    }*/
                                }
                            }
                        }
                    }
                }
            }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileLikedPosts(posts: List<Post>, navBarHeight: Int, postTags: Map<String, List<String>>) {
    val configuration = LocalConfiguration.current
    val maxScreenHeightDp = configuration.screenHeightDp.dp
    val maxScreenHeight = with(LocalDensity.current) { maxScreenHeightDp.toPx() }
    val availableHeight = maxScreenHeight - navBarHeight

    var isClicked by remember { mutableStateOf(false) }
    var clickedPost by remember { mutableStateOf<Post?>(null) } // Store the clicked post

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.height(availableHeight.dp)
        ) {
            itemsIndexed(posts) { index, post ->
                val itemsPerRow = 3
                val rowNumber = index / itemsPerRow
                val isLastRow = rowNumber == (posts.size - 1) / itemsPerRow
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(1.75.dp)
                        .clickable {
                            clickedPost = post
                            isClicked = true
                        }
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(model = post.imageUrl),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                if (isLastRow) {
                    Spacer(modifier = Modifier.padding(bottom = 195.dp))
                }
            }
        }

        if (isClicked && clickedPost != null) {
            AlertDialog(
                modifier = Modifier.border(2.3.dp, Color.White),
                onDismissRequest = {
                    isClicked = false
                    clickedPost = null
                }
            ) {
                clickedPost?.let { post ->
                    Box(
                        modifier = Modifier
                            .width(550.dp)
                            .height(600.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 4.dp)
                            ) {
                                RoundImageCard(
                                    image = R.drawable.profpic8, // Replace with the user's profile pic
                                    modifier = Modifier
                                        .size(48.dp)
                                        .padding(4.dp)
                                )
                                Text(
                                    text = post.username ?: "Anonymous",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }

                            Image(
                                painter = rememberAsyncImagePainter(model = post.imageUrl),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(0.68f)
                                    .padding(bottom = 8.dp),
                                contentScale = ContentScale.Crop
                            )
                            /*Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.likeicon),
                                    contentDescription = null,
                                    colorFilter = ColorFilter.tint(Color.Red)
                                )
                            }*/

                            val tagsForThisPost = postTags[post.imageUrl] ?: emptyList()
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalArrangement = Arrangement.Center
                            ) {
                                tagsForThisPost.forEach { tag ->
                                    Text(
                                        text = "#$tag",
                                        modifier = Modifier
                                            .padding(2.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(Color.Gray.copy(alpha = 0.2f))
                                            .padding(start = 2.dp, end = 2.dp),
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
