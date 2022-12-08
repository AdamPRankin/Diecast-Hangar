package com.example.diecasthangar.data.remote.postpag


import androidx.lifecycle.LiveData
import com.example.diecasthangar.core.util.docToPostClass
import com.example.diecasthangar.data.model.Post
import com.google.firebase.firestore.*



class PostListLiveData internal constructor(
    private val query: Query,
    val onLastVisiblePostCallback: OnLastVisiblePostCallback,
    val onLastPostReachedCallback: OnLastPostReachedCallback
) :
    LiveData<Operation?>(), EventListener<QuerySnapshot?> {
    private var listenerRegistration: ListenerRegistration? = null
    override fun onActive() {
        listenerRegistration = query.addSnapshotListener(this)
    }

    override fun onInactive() {
        listenerRegistration!!.remove()
    }

    override fun onEvent(querySnapshot: QuerySnapshot?, e: FirebaseFirestoreException?) {
        if (e != null) return
        for (documentChange in querySnapshot!!.documentChanges) {
            value = when (documentChange.type) {
                DocumentChange.Type.ADDED -> {
                    val addedPost: Post =
                        docToPostClass(documentChange.document)
                    val addOperation = Operation(addedPost, "added")
                    addOperation
                }
                DocumentChange.Type.MODIFIED -> {
                    val modifiedPost: Post =
                        docToPostClass(documentChange.document)
                    val modifyOperation = Operation(modifiedPost, "modified")
                    modifyOperation
                }
                DocumentChange.Type.REMOVED -> {
                    val removedPost: Post =
                        docToPostClass(documentChange.document)
                    val removeOperation = Operation(removedPost, "removed")
                    removeOperation
                }
            }
        }
        val querySnapshotSize = querySnapshot.size()
        if (querySnapshotSize < 25) {
            onLastPostReachedCallback.setLastPostReached(true)
        } else {
            val lastVisiblePost = querySnapshot.documents[querySnapshotSize - 1]
            onLastVisiblePostCallback.setLastVisiblePost(lastVisiblePost)
        }
    }

    interface OnLastVisiblePostCallback {
        fun setLastVisiblePost(lastVisiblePost: DocumentSnapshot?)
    }

    interface OnLastPostReachedCallback {
        fun setLastPostReached(isLastPostReached: Boolean)
    }
}