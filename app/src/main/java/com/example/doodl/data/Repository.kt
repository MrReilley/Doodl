package com.example.doodl.data
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import android.net.Uri
import java.io.File

class Repository {
    // Code that interacts with Firebase Firestore and Firebase Storage
    // API for data access for rest of application
    // Data operations, abstracting origin of data

}
class MyFirebaseDatabase {

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("message")

    fun setMessage(message: String) {
        myRef.setValue(message)
    }
}

class MyFirebaseStorage {

    /*A reference can be thought of as a pointer to a file in the cloud. References are lightweight,
    so you can create as many as you need. They are also reusable for multiple operations.*/

    //Create a reference using the FirebaseStorage singleton instance
    private val storage = FirebaseStorage.getInstance()
    // Create a storage reference from our app
    private val storageRef = storage.reference

    fun uploadImage(filePath: String) {
        // Create a Uri from the file path
        val file = Uri.fromFile(File(filePath))

        // Points to "feed"
        val imagesRef = storageRef.child("feed")

        // Create a reference to the file to be uploaded
        val fileRef = imagesRef.child(file.lastPathSegment ?: return)

        // Start the file upload
        val uploadTask = fileRef.putFile(file)

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
        }.addOnSuccessListener { taskSnapshot ->
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...
        }
    }
}
