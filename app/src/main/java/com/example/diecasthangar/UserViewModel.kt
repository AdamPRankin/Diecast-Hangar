package com.example.diecasthangar

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.example.diecasthangar.data.Post
import com.example.diecasthangar.domain.Response
import com.example.diecasthangar.domain.remote.FirestoreRepository
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserViewModel: ViewModel() {

    private var username: String = ""
    private var avatarUri: String = ""

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

    fun getAvatarUri(): String {
        return avatarUri
    }

    fun getUserData(){
        user = Firebase.auth.currentUser
        CoroutineScope(Dispatchers.IO).launch {

            when(val response = repository.getUserInfo(user!!.uid)) {
                is Response.Loading -> {
                }
                is Response.Success -> {
                    val (avatar,user) = response.data!!
                    avatarUri = avatar
                    username = user
                    dataLoaded.postValue(true)
                }
                is Response.Failure -> {
                    print(response.e)
                }
            }
        }
    }
}