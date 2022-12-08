package com.example.diecasthangar.ui.dashboard

import android.content.ClipData.Item
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.diecasthangar.core.util.loadingDummyPost
import com.example.diecasthangar.data.model.Comment
import com.example.diecasthangar.data.model.Post
import com.example.diecasthangar.data.remote.FirestoreRepository
import com.example.diecasthangar.data.remote.Response
import com.example.diecasthangar.data.remote.getUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch


class DashboardViewModel: ViewModel() {
    private val repository = FirestoreRepository()
    private var commentsLiveData: MutableLiveData<ArrayList<Comment>> = MutableLiveData(arrayListOf())

    private val localAddedPost: MutableLiveData<Post> = MutableLiveData()
    private val localEditedPost: MutableLiveData<Pair<Post,Post>> = MutableLiveData()

    val lastVisibleItem = MutableStateFlow(1)
    //todo paginate
    val lastVisibleItemTop = MutableStateFlow(1)
    val lastVisibleItemNews = MutableStateFlow(1)

    private val _allPosts = MutableStateFlow(listOf<Post>(loadingDummyPost()))
    val allPosts: StateFlow<List<Post>> = _allPosts

    private val _topPosts = MutableStateFlow(listOf<Post>(loadingDummyPost()))
    val topPosts: StateFlow<List<Post>> = _topPosts

    private val _newsPosts = MutableStateFlow(listOf<Post>(loadingDummyPost()))
    val newsPosts: StateFlow<List<Post>> = _newsPosts

    val selectedPost = MutableLiveData<Post>()

    var currentViewingPost: Post

    init {
        currentViewingPost = loadingDummyPost()
        //get all posts
        viewModelScope.launch {
            repository.getPosts(lastVisibleItem).collect { posts ->
                   _allPosts.value = posts
                }
        }
        //get top posts
        viewModelScope.launch {
            repository.getTopPosts(lastVisibleItemTop).collect { posts ->
                _topPosts.value = posts
            }
        }
        //get news posts
        viewModelScope.launch {
            repository.getNewsPosts(lastVisibleItemNews).collect { posts ->
                val cc = posts
                _newsPosts.value = posts
            }
        }
    }

     fun getCurrentCommentMutableLiveData(): MutableLiveData<ArrayList<Comment>> {
         return commentsLiveData
     }

    fun getLocalAddedPost(): MutableLiveData<Post>{
        return localAddedPost
    }

    fun getLocalEditedPost(): MutableLiveData<Pair<Post,Post>> {
        return localEditedPost
    }

    fun deletePost(pid: String){
        viewModelScope.launch {
            repository.deletePostFromFirestore(pid)
        }
    }

    fun loadComments(pid: String) {
        commentsLiveData.postValue(arrayListOf())
        viewModelScope.launch {
            //todo load all, load by rating etc
            when(val response = repository.getFireStoreCommentsPage(pid,null,50)) {
                is Response.Loading -> {
                }
                is Response.Success -> {
                    val (commentList,newSnap) = response.data!!
                    val newComments = (commentList)
                    commentsLiveData.postValue(newComments)
                }
                is Response.Failure -> {
                    Log.e("FIREBASE","Error loading comments: ${response.e}")
                }
            }
        }
    }

    fun addPost(post: Post) {
        CoroutineScope(Dispatchers.IO).launch {
            when (val result = repository.addPost(post)) {
                is Response.Loading -> {
                }
                is Response.Success -> {
                    val addedPost = post.copy(id = result.data!!)
                    localAddedPost.postValue(addedPost)
                }
                is Response.Failure -> {
                    Log.e("FIREBASE","Error loading comments: ${result.e}")
                }
            }
        }
    }

    fun addComment(pid: String,commentText: String){
        val user = getUser()
        val uid = user!!.uid
        CoroutineScope(Dispatchers.IO).launch{
            repository.addFirestoreComment(pid,commentText, uid)
        }
    }

    fun addReact(react: String, pid: String){
        viewModelScope.launch {
            when (val result = repository.addReaction(react,pid)) {
                is Response.Loading -> {
                }
                is Response.Success -> {
                }
                is Response.Failure -> {
                    Log.e("FIREBASE","Error adding react: ${result.e}")
                }
            }
        }
    }

    fun itemEdited(oldAndNewModel: Pair<Post,Post>){
        localEditedPost.postValue(oldAndNewModel)
    }

    fun deleteComment(cid: String){
        repository.deleteComment(cid)
        when (val result = repository.deleteComment(cid)) {
            is Response.Loading -> {
            }
            is Response.Success -> {
            }
            is Response.Failure -> {
                Log.e("FIREBASE","Error deleting comment: ${result.e}")
            }
        }
    }

    fun editComment(cid: String, text: String){
        repository.editComment(cid,text)
        when (val result = repository.editComment(cid,text)) {
            is Response.Loading -> {
            }
            is Response.Success -> {
            }
            is Response.Failure -> {
                Log.e("FIREBASE","Error editing comment: ${result.e}")
            }
        }
    }
}