package com.example.diecasthangar.domain.remote

import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import com.example.diecasthangar.data.Post
import com.example.diecasthangar.domain.Response
import com.example.diecasthangar.domain.usecase.remote.getUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await


open class UploadPhoto (
    private val storage: FirebaseStorage,
    private val db: FirebaseFirestore

    )  {
    suspend fun addPostToFireStore(imageUri: Uri): Response<Uri> {

        return try {
            val rootRef = FirebaseDatabase.getInstance().reference
            val postRef = rootRef.child("posts")
            val remoteUris = arrayListOf<Uri>()

            val newPostKey = rootRef.ref.child("posts").push().key

            //for ((index, uri) in imageUris.withIndex()) {
                val filename: String = newPostKey + "_"
                val remoteUri = FirebaseStorage.getInstance().reference.child(
                    "images/posts").child("$filename.jpg").putFile(imageUri)
                    .await().storage.downloadUrl.await()
                //remoteUris.add(downloadUrl)
            //}
            Response.Success(remoteUri)
        } catch (e: Exception) {
            Response.Failure(e)
        }



    }
}