package com.example.diecasthangar.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.diecasthangar.R
import com.example.diecasthangar.core.util.parseDate
import com.example.diecasthangar.data.model.Photo
import com.example.diecasthangar.data.model.Post
import com.example.diecasthangar.databinding.FragmentViewPostBinding


class ViewPostFragment(post: Post) : Fragment(), LifecycleOwner {
    private var _binding: FragmentViewPostBinding? = null
    private val binding get() = _binding!!
    private val dashViewModel: DashboardViewModel by viewModels()
    private val currentPost = post

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewPostBinding.inflate(inflater, container, false)
        val view = binding.root

        val dateTextView: TextView = binding.viewPostDate
        val avatarImageView: ImageView = binding.viewPostAvatar
        val bodyTextView: TextView = binding.viewPostBody
        val userTextView: TextView = binding.viewPostUsername
        val picImageview: ImageView = binding.viewPostPicture
        val commentRecyclerView: RecyclerView = binding.viewPostCommentRecycler

        val addCommentButton: Button = binding.viewPostCommentButton
        val commentEditText = binding.viewPostCommentEditText
        val photoRecyclerView = binding.viewPostPhotoRecycler

        Glide.with(requireContext())
            .load(currentPost.avatar)
            .placeholder(R.drawable.ic_airplane_black_48dp)
            .into(avatarImageView)
        userTextView.text = currentPost.username
        dateTextView.text = parseDate(currentPost.date)
        bodyTextView.text = currentPost.text

        if (currentPost.images.size == 0){
            photoRecyclerView.visibility = View.GONE
            picImageview.visibility = View.GONE
        }
        if (currentPost.images.size == 1) {
            photoRecyclerView.visibility = View.GONE
            picImageview.visibility = View.VISIBLE
            Glide.with(requireContext())
                .load(currentPost.images[0].remoteUri)
                .placeholder(R.drawable.ic_airplane_black_48dp)
                .into(picImageview)
        }
        else {
            picImageview.visibility = View.GONE
            photoRecyclerView.visibility = View.VISIBLE
            val photoAdapter = SideScrollImageRecyclerAdapter({ _ ->
                //not deleting items
            }, false)
            photoRecyclerView.adapter = photoAdapter
            photoRecyclerView.layoutManager = LinearLayoutManager(view.context,LinearLayoutManager.HORIZONTAL, false)

            photoAdapter.photos = currentPost.images
        }

        val commentAdapter = CommentRecyclerAdapter()
        val commentLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(view.context)
        commentRecyclerView.layoutManager = commentLayoutManager
        commentRecyclerView.adapter = commentAdapter

        dashViewModel.loadComments(currentPost.id)
        dashViewModel.getCurrentCommentMutableLiveData().observe(viewLifecycleOwner) { comments ->
            commentAdapter.comments = comments
            commentAdapter.notifyDataSetChanged()
        }

        addCommentButton.setOnClickListener {

            if (commentEditText.text.toString().isNotEmpty()) {
                val commentText = commentEditText.text.toString()
                val pid = currentPost.id
                dashViewModel.addComment(pid, commentText)
            }
            parentFragmentManager.popBackStack()
        }

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}