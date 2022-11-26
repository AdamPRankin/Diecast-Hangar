package com.example.diecasthangar.data.remote

import android.content.ContentValues
import android.net.Uri
import android.util.Log
import androidx.lifecycle.asLiveData
import com.example.diecasthangar.core.util.commentMapToClass
import com.example.diecasthangar.core.util.docToPostClass
import com.example.diecasthangar.core.util.modelMapToClass
import com.example.diecasthangar.data.model.*
import com.example.diecasthangar.domain.remote.getUser
import com.google.android.gms.tasks.Tasks.await
import com.google.firebase.Timestamp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.rpc.context.AttributeContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.*


open class FirestoreRepository (
    private val db: FirebaseFirestore = Firebase.firestore,

    )  {

    private fun Query.paginate(lastVisibleItem: Flow<Int>): Flow<List<DocumentSnapshot>> = flow {
        val documents = mutableListOf<DocumentSnapshot>()
        documents.addAll(
            suspendCoroutine { c ->
                this@paginate.limit(25).get().addOnSuccessListener { c.resume(it.documents) }
            }
        )
        emit(documents)
        lastVisibleItem.transform { lastVisible ->
            if (lastVisible == documents.size && documents.size > 0) {
                documents.addAll(
                    suspendCoroutine { c ->
                        this@paginate.startAfter(documents.last())
                            .limit(25)
                            .get()
                            .addOnSuccessListener {
                                c.resume(it.documents)
                            }
                    }
                )
                emit(documents)
            }
        }.collect { docs ->
            emit(docs)
        }
    }

    suspend fun addImageToStorage(imageUri: Uri, path: String = "posts"): Response<Uri> {

        return try {
            val rootRef = FirebaseDatabase.getInstance().reference
            val newPostKey = rootRef.ref.child(path).push().key
            val filename: String = newPostKey + "_"
            val remoteUri = FirebaseStorage.getInstance().reference.child(
                    "images/$path").child("$filename.jpg").putFile(imageUri)
                    .await().storage.downloadUrl.await()
            Response.Success(remoteUri)
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

    suspend fun getUserInfo(userId: String): Response<Pair<String, String>> {
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

    fun addUserInfoToDatabase(userId: String, avatarUri: String, username: String): Response<Boolean> {
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

    suspend fun addPost(post: Post): Response<String> {
        return try {
            val remoteUris = arrayListOf<String>()
            val localUris = arrayListOf<Uri>()
            for (photo in post.images) {
                photo.localUri?.let { localUris.add(it) }
            }
            val rootRef = FirebaseDatabase.getInstance().reference
            for (uri in localUris) {
                val newPostKey = rootRef.ref.child("posts").push().key
                val filename: String = newPostKey + "_"
                val remoteUri = FirebaseStorage.getInstance().reference.child(
                    "images/posts"
                ).child("$filename.jpg").putFile(uri)
                    .await().storage.downloadUrl.await()
                remoteUris.add(remoteUri.toString())
            }

            val postsRef = db.collection("posts")

            val hPost = hashMapOf(
                "text" to post.text,
                "images" to remoteUris,
                "user" to post.user,
                "date" to FieldValue.serverTimestamp(),
                "username" to post.username,
                "avatar" to post.avatar,
                "reactions" to hashMapOf<String,Int>()
            )
            val postID = postsRef.add(hPost).await().id
            Response.Success(postID)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    suspend fun deletePostFromFirestore(pid: String): Response<Boolean> {
        return try {
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
            Response<Pair<ArrayList<Post>, DocumentSnapshot>> {
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
                val reactions: MutableMap<String, Int> =
                    post.get("reactions") as MutableMap<String, Int>
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

                val photos: List<Photo> = imageUris.map { Photo(remoteUri = it) }

                val newPost = Post(
                    text, photos as ArrayList<Photo>, user, timestamp.toDate(), username, avatar, id,
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

/*    fun getPostsFlow(lastVisibleItem: Flow<Int>): Flow<List<Post>> =
        FirebaseFirestore
            .getInstance().collection("posts")
            .orderBy("date", Query.Direction.DESCENDING)
            .paginate(lastVisibleItem)
            .map { docs -> docs.map { docToPostClass(it) }
            }*/

    fun getPostsFlow(lastVisibleItem: Flow<Int>): Flow<List<Post>> {
        return FirebaseFirestore
            .getInstance().collection("posts")
            .orderBy("date", Query.Direction.DESCENDING)
            .paginate(lastVisibleItem).map { docs ->
                docs.map {
                    docToPostClass(it)
                }
            }


    }


    suspend fun userPostsFlow(userId: String): Flow<Response<ArrayList<Post>>> = callbackFlow  {
        // 2.- We create a reference to our data inside Firestore
        val eventDocument =  FirebaseFirestore
            .getInstance()
            .collection("posts")
            .whereEqualTo("user", userId)
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)

        // 3.- We generate a subscription that is going to let us listen for changes with
        // .addSnapshotListener and then offer those values to the channel that will be collected in our viewmodel
        val subscription = eventDocument.addSnapshotListener { snapshot, _ ->
            if(snapshot!!.documents.isNotEmpty()){
                val posts = ArrayList<Post>()
                for (doc in snapshot) {
                    posts.add(docToPostClass(doc))
                }
                trySend(Response.Success(posts)).isSuccess
            }
        }
        //Finally if collect is not in use or collecting any data we cancel this channel to prevent any leak and remove the subscription listener to the database
        awaitClose { subscription.remove() }
    }



    //TODO add current UID to repostiry
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
                    val hashComment = hashMapOf(
                        "avatar" to avatarUri,
                        "date" to FieldValue.serverTimestamp(),
                        "post" to pid,
                        "text" to text,
                        "user" to uid,
                        "username" to username,
                        "reactions" to hashMapOf<String,Int>(),
                        "total-reacts" to 0
                    )
                    db.collection("comments").add(hashComment).addOnSuccessListener {
                        Log.d(ContentValues.TAG, "comment added")
                    }.addOnFailureListener { e ->
                        Log.e(ContentValues.TAG, "error adding document")
                    }
                }
                is Response.Failure -> {
                    print(response.e)
                }
            }
            Response.Success(true)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    suspend fun getFireStoreCommentsPage(pid: String,lastVisible: DocumentSnapshot? = null, limit: Long)
    : Response<Pair<ArrayList<Comment>, DocumentSnapshot>> {
        return try {
            lateinit var queryCursor: DocumentSnapshot

            val commentsQuery = if (lastVisible != null) {
                db.collection("comments").whereEqualTo("post",pid)
                    .startAfter(lastVisible).limit(limit)
            } else {
                db.collection("comments").whereEqualTo("post",pid).limit(limit)
            }

            val comments = commentsQuery.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.documents.isNotEmpty()) {
                    queryCursor = documentSnapshot.documents[documentSnapshot.size() - 1]
                } else if (documentSnapshot.documents.isEmpty()) {
                    if (lastVisible != null) {
                        queryCursor = lastVisible
                    }
                }
            }.await()

            val commentsList = arrayListOf<Comment>()
            for (comment in comments) {
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
                val newComment = commentMapToClass(comment)
                commentsList.add(newComment)
            }
            Response.Success(commentsList)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    suspend fun getUserFriends(uid: String): Response<ArrayList<User>> {
        return try {
            val friendsRef = db.collection("userfriends").document(uid)
            val friendIdList = friendsRef.get().await().data?.get("friends") as ArrayList<String>
            val friendList = ArrayList<User>()

            val friendRequestsRef = db.collection("friend-requests").whereEqualTo("recipient",uid)
            val requestsList = friendRequestsRef.get().await().documents

            for (request in requestsList){
                val requesterId = request.get("sender").toString()
                when(val response = getUserInfo(requesterId)) {
                    is Response.Loading -> {
                    }
                    is Response.Success -> {
                        val (avatar,username) = response.data!!
                        val friend = User(requesterId,username,avatar,false,request.id)
                        friendList.add(friend)
                    }
                    is Response.Failure -> {
                        print(response.e)
                    }
                }
            }
            for (id in friendIdList){
                when(val response = getUserInfo(id)) {
                    is Response.Loading -> {
                    }
                    is Response.Success -> {
                        val (avatar,username) = response.data!!
                        val friend = User(id,username,avatar,true)
                        friendList.add(friend)
                    }
                    is Response.Failure -> {
                        print(response.e)
                    }
                }
            }
            val e = ""
            Response.Success(friendList)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

/*    suspend fun userFriendsFlow(userId: String): Flow<Response<ArrayList<User>>> = callbackFlow  {
        // create a reference
        val eventDocumentFriend =  FirebaseFirestore
            .getInstance()
            .collection("userfriends")
            .document(userId)

        // listen for changes with addSnapshotListener and then offer values to the channel
        val subscription = eventDocumentFriend.addSnapshotListener { snapshot, _ ->
            val users = snapshot!!.data
            val friends = kotlin.collections.ArrayList<User>()
            for (uid in users!!) {
                val b =uid.toString()
                CoroutineScope(Dispatchers.IO).launch {
                    when(val response = getUserInfo(b)) {
                        is Response.Loading -> {
                        }
                        is Response.Success -> {
                            val (avatar,username) = response.data!!
                            val friend = User(b,username,avatar,false)
                            friends.add(friend)
                        }
                        is Response.Failure -> {
                            print(response.e)
                        }
                    }
                }
                trySend(Response.Success(friends)).isSuccess
            }

        }

        //if collect is not in use remove the subscription listener to the database
        awaitClose { subscription.remove() }
}

    suspend fun userFriendRequestsFlow(userId: String): Flow<Response<ArrayList<User>>> = callbackFlow  {
        // create a reference
        val eventDocumentFriend =  FirebaseFirestore
            .getInstance()
            .collection("friend-requests")
            .whereEqualTo("recipient", userId)

        // listen for changes with addSnapshotListener and then offer values to the channel
        val subscription = eventDocumentFriend.addSnapshotListener { snapshot, _ ->
            if(snapshot!!.documents.isNotEmpty()){
                val users = ArrayList<User>()
                for (doc in snapshot) {
                    val avatar = doc.get("avatar").toString()
                    val username = doc.get("username").toString()
                    val friend = User(doc.id,username,avatar,false)
                    users.add(friend)
                }
                trySend(Response.Success(users)).isSuccess
            }
        }
        //if collect is not in use remove the subscription listener to the database
        awaitClose { subscription.remove() }
    }*/

    fun addFriend(uid: String, friendId: String): Response<Boolean> {
        return try {
            val friendRef = db.collection("userfriends")
            friendRef.document(uid).update("friends", FieldValue.arrayUnion(friendId))
            friendRef.document(friendId).update("friends", FieldValue.arrayUnion(uid))

            val friendRequestsRef = db.collection("friend-requests").document(uid)
            Response.Success(true)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    fun deleteFriendRequest(requestId: String): Response<Boolean> {
        return try {
            val friendRequestRef = db.collection("friend-requests").document(requestId)
            friendRequestRef.delete()
            Response.Success(true)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    fun addFriendRequest(sender: String, recipient: String): Response<String> {
        return try {
            val hashRequest = hashMapOf(
                "sender" to sender,
                "recipient" to recipient,
            )
            val docRef  = await(db.collection("friend-requests").add(hashRequest))
            Response.Success(docRef.id)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    fun addFriendRequestToken(sender: String): Response<String> {
        return try {
            val hashRequest = hashMapOf(
                "sender" to sender,
                "recipient" to null,
                "timestamp" to FieldValue.serverTimestamp()
            )
            val docRef  = await(db.collection("friend-requests").add(hashRequest))
            Response.Success(docRef.id)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    suspend fun addFriendFromToken(token: String, uid :String): Response<Boolean> {
        return try {
            val friendRequest= db.collection("friend-requests").document(token).get().await().data
            val friendId = friendRequest!!["sender"] as String
            val friendRef = db.collection("userfriends")
            friendRef.document(uid).update("friends", FieldValue.arrayUnion(friendId))
            friendRef.document(friendId).update("friends", FieldValue.arrayUnion(uid))

            db.collection("friend-requests").document(token).delete()
            Response.Success(true)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    suspend fun addModel(model: Model): Response<Model> {
        return try {
            val remoteUris = arrayListOf<String>()

            val rootRef = FirebaseDatabase.getInstance().reference
            for (photo in model.photos) {
                val newPostKey = rootRef.ref.child("models").push().key
                val filename: String = newPostKey + "_"
                val remoteUri = FirebaseStorage.getInstance().reference.child(
                    "images/models"
                ).child("$filename.jpg").putFile(photo.localUri!!)
                    .await().storage.downloadUrl.await()
                remoteUris.add(remoteUri.toString())
            }

            val modelsRef = db.collection("models")

           val h = hashMapOf(
               "user" to getUser()!!.uid,
               "manufacturer" to model.manufacturer,
               "mould" to model.mould,
               "scale" to model.scale,
               "frame" to model.frame,
               "airline" to model.airline,
               "livery" to model.livery,
               "photos" to remoteUris,
               "price" to model.price,
               "comment" to model.comment,
            )

            val mid = modelsRef.add(h).await().id
            val photos:ArrayList<Photo> = remoteUris.map { Photo(remoteUri = it) } as ArrayList<Photo>
            val modelWithId = model.copy(id = mid, photos = photos)
            Response.Success(modelWithId)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    suspend fun userModelsFlow(userId: String): Flow<Response<ArrayList<Model>>> = callbackFlow  {
        // create a reference
        val eventDocument =  FirebaseFirestore
            .getInstance()
            .collection("models")
            .whereEqualTo("user", userId)

        // listen for changes with addSnapshotListener and then offer values to the channel
        val subscription = eventDocument.addSnapshotListener { snapshot, _ ->
            if(snapshot!!.documents.isNotEmpty()){
                val models = ArrayList<Model>()
                for (doc in snapshot) {
                    models.add(modelMapToClass(doc))
                }
                trySend(Response.Success(models)).isSuccess
            }
        }
        //if collect is not in use remove the subscription listener to the database
        awaitClose { subscription.remove() }
    }

    suspend fun deleteModel(modelId: String): Response<Boolean> {
        return try {

            val images = (db.collection("posts").document(modelId).get().await().data?.get("images") ?: arrayListOf<String>()) as ArrayList<String>

            //todo more elegant solution
            for (uri in images) {
                val id: String = uri.toString().replace(
                    "https://firebasestorage.googleapis.com/v0/b/diecast-hangar.appspot.com/o/images%2Fposts%2F",
                    ""
                ).split(".jpg?")[0]
                FirebaseStorage.getInstance().reference.child(
                    "images/models/$id.jpg"
                ).delete()
            }

            val modelsRef = db.collection("models")
            modelsRef.document(modelId).delete()
            Response.Success(true)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    fun editFirestoreModel(model: Model) : Response<Boolean> {
        return try {
            val remoteUris = model.photos.map { it.remoteUri }

            db.collection("models").document(model.id!!).update(
                "user", getUser()!!.uid,
                "manufacturer" , model.manufacturer,
                "mould" , model.mould,
                "scale" , model.scale,
                "frame" , model.frame,
                "airline" , model.airline,
                "livery" , model.livery,
                "photos" , remoteUris,
                "price" , model.price,
                "comment" , model.comment,
            )

            Response.Success(true)
        } catch (e: Exception) {
            Response.Failure(e)

        }
    }
}



