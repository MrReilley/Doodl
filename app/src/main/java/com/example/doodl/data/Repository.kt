package com.example.doodl.data
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask

// Code that interacts with Firebase Firestore and Firebase Firestorage
// API for data access for rest of application
// Data operations, abstracting origin of data
class Repository {
    private val storageReference = FirebaseStorage.getInstance().reference

    // Function used in CanvasViewModel to upload byte array representing an image to Firebase Storage
    fun uploadByteArray(byteArray: ByteArray): UploadTask {
        // Generate a unique file reference in Firebase Storage using the current timestamp
        val fileRef = storageReference.child("feed/${System.currentTimeMillis()}.png")
        // Upload byte array to Firebase Storage reference
        return fileRef.putBytes(byteArray)
    }

}


