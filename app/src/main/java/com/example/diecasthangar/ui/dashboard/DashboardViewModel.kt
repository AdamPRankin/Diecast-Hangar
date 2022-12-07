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

    private val _allPosts = MutableStateFlow(listOf<Post>())
    val allPosts: StateFlow<List<Post>> = _allPosts

    private val _topPosts = MutableStateFlow(listOf<Post>())
    val topPosts: StateFlow<List<Post>> = _topPosts

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

    }




    fun getPostListLiveData() {
        //return repository.getPostsListLiveData()
    }

    @OptIn(FlowPreview::class)
    val fetchAllFriendPosts = liveData(Dispatchers.IO) {

        try{
            when(val response = repository.getUserFriends(getUser()!!.uid)) {
                is Response.Loading -> {
                }
                is Response.Success -> {
                    val friends = response.data!!
/*                    repository.friendPostsFlow(friends[2].id).collect {
                        emit(it)
                    }*/
                    val flowsList = arrayListOf<Flow<List<Post>>>()
                    for (friend in friends) {
                        val friendFlow = repository.friendPostsFlow(friend.id)
                        flowsList.add(friendFlow)
                    }
                    val merged = flowsList.merge()
                    merged.collect {
                        emit(it)
                    }
                }
                is Response.Failure -> {
                    print(response.e)
                }
            }
        }catch (e: Exception){
            emit(Response.Failure(e))
            e.message?.let { Log.e("ERROR:", it) }
        }
    }


/*    val fetchFriendPosts = liveData(Dispatchers.IO) {
        emit(Response.Loading)
        try{
            repository.allFriendsPostsFlow(getUser()!!.uid).collect {
                emit(Response.Success(it))
            }

        }catch (e: Exception){
            emit(Response.Failure(e))
            e.message?.let { Log.e("ERROR:", it) }
        }
    }*/

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