package com.example.doodl.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.doodl.R
import com.example.doodl.data.Post
import com.example.doodl.data.repository.Repository
import com.example.doodl.ui.EditableTextField
import com.example.doodl.ui.RoundImageCard
import com.example.doodl.ui.logout
import com.example.doodl.viewmodel.FeedViewModel
import com.example.doodl.viewmodel.FeedViewModelFactory


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

    // Observe images LiveData and pass it to the ImageFeed composable.
    val imageUrls = feedViewModel.userImageUrls.observeAsState(emptyList()).value

    var userName = feedViewModel.userName.observeAsState(null).value
    var userBioText = feedViewModel.userBio.observeAsState(null).value
    val likedPosts = feedViewModel.likedPosts.observeAsState(emptyList())
    val profilePicUrl by feedViewModel.profilePic.observeAsState()

    // Prepare the launcher for picking an image
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Handle the image URI - upload to Firebase and update the profile
            // This will need to be implemented
        }
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
                val maxUsernameLength = 15

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
                    ProfileEditPopup(
                        currentUsername = feedViewModel.userName.value,
                        currentBio = feedViewModel.userBio.value,
                        onImageSelected = { pickImageLauncher.launch("image/*") },
                        onConfirm = { newUsername, newBio ->
                            // Handle the profile update here
                            // You might need to pass the image as ByteArray
                        }
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
                if (profilePicUrl != null) {//new
                    Image(
                        painter = rememberAsyncImagePainter(model = profilePicUrl),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(115.dp)
                            .padding(5.dp)
                    )
                } else {
                    // Will show a default image of our choosing
                    RoundImageCard(
                        image = R.drawable.profpic8, // Default or placeholder image
                        Modifier.size(115.dp).padding(5.dp)
                    )
                }
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
                        ProfileUsersPosts(imageUrls = imageUrls, navBarHeight = navBarHeight )
                    }
                }
                1 -> {
                    Spacer(modifier = Modifier.width(20.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ProfileLikedPosts(posts = likedPosts.value, navBarHeight)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileEditPopup(
    currentUsername: String?,
    currentBio: String?,
    onImageSelected: () -> Unit, // Callback when the image icon is clicked
    onConfirm: (String, String) -> Unit // Callback when the save button is clicked
) {
    var isPopupVisible by remember { mutableStateOf(false) }
    var newUsername by remember { mutableStateOf(currentUsername ?: "") }
    var newBio by remember { mutableStateOf(currentBio ?: "") }

    // Icon to open the popup
    Icon(
        imageVector = Icons.Default.Edit,
        tint = MaterialTheme.colorScheme.primary,
        contentDescription = "Edit Profile",
        modifier = Modifier.clickable { isPopupVisible = true }
    )

    // The actual popup
    if (isPopupVisible) {
        AlertDialog(
            containerColor = Color.Black,
            modifier = Modifier.border(2.3.dp, Color.White, RoundedCornerShape(30.dp)),
            onDismissRequest = { isPopupVisible = false },
            title = { Text("Edit Your Profile") },
            text = {
                Column {
                    Text(text = "Set your profile picture", color = Color.White)
                    // Icon for picking an image
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Change Profile Picture",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { onImageSelected() }
                            .padding(16.dp)
                            .size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    EditableTextField(
                        label = "Username",
                        text = newUsername,
                        onTextChanged = { newUsername = it }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    EditableTextField(
                        label = "Biography",
                        text = newBio,
                        onTextChanged = { newBio = it }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isPopupVisible = false
                        onConfirm(newUsername, newBio)
                    }
                ) { Text("Save", color = Color.White) }
            }
        )
    }
}

        @Composable
fun ProfileUsersPosts(imageUrls: List<String>, navBarHeight: Int) {
            val configuration = LocalConfiguration.current
            val maxScreenHeightDp = configuration.screenHeightDp.dp
            val maxScreenHeight = with(LocalDensity.current) { maxScreenHeightDp.toPx() }
            val availableHeight = maxScreenHeight - navBarHeight
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
            }
}

@Composable
fun ProfileLikedPosts(posts: List<Post>, navBarHeight: Int) {
    val configuration = LocalConfiguration.current
    val maxScreenHeightDp = configuration.screenHeightDp.dp
    val maxScreenHeight = with(LocalDensity.current) { maxScreenHeightDp.toPx() }
    val availableHeight = maxScreenHeight - navBarHeight

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
                        .padding(8.dp)
                ) {
                    if (!post.imageUrl.isNullOrEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(model = post.imageUrl),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(1.1f) // Apply the scaling factor to individual images
                        )
                    } else {
                        // Display alternative content for empty imageUrl
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(1.1f)
                                .aspectRatio(1f)
                                .background(color = Color.DarkGray),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("No Image Available", color = Color.LightGray, textAlign = TextAlign.Center)
                        }
                    }
                }
                if (isLastRow) {
                    Spacer(modifier = Modifier.padding(bottom = 195.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileLikedPostsPreview() {
    // Mock data for preview
    val mockPosts = listOf(
        Post("https://example.com/image1.jpg"),
        Post("https://example.com/image2.jpg"),
        Post("https://example.com/image3.jpg"),
        Post("https://example.com/image4.jpg"),
        // We can add more here
    )

    ProfileLikedPosts(posts = mockPosts, navBarHeight = 56)
}