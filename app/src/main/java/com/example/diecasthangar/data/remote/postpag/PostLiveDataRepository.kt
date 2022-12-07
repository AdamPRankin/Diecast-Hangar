package com.example.diecasthangar.data.remote.postpag

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class FirestorePostListRepositoryCallback : PostListViewModel.PostListRepository,
    PostListLiveData.OnLastVisiblePostCallback, PostListLiveData.OnLastPostReachedCallback {
    private val firebaseFirestore = FirebaseFirestore.getInstance()
    private val postsRef = firebaseFirestore.collection("posts")
    private var query: Query = postsRef.orderBy("date", Query.Direction.DESCENDING).limit(25)
    private var lastVisiblePost: DocumentSnapshot? = null
    private var isLastPostReached = false
    override val postListLiveData: PostListLiveData?
        get() {
            if (isLastPostReached) {
                return null
            }
            if (lastVisiblePost != null) {
                query = query.startAfter(lastVisiblePost)
            }
            return PostListLiveData(query, this, this)
        }

    override fun setLastVisiblePost(lastVisiblePost: DocumentSnapshot?) {
        this.lastVisiblePost = lastVisiblePost
    }

    override fun setLastPostReached(isLastPostReached: Boolean) {
        this.isLastPostReached = isLastPostReached
    }


}