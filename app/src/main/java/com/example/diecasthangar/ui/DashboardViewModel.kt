package com.example.diecasthangar.ui

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.diecasthangar.core.util.loadingDummyPost
import com.example.diecasthangar.data.model.Comment
import com.example.diecasthangar.data.model.Post
import com.example.diecasthangar.data.remote.Response
import com.example.diecasthangar.data.remote.FirestoreRepository
import com.example.diecasthangar.domain.remote.getUser
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel: ViewModel() {
    private val loadingPost = loadingDummyPost()
    var isLoading = true
    private var postsLiveData: MutableLiveData<ArrayList<Post>> = MutableLiveData(arrayListOf(loadingPost))
    private val repository = FirestoreRepository()
    private var latestSnapshot: DocumentSnapshot? = null
    private var latestCommentSnapshot: DocumentSnapshot? = null
    private var commentsLiveData: MutableLiveData<ArrayList<Comment>> = MutableLiveData(arrayListOf())

    private val localAddedPost: MutableLiveData<Post> = MutableLiveData()
    private val localEditedPost: MutableLiveData<Pair<Post,Post>> = MutableLiveData()


/*    val postsFlow: Flow<List<Post>> = flow {
        while(true) {
            if (isLoading) {
                val userPosts = when (val response = repository.loadNextPagePosts(latestSnapshot)) {
                    is Response.Loading -> {
                        listOf()
                    }
                    is Response.Success -> {
                        val (postsList, snapshot) = response.data!!
                        if (latestSnapshot == snapshot){
                            isLoading = false
                        }
                        latestSnapshot = snapshot
                        postsList
                    }
                    is Response.Failure -> {
                        listOf()
                    }
                }
                emit(userPosts)
            }
            delay(3000)
        }

    }*/

    val lastVisibleItem = MutableStateFlow<Int>(0)
    //val posts: Flow<ArrayList<Post>> = repository.getPostsFlow(lastVisibleItem) as Flow<ArrayList<Post>>

    val fetchPosts = liveData(Dispatchers.IO) {
        emit(Response.Loading)
        try{
            repository.getPostsFlow(lastVisibleItem).collect {
                emit(Response.Success(it))
            }
        }catch (e: Exception){
            emit(Response.Failure(e))
            e.message?.let { Log.e("ERROR:", it) }
        }
    }

    init {
        //initialLoad()
    }

    fun getPostMutableLiveData(): MutableLiveData<ArrayList<Post>> {
        return postsLiveData
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

    fun loadMorePosts(snap: DocumentSnapshot? = latestSnapshot, number: Long = 8){
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
                    print(response.e)
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
                    print(result.e)
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
            repository.addReaction(react,pid)
        }
    }

    fun itemEdited(oldAndNewModel: Pair<Post,Post>){
        localEditedPost.postValue(oldAndNewModel)
    }
}