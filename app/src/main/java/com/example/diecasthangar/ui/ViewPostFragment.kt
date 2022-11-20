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
import com.example.diecasthangar.data.model.Post
import com.example.diecasthangar.databinding.FragmentViewPostBinding


class ViewPostFragment(post: Post) : Fragment(), LifecycleOwner {
    private var _binding: FragmentViewPostBinding? = null
    private val binding get() = _binding!!
    private val dashViewModel: DashboardViewModel by viewModels()
    private val currentPost = post

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewPostBinding.inflate(inflater, container, false)
        val view = binding.root

        val dateTextView: TextView = binding.viewPostDate
        val avatarImageView: ImageView = binding.viewPostAvatar
        val bodyTextView: TextView = binding.viewPostBody
        val userTextView: TextView = binding.viewPostUsername
        val picImageview: ImageView = binding.viewPostPicture
        val postImageHolder: LinearLayout = binding.viewPostPhotoContainer
        val leftImageButton: Button = binding.btnViewPostImgLeft
        val rightImageButton: Button = binding.btnViewPostImgRight
        val commentRecyclerView: RecyclerView = binding.viewPostCommentRecycler

        val addCommentButton: Button = binding.viewPostCommentButton
        val commentEditText = binding.viewPostCommentEditText

        Glide.with(requireContext())
            .load(currentPost.avatar)
            .placeholder(R.drawable.ic_airplane_black_48dp)
            .into(avatarImageView)

        Glide.with(requireContext())
            .load(currentPost.images[0])
            .placeholder(R.drawable.ic_airplane_black_48dp)
            .into(picImageview)

        //TODO add sice scroller adpater to show dem images
        rightImageButton.visibility = View.GONE
        leftImageButton.visibility = View.GONE

        userTextView.text = currentPost.username
        dateTextView.text = parseDate(currentPost.date)
        bodyTextView.text = currentPost.text

        val commentAdapter = CommentRecyclerAdapter()


        val postLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(view.context)
        commentRecyclerView.layoutManager = postLayoutManager
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