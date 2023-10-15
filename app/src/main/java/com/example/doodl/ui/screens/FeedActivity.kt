package com.example.doodl.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.doodl.R
import com.example.doodl.data.Repository
import com.example.doodl.viewmodel.FeedViewModel
import com.example.doodl.viewmodel.FeedViewModelFactory

@Composable
fun FeedScreen() {
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
    val images = feedViewModel.images.observeAsState(emptyList())
    ImageFeed(images.value)
}

@Composable
fun ImageFeed(images: List<Bitmap>) {
    // Display a vertical list of images.
    LazyColumn {
        items(images) { image ->
            // For each image, create an Image composable
                /*Image(
                    // Convert Bitmap to a format Image composable understands and renders it
                    painter = BitmapPainter(image.asImageBitmap()), // null implies decorative image (no alt text)
                    contentDescription = null,
                    // Style modifiers to control the layout and appearance of the image
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(8.dp)
                )*/
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.LightGray)
            ){
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(Color.White)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RoundImageCard(
                            image = R.drawable.likeicon,
                            Modifier
                                .size(48.dp)
                                .padding(4.dp)
                        )
                        Text(text = "userName", fontWeight = FontWeight.Bold)
                    }
                    Image(
                        // Convert Bitmap to a format Image composable understands and renders it
                        painter = BitmapPainter(image.asImageBitmap()), // null implies decorative image (no alt text)
                        contentDescription = null,
                        // Style modifiers to control the layout and appearance of the image
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.59f)//changed aspect ratio (Old A.R: 1)
                            .padding(8.dp),
                        contentScale = ContentScale.Crop
                    )
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.likeicon),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(Color.Red)
                        )
                        Text(text = "likes", modifier = Modifier.padding(start = 8.dp))
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

