package com.example.diecasthangar.ui

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.diecasthangar.R
import com.example.diecasthangar.data.model.Photo
import com.example.diecasthangar.databinding.RecyclerHorizontalImageRowLayoutBinding
import com.example.diecasthangar.databinding.RecyclerHorizontalImageRowLayoutFullscreenBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton


class FullScreenImageRecyclerAdapter: RecyclerView.Adapter<FullScreenImageRecyclerAdapter.ViewHolder>() {
    var photos = ArrayList<Photo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(RecyclerHorizontalImageRowLayoutFullscreenBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val photo = photos[position]
        @Suppress("KotlinConstantConditions")
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
    inner class ViewHolder(binding: RecyclerHorizontalImageRowLayoutFullscreenBinding): RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {

        private var view: View = binding.root

        val photoImageView: ImageView = view.findViewById(R.id.add_post_row_image)

        init {
            view.setOnClickListener(this)
        }
        override fun onClick(v: View?) {
        }
    }

    override fun getItemCount(): Int {
        return photos.size
    }

}