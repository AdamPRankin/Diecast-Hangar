package com.example.diecasthangar.profile.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.diecasthangar.R
import com.example.diecasthangar.UserViewModel
import com.example.diecasthangar.core.util.loadingDummyPost
import com.example.diecasthangar.domain.adapters.PostRecyclerAdapter
import com.example.diecasthangar.domain.usecase.remote.getUser
import com.google.android.material.floatingactionbutton.FloatingActionButton


open class ProfileFragment(uid: String = getUser()!!.uid): Fragment(), LifecycleOwner {
    private val currentUserId = uid
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
        val profileBioText: TextView = view.findViewById(R.id.profile_text_bio)


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

        var loading = true
        val loadingPost = loadingDummyPost()
        postAdapter.posts.add(loadingPost)

        val viewModel: ProfileViewModel by viewModels { ProfileViewModel.Factory(currentUserId) }
        //val viewModel =  ViewModelProvider(this)[ProfileViewModel::class.java]
        viewModel.getPostMutableLiveData().observe(viewLifecycleOwner) { postList ->
            if (loading) {
                loading = false
                //remove loading Ui elements
                postAdapter.posts.removeAt(0)
                postAdapter.notifyItemRemoved(0)
            }
            // update UI
            postAdapter.notifyItemRemoved(0)
            val prevSize = postAdapter.posts.size
            postAdapter.posts = postList
            //X previous posts, so we want to update from index X onwards
            postAdapter.notifyItemRangeChanged(prevSize,postAdapter.itemCount)
        }

        viewModel.getUserBioMutableLiveData().observe(viewLifecycleOwner) { bio->
            profileBioText.text = bio
        }
        viewModel.getUsernameMutableLiveData().observe(viewLifecycleOwner) { name->
            profileUsername.text = name
        }
        viewModel.getAvatarMutableLiveData().observe(viewLifecycleOwner) { uri->
            Glide.with(view).load(uri).into(profileImageView)
        }

        postRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1) && dy > 0 && viewModel.postsLoading) {
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