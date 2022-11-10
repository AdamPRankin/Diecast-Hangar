package com.example.diecasthangar.profile.presentation

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bumptech.glide.Glide.init
import com.example.diecasthangar.MainActivity
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
    private val loadingPost: Post = Post("Squeek is loading the posts as fast as he can...",
        java.util.ArrayList(),"Mr. Loading",
        Date() ,"Flyin' Squeek The Post Loader",
        "https://i.gyazo.com/02b2c623a812f221477160f3041f486a.png"
        ,"123",
        java.util.ArrayList(),hashMapOf())
    private val postsLiveData: MutableLiveData<ArrayList<Post>> = MutableLiveData(arrayListOf(loadingPost))
    private val postsArrayList: ArrayList<Post> = ArrayList()
    private val repository = FirestoreRepository()
    private var snapshot: DocumentSnapshot? = null
    var isLoading = true

    init {
        initialLoad()
    }
    val postValue : LiveData<ArrayList<Post>> get() = postsLiveData

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
                    postsLiveData!!.postValue(postsList)
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