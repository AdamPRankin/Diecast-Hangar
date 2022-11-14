package com.example.diecasthangar.domain.adapters

import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.diecasthangar.R
import com.example.diecasthangar.core.inflate
import com.example.diecasthangar.data.Photo

class SideScrollImageRecyclerAdapter: RecyclerView.Adapter<SideScrollImageRecyclerAdapter.ViewHolder>() {
    var photos = ArrayList<Photo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView = parent.inflate(R.layout.recycler_horizontal_image_row_layout, false)
        return ViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val photo = photos[position]
        if (photo.localUri != null) {
            holder.photoImageView.setImageURI(photos[position].localUri)
        }
        else if (photo.localUri == null){
            val uri = Uri.parse(photo.remoteUri).toString()

            //must use placeholder so glide can get the dimensions of imageView
            Glide.with(holder.itemView.context)
                .load(uri)
                .placeholder(R.drawable.ic_airplane_black_48dp)
                .into(holder.photoImageView)
        }
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
        return photos.size
    }



}