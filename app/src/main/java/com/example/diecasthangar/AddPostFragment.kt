package com.example.diecasthangar

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diecasthangar.data.Photo
import com.example.diecasthangar.data.Post
import com.example.diecasthangar.domain.Response
import com.example.diecasthangar.domain.adapters.SideScrollImageRecyclerAdapter
import com.example.diecasthangar.domain.remote.FirestoreRepository
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*


class AddPostFragment(post: Post? = null, editMode: Boolean = false) : Fragment() {
    private val currentPost = post
    private val editing = editMode


    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add_post, container, false)
        if (currentPost == null && editing){
            class CustomException (message: String) : Exception(message)
            throw CustomException ("do not use editing flag with null post")
        }
        val viewModel: AddPostViewModel by viewModels { AddPostViewModel.Factory(currentPost,editing) }
        val userViewModel: UserViewModel by activityViewModels()
        //val dashboardViewModel = ViewModelProvider(requireActivity())[DashboardViewModel::class.java]
        val avatarUri = userViewModel.getAvatarUri().value
        val username = userViewModel.getUsername()

        var photos: ArrayList<Photo> = ArrayList()

        val localUris: ArrayList<Uri> = ArrayList()

        val repository = FirestoreRepository()

        val addPhotoButton: FloatingActionButton = view.findViewById(R.id.add_post_btn_add_images)
        val confirmButton: Button = view.findViewById(R.id.add_post_btn_add)
        if (editing){
            confirmButton.text = resources.getText(R.string.edit)
        }

        val cancelButton: Button = view.findViewById(R.id.add_post_btn_cancel)
        val postBodyEditText: EditText = view.findViewById(R.id.add_post_text_field)
        val addImageRecyclerView: RecyclerView = view.findViewById(R.id.add_post_img_recycler)
        val addPostProgressBar: ProgressBar = view.findViewById(R.id.add_post_progress_bar)
        val addImageLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(
            view.context,LinearLayoutManager.HORIZONTAL, false)
        val addImageAdapter = SideScrollImageRecyclerAdapter()
        addImageRecyclerView.layoutManager = addImageLayoutManager
        addImageRecyclerView.adapter = addImageAdapter

        if (editing){
            for (uri in currentPost!!.images) {
                val photo = Photo(remoteUri = uri)
                photos.add(photo)
            }
            addImageAdapter.photos = photos
            addImageAdapter.notifyDataSetChanged()

        }

        viewModel.getPostBodyMutableLiveData().observe(viewLifecycleOwner) { text->
            postBodyEditText.setText(text)
        }
        viewModel.getPhotoMutableLiveData().observe(viewLifecycleOwner) {
            //photos = it

            addImageAdapter.photos = it
            addImageAdapter.notifyDataSetChanged()
        }

        val launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK
                && result.data != null
            ) {
                if (result.data!!.clipData != null) {
                    // reset list
                    photos = arrayListOf()
                    val mClipData = result.data!!.clipData
                    for (i in 0 until mClipData!!.itemCount) {
                        val item = mClipData.getItemAt(i)
                        val imageUri = item.uri
                        val photo = Photo(localUri = imageUri)
                        photos.add(photo)
                        localUris.add(imageUri)
                    }
                } else if (result.data!!.data != null) {
                    val imageUri = result.data!!.data
                    val photo = Photo(localUri = imageUri)
                    photos.add(photo)
                    localUris.add(imageUri!!)
                }
                viewModel.addPhotos(photos)
                addImageAdapter.photos = viewModel.getPhotoMutableLiveData().value!!
                addImageAdapter.notifyDataSetChanged()


            }
        }
        addPhotoButton.setOnClickListener {
            val intent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            launcher.launch(intent)
        }

        confirmButton.setOnClickListener {
            if (editing){
                viewModel.updatePostBodyText(postBodyEditText.text.toString())
                viewModel.updatePostPhotos()
                parentFragmentManager.popBackStack()
            }
            else {
                addPostProgressBar.visibility = View.VISIBLE
                val text = postBodyEditText.text.toString()
                val remoteUris = arrayListOf<Uri>()


                //TODO move to viewmodel
                CoroutineScope(Dispatchers.IO).launch {
                    if (localUris.isNotEmpty()) {

                        localUris.map { uri ->
                            async(Dispatchers.IO) {
                                when (val result = repository.addImageToStorage(uri)) {
                                    is Response.Loading -> {
                                    }
                                    is Response.Success -> {
                                        val remoteUri = result.data!!
                                        remoteUris.add(remoteUri)
                                    }
                                    is Response.Failure -> {
                                        print(result.e)
                                    }
                                }

                            }
                            // waiting for all request to finish executing in parallel
                        }.awaitAll()

                        when (val result = repository.addPostToFirestore(text, remoteUris,username,avatarUri)) {
                            is Response.Loading -> {
                            }
                            is Response.Success -> {
                                parentFragmentManager.popBackStack()
                                //dashboardViewModel.loadMorePosts(number = 1)
                            }
                            is Response.Failure -> {
                                print(result.e)
                            }
                        }
                    } else if (localUris.isEmpty()) {
                        when (val result = repository.addPostToFirestore(text, remoteUris,username,avatarUri)) {
                            is Response.Loading -> {
                            }
                            is Response.Success -> {
                                parentFragmentManager.popBackStack()
                                //dashboardViewModel.loadMorePosts(number = 1)
                            }
                            is Response.Failure -> {
                                print(result.e)
                            }
                        }
                    }
                }

            }
        }
        cancelButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        return view
    }


}
