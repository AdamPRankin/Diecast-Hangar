package com.example.diecasthangar.ui.viewpost

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.diecasthangar.data.model.Photo
import com.example.diecasthangar.data.model.Post
import com.example.diecasthangar.data.remote.Response
import com.example.diecasthangar.data.remote.FirestoreRepository
import kotlinx.coroutines.*

class AddPostViewModel(post: Post?, editing: Boolean = false): ViewModel() {

    private val postBody: MutableLiveData<String> = MutableLiveData("")
    private val photoMutableLiveData: MutableLiveData<ArrayList<Photo>> = MutableLiveData()
    private val repository: FirestoreRepository = FirestoreRepository()
    private val pid = post?.id

    init {
        if (post == null && editing){
            class CustomException (message: String) : Exception(message)
            throw CustomException ("do not use editing flag with null post")
        }
        else if (editing) {
            postBody.value = post!!.text
            val postPhotos = post.images
            photoMutableLiveData.value = postPhotos
        }
    }

    fun getPostBodyMutableLiveData(): MutableLiveData<String> {
        return postBody
    }
    fun getPhotoMutableLiveData(): MutableLiveData<ArrayList<Photo>> {
        return photoMutableLiveData
    }
    fun addPhotos(photos: ArrayList<Photo>) {
        val newPhotoList = photoMutableLiveData.value?.let { ArrayList(it) } ?: arrayListOf()
        newPhotoList.addAll(photos)
        photoMutableLiveData.value = newPhotoList
    }

    fun updatePostPhotos() {
        val photos = getPhotoMutableLiveData().value
        if (photos != null) {
            CoroutineScope(Dispatchers.IO).launch {
                photos.map { photo ->
                    async(Dispatchers.IO) {
                        //add photo to database iff it is not already there
                        if (photo.remoteUri == "") {
                            when (val result = repository.addImageToStorage(photo.localUri!!)) {
                                is Response.Loading -> {
                                }
                                is Response.Success -> {
                                    val remoteUri = result.data!!
                                    photo.remoteUri = remoteUri.toString()
                                }
                                is Response.Failure -> {
                                    Log.e("FIREBASE","Error uploading photo: ${result.e}")
                                }
                            }
                        }
                    }
                    // waiting for all request to finish executing in parallel
                }.awaitAll()
                val remoteUris = ArrayList<Uri>()

                for (photo in photos) {
                    remoteUris.add(Uri.parse(photo.remoteUri))
                }
                when (val result = repository.editFirestorePostPhotos(pid!!, remoteUris)) {
                    is Response.Loading -> {
                    }
                    is Response.Success -> {

                    }
                    is Response.Failure -> {
                        print(result.e)
                        Log.e("FIREBASE","Error updating post: ${result.e}")
                    }
                }
            }
        }
    }

    fun updatePostBodyText(updatedText: String = getPostBodyMutableLiveData().value ?: ""){
        CoroutineScope(Dispatchers.IO).launch {
            when (val result = repository.editFirestorePostText(pid!!, updatedText)) {
                is Response.Loading -> {
                }
                is Response.Success -> {
                }
                is Response.Failure -> {
                    Log.e("FIREBASE","Error editing post: ${result.e}")
                }
            }
        }
    }

    //use this to create a viewModel with uid parameter
    class Factory(private val post: Post? = null, private val editing: Boolean) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddPostViewModel(post,editing) as T
        }
    }

}


