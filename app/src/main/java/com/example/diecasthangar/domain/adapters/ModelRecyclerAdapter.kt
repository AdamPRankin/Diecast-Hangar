package com.example.diecasthangar.domain.adapters

import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.diecasthangar.R
import com.example.diecasthangar.core.inflate
import com.example.diecasthangar.data.Model
import com.example.diecasthangar.data.Photo
import com.example.diecasthangar.databinding.RecyclerModelRowLayoutBinding
import com.google.android.material.button.MaterialButton

class ModelRecyclerAdapter: RecyclerView.Adapter<ModelRecyclerAdapter.ViewHolder>() {
    var models = ArrayList<Model>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rowBinding = RecyclerModelRowLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(rowBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = models[position]
        var currentImagePosition = 0
        if (model.photos.size > 0) {

            Glide.with(holder.itemView.context).load(model.photos[currentImagePosition].remoteUri)
                .placeholder(R.drawable.airplane)
                .into(holder.modelPhotoImageView)
        }
        else {
            holder.modelPhotoImageView.visibility = View.GONE
            holder.rightImageButton.visibility = View.GONE
            holder.leftImageButton.visibility = View.GONE
        }

        holder.modelComment.text = model.comment
        holder.modelTitle.text = "${model.scale} ${model.airline} ${model.frame}"
        holder.modelLivery.text = model.livery
        holder.modelBrand.text = model.manufacturer


        if (model.photos.size < 2) {
            holder.leftImageButton.visibility = View.GONE
            holder.rightImageButton.visibility = View.GONE
        }
        else {
            holder.leftImageButton.setOnClickListener {
                if (currentImagePosition == 0){
                    currentImagePosition = model.photos.size-1
                }
                else{
                    currentImagePosition -=1
                }
                val imageUri: Uri = Uri.parse(model.photos[currentImagePosition].remoteUri)
                Glide.with(holder.itemView.context)
                    .load(imageUri)
                    .placeholder(R.drawable.airplane)
                    .into(holder.modelPhotoImageView)

            }
            holder.rightImageButton.setOnClickListener {
                if (currentImagePosition == model.photos.size-1){
                    currentImagePosition = 0
                }
                else{
                    currentImagePosition +=1
                }
                val imageUri: Uri = Uri.parse(model.photos[currentImagePosition].remoteUri)
                Glide.with(holder.itemView.context)
                    .load(imageUri)
                    .placeholder(R.drawable.airplane)
                    .into(holder.modelPhotoImageView)
            }
        }

    }

    inner class ViewHolder(binding: RecyclerModelRowLayoutBinding): RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {

        val modelComment = binding.modelRowCommentTextview
        val modelBrand = binding.modelRowBrandTextview
        val modelLivery = binding.modelRowLiveryTextview
        val modelTitle = binding.modelRowTitleTextview


        val leftImageButton = binding.modelRowBtnImgLeft
        val rightImageButton = binding.modelRowBtnImgRight
        val modelPhotoImageView = binding.modelRowImageView


        init {
            binding.root.setOnClickListener(this)
        }
        override fun onClick(v: View?) {
        }
    }

    override fun getItemCount(): Int {
        return models.size
    }



}