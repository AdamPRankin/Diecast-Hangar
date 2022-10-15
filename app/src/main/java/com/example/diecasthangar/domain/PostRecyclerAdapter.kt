package com.example.diecasthangar.domain

import android.annotation.SuppressLint
import android.media.Image
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.diecasthangar.R
import com.example.diecasthangar.core.inflate
import com.example.diecasthangar.data.Post

class PostRecyclerAdapter: RecyclerView.Adapter<PostRecyclerAdapter.ViewHolder>() {
    var posts =  ArrayList<Post>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView = parent.inflate(R.layout.recycler_post_row_layout, false)

        return ViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = posts[position]
        holder.dateTextView.text = post.date.toString()
        holder.bodyTextView.text = post.text
        holder.userTextView.text = post.user.username
        holder.avatarImageView.setImageResource(R.drawable.ic_launcher_foreground)
        holder.picImageview.setImageResource(R.drawable.inuit)

    }

    inner class ViewHolder(v: View): RecyclerView.ViewHolder(v),
            View.OnClickListener {

        private var view: View = v
        val dateTextView: TextView = view.findViewById(R.id.post_date)
        val avatarImageView: ImageView = view.findViewById(R.id.post_picture)
        val bodyTextView: TextView = view.findViewById(R.id.post_body)
        val userTextView: TextView = view.findViewById(R.id.post_username)
        val picImageview: ImageView = view.findViewById(R.id.post_picture)




        init {
            v.setOnClickListener(this)

        }

        override fun onClick(v: View?) {
            //TODO
        }
    }

    override fun getItemCount(): Int {
        return posts.size
    }



}