package com.example.diecasthangar.domain.remote

import android.content.ContentValues
import android.net.Uri
import android.util.Log
import com.example.diecasthangar.data.Comment
import com.example.diecasthangar.data.Post
import com.example.diecasthangar.data.getReacts
import com.example.diecasthangar.domain.Response
import com.example.diecasthangar.domain.usecase.remote.getUser
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firestore.v1.Cursor
import kotlinx.coroutines.tasks.await


open class FirestoreRepository (

    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val db: FirebaseFirestore = Firebase.firestore

    )  {

    suspend fun addImageToStorage(imageUri: Uri): Response<Uri> {

        return try {
            val rootRef = FirebaseDatabase.getInstance().reference
            val postRef = rootRef.child("posts")
            val remoteUris = arrayListOf<Uri>()
            val newPostKey = rootRef.ref.child("posts").push().key
            val filename: String = newPostKey + "_"
            val remoteUri = FirebaseStorage.getInstance().reference.child(
                    "images/posts").child("$filename.jpg").putFile(imageUri)
                    .await().storage.downloadUrl.await()
            Response.Success(remoteUri)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    suspend fun getPostsFromFireStore(): Response<ArrayList<Post>> {

        val posts =     db.collection("posts")
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING).limit(10)
            .get().await().documents

        val postsList = arrayListOf<Post>()
        for (post in posts){
            val imageUris: ArrayList<String> = post.get("images") as ArrayList<String>
            val text: String = post.get("text") as String
            val timestamp: Timestamp = post.getTimestamp("date") as Timestamp
            val user: String = post.get("user").toString()
            val username : String = post.get("username").toString()
            val avatar: String = post.get("avatar").toString()
            val id = post.id
            val comments: ArrayList<Comment> = ArrayList()
            //TODO implement
            val reactions: MutableMap<String,Int> = post.get("reactions") as MutableMap<String, Int>

            val newPost = Post(text, imageUris, user, timestamp.toDate(),username,avatar,id,comments,reactions)

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
    suspend fun addReaction(reaction: String, id: String) : Response<Boolean> {
        return try {
            db.collection("posts").document(id).update(
                "reactions.$reaction",FieldValue.increment(1)).await()

            Response.Success(true)
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

    suspend fun addPostToFirestore(text:String, remoteUris: ArrayList<Uri>): Response<Boolean> {

        return try {
            val rootRef = FirebaseDatabase.getInstance().reference
            val uid = getUser()!!.uid
            var avatarUri = ""

            when(val response = getUserAvatar(getUser()!!.uid)) {
                is Response.Loading -> {
                }
                is Response.Success -> {
                    avatarUri = response.data.toString()

                }
                is Response.Failure -> {
                    print(response.e)
                }
            }
            val newPostKey = rootRef.ref.child("posts").push().key
            val hashPost = hashMapOf(
                "text" to text,
                "id" to newPostKey,
                "images" to remoteUris,
                "user" to uid,
                "date" to FieldValue.serverTimestamp(),
                "username" to getUser()!!.displayName,
                "avatar" to avatarUri,
                "reactions" to getReacts()
                //TODO add hashmap to Post class/ vice versa helper functions
            )
            db.collection("posts").add(hashPost)
                .addOnSuccessListener {
                    Log.d(ContentValues.TAG, "post  added")
                }.addOnFailureListener { e ->
                    Log.e(ContentValues.TAG, "error adding document")
                }
            Response.Success(true)
        } catch (e: Exception) {
            Response.Failure(e)
        }

    }

    suspend fun deletePostFromFirestore(pid: String): Response<Boolean> {
        return try {
            db.collection("posts").document(pid).delete().await()
            Response.Success(true)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    suspend fun editFirestorePost(id: String, text: String) : Response<Boolean> {
        return try {
            db.collection("posts").document(id).update("text",text)

            Response.Success(true)
        } catch (e: Exception) {
            Response.Failure(e)

        }
    }

    suspend fun loadNextPagePosts(lastVisible: DocumentSnapshot? = null):
            Response<Pair<ArrayList<Post>,DocumentSnapshot>> {
        return try {
            lateinit var queryCursor: DocumentSnapshot


            val postsQuery = if (lastVisible != null) {
                db.collection("posts")
                    .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .startAfter(lastVisible).limit(10)
            } else {
                db.collection("posts")
                    .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(10)
            }

            val posts = postsQuery.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.documents.isNotEmpty()) {
                    queryCursor = documentSnapshot.documents[documentSnapshot.size() - 1]
                }
                else if (documentSnapshot.documents.isEmpty()){
                    queryCursor = lastVisible!!
                }

            }.await()

            val postsList = arrayListOf<Post>()
            for (post in posts) {
                val imageUris: ArrayList<String> = post.get("images") as ArrayList<String>
                val text: String = post.get("text") as String
                val timestamp: Timestamp = post.getTimestamp("date") as Timestamp
                val user: String = post.get("user").toString()
                val username: String = post.get("username").toString()
                val avatar: String = post.get("avatar").toString()
                val id = post.id
                val comments: ArrayList<Comment> = ArrayList()
                //TODO implement
                val reactions: MutableMap<String, Int> =
                    post.get("reactions") as MutableMap<String, Int>

                val newPost = Post(
                    text, imageUris, user, timestamp.toDate(), username, avatar, id,
                    comments, reactions
                )
                postsList.add(newPost)
            }
            val pair: Pair<ArrayList<Post>, DocumentSnapshot> = postsList to queryCursor
            return Response.Success(pair)
        } catch (e: ArrayIndexOutOfBoundsException){
            Response.Failure(e)
        } catch (e: Exception) {
            Response.Failure(e)

        }







    }


}



