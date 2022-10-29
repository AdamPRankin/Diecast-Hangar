package com.example.diecasthangar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.bumptech.glide.Glide
import com.example.diecasthangar.domain.Response
import com.example.diecasthangar.domain.adapters.PostRecyclerAdapter
import com.example.diecasthangar.domain.remote.FirestoreRepository
import com.example.diecasthangar.domain.usecase.remote.getUser
import com.example.diecasthangar.domain.usecase.remote.getUserUsername
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch


/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class DashboardFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //loadData()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_dashboard, container, false)
        val picView = view.findViewById<ImageView>(R.id.dashboard_profile_pic)

        val usernameTextView = view.findViewById<TextView>(R.id.dashboard_text_username)
        usernameTextView.text = getUserUsername()

        val addPostButton = view.findViewById<FloatingActionButton>(R.id.dash_btn_add_post)

        addPostButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, AddPostFragment())
                .commit()
        }


        picView.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, ProfileFragment())
                .commit()
        }

        val postRecyclerView = view.findViewById<RecyclerView>(R.id.post_recycler_view)
        val postAdapter = PostRecyclerAdapter()
        val postLayoutManager: LayoutManager = LinearLayoutManager(view.context)
        var isLoading = false

        postRecyclerView.layoutManager = postLayoutManager
        postRecyclerView.adapter = postAdapter

        val storage: FirebaseStorage = FirebaseStorage.getInstance()
        val db: FirebaseFirestore = Firebase.firestore
        val repository = FirestoreRepository(storage,db)

        lifecycleScope.launch {

            when(val response = repository.getPostsFromFireStore()) {
                is Response.Loading -> {
                    //TODO display progress bar
                }
                is Response.Success -> {
                    val postsList = response.data!!
                    postAdapter.posts = postsList
                    //TODO change to notify tem range changed
                    postAdapter.notifyItemRangeChanged(
                        postAdapter.itemCount-10,postAdapter.itemCount)
                }
                is Response.Failure -> {
                    print(response.e)
                }
            }
        }

        lifecycleScope.launch {

            when(val response = repository.getUserAvatar(getUser()!!.uid)) {
                is Response.Loading -> {
                }
                is Response.Success -> {
                    val avatarURL = response.data
                    Glide.with(view.context).load(avatarURL).into(picView)
                }
                is Response.Failure -> {
                    print(response.e)
                }
            }
        }


        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        super.onCreate(savedInstanceState)

    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()

    }

    override fun onDestroy() {
        super.onDestroy()
    }

}