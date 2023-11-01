package com.example.doodl.ui.screens

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
fun LoginScreen(navController: NavController? = null, activity: ComponentActivity? = null) {

    // MutableState variables to hold the input values for email and password.
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Obtain the ViewModel
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(AuthRepository()))
    // Observe the LiveData
    val authState by authViewModel.authState.observeAsState()

    val focusManager = LocalFocusManager.current
    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }

    // Handle back button press in LoginScreen
    BackHandler {
        authViewModel.resetAuthState()
        // Close the app
        activity?.finish()
    }

    // Root Column: Main parent layout which is vertically oriented.
    Column(
        modifier = Modifier
            .fillMaxSize()  // Fill the entires available screen size.
            .background(Color.Black)  // Apply padding of 16dp to every side of the column.
            .padding(16.dp),
        verticalArrangement = Arrangement.Center  // Vertically center the contents of the column.
    ) {
        // Centered Column: Contains input fields and Login button.
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,  // Horizontally center the children.
            verticalArrangement = Arrangement.Center,  // Vertically center the children.
            modifier = Modifier.weight(1f)  // Occupy the remaining available vertical space.
        ) {
            // Text Composable to display a title or heading.
            Text("Log in Doodler!", fontWeight = FontWeight.Bold, fontSize = 24.sp, color= Color.White)

            // Provide a vertical space of 16dp.
            Spacer(modifier = Modifier.height(16.dp))

            // Input field for email.
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },  // Update the email state when the text changes.
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(emailFocusRequester), // Assign the focus requester
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { // Use onNext action for email
                        passwordFocusRequester.requestFocus() // Move focus to password field
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

            // Input field for password, visually obscuring the text.
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(passwordFocusRequester), // Assign the focus requester
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus() // Clear the focus
                        loginInputValidation(email, password, authViewModel, activity)
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

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = {
                // On successful login, navigate to canvas.
                loginInputValidation(email, password, authViewModel, activity)
            },
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colorScheme.primary))
            {
                Text("Login", color=Color.White)
            }
        }

        // Bottom Row: Contains the registration call-to-action and button.
        Row(
            horizontalArrangement = Arrangement.Center,  // Horizontally center the children.
            modifier = Modifier
                .fillMaxWidth()  // Make the row occupy all available horizontal space.
                .padding(bottom = 16.dp)  // Apply padding of 16dp to the bottom of the row.
        ) {
            // Text Composable for informing users they can register.
            Text("Don't have an account?",
                color=Color.White,
                modifier = Modifier.align(Alignment.CenterVertically)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    authViewModel.resetAuthState()  // Reset the state here
                    navController?.navigate("registrationScreen")
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colorScheme.primary))
            {
                Text("Register", color=Color.White)
            }
            LoginStateNavigation(authState, navController, activity, authViewModel)
        }
    }
}

@Composable
fun LoginStateNavigation(
    authState: AuthState?,
    navController: NavController?,
    activity: ComponentActivity?,
    authViewModel: AuthViewModel
) {
    // LaunchedEffect to prevent re-triggering events.
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                navController?.navigate("feed") {
                    popUpTo("loginScreen") { inclusive = true }
                }
                // reset authState to avoid re-triggering.
                authViewModel.resetAuthState()
            }
            is AuthState.Error -> {
                // Show a SnackBar, Toast, or any other indication of the error here.
                val errorMessage = (authState).message
                Toast.makeText(activity, errorMessage, Toast.LENGTH_SHORT).show()
            }
            null -> {} // Handle any default state or initialization state here, if needed.
        }
    }
}

fun loginInputValidation(
    email: String,
    password: String,
    authViewModel: AuthViewModel,
    activity: ComponentActivity?  // If you want to show Toast messages, you'll need a context.
) {
    if(email.isNotEmpty() && password.isNotEmpty()) {
        if(!isValidEmail(email)) {
            Toast.makeText(activity, "Invalid email format!", Toast.LENGTH_SHORT).show()
        } else if(password.length < 6) {
            Toast.makeText(activity, "Password is too short!", Toast.LENGTH_SHORT).show()
        } else if(!containsLetterAndNumber(password)) {
            Toast.makeText(activity, "Password must contain both letters and numbers!", Toast.LENGTH_SHORT).show()
        } else {
            authViewModel.login(email, password) // Call ViewModel method
        }
    } else {
        Toast.makeText(activity, "Please check your input fields.", Toast.LENGTH_SHORT).show()
    }
}

@Preview
@Composable
//remove ?= null from LoginScreen() if you no longer need the preview
//same for ? in navController?.navigate()
fun PreviewLoginScreen() {
    LoginScreen()
}