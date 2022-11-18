package com.example.diecasthangar

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val loadingPost = loadingDummyPost()
    var isLoading = true
    private var postsLiveData: MutableLiveData<ArrayList<Post>> = MutableLiveData(arrayListOf(loadingPost))
    private val repository = FirestoreRepository()
    var latestSnapshot: DocumentSnapshot? = null

    init {
        initialLoad()
    }
    fun getPostMutableLiveData(): MutableLiveData<ArrayList<Post>> {
        return postsLiveData
    }

    private fun initialLoad(){
        viewModelScope.launch {

            when(val response = repository.loadNextPagePosts(latestSnapshot)) {
                is Response.Loading -> {

                }
                is Response.Success -> {
                    postsLiveData.postValue(arrayListOf())
                    val (postsList,newSnap) = response.data!!
                    latestSnapshot = newSnap
                    val newPosts = (postsList)
                    postsLiveData.postValue(newPosts)

                }
                is Response.Failure -> {
                    print(response.e)
                }
            }
        }
    }

    suspend fun loadMorePosts(snap: DocumentSnapshot? = latestSnapshot, number: Long = 8){
        viewModelScope.launch {
        when(val response = repository.loadNextPagePosts(snap,number)) {
            is Response.Loading -> {

            }
            is Response.Success -> {
                val (postsList,newSnap) = response.data!!
                // if document snapshot is the same, then there are no more posts
                // to load, so set loading to false
                if (newSnap == snap){
                    isLoading = false
                }
                latestSnapshot = newSnap
                val newPosts = postsLiveData.value?.plus(postsList) as ArrayList<Post>?
                postsLiveData.postValue(newPosts!!)
            }
            is Response.Failure -> {
                print(response.e)
            }
        }
    }
    }



}