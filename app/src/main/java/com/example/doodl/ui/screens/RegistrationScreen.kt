package com.example.doodl.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.navigation.NavController

@Composable
fun RegistrationScreen(navController: NavController? = null) {
    // MutableState variables to hold the input values for email, password, and confirmPassword.
    // The 'remember' function will remember these values even after recomposition.
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Scaffold is a high-level composable that provides structure to visual content and
    // top-level components such as TopAppBar, Drawer, BottomNavigation, and more.
    Scaffold(
        // TopAppBar represents a material design app bar that can hold title, icons, and more.
        topBar = {
            TopAppBar(
                title = { Text("") }, // Title text displayed in the app bar.
                // Navigation icon (back arrow) that, when clicked, pops the back stack to navigate back.
                navigationIcon = {
                    IconButton(onClick = {
                        // Navigate back to the previous screen when the navigation icon is clicked.
                        navController?.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Navigate Up")
                    }
                }
            )
        }
    ) { innerPadding ->
        // Column is a composable that places its children in a vertical sequence,
        // ensuring they're positioned and spaced appropriately in this case.
        Column(
            // Modifiers are used to modify the appearance and behavior of composables.
            modifier = Modifier
                .fillMaxSize() // Make column take up all available space.
                .padding(innerPadding) // Apply padding provided by Scaffold to avoid overlap with TopAppBar.
                .padding(16.dp), // Additional padding for visual spacing around the content.
            horizontalAlignment = Alignment.CenterHorizontally, // Horizontally center child composables.
            verticalArrangement = Arrangement.Center // Vertically arrange children with center alignment.
        ) {
            // Text composable to display a static text string ("Create Account").
            Text("Create Account", fontWeight = FontWeight.Bold, fontSize = 24.sp)

            Spacer(modifier = Modifier.height(16.dp)) // Provide vertical spacing.

            // OutlinedTextField is a material design text field that has an outline when focused.
            // This one is for user's email input.
            OutlinedTextField(
                value = email,
                onValueChange = { email = it }, // Update the email variable when text changes.
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp)) // Provide vertical spacing.

            // Text field for user's password input, visually obscuring the input.
            OutlinedTextField(
                value = password,
                onValueChange = { password = it }, // Update the password variable when text changes.
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.height(16.dp)) // Provide vertical spacing.

            // Text field for user's password confirmation input, also obscured.
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it }, // Update the confirmPassword variable when text changes.
                label = { Text("Confirm Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.height(24.dp)) // Provide vertical spacing.

            // Button that, when clicked, performs registration logic.
            Button(onClick = {
                // TODO: Implement registration logic here.
                // On successful registration, navigate to canvas screen.

                // Sample logic: navigate if email, password, and confirmPassword are not empty and password equals confirmPassword
                if(email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty() && password == confirmPassword) {
                    // If registration is successful, navigate to canvas.
                    navController?.navigate("feed") {
                        // Pop up to the root of the navigation graph and remove everything in the back stack.
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                    }
                }
            }) {
                Text("Register")
            }
        }
    }
}

@Preview
@Composable
fun PreviewRegistrationScreen() {
    RegistrationScreen()
}
