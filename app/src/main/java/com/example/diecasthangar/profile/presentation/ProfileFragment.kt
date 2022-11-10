package com.example.diecasthangar.profile.presentation

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.diecasthangar.MainActivity
import com.example.diecasthangar.R
import com.example.diecasthangar.UserViewModel
import com.example.diecasthangar.domain.adapters.PostRecyclerAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton


class ProfileFragment: Fragment(), LifecycleOwner {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        // Inflate the layout for this fragment

        val saveProfileButton: FloatingActionButton = view.findViewById(R.id.profile_btn_edit_profile)
        val userViewModel: UserViewModel by activityViewModels()
        val profileImageView: ImageView = view.findViewById(R.id.profile_avatar)
        val profileUsername: TextView = view.findViewById(R.id.profile_name)

        Glide.with(view).load(userViewModel
            .getAvatarUri()).into(profileImageView)
        profileUsername.text = userViewModel.getUsername()

/*        val launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK
                && result.data != null
            ) {
                val photoUri: Uri? = result.data!!.data
                profileImage.setImageURI(photoUri)
                saveProfileButton.visibility = View.VISIBLE
            }
        }
        profileImage.setOnClickListener {
            val intent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            launcher.launch(intent)
        }*/

        saveProfileButton.setOnClickListener {

        }

        val postRecyclerView = view.findViewById<RecyclerView>(R.id.profile_post_recycler)
        val postAdapter = PostRecyclerAdapter()
        val postLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(view.context)
        postRecyclerView.layoutManager = postLayoutManager
        postRecyclerView.adapter = postAdapter


        val viewModel =  ViewModelProvider(this)[ProfileViewModel::class.java]
        postAdapter.posts = viewModel.getPostMutableLiveData().value!!

        viewModel.getPostMutableLiveData().observe(viewLifecycleOwner) { postList ->
            // update UI
            postAdapter.notifyItemRemoved(0)
            val prevSize = postAdapter.posts.size
            postAdapter.posts = postList
            //X previous posts, so we want to update from index X onwards
            postAdapter.notifyItemRangeChanged(prevSize,postAdapter.itemCount)
        }

        postRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1) && dy > 0 && viewModel.isLoading) {
                    viewModel.loadMorePosts()
                }
            }
        })
        return view
    }

    override fun onPause() {
        super.onPause()
        //TODO save profile data
    }

}