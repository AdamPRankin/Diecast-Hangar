package com.example.diecasthangar.ui.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.example.diecasthangar.data.model.Model
import com.example.diecasthangar.data.model.Photo
import com.example.diecasthangar.data.model.Post
import com.example.diecasthangar.data.model.User
import com.example.diecasthangar.data.remote.FirestoreRepository
import com.example.diecasthangar.data.remote.Response
import com.example.diecasthangar.domain.remote.getUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.SetOptions.merge
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.merge


class ProfileViewModel(uid: String) : ViewModel() {
    //val postsLiveData: MutableLiveData<ArrayList<Post>> = MutableLiveData()
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

    private val currentModelPhotosMutableLiveData: MutableLiveData<ArrayList<Photo>> = MutableLiveData()
    private val currentModelDeletedPhotosMutableLiveData: MutableLiveData<ArrayList<Photo>> = MutableLiveData()

    val fetchPosts = liveData(Dispatchers.IO) {
        emit(Response.Loading)
        try{
            repository.userPostsFlow(profileUid).collect {
                emit(it)
            }
        }catch (e: Exception){
            emit(Response.Failure(e))
            e.message?.let { Log.e("ERROR:", it) }
        }
    }

    val fetchModels = liveData(Dispatchers.IO) {
        emit(Response.Loading)
        try{
            repository.userModelsFlow(profileUid).collect {
                emit(it)
            }
        }catch (e: Exception){
            emit(Response.Failure(e))
            e.message?.let { Log.e("ERROR:", it) }
        }
    }

    val fetchFriendsAndRequests = liveData(Dispatchers.IO) {
        emit(Response.Loading)
        try{
            val friendFlow = repository.userFriendsFlow(profileUid)
            val requestFlow = repository.userFriendsFlow(profileUid)
            merge(friendFlow, requestFlow).collect{ emit(it) }
        } catch (e: Exception){
            emit(Response.Failure(e))
            e.message?.let { Log.e("ERROR:", it) }
        }
    }

    val fetchFriends = liveData(Dispatchers.IO) {
        emit(Response.Loading)
        try{
            repository.userFriendsFlow(profileUid).collect {
                emit(it)
            }
        }catch (e: Exception){
            emit(Response.Failure(e))
            e.message?.let { Log.e("ERROR:", it) }
        }
    }

    init {
        loadUserBio()
        loadUserInfo()

        if (profileUid == getUser()!!.uid) {
            generateFriendRequestToken()
        }
    }

    fun getFriendsMutableLiveData(): MutableLiveData<ArrayList<User>> {
        return friendsLiveData
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

    fun getSelectedModelMutableLiveData(): MutableLiveData<ArrayList<Photo>> {
        return currentModelPhotosMutableLiveData
    }

    fun getCurrentModelDeletedPhotosMutableLiveData(): MutableLiveData<ArrayList<Photo>> {
        return currentModelDeletedPhotosMutableLiveData
    }

    fun addCurrentModelDeletedPhoto(photo: Photo){
        val newDeletedModels = currentModelDeletedPhotosMutableLiveData.value?.let { ArrayList(it) } ?: arrayListOf()
        newDeletedModels.add(photo)
        currentModelDeletedPhotosMutableLiveData.value
    }

    fun addCurrentModelPhotos(photos: ArrayList<Photo>){
        val newPhotos = currentModelPhotosMutableLiveData.value?.let { ArrayList(it) } ?: arrayListOf()
        newPhotos.addAll(photos)
        currentModelPhotosMutableLiveData.value = newPhotos

    }
    fun clearCurrentModelPhotos(){
        currentModelPhotosMutableLiveData.value = arrayListOf()
        currentModelDeletedPhotosMutableLiveData.value = arrayListOf()
    }

    fun getCurrentNonDeletedPhotos(): ArrayList<Photo> {
        val photos =  currentModelPhotosMutableLiveData.value?.let { ArrayList(it) } ?: arrayListOf()
        currentModelDeletedPhotosMutableLiveData.value?.let { photos.removeAll(it.toSet()) }
        return photos
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
                    //get model ID and add to model
                    val newModel = response.data
                    //localAddedModel.postValue(newModel!!)
                }
                is Response.Failure -> {
                    print(response.e)
                }
            }
        }
    }

    fun deleteModel(mid: String) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.deleteModel(mid)
        }
    }
    fun addReact(react: String, pid: String){
        viewModelScope.launch {
            repository.addReaction(react,pid)
        }
    }

    fun updateModel(model: Model) {
        val photos = model.photos
        CoroutineScope(Dispatchers.IO).launch {
            photos.map { photo ->
                async(Dispatchers.IO) {
                    //add photo to database iff it is not already there
                    if (photo.remoteUri == "") {
                        when (val result = repository.addImageToStorage(photo.localUri!!)) {
                            is Response.Loading -> {
                            }
                            is Response.Success -> {
                                val remoteUri = result.data!!
                                photo.remoteUri = remoteUri.toString()
                            }
                            is Response.Failure -> {
                                print(result.e)
                            }
                        }
                    }
                }
                // waiting for all request to finish executing in parallel
            }.awaitAll()
            model.photos = photos
            repository.editFirestoreModel(model)
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