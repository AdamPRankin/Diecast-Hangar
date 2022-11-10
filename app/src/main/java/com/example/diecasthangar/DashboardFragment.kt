package com.example.diecasthangar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.bumptech.glide.Glide
import com.example.diecasthangar.core.util.loadingDummyPost
import com.example.diecasthangar.domain.Response
import com.example.diecasthangar.domain.adapters.PostRecyclerAdapter
import com.example.diecasthangar.domain.remote.FirestoreRepository
import com.example.diecasthangar.profile.presentation.ProfileFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.DocumentSnapshot
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
        //val viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]
        val view: View = inflater.inflate(R.layout.fragment_dashboard, container, false)
        val picView = view.findViewById<ImageView>(R.id.dashboard_profile_pic)
        val postLoadingCircle: ProgressBar = view.findViewById(R.id.dashboard_progress_indicator)
        var loading = true

        val usernameTextView = view.findViewById<TextView>(R.id.dashboard_text_username)
        val main = (activity as MainActivity)
        usernameTextView.text = main.username

        Glide.with(view).load(main.avatarUri).into(picView)


        val addPostButton = view.findViewById<FloatingActionButton>(R.id.dash_btn_add_post)

        addPostButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .add(R.id.container, AddPostFragment()).addToBackStack("home")
                .commit()
        }

        picView.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .add(R.id.container, ProfileFragment()).addToBackStack("home")
                .commit()
        }

        val postRecyclerView = view.findViewById<RecyclerView>(R.id.post_recycler_view)
        val postAdapter = PostRecyclerAdapter()
        //postAdapter.posts = viewModel.posts.value as ArrayList<Post>
        val postLayoutManager: LayoutManager = LinearLayoutManager(view.context)
        var snapshot: DocumentSnapshot? = null

        postRecyclerView.layoutManager = postLayoutManager
        postRecyclerView.adapter = postAdapter
        val loadingPost = loadingDummyPost()
        postAdapter.posts.add(loadingPost)

        val storage: FirebaseStorage = FirebaseStorage.getInstance()
        val db: FirebaseFirestore = Firebase.firestore
        val repository = FirestoreRepository(storage,db)

        lifecycleScope.launch {

            when(val response = repository.loadNextPagePosts(snapshot)) {
                is Response.Loading -> {

                }
                is Response.Success -> {
                    val (postsList,newSnap) = response.data!!
                    //postAdapter.posts.removeAt(0)

                    //val postsList = addCommentsToPosts(repository,noCommentsPostList,3)

                    snapshot = newSnap
                    postAdapter.posts = postsList
                    postAdapter.notifyItemRangeChanged(
                        postAdapter.itemCount-10,postAdapter.itemCount)
                    postLoadingCircle.visibility = View.GONE
                }
                is Response.Failure -> {
                    print(response.e)
                }
            }
        }



        postRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1) && dy > 0 && loading) {
                    lifecycleScope.launch {
                        when(val response = repository.loadNextPagePosts(snapshot)) {
                            is Response.Loading -> {

                            }
                            is Response.Success -> {
                                val (postsList,newSnap) = response.data!!
                                //val postsList = addCommentsToPosts(repository,noCommentsPostList,3)
                                // if document snapshot is the same, then there are no more posts
                                // to load, so set loading to false
                                if (newSnap == snapshot){
                                    loading = false
                                }
                                snapshot = newSnap
                                postAdapter.posts.addAll(postsList)
                                postAdapter.notifyItemRangeChanged(
                                    postAdapter.itemCount-10,postAdapter.itemCount)
                            }
                            is Response.Failure -> {
                                print(response.e)
                            }
                        }
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