package com.example.doodl.data.repository

import android.util.Log
import com.example.doodl.data.DoodlUser
import com.example.doodl.viewmodel.LoginState
import com.example.doodl.viewmodel.RegistrationState
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.random.Random


class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    suspend fun register(email: String, password: String): RegistrationState {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                val uniqueUsername = generateUniqueUsername().await()
                val user = DoodlUser(firebaseUser.uid, uniqueUsername, null, "DefaultBio")
                firestore.collection("users").document(firebaseUser.uid).set(user).await()
                RegistrationState.Success
            } else {
                Log.e("AuthError", "User creation was successful but user is null")
                RegistrationState.Error("User creation was successful but user is null")
            }
        } catch (e: Exception) {
            Log.e("AuthError", "Error during registration: ${e.message}")
            RegistrationState.Error(e.message ?: "An error occurred during registration.")
        }
    }



    suspend fun login(email: String, password: String): LoginState {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            LoginState.Success
        } catch (e: Exception) {
            Log.e("AuthError", "Error logging in with Firebase Auth: ${e.message}")
            LoginState.Error(e.message ?: "An error occurred while logging in.")
        }
    }

    private fun generateUniqueUsername(): Task<String> {
        val taskSource = TaskCompletionSource<String>()
        val potentialUsername = "Doodl${Random.nextInt(1000, 9999)}"

        firestore.collection("users").whereEqualTo("username", potentialUsername).get().addOnSuccessListener { snapshot ->
            if (snapshot.isEmpty) {
                taskSource.setResult(potentialUsername)
            } else {
                // Recursive call to try generating another username
                generateUniqueUsername().addOnSuccessListener { uniqueUsername ->
                    taskSource.setResult(uniqueUsername)
                }.addOnFailureListener { exception ->
                    taskSource.setException(exception)
                }
            }
        }.addOnFailureListener { exception ->
            taskSource.setException(exception)
        }

        return taskSource.task
    }
}