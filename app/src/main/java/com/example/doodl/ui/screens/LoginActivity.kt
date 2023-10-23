package com.example.doodl.ui.screens

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.doodl.data.repository.AuthRepository
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
    var hasNavigated by rememberSaveable { mutableStateOf(false) }


    // Handle back button press in LoginScreen
    BackHandler {
        // Close the app
        activity?.finish()
    }

    // Root Column: Main parent layout which is vertically oriented.
    Column(
        modifier = Modifier
            .fillMaxSize()  // Fill the entires available screen size.
            .background(MaterialTheme.colorScheme.tertiary)  // Apply padding of 16dp to every side of the column.
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
            Text("Log in Doodler!", fontWeight = FontWeight.Bold, fontSize = 24.sp)

            // Provide a vertical space of 16dp.
            Spacer(modifier = Modifier.height(16.dp))

            // Input field for email.
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },  // Update the email state when the text changes.
                label = { Text("Email") },  // Label that appears when the field is empty and shrinks when text is entered.
                modifier = Modifier.fillMaxWidth(), // Make the text field as wide as possible within the Column.
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    focusedLabelColor = Color.Black,
                    cursorColor = MaterialTheme.colorScheme.secondary
                )
            )

            // Provide a vertical space of 16dp.
            Spacer(modifier = Modifier.height(16.dp))

            // Input field for password, visually obscuring the text.
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },  // Update the password state when text changes.
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),  // Make the text field as wide as possible within the Column.
                visualTransformation = PasswordVisualTransformation(),  // Visual transformation to obscure password input.
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),  // Set keyboard type to password to aid user input.
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    focusedLabelColor = Color.Black,
                    cursorColor = MaterialTheme.colorScheme.secondary
                )
            )

            // Provide a vertical space of 24dp.
            Spacer(modifier = Modifier.height(24.dp))

            // Button which triggers login logic when clicked.
            Button(onClick = {
                // TODO: Implement login logic here. Currently only handles password >= 6
                // On successful login, navigate to canvas.
                loginInputValidation(email, password, authViewModel, activity)
            },
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colorScheme.primary))
            {
                Text("Login")
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
                modifier = Modifier.align(Alignment.CenterVertically)  // Vertically center align the text within the Row.
            )

            // Provide a horizontal space of 8dp.
            Spacer(modifier = Modifier.width(8.dp))

            // Button which navigates to the registration screen when clicked.
            Button(
                onClick = {
                navController?.navigate("registrationScreen")  // Use NavController to navigate to the registration screen.
            },
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colorScheme.primary))
            {
                Text("Register")
            }
            LoginStateNavigation(authState, navController, hasNavigated, {
                hasNavigated = true
            }, activity, authViewModel)
        }
    }
}

@Composable
fun LoginStateNavigation(
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
                onSuccessfulNavigation()  // This lambda will be responsible for setting hasNavigated to true.
                // reset authState to avoid re-triggering.
                authViewModel.resetAuthState()
            }
        }
        is AuthState.Error -> {
            // Show a SnackBar, Toast, or any other indication of the error here.
            val errorMessage = (authState as AuthState.Error).message
            Toast.makeText(activity, errorMessage, Toast.LENGTH_SHORT).show()
        }
        null -> {} // Handle any default state or initialization state here, if needed.
    }
}

fun loginInputValidation(
    email: String,
    password: String,
    authViewModel: AuthViewModel,
    activity: ComponentActivity?  // If you want to show Toast messages, you'll need a context.
) {
    // Sample logic: navigate if email and password are not empty (modify as per your requirement)
    if(email.isNotEmpty() && password.isNotEmpty()) {
        // After successfully logging in, navigate to your main screen.
        if(password.length >= 6) {
            authViewModel.login(email, password) // Call ViewModel method
        } else {
            // Show feedback to user that password is too short.
            Toast.makeText(activity, "Password is too short!", Toast.LENGTH_SHORT).show()
        }
    } else {
        // Show feedback to user if conditions aren't met ("Please check your input fields.").
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