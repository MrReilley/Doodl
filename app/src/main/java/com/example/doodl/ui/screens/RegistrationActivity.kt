package com.example.doodl.ui.screens

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.doodl.data.repository.AuthRepository
import com.example.doodl.ui.containsLetterAndNumber
import com.example.doodl.ui.isValidEmail
import com.example.doodl.viewmodel.AuthState
import com.example.doodl.viewmodel.AuthViewModel
import com.example.doodl.viewmodel.AuthViewModelFactory

@Composable
fun RegistrationScreen(navController: NavController? = null, activity: ComponentActivity? = null) {
    // MutableState variables to hold the input values for email, password, and confirmPassword.
    // The 'remember' function will remember these values even after recomposition.
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Obtain the ViewModel
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(AuthRepository()))
    // Observe the LiveData
    val authState by authViewModel.authState.observeAsState()
    var hasNavigated by rememberSaveable { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val confirmPasswordFocusRequester = remember { FocusRequester() }


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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Navigate Up", tint= Color.White)
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
                .background(Color.Black)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally, // Horizontally center child composables.
            verticalArrangement = Arrangement.Center // Vertically arrange children with center alignment.
        ) {
            // Text composable to display a static text string ("Create Account").
            Text("Create Account", fontWeight = FontWeight.Bold, fontSize = 24.sp, color=Color.White)

            Spacer(modifier = Modifier.height(16.dp)) // Provide vertical spacing.

            // OutlinedTextField is a material design text field that has an outline when focused.
            // This one is for user's email input.
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(emailFocusRequester),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        passwordFocusRequester.requestFocus()
                    }
                ),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                    unfocusedBorderColor = Color.White,
                    focusedLabelColor = MaterialTheme.colorScheme.tertiary,
                    unfocusedLabelColor = Color.White,
                    cursorColor = MaterialTheme.colorScheme.tertiary,
                    textColor = MaterialTheme.colorScheme.tertiary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Text field for user's password input, visually obscuring the input.
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(passwordFocusRequester),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        confirmPasswordFocusRequester.requestFocus()
                    }
                ),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                    unfocusedBorderColor = Color.White,
                    focusedLabelColor = MaterialTheme.colorScheme.tertiary,
                    unfocusedLabelColor = Color.White,
                    cursorColor = MaterialTheme.colorScheme.tertiary,
                    textColor = MaterialTheme.colorScheme.tertiary
                )
            )

            Spacer(modifier = Modifier.height(16.dp)) // Provide vertical spacing.

            // Text field for user's password confirmation input, also obscured.
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(confirmPasswordFocusRequester),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        registrationInputValidation(email, password, confirmPassword, authViewModel, activity)
                    }
                ),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                    unfocusedBorderColor = Color.White,
                    focusedLabelColor = MaterialTheme.colorScheme.tertiary,
                    unfocusedLabelColor = Color.White,
                    cursorColor = MaterialTheme.colorScheme.tertiary,
                    textColor = MaterialTheme.colorScheme.tertiary
                )
            )

            Spacer(modifier = Modifier.height(24.dp)) // Provide vertical spacing.

            // Button that, when clicked, performs registration logic.
            Button(onClick = {
                registrationInputValidation(email, password, confirmPassword, authViewModel, activity)
            },
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colorScheme.primary))
            {
                Text("Register", color=Color.White)
            }
            RegistrationStateNavigation(authState, navController, hasNavigated, {
                hasNavigated = true
            }, activity, authViewModel)
        }
    }
}

@Composable
fun RegistrationStateNavigation(
    authState: AuthState?,
    navController: NavController?,
    hasNavigated: Boolean,
    onSuccessfulNavigation: () -> Unit,
    activity: ComponentActivity?,
    authViewModel: AuthViewModel
) {
    when (authState) {
        is AuthState.Success -> {
            if (!hasNavigated) {
                navController?.navigate("feed") {
                    popUpTo("loginScreen") { inclusive = true }
                }
                onSuccessfulNavigation()
                // reset authState to avoid re-triggering.
                authViewModel.resetAuthState()
            }
        }
        is AuthState.Error -> {
            // Show a SnackBar, Toast, or any other indication of the error here.
            val errorMessage = (authState).message
            Toast.makeText(activity, errorMessage, Toast.LENGTH_SHORT).show()
        }
        null -> {} // Handle any default state or initialization state here, if needed.
    }
}

fun registrationInputValidation(
    email: String,
    password: String,
    confirmPassword: String,
    authViewModel: AuthViewModel,
    activity: ComponentActivity?
) {
    if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
        if(!isValidEmail(email)) {
            Toast.makeText(activity, "Invalid email format!", Toast.LENGTH_SHORT).show()
        } else if(password != confirmPassword) {
            Toast.makeText(activity, "Passwords do not match!", Toast.LENGTH_SHORT).show()
        } else if(password.length < 6) {
            Toast.makeText(activity, "Password is too short!", Toast.LENGTH_SHORT).show()
        } else if(!containsLetterAndNumber(password)) {
            Toast.makeText(activity, "Password must contain both letters and numbers!", Toast.LENGTH_SHORT).show()
        } else {
            authViewModel.register(email, password) // Call ViewModel method
        }
    } else {
        Toast.makeText(activity, "Please check your input fields.", Toast.LENGTH_SHORT).show()
    }
}

@Preview
@Composable
fun PreviewRegistrationScreen() {
    RegistrationScreen()
}
