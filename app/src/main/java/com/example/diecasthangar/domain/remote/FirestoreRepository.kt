package com.example.diecasthangar.domain.remote

import android.net.Uri
import com.example.diecasthangar.data.Post
import com.example.diecasthangar.domain.Response
import com.example.diecasthangar.domain.usecase.remote.getUser
import com.google.firebase.Timestamp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.*


open class FirestoreRepository (

    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val db: FirebaseFirestore = Firebase.firestore

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

    suspend fun getPostsFromFireStore(): Response<kotlin.collections.ArrayList<Post>> {

        val posts =     db.collection("posts")
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING).limit(10)
            .get().await().documents

        val postsList = arrayListOf<Post>()
        for (post in posts){
            val imageUris: ArrayList<String> = post.get("images") as ArrayList<String>
            val text: String = post.get("text") as String
            val timestamp: Timestamp = post.get("date") as Timestamp
            val user: String = post.get("user").toString()
            val username : String = post.get("username").toString()
            val avatar: String = post.get("avatar").toString()

            val newPost = Post(text, imageUris, user, timestamp.toDate(),username,avatar)

            postsList.add(newPost)
        }

        return try {
            Response.Success(postsList)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    suspend fun getUserUserName(userId: String) : Response<String> {
        return try {
            val userData = db.collection("userdata").document(userId).get().await().data
            Response.Success(userData!!["username"].toString())
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    suspend fun getUserAvatar(userId: String) : Response<String> {
        return try {
            val userData = db.collection("userdata").document(userId).get().await().data
            Response.Success(userData!!["avatar"].toString())
        } catch (e: Exception) {
            Response.Failure(e)
        }


    }

    fun addUserInfoToDatabase(userID: String, avatarUri: String, username: String): Response<Boolean>{
        return try {
            val userDataRef = db.collection("userdata").document(userID)
            val newUserData = hashMapOf(
                "avatar" to avatarUri,
                "username" to username
            )
            userDataRef.set(newUserData)
            Response.Success(true)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }
}