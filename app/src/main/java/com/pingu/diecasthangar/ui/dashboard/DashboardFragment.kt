package com.pingu.diecasthangar.ui.dashboard

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.pingu.diecasthangar.R
import com.pingu.diecasthangar.databinding.FragmentDashboardBinding
import com.pingu.diecasthangar.ui.AddPostFragment
import com.pingu.diecasthangar.ui.PostRecyclerAdapter
import com.pingu.diecasthangar.ui.SettingsFragment
import com.pingu.diecasthangar.ui.UserViewModel
import com.pingu.diecasthangar.ui.profile.ProfileFragment
import com.pingu.diecasthangar.ui.viewpost.ViewPostFragment
import com.google.android.gms.common.util.DeviceProperties.isTablet
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch


class DashboardFragment : Fragment(), LifecycleOwner {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var frameLayout: FrameLayout
    private var currentTab: String = "all"


    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState != null){
            currentTab = savedInstanceState.getString("TAB") ?: "all"
        }
        super.onCreate(savedInstanceState)
    }
    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        //frameLayout = FrameLayout(requireActivity())
        val view = binding.root
        //frameLayout.addView(view)
        val dashViewModel: DashboardViewModel by activityViewModels()
        val isTablet: Boolean = isTablet(requireContext())
        val fragContainer =
            if (isTablet){
                R.id.right_container
            } else {
                R.id.container
            }


        val picView = binding.dashboardProfilePic
        val usernameTextView = binding.dashboardTextUsername
        val postRecyclerView = binding.postRecyclerView

        val hotRecyclerView = binding.postFriendsRecyclerView
        val settingsButton = binding.dashBtnSettings

        val allPostsButton = binding.btnAllPosts
        val hotPostsButton = binding.btnHotPosts
        val officialPostsButton = binding.btnAnnouncementPosts
        val dashButtonTabToggle = binding.dashTabToggleButton

        val newsRecyclerView = binding.postNewsRecyclerView
        val newsLayoutManager: LayoutManager = LinearLayoutManager(view.context)
        newsRecyclerView.layoutManager = newsLayoutManager

        fun swapToHotTab() {
            dashButtonTabToggle.check(R.id.btn_hot_posts)
            postRecyclerView.visibility = View.GONE
            hotRecyclerView.visibility = View.VISIBLE
            newsRecyclerView.visibility = View.GONE
            currentTab = "hot"
        }

        fun swapToAllTab() {
            dashButtonTabToggle.check(R.id.btn_all_posts)
            postRecyclerView.visibility = View.VISIBLE
            hotRecyclerView.visibility = View.GONE
            newsRecyclerView.visibility = View.GONE
            currentTab = "all"
        }

        fun swapToNewsTab(){
            postRecyclerView.visibility = View.GONE
            hotRecyclerView.visibility = View.GONE
            newsRecyclerView.visibility = View.VISIBLE
            dashButtonTabToggle.check(R.id.btn_announcement_posts)
            currentTab = "news"
        }

        fun navigateToFragment(fragment: Fragment){
            if (isTablet){
                parentFragmentManager.beginTransaction()
                    .replace(fragContainer, fragment).addToBackStack("home")
                    .commit()
            }
            else {
                parentFragmentManager.beginTransaction()
                    .add(fragContainer, fragment).addToBackStack("home")
                    .hide(this).commit()
            }
        }

        when (currentTab) {
            "hot" -> swapToHotTab()
            "all" -> swapToAllTab()
            "news" -> swapToNewsTab()
            //else -> swapToAllTab()
        }

        val postAdapter = PostRecyclerAdapter(
            // avatar clicked, go to user profile
            { post ->
                val uid = post.user
                navigateToFragment(ProfileFragment(uid))
            },
            // post edited, go to post fragment
            { post ->
                navigateToFragment(AddPostFragment(post,true))
            },
            // post deleted
            { post ->
                dashViewModel.deletePost(post.id)
            },
            //comment button clicked
            { post ->
                //dashViewModel.selectedPost.value = post
                //dashViewModel.currentViewingPost = post
                navigateToFragment(ViewPostFragment(post))
            },
            //reaction clicked
            { pair ->
                val (reaction, pid) = pair
                dashViewModel.addReact(reaction, pid)
            }
        )

        hotPostsButton.setOnClickListener {
            swapToHotTab()
        }

        allPostsButton.setOnClickListener {
            swapToAllTab()
        }

        officialPostsButton.setOnClickListener {
            swapToNewsTab()
        }

        val newsPostAdapter = PostRecyclerAdapter(
            // avatar clicked, go to user profile
            { post ->
                val uid = post.user
                navigateToFragment(ProfileFragment(uid))
            },
            // post edited, go to post fragment
            { post ->
                navigateToFragment(AddPostFragment(post,true))
            },
            // post deleted
            { post ->
                dashViewModel.deletePost(post.id)
            },
            //comment button clicked
            { post ->
                dashViewModel.selectedPost.value = post
                dashViewModel.currentViewingPost = post
                navigateToFragment(ViewPostFragment(post))
            },
            //reaction clicked
            { pair ->
                val (reaction, pid) = pair
                dashViewModel.addReact(reaction, pid)
            }
        )

        val friendPostAdapter = PostRecyclerAdapter(
            // avatar clicked, go to user profile
            { post ->
                val uid = post.user
                navigateToFragment(ProfileFragment(uid))
            },
            // post edited, go to post fragment
            { post ->
                navigateToFragment(AddPostFragment(post,true))
            },
            // post deleted
            { post ->
                dashViewModel.deletePost(post.id)
            },
            //comment button clicked
            { post ->
                dashViewModel.currentViewingPost = post
                navigateToFragment(ViewPostFragment(post))
            },
            //reaction clicked
            { pair ->
                val (reaction, pid) = pair
                dashViewModel.addReact(reaction, pid)
            }
        )

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                dashViewModel.topPosts.collect {
                    friendPostAdapter.setData(it)
                }
            }
        }

        hotPostsButton.setOnClickListener {
            swapToHotTab()
        }

        allPostsButton.setOnClickListener {
            swapToAllTab()
        }

        officialPostsButton.setOnClickListener {
            swapToNewsTab()
        }

        settingsButton.setOnClickListener {
            navigateToFragment(SettingsFragment())
        }

        val postLayoutManager: LayoutManager = LinearLayoutManager(view.context)
        postRecyclerView.layoutManager = postLayoutManager
        postRecyclerView.adapter = postAdapter


        newsRecyclerView.adapter = newsPostAdapter

        // Using the activityViewModels() Kotlin property delegate from the
        // fragment-ktx artifact to retrieve the ViewModel in the activity scope
        val userViewModel: UserViewModel by activityViewModels()


        userViewModel.isDataLoaded().observe(viewLifecycleOwner) { userViewModel.isDataLoaded()
            usernameTextView.text = userViewModel.getUsername()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.getAvatarUri().observe(viewLifecycleOwner) { uri ->
                    val avatarUri = uri.toString()
                    Glide.with(view)
                        .load(avatarUri)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .into(picView)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                dashViewModel.allPosts.collect {
                    postAdapter.setData(it)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                dashViewModel.newsPosts.collect {
                    newsPostAdapter.setData(it)
                }
            }
        }

        val friendPostLayoutManager: LayoutManager = LinearLayoutManager(view.context)
        hotRecyclerView.layoutManager = friendPostLayoutManager
        hotRecyclerView.adapter = friendPostAdapter

        val addPostButton = view.findViewById<FloatingActionButton>(R.id.dash_btn_add_post)

        addPostButton.setOnClickListener { //navigate(AddPostFragment())
            navigateToFragment(AddPostFragment())
        }

        picView.setOnClickListener {
            navigateToFragment(ProfileFragment())
        }

        //keep track of furthest scrolled position and trigger post load when needed
        val lm = postRecyclerView.layoutManager as LinearLayoutManager
        postRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lastVisible = lm.findLastVisibleItemPosition() + 1
                if (dashViewModel.lastVisibleItem.value < lastVisible) {
                    dashViewModel.lastVisibleItem.value = lastVisible
                }
            }
        })

        val lmNews = newsRecyclerView.layoutManager as LinearLayoutManager
        newsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lastVisible = lmNews.findLastVisibleItemPosition() + 1
                if (dashViewModel.lastVisibleItemNews.value < lastVisible) {
                    dashViewModel.lastVisibleItemNews.value = lastVisible
                }
            }
        })

        val lmHot = hotRecyclerView.layoutManager as LinearLayoutManager
        hotRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lastVisible = lmHot.findLastVisibleItemPosition() + 1
                if (dashViewModel.lastVisibleItemTop.value < lastVisible) {
                    dashViewModel.lastVisibleItemTop.value = lastVisible
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


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        //int orientation = this.getResources().getConfiguration().orientation;
        super.onConfigurationChanged(newConfig)
/*        frameLayout.removeAllViews()
        val inflater =
            requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val newView = inflater.inflate(R.layout.fragment_profile, frameLayout,false)
        frameLayout.addView(newView)*/
    }

    override fun onSaveInstanceState(outState: Bundle) {
        //save UI state
        outState.putString("TAB", currentTab)
        super.onSaveInstanceState(outState)
        //outState.putString("avatar-uri", )

    }


}