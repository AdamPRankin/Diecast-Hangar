package com.example.diecasthangar.ui.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.example.diecasthangar.data.model.Model
import com.example.diecasthangar.data.model.Photo
import com.example.diecasthangar.data.model.User
import com.example.diecasthangar.data.remote.FirestoreRepository
import com.example.diecasthangar.data.remote.Response
import com.example.diecasthangar.data.remote.getUser
import kotlinx.coroutines.*


class ProfileViewModel(uid: String) : ViewModel() {
    private val friendsLiveData: MutableLiveData<ArrayList<User>> = MutableLiveData()
    private val modelPhotosLiveData: MutableLiveData<ArrayList<Uri>> = MutableLiveData()
    private val repository = FirestoreRepository()
    private var bio: MutableLiveData<String> = MutableLiveData("Loading...")
    private var profileUid = uid
    val avatarUri :MutableLiveData<String> = MutableLiveData("")
    val username :MutableLiveData<String> = MutableLiveData("")
    private val token :MutableLiveData<String> = MutableLiveData("")
    private var addFriendFromTokenResponse :MutableLiveData<String> = MutableLiveData("")
    var currentModelViewing: Model? = null
    var currentModelEditing: Model? = null

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
            e.message?.let { Log.e("PROFILE", it) }
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
            e.message?.let { Log.e("PROFILE", it) }
        }
    }

/*    val fetchFriendsAndRequests = liveData(Dispatchers.IO) {
        emit(Response.Loading)
        try{
            val friendFlow = repository.userFriendsFlow(profileUid)
            val requestFlow = repository.userFriendRequestsFlow(profileUid)
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
    }*/

    init {
        loadUserBio()
        loadUserInfo()
        loadUserFriends()

        if (profileUid == getUser()!!.uid) {
            generateFriendRequestToken()
        }
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

    fun removeCurrentDeletedModelPhotos(){
        val photos = currentModelDeletedPhotosMutableLiveData.value
        if (photos != null) {
            for (photo in photos) {
                repository.deleteImage(photo.remoteUri,"models")
            }
        }
    }

    fun getFriendsMutableLiveData(): MutableLiveData<ArrayList<User>> {
        return friendsLiveData
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
                    Log.e("PROFILE", "failed to load bio: ${response.e}")
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
                    Log.e("PROFILE", "failed to load info: ${response.e}")
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
                    Log.e("PROFILE", "failed to load friends: ${response.e}")
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
                        Log.e("PROFILE", "failed to update bio: ${response.e}")
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
                        Log.e("PROFILE", "failed to update avatar: ${response.e}")
                    }
                }
            }
        }
    }

    fun deletePost(pid: String){
        viewModelScope.launch {
            when (val response = repository.deletePostFromFirestore(pid)) {
                is Response.Loading -> {
                }
                is Response.Success -> {

                }
                is Response.Failure -> {
                    Log.e("PROFILE", "failed to delete post: ${response.e}")
                }
            }
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
                    Log.e("PROFILE", "failed to add friend: ${response.e}")
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
                    Log.e("PROFILE", "failed to send request: ${response.e}")
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
                    Log.e("PROFILE", "failed to push token: ${response.e}")
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
                    Log.e("PROFILE", "failed to add friend: ${response.e}")
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
                    Log.e("PROFILE", "failed to upload model: ${response.e}")
                }
            }
        }
    }

    fun deleteModel(mid: String) {
        CoroutineScope(Dispatchers.IO).launch {
            when (val response =repository.deleteModel(mid)) {
                is Response.Loading -> {
                }
                is Response.Success -> {
                }
                is Response.Failure -> {
                    Log.e("PROFILE", "failed to delete model: ${response.e}")
                }
            }
        }
    }
    fun addReact(react: String, pid: String){
        viewModelScope.launch {
            when (val response = repository.addReaction(react,pid)) {
                is Response.Loading -> {
                }
                is Response.Success -> {
                }
                is Response.Failure -> {
                    Log.e("PROFILE", "failed to add react: ${response.e}")
                }
            }
        }
    }

    fun updateModel(model: Model) {
        val photos = model.photos
        CoroutineScope(Dispatchers.IO).launch {
            photos.map { photo ->
                async(Dispatchers.IO) {
                    //add photo to database iff it is not already there
                    if (photo.remoteUri == "") {
                        when (val response = repository.addImageToStorage(photo.localUri!!)) {
                            is Response.Loading -> {
                            }
                            is Response.Success -> {
                                val remoteUri = response.data!!
                                photo.remoteUri = remoteUri.toString()
                            }
                            is Response.Failure -> {
                                Log.e("PROFILE", "failed to update model: ${response.e}")
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