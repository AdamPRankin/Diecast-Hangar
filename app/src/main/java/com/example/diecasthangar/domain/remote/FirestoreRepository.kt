package com.example.diecasthangar.domain.remote

import android.R.attr.bitmap
import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.example.diecasthangar.core.util.commentMapToClass
import com.example.diecasthangar.data.Comment
import com.example.diecasthangar.data.Photo
import com.example.diecasthangar.data.Post
import com.example.diecasthangar.data.getReacts
import com.example.diecasthangar.domain.Response
import com.example.diecasthangar.domain.usecase.remote.getUser
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import io.grpc.Compressor
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.IOException


open class FirestoreRepository (

    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val db: FirebaseFirestore = Firebase.firestore,

    )  {

    suspend fun addImageToStorage(imageUri: Uri): Response<Uri> {

        return try {
            val rootRef = FirebaseDatabase.getInstance().reference
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

    suspend fun getUserUsername(userId: String) : Response<String> {
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

    suspend fun updateUserAvatar(imageUri: Uri, userId: String): Response<Uri> {
        return try {

            val remoteUri = FirebaseStorage.getInstance().reference.child(
                "images/avatars").child("$userId.jpg").putFile(imageUri)
                .await().storage.downloadUrl.await()
            val userDataRef = db.collection("userdata").document(userId)
            userDataRef.update("avatar",remoteUri)
            Response.Success(remoteUri)
        } catch (e: Exception) {
            Response.Failure(e)
        }

    }

    suspend fun getUserInfo(userId: String): Response<Pair<String,String>> {
        return try {
            val userData = db.collection("userdata").document(userId).get().await().data
            val avatarUri = userData!!["avatar"].toString()
            val username = userData["username"].toString()
            Response.Success(Pair(avatarUri,username))
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

    fun addUserInfoToDatabase(userId: String, avatarUri: String, username: String): Response<Boolean>{
        return try {
            val userDataRef = db.collection("userdata").document(userId)
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

    fun updateUserBio(userId: String, text: String): Response<Boolean> {
        return try {
            val userDataRef = db.collection("userdata").document(userId)
            userDataRef.update("bio",text)
            Response.Success(true)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    suspend fun getUserBio(userId: String): Response<String> {
        return try {
            val userData = db.collection("userdata").document(userId).get().await()

            val bio =
            if(userData.get("bio") != null){
                userData.get("bio") as String
            }
            else {
                 "This user has not written anything for their bio yet"
            }

            Response.Success(bio)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    suspend fun addPostToFirestore(text:String, remoteUris: ArrayList<Uri>, username: String, avatarUri: String?): Response<Boolean> {

        return try {
            val rootRef = FirebaseDatabase.getInstance().reference
            val avatar = avatarUri ?: ""

            val newPostKey = rootRef.ref.child("posts").push().key
            val hashPost = hashMapOf(
                "text" to text,
                "id" to newPostKey,
                "images" to remoteUris,
                "user" to getUser()!!.uid,
                "date" to FieldValue.serverTimestamp(),
                "username" to username,
                "avatar" to avatar,
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
            //TODO clean up photos from storage
            val images = db.collection("posts").document(pid).get().await().data?.get("images") as ArrayList<String>

            //todo more elegant solution
            for (uri in images) {
                val id: String = uri.replace("https://firebasestorage.googleapis.com/v0/b/diecast-hangar.appspot.com/o/images%2Fposts%2F","").split(".jpg?")[0]
                FirebaseStorage.getInstance().reference.child(
                    "images/posts/$id.jpg").delete()
            }

            //delete images from storage
            db.collection("posts").document(pid).delete().await()
            Response.Success(true)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    fun editFirestorePostText(pid: String, text: String) : Response<Boolean> {
        return try {
            db.collection("posts").document(pid).update("text",text)

            Response.Success(true)
        } catch (e: Exception) {
            Response.Failure(e)

        }
    }

    fun editFirestorePostPhotos(id: String, photos: ArrayList<Uri>) : Response<Boolean> {
        return try {
            db.collection("posts").document(id).update("images",photos)

            Response.Success(true)
        } catch (e: Exception) {
            Response.Failure(e)

        }
    }

    suspend fun loadNextPagePosts(lastVisible: DocumentSnapshot? = null,limit: Long  = 8):
            Response<Pair<ArrayList<Post>,DocumentSnapshot>> {
        return try {
            lateinit var queryCursor: DocumentSnapshot


            val postsQuery = if (lastVisible != null) {
                db.collection("posts")
                    .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .startAfter(lastVisible).limit(limit)
            } else {
                db.collection("posts")
                    .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(limit)
            }

            val posts = postsQuery.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.documents.isNotEmpty()) {
                    queryCursor = documentSnapshot.documents[documentSnapshot.size() - 1]
                } else if (documentSnapshot.documents.isEmpty()) {
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
                var comments: ArrayList<Comment>? = ArrayList()
                when(val response = getTopRatedComments(post.id,8)) {
                    is Response.Loading -> {
                    }
                    is Response.Success -> {
                        comments = response.data

                    }
                    is Response.Failure -> {
                        print(response.e)
                    }
                }
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
        } catch (e: ArrayIndexOutOfBoundsException) {
            Response.Failure(e)
        } catch (e: Exception) {
            Response.Failure(e)

        }
    }

    suspend fun loadNextPagePostsFromUser(lastVisible: DocumentSnapshot? = null,limit: Long  = 10,userId: String):
            Response<Pair<ArrayList<Post>,DocumentSnapshot>> {
        return try {
            lateinit var queryCursor: DocumentSnapshot

            val postsQuery = if (lastVisible != null) {
                db.collection("posts").whereEqualTo("user",userId)
                    .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .startAfter(lastVisible).limit(limit)
            } else {
                db.collection("posts").whereEqualTo("user",userId)
                    .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(8)
            }

            val posts = postsQuery.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.documents.isNotEmpty()) {
                    queryCursor = documentSnapshot.documents[documentSnapshot.size() - 1]
                } else if (documentSnapshot.documents.isEmpty()) {
                    if (lastVisible != null) {
                        queryCursor = lastVisible
                    }
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
                var comments: ArrayList<Comment>? = ArrayList()
                when(val response = getTopRatedComments(post.id,8)) {
                    is Response.Loading -> {
                    }
                    is Response.Success -> {
                        comments = response.data

                    }
                    is Response.Failure -> {
                        print(response.e)
                    }
                }
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
        } catch (e: ArrayIndexOutOfBoundsException) {
            Response.Failure(e)
        } catch (e: Exception) {
            Response.Failure(e)

        }
    }

    suspend fun addFirestoreComment(pid: String, text: String, uid: String) : Response<Boolean> {
        return try {
            var avatarUri = ""
            var username = ""

            when(val response = getUserInfo(getUser()!!.uid)) {
                is Response.Loading -> {
                }
                is Response.Success -> {
                    val (av,usr) = response.data!!
                    avatarUri = av
                    username = usr

                }
                is Response.Failure -> {
                    print(response.e)
                }
            }

            val hashComment = hashMapOf(
                "avatar" to avatarUri,
                "date" to FieldValue.serverTimestamp(),
                "post" to pid,
                "text" to text,
                "user" to uid,
                "username" to username,
                "reactions" to getReacts(),
                "total-reacts" to 0
            )

            db.collection("comments").add(hashComment).addOnSuccessListener {
                Log.d(ContentValues.TAG, "comment added")
            }.addOnFailureListener { e ->
                Log.e(ContentValues.TAG, "error adding document")
            }
            Response.Success(true)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    suspend fun getFireStoreCommentsPage(pid: String,lastVisible: DocumentSnapshot? = null)
    : Response<Pair<ArrayList<Comment>,DocumentSnapshot>> {
        return try {
            lateinit var queryCursor: DocumentSnapshot

            val commentsQuery = if (lastVisible != null) {
                db.collection("comments").whereEqualTo("post",pid)
                    .startAfter(lastVisible).limit(10)
            } else {
                db.collection("comments").whereEqualTo("post",pid).limit(10)
            }

            val comments = commentsQuery.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.documents.isNotEmpty()) {
                    queryCursor = documentSnapshot.documents[documentSnapshot.size() - 1]
                } else if (documentSnapshot.documents.isEmpty()) {
                    queryCursor = lastVisible!!
                }
            }.await()

            val commentsList = arrayListOf<Comment>()
            for (comment in comments) {
/*
                val text: String = comment.get("text") as String
                val timestamp: Timestamp = comment.getTimestamp("date") as Timestamp
                val user: String = comment.get("user").toString()
                val username: String = comment.get("username").toString()
                val avatar: String = comment.get("avatar").toString()
                val id = comment.id
                val postId: String = comment.get("post") as String
                val reactions: MutableMap<String, Int> = comment.get("reactions")
                        as MutableMap<String, Int>

                val newComment = Comment(text,user,username,avatar,id,timestamp.toDate(),postId,reactions)
*/

                val newComment = commentMapToClass(comment)
                commentsList.add(newComment)
            }
            val pair: Pair<ArrayList<Comment>, DocumentSnapshot> = commentsList to queryCursor

            Response.Success(pair)
        } catch (e: Exception) {
            Response.Failure(e)

        }
    }

    suspend fun getTopRatedComments(pid: String,number: Int)
            : Response<ArrayList<Comment>> {
        return try {
            lateinit var queryCursor: DocumentSnapshot

            val commentsQuery =
                db.collection("comments").whereEqualTo("post",pid)

                    .limit(number.toLong())

            val comments = commentsQuery.get().addOnSuccessListener {}.await()

            val commentsList = arrayListOf<Comment>()
            for (comment in comments) {
/*                val text: String = comment.get("text") as String
                val timestamp: Timestamp = comment.getTimestamp("date") as Timestamp
                val user: String = comment.get("user").toString()
                val username: String = comment.get("username").toString()
                val avatar: String = comment.get("avatar").toString()
                val id = comment.id
                val postId: String = comment.get("post") as String
                val reactions: MutableMap<String, Int> = comment.get("reactions")
                        as MutableMap<String, Int>*/

                //val newComment = Comment(text,user,username,avatar,id,timestamp.toDate(),postId,reactions)
                val newComment = commentMapToClass(comment)
                commentsList.add(newComment)
            }
            Response.Success(commentsList)
        } catch (e: Exception) {
            Response.Failure(e)

        }
    }




}



