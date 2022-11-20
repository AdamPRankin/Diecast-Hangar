package com.example.diecasthangar.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.diecasthangar.core.util.parseDate
import com.example.diecasthangar.data.model.Comment
import com.example.diecasthangar.databinding.RecyclerCommentRowLayoutBinding


class CommentRecyclerAdapter(): RecyclerView.Adapter<CommentRecyclerAdapter.ViewHolder>() {
    var comments = ArrayList<Comment>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecyclerCommentRowLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = comments[position]

        holder.bodyTextView.text = comment.text
        holder.dateTextView.text = parseDate(comment.date)
        holder.usernameTextView.text = comment.username

        Glide.with(holder.itemView.context)
            .load(comment.avatarUri)
            .into(holder.avatarImageView)
    }

    inner class ViewHolder(binding: RecyclerCommentRowLayoutBinding): RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {
        val view = binding.root

        val avatarImageView = binding.postCommentAvatar
        val bodyTextView = binding.postCommentBody
        val dateTextView = binding.postCommentDate
        val usernameTextView = binding.postCommentUsername


        init {
            view.setOnClickListener(this)
        }
        override fun onClick(v: View?) {
        }
    }

    override fun getItemCount(): Int {
        return comments.size
    }
}