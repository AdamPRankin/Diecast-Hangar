package com.example.diecasthangar.domain.adapters

import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.diecasthangar.R
import com.example.diecasthangar.core.inflate

class SideScrollImageRecyclerAdapter: RecyclerView.Adapter<SideScrollImageRecyclerAdapter.ViewHolder>() {
    var localUris = ArrayList<Uri>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView = parent.inflate(R.layout.recycler_horizontal_image_row_layout, false)
        return ViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            holder.photoImageView.setImageURI(localUris[position])
    }

    inner class ViewHolder(v: View): RecyclerView.ViewHolder(v),
        View.OnClickListener {

        private var view: View = v

        val photoImageView: ImageView = view.findViewById(R.id.add_post_row_image)

        init {
            v.setOnClickListener(this)
        }
        override fun onClick(v: View?) {
        }
    }

    override fun getItemCount(): Int {
        return localUris.size
    }



}