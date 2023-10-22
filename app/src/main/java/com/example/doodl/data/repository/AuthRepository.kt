package com.example.doodl.data.repository

import android.util.Log
import com.example.doodl.data.DoodlUser
import com.example.doodl.viewmodel.LoginState
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.random.Random


class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun register(email: String, password: String): Task<Void> {
        val registerTask = TaskCompletionSource<Void>()
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val firebaseUser = auth.currentUser
                if (firebaseUser != null) {
                    generateUniqueUsername().addOnSuccessListener { uniqueUsername ->
                        val user = DoodlUser(firebaseUser.uid, uniqueUsername, null, "DefaultBio")
                        firestore.collection("users").document(firebaseUser.uid).set(user).addOnCompleteListener { firestoreTask ->
                            if (firestoreTask.isSuccessful) {
                                registerTask.setResult(null)
                            } else {
                                firestoreTask.exception?.let {
                                    registerTask.setException(it)
                                } ?: run {
                                    registerTask.setException(Exception("Unknown error during Firestore write operation"))
                                }
                                Log.e("FirestoreError", "Error writing to Firestore: ${firestoreTask.exception?.message}")
                            }
                        }

                    }.addOnFailureListener { exception ->
                        registerTask.setException(exception)
                        Log.e("UsernameError", "Error generating unique username: ${exception.message}")
                    }
                } else {
                    registerTask.setException(Exception("User creation was successful but user is null"))
                    Log.e("AuthError", "User creation was successful but user is null")
                }
            } else {
                task.exception?.let {
                    registerTask.setException(it)
                } ?: run {
                    registerTask.setException(Exception("Unknown error during authentication"))
                }
                Log.e("AuthError", "Error creating user with Firebase Auth: ${task.exception?.message}")
            }
        }
        return registerTask.task
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