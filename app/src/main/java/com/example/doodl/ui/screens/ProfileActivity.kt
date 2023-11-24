package com.example.doodl.ui.screens

import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
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
import com.example.doodl.ui.ConfirmationDialog
import com.example.doodl.ui.EditableTextField
import com.example.doodl.ui.ReAuthenticateDialog
import com.example.doodl.ui.RoundImageCard
import com.example.doodl.ui.logout
import com.example.doodl.util.ComposableStateUtil
import com.example.doodl.util.ValidationUtils
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
        feedViewModel.fetchUserPosts()
        feedViewModel.fetchUserDetails(userId)
        feedViewModel.fetchLikedPosts()
        feedViewModel.fetchProfileImages()
    }

    // Observe images LiveData and pass it to the ImageFeed composable.
    val userPosts = feedViewModel.userPosts.observeAsState(emptyList()).value
    val userName = feedViewModel.userName.observeAsState(null).value
    val userBioText = feedViewModel.userBio.observeAsState(null).value
    val likedPosts = feedViewModel.likedPosts.observeAsState(emptyList())
    val profilePicUrl by feedViewModel.profilePic.observeAsState()
    val profileImages by feedViewModel.profileImages.observeAsState(emptyList())
    val isFetchingUserPosts by feedViewModel.isFetchingUserPosts.observeAsState(false)
    val isFetchingLikedPosts by feedViewModel.isFetchingLikedPosts.observeAsState(false)
    val context = LocalContext.current //For updating profile pic

    // Prepare the launcher for picking an image
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedImageUri ->
            // Launch a coroutine to process the image and update the profile
            feedViewModel.onImageSelected(selectedImageUri, context)
        } ?: run {
            // No image was selected, call updateProfile with null for imageByteArray
            feedViewModel.updateProfile(
                newUsername = userName ?: "",
                newBio = userBioText ?: "",
                imageByteArray = null
            )
        }
    }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showProgressIndicator by remember { mutableStateOf(false) }
    var showReAuthenticateDialog by remember { mutableStateOf(false) }
    var canDeleteAccount by remember { mutableStateOf(false) }
    var reAuthError by remember { mutableStateOf("") }


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
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (showProgressIndicator) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.tertiary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                Text(text = "")
                Spacer(modifier = Modifier.width(15.dp))
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
                Spacer(modifier = Modifier.weight(1f)) // Flexible spacer to push content to sides
                userName?.let {
                    ProfileEditPopup(
                        currentUsername = feedViewModel.userName.value,
                        currentBio = feedViewModel.userBio.value,
                        profileImages = profileImages,
                        onImageSelected = { pickImageLauncher.launch("image/*") },
                        onConfirm = { newUsername, newBio, selectedImageUrl ->
                            // If a new image URL is selected from the stored images, update with that URL
                            if (selectedImageUrl != null) {
                                // User selected an existing image URL
                                feedViewModel.updateProfileWithImageUrl(newUsername, newBio, selectedImageUrl)
                            } else {
                                // Only username and bio are being updated
                                feedViewModel.updateProfile(newUsername, newBio, null)
                            }
                        },
                        viewModel = feedViewModel
                    )
                }
                // Logout Account Icon
                Spacer(modifier = Modifier.width(15.dp))
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable {
                            showLogoutDialog = true
                        }
                )
                ConfirmationDialog(
                    showDialog = showLogoutDialog,
                    onDismiss = { showLogoutDialog = false },
                    title = "Logout",
                    message = "Are you sure you want to log out?",
                    onConfirm = {
                        if (navController != null) {
                            logout(navController)
                        }
                        showLogoutDialog = false
                    },
                    onCancel = { showLogoutDialog = false }
                )
                Spacer(modifier = Modifier.width(15.dp))
                // Delete Account Icon
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Account",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable {
                            showReAuthenticateDialog = true  // First show the re-authentication dialog
                        }
                )
                // Show re-authentication dialog
                ReAuthenticateDialog(
                    showDialog = showReAuthenticateDialog,
                    onDismiss = {
                        showReAuthenticateDialog = false
                        reAuthError = ""
                    },
                    onReAuthenticate = { password ->
                        // Perform re-authentication with the provided password
                        feedViewModel.reAuthenticateUser(password) { isReAuthenticated ->
                            if (isReAuthenticated) {
                                canDeleteAccount = true
                                showDeleteAccountDialog = true
                                reAuthError = ""
                            } else {
                                // failed re-authentication
                                canDeleteAccount = false
                                reAuthError = "Re-authentication failed. Please try again."
                            }
                        }
                    },
                    errorMessage = reAuthError
                )

                // Confirmation dialog for account deletion
                ConfirmationDialog(
                    showDialog = showDeleteAccountDialog,
                    onDismiss = { showDeleteAccountDialog = false },
                    title = "Delete Account",
                    message = "Are you sure you want to delete your account? This action cannot be undone.",
                    onConfirm = {
                        showDeleteAccountDialog = false
                        showProgressIndicator = true
                        feedViewModel.deleteAccount(userId) {
                            showProgressIndicator = false
                            if (navController != null) {
                                logout(navController)
                            }
                        }
                    },
                    onCancel = {
                        showDeleteAccountDialog = false
                        canDeleteAccount = false

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
                    // We can show a default image here
                    RoundImageCard(
                        image = R.drawable.profpic8,
                        Modifier
                            .size(115.dp)
                            .padding(5.dp)
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
                    if (isFetchingUserPosts) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.tertiary, strokeWidth = 2.dp)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(20.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ProfileUsersPosts(posts = userPosts, navBarHeight = navBarHeight )
                        }
                    }

                }
                1 -> {
                    if (isFetchingLikedPosts) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.tertiary, strokeWidth = 2.dp)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(20.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ProfileLikedPosts(posts = likedPosts.value, navBarHeight)
                        }
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
    profileImages: List<String>,
    onImageSelected: () -> Unit, // Callback when the image icon is clicked
    onConfirm: (String, String, String?) -> Unit, // Callback when the save button is clicked
    viewModel: FeedViewModel
) {
    var isPopupVisible by remember { mutableStateOf(false) }
    var newUsername by remember { mutableStateOf(currentUsername ?: "") }
    var newBio by remember { mutableStateOf(currentBio ?: "") }
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current // Get the local context

    // Icon to open the popup
    Icon(
        imageVector = Icons.Default.Edit,
        tint = MaterialTheme.colorScheme.primary,
        contentDescription = "Edit Profile",
        modifier = Modifier.clickable {
            ComposableStateUtil.resetEditableFields(
                currentUsername = currentUsername,
                currentBio = currentBio,
                onUsernameReset = { newUsername = it },
                onBioReset = { newBio = it }
            )
            isPopupVisible = true
        }
    )

    // The actual popup
    if (isPopupVisible) {
        AlertDialog(
            containerColor = Color.Black,
            modifier = Modifier.border(2.3.dp, Color.White, RoundedCornerShape(30.dp)),
            onDismissRequest = {
                isPopupVisible = false
                selectedImageUrl = null
            },
            title = { Text("Edit Your Profile") },
            text = {
                Column {
                    Text(text = "Set your profile picture", color = Color.White)
                    // Icon for picking an image
                    Row {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Change Profile Picture",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable { onImageSelected() }
                                .padding(16.dp)
                                .size(48.dp)
                        )
                        LazyRow {
                            items(profileImages) { imageUrl ->
                                val isSelected = imageUrl == selectedImageUrl
                                Image(
                                    painter = rememberAsyncImagePainter(model = imageUrl),
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .padding(8.dp)
                                        .border(
                                            width = if (isSelected) 2.dp else 0.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.tertiary else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            selectedImageUrl = imageUrl
                                        }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    EditableTextField(
                        label = "Username",
                        text = newUsername,
                        onTextChanged = { newUsername = it }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    EditableTextField(
                        label = "Bio",
                        text = newBio,
                        onTextChanged = { newBio = it }
                    )

                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val (isUsernameValid, usernameMessage) = ValidationUtils.validateUsername(newUsername)
                        val (isBioValid, bioMessage) = ValidationUtils.validateBio(newBio)
                        if (!isUsernameValid) {
                            Toast.makeText(context, usernameMessage, Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (!isBioValid) {
                            Toast.makeText(context, bioMessage, Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        viewModel.checkUsernameAvailability(newUsername) { isAvailable ->
                            if (!isAvailable && newUsername != currentUsername) {
                                Toast.makeText(context, "Username already taken", Toast.LENGTH_SHORT).show()
                            } else {
                                onConfirm(newUsername, newBio, selectedImageUrl)
                                isPopupVisible = false
                                Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) { Text("Save", color = Color.White) }
            }
        )
    }
}

@Composable
fun ProfileUsersPosts(posts: List<Post>, navBarHeight: Int) {
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
                    itemsIndexed(posts) { index, post ->
                        val itemsPerRow = 3
                        val rowNumber = index / itemsPerRow
                        val isLastRow = rowNumber == (posts.size - 1) / itemsPerRow
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .padding(8.dp)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(model = post.imageUrl),
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