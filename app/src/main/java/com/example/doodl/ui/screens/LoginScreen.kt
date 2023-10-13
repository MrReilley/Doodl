package com.example.doodl.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation

@Composable
fun LoginScreen(navController: NavController? = null) {
    // MutableState variables to hold the input values for email and password.
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Root Column: Main parent layout which is vertically oriented.
    Column(
        modifier = Modifier
            .fillMaxSize()  // Fill the entire available screen size.
            .padding(16.dp),  // Apply padding of 16dp to every side of the column.
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
                modifier = Modifier.fillMaxWidth()  // Make the text field as wide as possible within the Column.
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)  // Set keyboard type to password to aid user input.
            )

            // Provide a vertical space of 24dp.
            Spacer(modifier = Modifier.height(24.dp))

            // Button which triggers login logic when clicked.
            Button(onClick = {
                // Implement login logic here.
            }) {
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
            Button(onClick = {
                navController?.navigate("registrationScreen")  // Use NavController to navigate to the registration screen.
            }) {
                Text("Register")
            }
        }
    }
}


@Preview
@Composable
//remove ?= null from LoginScreen() if you no longer need the preview
//same for ? in navController?.navigate()
fun PreviewLoginScreen() {
    LoginScreen()
}