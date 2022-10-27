package com.example.diecasthangar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.example.diecasthangar.core.MockPosts
import com.example.diecasthangar.data.Post
import com.example.diecasthangar.domain.adapters.PostRecyclerAdapter
import com.example.diecasthangar.domain.usecase.remote.getUserUsername
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.collections.ArrayList

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

        addPostButton.setOnClickListener(){
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, AddPostFragment())
                .commit()
        }

        picView.setImageResource(R.drawable.inuit)

        picView.setOnClickListener(){
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, ProfileFragment())
                .commit()
        }

        val postRecyclerView = view.findViewById<RecyclerView>(R.id.post_recycler_view)
        val postAdapter = PostRecyclerAdapter()
        val postLayoutManager: LayoutManager = LinearLayoutManager(view.context)
        var isLoading: Boolean = false

        postRecyclerView.layoutManager = postLayoutManager
        postRecyclerView.adapter = postAdapter

        val mockList  = MockPosts("s")
        val posts: ArrayList<Post> = mockList.getPosts()

        postAdapter.posts = posts
        //postRecyclerView.addOnScrollListener()

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