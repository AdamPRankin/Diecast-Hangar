package com.example.diecasthangar

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.diecasthangar.data.Post
import com.example.diecasthangar.domain.Response
import com.example.diecasthangar.domain.remote.FirestoreRepository
import com.example.diecasthangar.domain.usecase.remote.getUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

public class DashboardViewModel: ViewModel() {

    var isLoading = false
    var posts: MutableLiveData<List<Post>> = MutableLiveData()
    val storage: FirebaseStorage = FirebaseStorage.getInstance()
    val db: FirebaseFirestore = Firebase.firestore
    val repository = FirestoreRepository(storage,db)
    var userAvatarUri: String = ""

    init {
        CoroutineScope(Dispatchers.IO).launch {
            when(val response = repository.getUserAvatar(getUser()!!.uid)) {
                is Response.Loading -> {
                }
                is Response.Success -> {
                    userAvatarUri = response.data!!
                }
                is Response.Failure -> {
                    print(response.e)
                }
            }
        }
        if (posts.value == null) {
            loadPosts()
        }
    }


    fun loadPosts() {
        CoroutineScope(Dispatchers.IO).launch {
            when (val response = repository.getPostsFromFireStore()) {
                is Response.Loading -> {

                }
                is Response.Success -> {
                    val postsList = response.data!!
                    posts.value = postsList
                }
                is Response.Failure -> {
                    print(response.e)
                }
            }
        }
    }
}