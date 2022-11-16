package com.example.diecasthangar.domain.adapters

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.diecasthangar.R
import com.example.diecasthangar.core.inflate
import com.google.firebase.firestore.auth.User

class FriendRecyclerAdapter: RecyclerView.Adapter<FriendRecyclerAdapter.ViewHolder>() {
    var users= ArrayList<User>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView = parent.inflate(R.layout.recycler_horizontal_image_row_layout, false)
        return ViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = users[position]

    }

    inner class ViewHolder(v: View): RecyclerView.ViewHolder(v),
        View.OnClickListener {

        private var view: View = v

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