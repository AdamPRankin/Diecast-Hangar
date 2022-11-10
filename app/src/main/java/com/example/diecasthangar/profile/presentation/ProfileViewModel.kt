package com.example.diecasthangar.profile.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.diecasthangar.core.util.loadingDummyPost
import com.example.diecasthangar.data.Post
import com.example.diecasthangar.domain.Response
import com.example.diecasthangar.domain.remote.FirestoreRepository
import com.example.diecasthangar.domain.usecase.remote.getUser
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList


class ProfileViewModel: ViewModel() {
    private val loadingPost: Post = loadingDummyPost()
    private val postsLiveData: MutableLiveData<ArrayList<Post>> = MutableLiveData(arrayListOf(loadingPost))
    private val postsArrayList: ArrayList<Post> = ArrayList()
    private val repository = FirestoreRepository()
    private var snapshot: DocumentSnapshot? = null
    var isLoading = true

    init {
        initialLoad()
    }

    fun getPostMutableLiveData(): MutableLiveData<ArrayList<Post>> {
        return postsLiveData
    }

    private fun initialLoad(){
        CoroutineScope(Dispatchers.IO).launch {
            when(val response = repository.loadNextPagePostsFromUser(userId = getUser()!!.uid)) {
                is Response.Loading -> {
                }
                is Response.Success -> {
                    val (postsList,newSnap) = response.data!!
                    snapshot = newSnap

                    postsLiveData.postValue(postsList)
                }
                is Response.Failure -> {
                    print(response.e)
                }
            }
        }

    }
    fun loadMorePosts() {
        CoroutineScope(Dispatchers.IO).launch {
            when(val response = repository.loadNextPagePostsFromUser(lastVisible = snapshot,userId = getUser()!!.uid)) {
                is Response.Loading -> {
                }
                is Response.Success -> {
                    val (postsList,newSnap) = response.data!!
                    snapshot = newSnap
                    val newList = ArrayList<Post>()
                    newList.addAll(postsLiveData.value!!)
                    newList.addAll(postsList)
                    postsLiveData.postValue(newList)
                    //if these are equal then there are no more posts to load
                    if (newSnap == snapshot){
                        isLoading = false
                    }
                }
                is Response.Failure -> {
                    print(response.e)
                }
            }
        }
    }
}