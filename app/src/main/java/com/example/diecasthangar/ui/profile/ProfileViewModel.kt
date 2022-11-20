package com.example.diecasthangar.ui.profile

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.diecasthangar.data.model.Model
import com.example.diecasthangar.data.model.Post
import com.example.diecasthangar.data.model.User
import com.example.diecasthangar.data.remote.Response
import com.example.diecasthangar.data.remote.FirestoreRepository
import com.example.diecasthangar.domain.remote.getUser
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.*
import kotlin.collections.ArrayList


class ProfileViewModel(uid: String) : ViewModel() {
    val postsLiveData: MutableLiveData<ArrayList<Post>> = MutableLiveData()
    private val friendsLiveData: MutableLiveData<ArrayList<User>> = MutableLiveData()
    private val modelsLiveData: MutableLiveData<ArrayList<Model>?> = MutableLiveData()
    private val modelPhotosLiveData: MutableLiveData<ArrayList<Uri>> = MutableLiveData()
    private val postsArrayList: ArrayList<Post> = ArrayList()
    private val repository = FirestoreRepository()
    private var snapshot: DocumentSnapshot? = null
    var postsLoading = true
    private var bio: MutableLiveData<String> = MutableLiveData("Loading...")
    var profileUid = uid
    val avatarUri :MutableLiveData<String> = MutableLiveData("")
    val username :MutableLiveData<String> = MutableLiveData("")
    private val token :MutableLiveData<String> = MutableLiveData("")
    private var addFriendFromTokenResponse :MutableLiveData<String> = MutableLiveData("")

    init {
        loadUserBio()
        loadUserInfo()
        initialLoad()
        loadUserFriends()
        getUserModels()

        if (profileUid == getUser()!!.uid) {
            generateFriendRequestToken()
        }
    }

    fun getPostMutableLiveData(): MutableLiveData<ArrayList<Post>> {
        return postsLiveData
    }

    fun getFriendsMutableLiveData(): MutableLiveData<ArrayList<User>> {
        return friendsLiveData
    }

    fun getModelsMutableLiveData(): MutableLiveData<ArrayList<Model>?> {
        return modelsLiveData
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
    fun getFriendRequestToken(): String {
        return token.value!!
    }
    fun getAddFriendFromTokenResponseMutableLiveData(): MutableLiveData<String> {
        return addFriendFromTokenResponse
    }

    private fun initialLoad(){
        viewModelScope.launch {
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
        viewModelScope.launch {
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
        viewModelScope.launch {
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

    private fun loadUserFriends() {
        viewModelScope.launch {
            when (val response = repository.getUserFriends(profileUid)) {
                is Response.Loading -> {
                }
                is Response.Success -> {
                    val friendsList = response.data ?: ArrayList()
                    friendsLiveData.postValue(friendsList)
                }
                is Response.Failure -> {
                    print(response.e)
                }
            }
        }
    }

    fun loadMorePosts() {
        viewModelScope.launch {
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

    fun updateBio(text: String) {
        if (profileUid == getUser()!!.uid) {
            viewModelScope.launch {
                when (val response = repository.updateUserBio(profileUid, text)) {
                    is Response.Loading -> {
                    }
                    is Response.Success -> {

                    }
                    is Response.Failure -> {
                        print(response.e)
                    }
                }
            }
        }
    }

    fun updateAvatar(uri: Uri = Uri.parse(avatarUri.value), userId: String = profileUid) {
        if (profileUid == getUser()!!.uid) {
            viewModelScope.launch {
                when (val response = repository.updateUserAvatar(uri,profileUid)) {
                    is Response.Loading -> {
                    }
                    is Response.Success -> {

                    }
                    is Response.Failure -> {
                        print(response.e)
                    }
                }
            }
        }

    }

    fun deletePost(pid: String){
        viewModelScope.launch {
            repository.deletePostFromFirestore(pid)
        }
    }


    fun updatePhotos(photos: ArrayList<Uri>){
        modelPhotosLiveData.postValue(photos)
    }

    fun addFriend(friend: User){
        viewModelScope.launch {
            when (val response = repository.addFriend(profileUid,friend.id)) {
                is Response.Loading -> {
                }
                is Response.Success -> {
                    repository.deleteFriendRequest(friend.requestToken!!)
                }
                is Response.Failure -> {
                    print(response.e)
                }
            }
        }
    }

    fun sendFriendRequest(recipientId: String){
        viewModelScope.launch {
            when (val response = repository.addFriendRequest(getUser()!!.uid,recipientId)) {
                is Response.Loading -> {
                }
                is Response.Success -> {

                }
                is Response.Failure -> {
                    print(response.e)
                }
            }
        }
    }

    private fun generateFriendRequestToken() {
        CoroutineScope(Dispatchers.IO).launch {
            when (val response = repository.addFriendRequestToken(getUser()!!.uid)) {
                is Response.Loading -> {
                }
                is Response.Success -> {
                    token.postValue(response.data ?: "could not generate token, try again later")
                }
                is Response.Failure -> {
                    print(response.e)
                }
            }
        }

    }
    fun addFriendFromToken(token: String){
        viewModelScope.launch {
            when (val response = repository.addFriendFromToken(token, getUser()!!.uid)) {
                is Response.Loading -> {
                }
                is Response.Success -> {
                    addFriendFromTokenResponse.postValue("friend added")
                }
                is Response.Failure -> {
                    print(response.e)
                    addFriendFromTokenResponse.postValue("invalid token")
                }
            }
        }
    }

    fun uploadModel(model: Model) {
        viewModelScope.launch {
            when (val response = repository.addModel(model)) {
                is Response.Loading -> {
                }
                is Response.Success -> {

                }
                is Response.Failure -> {
                    print(response.e)
                }
            }
        }
    }

    private fun getUserModels(){
        viewModelScope.launch {
            when (val response = repository.getUserModels(profileUid)) {
                is Response.Loading -> {
                }
                is Response.Success -> {
                    val models = response.data
                    modelsLiveData.postValue(models)
                    val e = "e"
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