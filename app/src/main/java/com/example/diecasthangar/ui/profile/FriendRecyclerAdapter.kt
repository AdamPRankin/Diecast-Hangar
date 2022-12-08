package com.example.diecasthangar.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.diecasthangar.R
import com.example.diecasthangar.data.model.User
import com.example.diecasthangar.databinding.RecyclerFriendRowLayoutBinding

class FriendRecyclerAdapter(private val onFriendAccept: (User) -> Unit, private val onFriendDecline: (User) -> Unit, private val onItemClicked: (User) -> Unit): RecyclerView.Adapter<FriendRecyclerAdapter.ViewHolder>() {
    var users= ArrayList<User>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecyclerFriendRowLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]

        holder.usernameTextView.text = user.username
        Glide.with(holder.itemView.context)
            .load(user.avatarUri)
            .placeholder(R.drawable.ic_airplane_black_48dp)
            .into(holder.avatarImageView)

        if (!user.friend) {
            holder.declineButton.visibility = View.VISIBLE
            holder.acceptButton.visibility = View.VISIBLE
            holder.requestTextView.visibility = View.VISIBLE

            holder.acceptButton.setOnClickListener {
                onFriendAccept(user)
                holder.declineButton.visibility = View.GONE
                holder.acceptButton.visibility = View.GONE
                holder.requestTextView.visibility = View.GONE
            }
        }
        else {
            holder.declineButton.visibility = View.GONE
            holder.acceptButton.visibility = View.GONE
            holder.requestTextView.visibility = View.GONE
        }

        holder.avatarImageView.setOnClickListener {
            onItemClicked(user)
        }
        holder.usernameTextView.setOnClickListener {
            onItemClicked(user)
        }
    }



    inner class ViewHolder(binding: RecyclerFriendRowLayoutBinding): RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {
        val view = binding.root
        val avatarImageView: ImageView = binding.friendRowAvatar
        val usernameTextView: TextView = binding.friendRowUsername
        val acceptButton: ImageButton = binding.friendRowBtnAccept
        val declineButton: ImageButton = binding.friendRowBtnDecline
        val requestTextView: TextView = binding.friendRowRequest

        init {
            view.setOnClickListener(this)
        }
        override fun onClick(v: View?) {
        }
    }

    override fun getItemCount(): Int {
        return users.size
    }
}