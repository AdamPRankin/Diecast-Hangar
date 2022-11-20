package com.example.diecasthangar.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.bumptech.glide.Glide
import com.example.diecasthangar.R
import com.example.diecasthangar.databinding.FragmentDashboardBinding
import com.example.diecasthangar.ui.profile.ProfileFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton


class DashboardFragment : Fragment(), LifecycleOwner {
    val dashViewModel by viewModels<DashboardViewModel>()
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!



    override fun onCreate(savedInstanceState: Bundle?) {
/*        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                dashViewModel.postsFlow.collect() { posts ->
                    postAdapter.posts.addAll(posts)
                    postAdapter.notifyDataSetChanged()
                }
            }
        }*/
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val view = binding.root
        var loading = true

        val picView = binding.dashboardProfilePic
        val usernameTextView = binding.dashboardTextUsername
        val postRecyclerView = binding.postRecyclerView

        val postAdapter = PostRecyclerAdapter(
            // avatar clicked, go to user profile
            { post ->
                val uid = post.user
                parentFragmentManager.beginTransaction()
                    .add(R.id.container, ProfileFragment(uid)).addToBackStack("home").hide(this)
                    .commit()
            },
            // post edited, go to post fragment
            { post ->
                parentFragmentManager.beginTransaction()
                    .add(R.id.container, AddPostFragment(post, true)).addToBackStack("home")
                    .commit()
            },
            // post deleted
            { post ->
                dashViewModel.deletePost(post.id)
            },
            //comment button clicked
            { post ->
                parentFragmentManager.beginTransaction()
                    .add(R.id.container, ViewPostFragment(post)).addToBackStack("home")
                    .commit()
            }

        )

        val postLayoutManager: LayoutManager = LinearLayoutManager(view.context)
        postRecyclerView.layoutManager = postLayoutManager
        postRecyclerView.adapter = postAdapter

        // Using the activityViewModels() Kotlin property delegate from the
        // fragment-ktx artifact to retrieve the ViewModel in the activity scope
        val userViewModel: UserViewModel by activityViewModels()


        userViewModel.isDataLoaded().observe(viewLifecycleOwner) { userViewModel.isDataLoaded()
            usernameTextView.text = userViewModel.getUsername()
        }

        userViewModel.getAvatarUri().observe(viewLifecycleOwner) { uri ->
            val avatarUri = uri.toString()
            Glide.with(view).load(avatarUri).into(picView)
        }

        dashViewModel.getPostMutableLiveData().observe(viewLifecycleOwner) {
            if (loading) {
                loading = false
                //remove loading Ui elements
                //postLoadingCircle.visibility = View.GONE
                postRecyclerView.visibility = View.VISIBLE
            }
            val end = postAdapter.posts.size

            postAdapter.posts = it
            postAdapter.notifyItemChanged(0)
            postAdapter.notifyItemRangeChanged(end,postAdapter.posts.size)
        }

        val addPostButton = view.findViewById<FloatingActionButton>(R.id.dash_btn_add_post)

        addPostButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .add(R.id.container, AddPostFragment()).addToBackStack("home")
                .commit()
        }

        picView.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .add(R.id.container, ProfileFragment()).addToBackStack("home").hide(this)
                .commit()
        }

        postRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                // load more posts if recyclerview position is at the end
                //TODO load posts earlier
                if (!recyclerView.canScrollVertically(1) && dy > 0 && dashViewModel.isLoading) {
                    dashViewModel.loadMorePosts()
                } else if (!recyclerView.canScrollVertically(-1) && dy < 0) {
                    //scrolled to TOP
                }
            }
        })

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
        _binding = null
    }
}