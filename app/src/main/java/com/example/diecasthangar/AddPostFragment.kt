package com.example.diecasthangar

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diecasthangar.data.Photo
import com.example.diecasthangar.domain.Response
import com.example.diecasthangar.domain.adapters.SideScrollImageRecyclerAdapter
import com.example.diecasthangar.domain.remote.FirestoreRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*


class AddPostFragment : Fragment() {

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add_post, container, false)

        val photos: ArrayList<Photo> = ArrayList()
        val localUris = ArrayList<Uri>()

        val storage: FirebaseStorage = FirebaseStorage.getInstance()
        val db: FirebaseFirestore = Firebase.firestore
        val repository = FirestoreRepository()

        val photoImageView: ImageView = view.findViewById(R.id.add_post_placeholder_picture)
        val addButton: Button = view.findViewById(R.id.add_post_btn_add)
        val cancelButton: Button = view.findViewById(R.id.add_post_btn_cancel)
        val postTextView: TextView = view.findViewById(R.id.add_post_text_field)
        val addImageRecyclerView: RecyclerView = view.findViewById(R.id.add_post_img_recycler)
        val addPostProgressBar: ProgressBar = view.findViewById(R.id.add_post_progress_bar)
        val addImageLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(
            view.context,LinearLayoutManager.HORIZONTAL, false)
        val addImageAdapter = SideScrollImageRecyclerAdapter()
        addImageRecyclerView.layoutManager = addImageLayoutManager
        addImageRecyclerView.adapter = addImageAdapter


        val launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK
                && result.data != null
            ) {
                if (result.data!!.clipData != null) {
                    val mClipData = result.data!!.clipData
                    for (i in 0 until mClipData!!.itemCount) {
                        val item = mClipData.getItemAt(i)
                        val imageUri = item.uri
                        val photo = Photo(remoteUri = imageUri.toString())
                        photos.add(photo)
                        localUris.add(imageUri)
                    }
                } else if (result.data!!.data != null) {
                    val imageUri = result.data!!.data
                    val photo = Photo(remoteUri = imageUri.toString())
                    photos.add(photo)
                    localUris.add(imageUri!!)
                }
                photoImageView.visibility = View.GONE
                addImageRecyclerView.visibility = View.VISIBLE
                addImageAdapter.localUris = localUris
                addImageAdapter.notifyDataSetChanged()
            }
        }
        photoImageView.setOnClickListener {
            val intent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            launcher.launch(intent)
        }

        addButton.setOnClickListener {
            addPostProgressBar.visibility = View.VISIBLE
            val text = postTextView.text.toString()
            val remoteUris = arrayListOf<Uri>()



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

                        when (val result = repository.addPostToFirestore(text,remoteUris)) {
                            is Response.Loading -> {
                            }
                            is Response.Success -> {
                                parentFragmentManager.popBackStack()
                            }
                            is Response.Failure -> {
                                print(result.e)
                            }
                        }
                }
                    else if (localUris.isEmpty()){
                        when (val result = repository.addPostToFirestore(text,remoteUris)) {
                            is Response.Loading -> {
                            }
                            is Response.Success -> {
                                parentFragmentManager.popBackStack()
                            }
                            is Response.Failure -> {
                                print(result.e)
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

