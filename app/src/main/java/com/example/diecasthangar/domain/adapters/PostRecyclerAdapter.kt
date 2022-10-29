package com.example.diecasthangar.domain.adapters

import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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
        var currentImagePosition = 0
        holder.dateTextView.text = post.date.toString()
        holder.bodyTextView.text = post.text
        holder.userTextView.text = post.username

        val avatarUri = post.avatar
        Glide.with(holder.itemView.context).load(avatarUri).into(holder.avatarImageView)



        if (post.images.isNotEmpty()){
            val firstImageUri: Uri = Uri.parse(post.images[0])
            Glide.with(holder.itemView.context).load(firstImageUri).into(holder.picImageview)
        }
        if (post.images.isEmpty()){
            holder.postImageHolder.visibility = View.GONE
        }
        if (post.images.size < 2) {
            holder.leftImageButton.visibility = View.GONE
            holder.rightImageButton.visibility = View.GONE
        }
        else {
            holder.leftImageButton.setOnClickListener {
                if (currentImagePosition == 0){
                    currentImagePosition = post.images.size-1
                }
                else{
                    currentImagePosition -=1
                }
                val imageUri: Uri = Uri.parse(post.images[currentImagePosition])
                Glide.with(holder.itemView.context).load(imageUri).into(holder.picImageview)

            }
            holder.rightImageButton.setOnClickListener {
                if (currentImagePosition == post.images.size-1){
                    currentImagePosition = 0
                }
                else{
                    currentImagePosition +=1
                }
                val imageUri: Uri = Uri.parse(post.images[currentImagePosition])
                Glide.with(holder.itemView.context).load(imageUri).into(holder.picImageview)
            }
        }
    }

    inner class ViewHolder(v: View): RecyclerView.ViewHolder(v),
            View.OnClickListener {

        private var view: View = v
        val dateTextView: TextView = view.findViewById(R.id.post_date)
        val avatarImageView: ImageView = view.findViewById(R.id.post_avatar)
        val bodyTextView: TextView = view.findViewById(R.id.post_body)
        val userTextView: TextView = view.findViewById(R.id.post_username)
        val picImageview: ImageView = view.findViewById(R.id.post_picture)
        val postImageHolder: LinearLayout = view.findViewById(R.id.post_photo_container)
        val leftImageButton: Button = view.findViewById(R.id.btn_post_img_left)
        val rightImageButton: Button = view.findViewById(R.id.btn_post_img_right)

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