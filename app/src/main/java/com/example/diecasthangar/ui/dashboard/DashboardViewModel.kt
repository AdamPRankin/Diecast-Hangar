package com.example.diecasthangar.ui.dashboard

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.diecasthangar.data.model.Comment
import com.example.diecasthangar.data.model.Post
import com.example.diecasthangar.data.remote.FirestoreRepository
import com.example.diecasthangar.data.remote.Response
import com.example.diecasthangar.data.remote.getUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel: ViewModel() {
    private val repository = FirestoreRepository()
    private var commentsLiveData: MutableLiveData<ArrayList<Comment>> = MutableLiveData(arrayListOf())

    private val localAddedPost: MutableLiveData<Post> = MutableLiveData()
    private val localEditedPost: MutableLiveData<Pair<Post,Post>> = MutableLiveData()

    val lastVisibleItem = MutableStateFlow(1)

    val fetchPosts = liveData(Dispatchers.IO) {
        emit(Response.Loading)
        try{
            val pagingFlow = repository.getPosts(lastVisibleItem).collect {
                emit(Response.Success(it))
            }
            val newFlow = repository.newPostFlow(lastVisibleItem)
            //merge(pagingFlow, newFlow)
        }catch (e: Exception){
            emit(Response.Failure(e))
            e.message?.let { Log.e("ERROR:", it) }
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

    fun deleteComment(cid: String){
        repository.deleteComment(cid)
    }

    fun editComment(cid: String, text: String){
        repository.editComment(cid,text)
    }
}