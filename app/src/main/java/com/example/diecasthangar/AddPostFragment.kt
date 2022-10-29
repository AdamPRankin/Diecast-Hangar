package com.example.diecasthangar

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diecasthangar.data.Photo
import com.example.diecasthangar.domain.Response
import com.example.diecasthangar.domain.adapters.SideScrollImageRecyclerAdapter
import com.example.diecasthangar.domain.remote.FirestoreRepository
import com.example.diecasthangar.domain.usecase.remote.getUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await


class AddPostFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

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


        val storageReference = FirebaseStorage.getInstance().reference
        val userDbRef = FirebaseDatabase.getInstance().getReference("Users")

        val photoImageView: ImageView = view.findViewById(R.id.add_post_placeholder_picture)
        val addButton: Button = view.findViewById(R.id.add_post_btn_add)
        val cancelButton: Button = view.findViewById(R.id.add_post_btn_cancel)
        val postTextView: TextView = view.findViewById(R.id.add_post_text_field)
        val addImageRecyclerView: RecyclerView = view.findViewById(R.id.add_post_img_recycler)

        val addImageLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(
            view.context,LinearLayoutManager.HORIZONTAL, false)
        val addImageAdapter = SideScrollImageRecyclerAdapter()
        addImageRecyclerView.layoutManager = addImageLayoutManager
        addImageRecyclerView.adapter = addImageAdapter

        addImageAdapter.localUris = localUris

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
        photoImageView.setOnClickListener(){
            val intent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            launcher.launch(intent)
        }

        addButton.setOnClickListener() {
            val storage: FirebaseStorage = FirebaseStorage.getInstance()
            val db: FirebaseFirestore = Firebase.firestore
            val text = postTextView.text.toString()
            val remoteUris = arrayListOf<Uri>()

            if (localUris.size > 0) {

                lifecycleScope.launch {
                    val uploadPhoto = FirestoreRepository(storage,db)

                    localUris.map { uri ->
                        async(Dispatchers.IO) {
                            when(val result = uploadPhoto.addPostToFireStore(uri)) {
                                is Response.Loading -> {
                                    //TODO display progress bar
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
                    val rootRef = FirebaseDatabase.getInstance().reference

                    val newPostKey = rootRef.ref.child("posts").push().key
                    val hashPost = hashMapOf(
                        "text" to text,
                        "id" to newPostKey,
                        "images" to remoteUris,
                        "user" to getUser()!!.uid,
                        "date" to FieldValue.serverTimestamp()
                    )
                    db.collection("posts").add(hashPost)
                        .addOnSuccessListener {
                            Log.d(ContentValues.TAG, "post  added")
                        }.addOnFailureListener { e ->
                            Log.e(ContentValues.TAG, "error adding document")
                        }.await()

                }

            }

            else if (localUris.isEmpty()){
                val rootRef = FirebaseDatabase.getInstance().reference

                val newPostKey = rootRef.ref.child("posts").push().key
                val hashPost = hashMapOf(
                    "text" to text,
                    "id" to newPostKey,
                    "user" to getUser()!!.uid,
                    "date" to FieldValue.serverTimestamp()
                )
                db.collection("posts").add(hashPost).addOnSuccessListener {
                    Log.d(ContentValues.TAG,"post  added")
                }.addOnFailureListener { e ->
                    Log.e(ContentValues.TAG,"error adding document")
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, DashboardFragment())
                .commit()
        }
        cancelButton.setOnClickListener() {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, DashboardFragment())
                .commit()
        }
        return view
    }
}