package com.example.diecasthangar

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.diecasthangar.core.util.loadingDummyPost
import com.example.diecasthangar.data.Post
import com.example.diecasthangar.domain.Response
import com.example.diecasthangar.domain.adapters.PostRecyclerAdapter
import com.example.diecasthangar.domain.remote.FirestoreRepository
import com.example.diecasthangar.domain.usecase.remote.getUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DashboardViewModel: ViewModel() {

    var isLoading = false
    var posts: MutableLiveData<ArrayList<Post>> = MutableLiveData()
    val storage: FirebaseStorage = FirebaseStorage.getInstance()
    val db: FirebaseFirestore = Firebase.firestore
    val repository = FirestoreRepository(storage,db)
    var userAvatarUri: String = ""
    val postAdapter = PostRecyclerAdapter()
    var snapshot: DocumentSnapshot? = null

    init {

        val loadingPost = loadingDummyPost()
        val list = arrayListOf(loadingPost)
        posts.value = list


        CoroutineScope(Dispatchers.IO).launch {

            when(val response = repository.loadNextPagePosts(snapshot)) {
                is Response.Loading -> {

                }
                is Response.Success -> {
                    val (postsList,newSnap) = response.data!!

                    //val postsList = addCommentsToPosts(repository,noCommentsPostList,3)

                    snapshot = newSnap
                    val newPosts = (postsList) as ArrayList<Post>?
                    posts.postValue(newPosts!!)

                }
                is Response.Failure -> {
                    print(response.e)
                }
            }
        }

        CoroutineScope(Dispatchers.IO).launch {

            when(val response = repository.getUserAvatar(getUser()!!.uid)) {
                is Response.Loading -> {
                }
                is Response.Success -> {
                    userAvatarUri = response.data.toString()

                }
                is Response.Failure -> {
                    print(response.e)
                }
            }
        }
    }

    suspend fun scrollLoadMorePosts(snap: DocumentSnapshot, loading: Boolean = isLoading){
        when(val response = repository.loadNextPagePosts(snapshot)) {
            is Response.Loading -> {

            }
            is Response.Success -> {
                val (postsList,newSnap) = response.data!!
                //val postsList = addCommentsToPosts(repository,noCommentsPostList,3)
                // if document snapshot is the same, then there are no more posts
                // to load, so set loading to false
                if (newSnap == snapshot){
                    isLoading = false
                }
                snapshot = newSnap
                val newPosts = posts.value?.plus(postsList) as ArrayList<Post>?
                posts.postValue(newPosts!!)

            }
            is Response.Failure -> {
                print(response.e)
            }
        }
    }



}