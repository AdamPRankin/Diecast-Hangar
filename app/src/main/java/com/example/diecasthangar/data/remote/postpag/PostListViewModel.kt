package com.example.diecasthangar.data.remote.postpag

import androidx.lifecycle.ViewModel


class PostListViewModel : ViewModel() {
    val postListRepository = FirestorePostListRepositoryCallback()

    fun getPostListLiveData(): PostListLiveData? {
        return postListRepository.postListLiveData
    }

    internal interface PostListRepository {
        val postListLiveData: PostListLiveData?
    }
}