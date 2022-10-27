package com.example.diecasthangar.data.repository

import android.net.Uri
import com.example.diecasthangar.domain.Response
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

open class PostImageRepository (
    private val storage: FirebaseStorage,
    private val db: FirebaseFirestore,

)  {
    suspend fun addImageToFirebaseStorage(imageUri: Uri, pid: String): Response<Uri> {
        return try {
            val downloadUrl = storage.reference.child("images/posts").child("$pid.jpg")
                .putFile(imageUri).await()
                .storage.downloadUrl.await()
            Response.Success(downloadUrl)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    suspend fun addAvatarToFirebaseStorage(imageUri: Uri, pid: String): Response<Uri> {
        return try {
            val downloadUrl = storage.reference.child("images/avatars").child("$pid.jpg")
                .putFile(imageUri).await()
                .storage.downloadUrl.await()
            Response.Success(downloadUrl)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    suspend fun addImageUrlToFirestore(downloadUrl: Uri, pid: String): Response<Boolean> {
        return try {
            db.collection("images").document(pid).set(
                mapOf(
                    "url" to downloadUrl,
                    "created at" to FieldValue.serverTimestamp()
                )
            ).await()
            Response.Success(true)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    suspend fun getImageUrlFromFirestore(pid: String): Response<String> {
        return try {
            val imageUrl = db.collection("images").document(pid).get().await().getString("url")
            Response.Success(imageUrl)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

}