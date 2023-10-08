package com.example.doodl.ui.screens

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.doodl.viewmodel.FeedViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.doodl.data.Repository
import com.example.doodl.ui.theme.DoodlTheme
import com.example.doodl.viewmodel.FeedViewModelFactory
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun FeedScreen() {
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
            Image(
                // Convert Bitmap to a format Image composable understands and renders it
                painter = BitmapPainter(image.asImageBitmap()), // null implies decorative image (no alt text)
                contentDescription = null,
                // Style modifiers to control the layout and appearance of the image
                modifier = Modifier.fillMaxWidth().aspectRatio(1f).padding(8.dp)
            )
        }
    }
}