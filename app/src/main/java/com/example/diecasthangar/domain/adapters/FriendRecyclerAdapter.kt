package com.example.diecasthangar.domain.adapters

import android.media.Image
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.diecasthangar.R
import com.example.diecasthangar.core.inflate
import com.example.diecasthangar.data.Post
import com.example.diecasthangar.data.User
import com.example.diecasthangar.domain.usecase.remote.getUser
import org.w3c.dom.Text

class FriendRecyclerAdapter(private val onFriendAccept: (User) -> Unit,private val onFriendDecline: (User) -> Unit,private val onItemClicked: (User) -> Unit): RecyclerView.Adapter<FriendRecyclerAdapter.ViewHolder>() {
    var users= ArrayList<User>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView = parent.inflate(R.layout.recycler_friend_row_layout, false)
        return ViewHolder(inflatedView)
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



    inner class ViewHolder(v: View): RecyclerView.ViewHolder(v),
        View.OnClickListener {
        private var view: View = v
        val avatarImageView: ImageView = view.findViewById(R.id.friend_row_avatar)
        val usernameTextView: TextView = view.findViewById(R.id.friend_row_username)
        val acceptButton: ImageButton = view.findViewById(R.id.friend_row_btn_accept)
        val declineButton: ImageButton = view.findViewById(R.id.friend_row_btn_decline)
        val requestTextView: TextView = view.findViewById(R.id.friend_row_request)

        init {
            v.setOnClickListener(this)
        }
        override fun onClick(v: View?) {
        }
    }

    override fun getItemCount(): Int {
        return users.size
    }
}