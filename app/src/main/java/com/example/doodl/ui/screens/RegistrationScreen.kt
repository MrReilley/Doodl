package com.example.doodl.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.doodl.data.repository.AuthRepository
import com.example.doodl.viewmodel.AuthState
import com.example.doodl.viewmodel.AuthViewModel
import com.example.doodl.viewmodel.AuthViewModelFactory

@Composable
fun RegistrationScreen(navController: NavController? = null) {
    // MutableState variables to hold the input values for email, password, and confirmPassword.
    // The 'remember' function will remember these values even after recomposition.
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val repository = AuthRepository()  // Or obtain it from another source if needed
    val factory = AuthViewModelFactory(repository)
    val authViewModel: AuthViewModel = viewModel(factory = factory)



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
                },
                backgroundColor = MaterialTheme.colorScheme.primary
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
                .background(MaterialTheme.colorScheme.tertiary)
                .padding(16.dp),
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
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    focusedLabelColor = Color.Black,
                    cursorColor = MaterialTheme.colorScheme.secondary
                )
            )

            Spacer(modifier = Modifier.height(16.dp)) // Provide vertical spacing.

            // Text field for user's password input, visually obscuring the input.
            OutlinedTextField(
                value = password,
                onValueChange = { password = it }, // Update the password variable when text changes.
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    focusedLabelColor = Color.Black,
                    cursorColor = MaterialTheme.colorScheme.secondary
                )
            )

            Spacer(modifier = Modifier.height(16.dp)) // Provide vertical spacing.

            // Text field for user's password confirmation input, also obscured.
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it }, // Update the confirmPassword variable when text changes.
                label = { Text("Confirm Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    focusedLabelColor = Color.Black,
                    cursorColor = MaterialTheme.colorScheme.secondary
                )
            )

            Spacer(modifier = Modifier.height(24.dp)) // Provide vertical spacing.

            // Button that, when clicked, performs registration logic.
            Button(onClick = {
                // TODO: Implement registration logic here.
                // On successful registration, navigate to canvas screen.

                // Sample logic: navigate if email, password, and confirmPassword are not empty and password equals confirmPassword
                if(email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty() && password == confirmPassword) {
                    if(password.length >= 6) {
                        authViewModel.register(email, password) // Call ViewModel method
                    } else {
                        // Show feedback to user that password is too short.
                    }
                } else {
                    // Show feedback to user if conditions aren't met ("Please check your input fields.").
                }
            },
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colorScheme.primary))
            {
                Text("Register")
            }

            // Observe the authState LiveData from the ViewModel
            authViewModel.authState.observeAsState().value?.let { state ->
                when (state) {
                    is AuthState.Success -> {
                        // Navigate to canvas/feed screen if registration is successful
                        navController?.navigate("feed") {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    }
                    is AuthState.Error -> {
                        // Display error to user, e.g., using a Snackbar.
                        //showSnackbar(state.errorMessage)
                    }
                    else -> { /* Handle other cases if needed */ }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewRegistrationScreen() {
    RegistrationScreen()
}
