package com.example.diecasthangar.ui

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.diecasthangar.data.remote.Response
import com.example.diecasthangar.data.remote.FirestoreRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserViewModel: ViewModel() {

    private var username: String = ""
    private val avatarUri: MutableLiveData<String> = MutableLiveData("")


    private val repository = FirestoreRepository()
    private var user = Firebase.auth.currentUser
    private var dataLoaded: MutableLiveData<Boolean> = MutableLiveData(false)

    init {
        getUserData()
    }

    fun isDataLoaded(): MutableLiveData<Boolean> {
        return dataLoaded
    }

    fun getUsername(): String {
        return username
    }

    fun getAvatarUri(): MutableLiveData<String> {
        return avatarUri
    }

    fun setAvatarUri(uri: Uri) {
        avatarUri.value = uri.toString()
    }

    fun setUsername(username: String){
        this.username = username
    }

    fun getUserData() {
        user = Firebase.auth.currentUser
        CoroutineScope(Dispatchers.IO).launch {

            when(val response = user?.let { repository.getUserInfo(it.uid) }) {
                is Response.Loading -> {
                }
                is Response.Success -> {
                    val (avatar,user) = response.data!!
                    avatarUri.postValue(avatar)
                    username = user
                    dataLoaded.postValue(true)
                }
                is Response.Failure -> {
                    print(response.e)
                }
                else -> {}
            }
        }
    }
}