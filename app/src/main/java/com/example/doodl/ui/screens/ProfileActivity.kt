package com.example.doodl.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.doodl.R
import com.example.doodl.data.Repository
import com.example.doodl.viewmodel.FeedViewModel
import com.example.doodl.viewmodel.FeedViewModelFactory
@Composable
fun ProfileScreen() {
    BackHandler {
        // Do nothing, effectively disabling the back button
    }
    val repository = Repository()
    val feedViewModel:FeedViewModel = viewModel(factory = FeedViewModelFactory(repository))
    // Fetch images once the composable is launched
    LaunchedEffect(feedViewModel) {
        feedViewModel.fetchImages()
    }
    // Observe images LiveData and pass it to the ImageFeed composable.
    val images = feedViewModel.liveImages.observeAsState(emptyList())
    Column(
        modifier = Modifier.fillMaxSize()
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray)
        ){
                // feed card layout
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "")
                Spacer(modifier = Modifier.width(140.dp))
                Text(
                    text = "userName",
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(100.dp))
                Text(
                    text = "logout",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.Blue
                )
            }
            Spacer(modifier = Modifier.width(25.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                RoundImageCard(
                    image = R.drawable.likeicon,
                    Modifier
                        .size(125.dp)
                        .padding(4.dp)
                )
                Text(
                    text = "This is for the bio/description box for the template section of the profile page :)." +
                            "I'm going to test the text wrapping now lol ewqdqdqdawdawdwadwadawddsawdawdasadawdasawdadawda",
                    letterSpacing = 0.5.sp,
                    lineHeight = 20.sp,
                    softWrap = true,
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.width(25.dp))

            var selectedTabIndex by remember { mutableStateOf(0) }
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.background(Color.LightGray),
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = Color.Blue
                    )
                }
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 }
                ){
                    Image(
                        painter =  painterResource(id = R.drawable.ic_grid),
                        contentDescription = null
                    )
                }
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 }
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
                        ProfilePosts(images = images.value)
                    }
                }
                1 -> {
                    Spacer(modifier = Modifier.width(20.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ProfilePosts(images = images.value)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfilePosts(images: List<Bitmap>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3)
    ) {
        itemsIndexed(images) { _, image ->
            Box(
                modifier = Modifier
                    .aspectRatio(1f) // Set the aspect ratio to make images square
                    .padding(8.dp)
            ) {
                Image(
                    painter = remember { BitmapPainter(image.asImageBitmap()) },
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