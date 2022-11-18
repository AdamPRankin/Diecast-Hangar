package com.example.diecasthangar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.bumptech.glide.Glide
import com.example.diecasthangar.domain.adapters.PostRecyclerAdapter
import com.example.diecasthangar.profile.presentation.ProfileFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class DashboardFragment : Fragment(), LifecycleOwner {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_dashboard, container, false)
        val picView = view.findViewById<ImageView>(R.id.dashboard_profile_pic)
        val postLoadingCircle: ProgressBar = view.findViewById(R.id.dashboard_progress_indicator)
        val usernameTextView = view.findViewById<TextView>(R.id.dashboard_text_username)

        var loading = true

        val postRecyclerView = view.findViewById<RecyclerView>(R.id.post_recycler_view)
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
        })

        val postLayoutManager: LayoutManager = LinearLayoutManager(view.context)
        postRecyclerView.layoutManager = postLayoutManager
        postRecyclerView.adapter = postAdapter

        // Using the activityViewModels() Kotlin property delegate from the
        // fragment-ktx artifact to retrieve the ViewModel in the activity scope
        val userViewModel: UserViewModel by activityViewModels()
        val dashViewModel = ViewModelProvider(this)[DashboardViewModel::class.java]


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
            //TODO hide dashboard fragment
        }

        postRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = postRecyclerView.layoutManager as LinearLayoutManager

                // load more posts if recyclerview position is nearing the end
/*                if ((layoutManager.findLastVisibleItemPosition() > postAdapter.itemCount - 5) && dashViewModel.isLoading) {
                    CoroutineScope(Dispatchers.IO).launch {
                        dashViewModel.loadMorePosts()
                    }
                }*/
                //TODO load posts earlier
                if (!recyclerView.canScrollVertically(1) && dy > 0 && dashViewModel.isLoading) {
                    CoroutineScope(Dispatchers.IO).launch {
                        dashViewModel.loadMorePosts()
                    }
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
    }
}