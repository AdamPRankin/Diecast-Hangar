package com.example.diecasthangar.domain.adapters

import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.diecasthangar.R
import com.example.diecasthangar.core.inflate
import com.example.diecasthangar.data.Model
import com.example.diecasthangar.data.Photo

class ModelRecyclerAdapter: RecyclerView.Adapter<ModelRecyclerAdapter.ViewHolder>() {
    var models = ArrayList<Model>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView = parent.inflate(R.layout.recycler_horizontal_image_row_layout, false)
        return ViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = models[position]

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
        return models.size
    }



}