package com.pingu.diecasthangar.data.remote

import android.content.ContentValues
import android.net.Uri
import android.util.Log
import com.pingu.diecasthangar.core.util.commentMapToClass
import com.pingu.diecasthangar.core.util.docToPostClass
import com.pingu.diecasthangar.core.util.modelMapToClass
import com.google.android.gms.tasks.Tasks.await
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.pingu.diecasthangar.data.model.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


open class FirestoreRepository (
    private val db: FirebaseFirestore = Firebase.firestore,
    // Jan 1st 2022
    private val SERVER_START: Long = 1641020400000
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

    fun getPosts(lastVisibleItem: Flow<Int>): Flow<List<Post>> =
        flow {
            val postsDocSnap = mutableListOf<DocumentSnapshot>()
            postsDocSnap.addAll(
                suspendCoroutine<List<DocumentSnapshot>> { c ->
                    Firebase.firestore.collection("posts")
                        .orderBy("date", Query.Direction.DESCENDING)
                        .limit(25)
                        .get().addOnSuccessListener {
                            c.resume(it.documents)
                        }
                }
            )

            emit(postsDocSnap.map { docToPostClass(it) })

            lastVisibleItem.transform { lastVisibleItem ->
                if (lastVisibleItem == postsDocSnap.size) {
                    postsDocSnap.addAll(
                        suspendCoroutine<List<DocumentSnapshot>> { c ->
                            Firebase.firestore.collection("posts")
                                .orderBy("date", Query.Direction.DESCENDING)
                                .startAfter(postsDocSnap.last())
                                .limit(25)
                                .get().addOnSuccessListener {
                                    c.resume(it.documents)
                                }
                        }
                    )
                    emit(postsDocSnap.map { docToPostClass(it) })
                }
            }.collect { newPosts: List<Post> ->
                emit(newPosts)
            }
        }

    fun getTopPosts(lastVisibleItem: Flow<Int>): Flow<List<Post>> =
        flow {
            val postsDocSnap = mutableListOf<DocumentSnapshot>()
            val time = System.currentTimeMillis()
            val currentHoursElapsed = kotlin.math.floor((time-SERVER_START) / 60.0 / 60.0 / 1000).toInt()
            postsDocSnap.addAll(
                suspendCoroutine<List<DocumentSnapshot>> { c ->
                    Firebase.firestore.collection("posts")
                        .whereArrayContains("recent", currentHoursElapsed)
                        .orderBy("total-reacts", Query.Direction.DESCENDING)
                        .limit(25)
                        .get().addOnSuccessListener {
                            c.resume(it.documents)
                        }
                }
            )

            emit(postsDocSnap.map { docToPostClass(it) })

            lastVisibleItem.transform { lastVisibleItem ->
                if (lastVisibleItem == postsDocSnap.size) {
                    postsDocSnap.addAll(
                        suspendCoroutine<List<DocumentSnapshot>> { c ->
                            Firebase.firestore.collection("posts")
                                .whereArrayContains("recent", currentHoursElapsed)
                                .orderBy("total-reacts", Query.Direction.DESCENDING)
                                .startAfter(postsDocSnap.last())
                                .limit(25)
                                .get().addOnSuccessListener {
                                    c.resume(it.documents)
                                }
                        }
                    )
                    emit(postsDocSnap.map { docToPostClass(it) })
                }
            }.collect { newPosts: List<Post> ->
                emit(newPosts)
            }
        }

    fun getNewsPosts(lastVisibleItem: Flow<Int>): Flow<List<Post>> =
        flow {
            val postsDocSnap = mutableListOf<DocumentSnapshot>()
            postsDocSnap.addAll(
                suspendCoroutine<List<DocumentSnapshot>> { c ->
                    Firebase.firestore.collection("posts")
                        .whereEqualTo("news",true)
                        .orderBy("date", Query.Direction.DESCENDING)
                        .limit(25)
                        .get().addOnSuccessListener {
                            c.resume(it.documents)
                        }
                }
            )
            Log.d("log","${postsDocSnap.size} docs")
            emit(postsDocSnap.map { docToPostClass(it) })

            lastVisibleItem.transform { lastVisibleItem ->
                if (lastVisibleItem == postsDocSnap.size) {
                    postsDocSnap.addAll(
                        suspendCoroutine<List<DocumentSnapshot>> { c ->
                            val e = Firebase.firestore.collection("posts")
                                .whereEqualTo("news",true)
                                .orderBy("date", Query.Direction.DESCENDING)
                                .startAfter(postsDocSnap.last())
                                .limit(25)
                                .get().addOnSuccessListener {
                                    c.resume(it.documents)
                                }
                            Log.d("log", e.toString())
                        }
                    )
                    Log.d("log","${postsDocSnap.size} docs")
                    emit(postsDocSnap.map { docToPostClass(it) })
                }
            }.collect { newPosts: List<Post> ->
                Log.d("log","${postsDocSnap.size} posts")
                emit(newPosts)
            }
        }

    fun newPostsFlow(): Flow<Response<List<Post>>> = callbackFlow  {
        val eventDocument =  FirebaseFirestore
            .getInstance()
            .collection("posts")
            .orderBy("date", Query.Direction.ASCENDING)
            .endAt(System.currentTimeMillis())

        //listen for changes with addSnapshotListener
        val subscription = eventDocument.addSnapshotListener { snapshot, _ ->
            val posts = ArrayList<Post>()
            if(snapshot!!.documents.isNotEmpty()){
                for (doc in snapshot) {
                    posts.add(docToPostClass(doc))
                }
            }
            trySend(Response.Success(posts)).isSuccess
        }
        //if collect is not in use cancel this channel to prevent any leak and remove the subscription
        awaitClose { subscription.remove() }
    }
/*
    fun newPostFlow(): Flow<List<Post>> =
        flow {
            val postsDocSnap = mutableListOf<DocumentSnapshot>()
            postsDocSnap.addAll(
                suspendCoroutine<List<DocumentSnapshot>> { c ->
                    Firebase.firestore.collection("posts")
                        .orderBy("date", Query.Direction.ASCENDING)
                        .startAfter(FieldValue.serverTimestamp())
                        .get().addOnSuccessListener {
                            c.resume(it.documents)
                        }
                }
            )

            emit(postsDocSnap.map { docToPostClass(it) })
        }
*/

/*    suspend fun newPostFlow(lastVisibleItem: Flow<Int>): Flow<Response<ArrayList<Post>>> = callbackFlow  {
        // 2.- We create a reference to our data inside Firestore
        val eventDocument =  FirebaseFirestore
            .getInstance()
            .collection("posts")
            .orderBy("date", Query.Direction.DESCENDING)
            .endBefore(lastVisibleItem)

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
    }*/

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
            Response.Success(Pair(avatarUri, username))
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    suspend fun addReaction(reaction: String, id: String) : Response<Boolean> {
        return try {
            db.collection("posts").document(id).update(
                "reactions.$reaction",FieldValue.increment(1)).await()
            db.collection("posts").document(id).update("total-reacts",FieldValue.increment(1)).await()

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

            //used to track which posts are recent within 1 week
            val timeFrameArray = getHoursArray()

            val postsRef = db.collection("posts")

            val hPost = hashMapOf(
                "text" to post.text,
                "images" to remoteUris,
                "user" to post.user,
                "date" to FieldValue.serverTimestamp(),
                "username" to post.username,
                "avatar" to post.avatar,
                "reactions" to hashMapOf<String,Int>(),
                "total-reacts" to 0,
                "recent" to timeFrameArray
            )
            val postID = postsRef.add(hPost).await().id
            Log.d("info", "Post: $postID added")
            Response.Success(postID)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    //returns an array of hours since server start time
    //used to get last week of posts from firebase to work around compound query limitations
    private fun getHoursArray(): ArrayList<Int> {
        val time = System.currentTimeMillis()
        val withinTimeframeArray = arrayListOf<Int>()
        val currentHoursElapsed = kotlin.math.floor((time-SERVER_START) / 60.0 / 60.0 / 1000).toInt()
        // hours within 1 week
        for (i in 0..168){
            withinTimeframeArray.add(currentHoursElapsed+i)
        }
        return withinTimeframeArray
    }

    suspend fun deletePostFromFirestore(pid: String): Response<Boolean> {
        return try {
            val images = db.collection("posts").document(pid).get().await().data?.get("images") as ArrayList<String>
            //todo more elegant solution
            //delete images from storage
            for (uri in images) {
                val id: String = uri.replace("https://firebasestorage.googleapis.com/v0/b/diecast-hangar.appspot.com/o/images%2Fposts%2F","").split(".jpg?")[0]
                FirebaseStorage.getInstance().reference.child(
                    "images/posts/$id.jpg").delete()
            }

            db.collection("posts").document(pid).delete().await()
            Response.Success(true)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    fun deleteImage(imageUri: String, dir: String){
        val id: String = imageUri.replace("https://firebasestorage.googleapis.com/v0/b/diecast-hangar.appspot.com/o/images%2Fposts%2F","").split(".jpg?")[0]
        FirebaseStorage.getInstance().reference.child(
            "images/$dir/$id.jpg").delete()

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

    suspend fun userPostsFlow(userId: String): Flow<Response<List<Post>>> = callbackFlow  {
        // 2.- We create a reference to our data inside Firestore
        val eventDocument =  FirebaseFirestore
            .getInstance()
            .collection("posts")
            .whereEqualTo("user", userId)
            .orderBy("date", Query.Direction.DESCENDING)

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

    suspend fun friendPostsFlow(userId: String): Flow<List<Post>> = callbackFlow  {
        // 2.- We create a reference to our data inside Firestore
        val eventDocument =  FirebaseFirestore
            .getInstance()
            .collection("posts")
            .whereEqualTo("user", userId)
            .orderBy("date", Query.Direction.DESCENDING)

        // 3.- We generate a subscription that is going to let us listen for changes with
        // .addSnapshotListener and then offer those values to the channel that will be collected in our viewmodel
        val subscription = eventDocument.addSnapshotListener { snapshot, _ ->
            if(snapshot!!.documents.isNotEmpty()){
                val posts = ArrayList<Post>()
                for (doc in snapshot) {
                    posts.add(docToPostClass(doc))
                }
                trySend(posts).isSuccess
            }
        }
        //Finally if collect is not in use or collecting any data we cancel this channel to prevent any leak and remove the subscription listener to the database
        awaitClose { subscription.remove() }
    }

    suspend fun addFirestoreComment(pid: String, text: String, uid: String) : Response<Boolean> {
        return try {
            val avatarUri: String
            val username: String

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

    fun deleteComment(cid: String): Response<Boolean> {
        return try {
            db.collection("comments").document(cid).delete()
            Response.Success(true)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    fun editComment(cid: String, newText: String): Response<Boolean> {
        db.collection("comments").document(cid).update("text",newText)
        return try {
            db.collection("comments").document(cid).update("text",newText)
            Response.Success(true)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    suspend fun getUserFriends(uid: String, requests: Boolean = true): Response<ArrayList<User>> {
        return try {
            val friendsRef = db.collection("userfriends").document(uid)
            val friendIdList = friendsRef.get().await().data?.get("friends") as ArrayList<String>
            val friendList = ArrayList<User>()

            val friendRequestsRef = db.collection("friend-requests").whereEqualTo("recipient",uid)
            val requestsList = friendRequestsRef.get().await().documents

            //only add requests for current user profile
            if (uid == getUser()!!.uid && requests) {
                for (request in requestsList) {
                    val requesterId = request.get("sender").toString()
                    when (val response = getUserInfo(requesterId)) {
                        is Response.Loading -> {
                        }
                        is Response.Success -> {
                            val (avatar, username) = response.data!!
                            val friend = User(requesterId, username, avatar, false, request.id)
                            friendList.add(friend)
                        }
                        is Response.Failure -> {
                            print(response.e)
                        }
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

    fun addFriend(uid: String, friendId: String): Response<Boolean> {
        return try {
            val friendRef = db.collection("userfriends")
            friendRef.document(uid).update("friends", FieldValue.arrayUnion(friendId))
            friendRef.document(friendId).update("friends", FieldValue.arrayUnion(uid))
            val friendRequestsRef = db.collection("friend-requests").document(uid)
            //todo remove
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
               "registration" to model.reg
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
                val id: String = uri.replace(
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
                "registration", model.reg
            )

            Response.Success(true)
        } catch (e: Exception) {
            Response.Failure(e)

        }
    }
}



