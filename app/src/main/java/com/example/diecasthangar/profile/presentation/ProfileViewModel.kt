package com.example.diecasthangar.profile.presentation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.diecasthangar.data.Post
import com.example.diecasthangar.domain.Response
import com.example.diecasthangar.domain.remote.FirestoreRepository
import com.example.diecasthangar.domain.usecase.remote.getUser
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList


class ProfileViewModel(uid: String) : ViewModel() {
    private val postsLiveData: MutableLiveData<ArrayList<Post>> = MutableLiveData()
    private val postsArrayList: ArrayList<Post> = ArrayList()
    private val repository = FirestoreRepository()
    private var snapshot: DocumentSnapshot? = null
    var postsLoading = true
    private var bio: MutableLiveData<String> = MutableLiveData("Loading...")
    var profileUid = uid
    val avatarUri :MutableLiveData<String> = MutableLiveData("")
    val username :MutableLiveData<String> = MutableLiveData("")

    init {
        loadUserBio()
        loadUserInfo()
        initialLoad()
    }

    fun getPostMutableLiveData(): MutableLiveData<ArrayList<Post>> {
        return postsLiveData
    }

    fun getUserBioMutableLiveData(): MutableLiveData<String> {
        return bio
    }

    fun getUsernameMutableLiveData(): MutableLiveData<String> {
        return username
    }

    fun getAvatarMutableLiveData(): MutableLiveData<String> {
        return avatarUri
    }

    private fun initialLoad(){
        CoroutineScope(Dispatchers.IO).launch {
            when(val response = repository.loadNextPagePostsFromUser(userId = profileUid)) {
                is Response.Loading -> {
                }
                is Response.Success -> {
                    val (postsList,newSnap) = response.data!!
                    snapshot = newSnap

                    //disable on scroll loading if we have loaded the last post
                    if (postsList.size < 10){
                        postsLoading = false
                    }
                    postsLiveData.postValue(postsList)
                }
                is Response.Failure -> {
                    print(response.e)
                }
            }
        }
    }

    private fun loadUserBio(){
        CoroutineScope(Dispatchers.IO).launch {
            when (val response = repository.getUserBio(profileUid)) {
                is Response.Loading -> {
                }
                is Response.Success -> {
                    bio.postValue(response.data!!)
                }
                is Response.Failure -> {
                    print(response.e)
                }
            }
        }
    }

    private fun loadUserInfo(){
        CoroutineScope(Dispatchers.IO).launch {
            when (val response = repository.getUserInfo(profileUid)) {
                is Response.Loading -> {
                }
                is Response.Success -> {
                    val (avatar, name) = (response.data!!)
                    avatarUri.postValue(avatar)
                    username.postValue(name)

                }
                is Response.Failure -> {
                    print(response.e)
                }
            }
        }
    }
    fun loadMorePosts() {
        CoroutineScope(Dispatchers.IO).launch {
            when(val response = repository.loadNextPagePostsFromUser(
                lastVisible = snapshot,userId = profileUid)) {
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
                        postsLoading = false
                    }
                }
                is Response.Failure -> {
                    print(response.e)
                }
            }
        }
    }

    //use this to create a viewModel with uid parameter
    class Factory(private val uid: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileViewModel(uid) as T
        }
    }

}